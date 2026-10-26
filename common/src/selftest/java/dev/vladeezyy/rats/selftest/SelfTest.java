package dev.vladeezyy.rats.selftest;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Functional checks on a dev server ({@code tools/selftest.sh}), only with -Drats.selftest=true (the source set is
 * not in the jar). Prints "[Rats selftest] PASS n/n" or the failed checks, then stops the server.
 * Add the mod's checks to {@link #checks} (right after start) and {@link #laterChecks} (after a few ticks).
 */
public final class SelfTest {
  private static final String PREFIX = "[Rats selftest] ";
  private static final boolean ENABLED = Boolean.getBoolean("rats.selftest");
  private static final List<String> failures = new ArrayList<>();
  private static int checks;
  private static int tick = -1;
  private static BlockPos origin;
  private static ServerPlayer player;

  static void check(String name, BooleanSupplier test) {
    // also called by GameplayTest
    boolean result;
    try {
      result = test.getAsBoolean();
    } catch (RuntimeException e) {
      RatsMod.LOGGER.error(PREFIX + "exception in {}", name, e);
      result = false;
    }
    checks++;
    if (result) {
      RatsMod.LOGGER.info(PREFIX + "ok   {}", name);
    } else {
      failures.add(name);
      RatsMod.LOGGER.error(PREFIX + "FAIL {}", name);
    }
  }

  private static BlockPos at(int x, int y, int z) {
    return origin.offset(x, y, z);
  }

  /** Server started (wired by the loader's test hooks) */
  public static void serverStarted(MinecraftServer server) {
    if (!ENABLED) {
      return;
    }
    ServerLevel level = server.overworld();
    BlockPos spawn = level.getRespawnData().pos();
    origin = new BlockPos(spawn.getX() + 200, 120, spawn.getZ());
    for (int cx = -1; cx <= 1; cx++) {
      for (int cz = -1; cz <= 1; cz++) {
        level.setChunkForced((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz, true);
      }
    }
    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -1, -8), origin.offset(8, 4, 8))) {
      level.setBlock(pos, pos.getY() == origin.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    player = TestHooks.fakePlayer.apply(level, TestHooks.TESTER);
    player.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() - 2.5);
    if (StressTest.enabled()) {
      // -Drats.stress=<rats>: the load measurement instead of the checks
      StressTest.start(level, player, origin.offset(16, 0, 16));
      tick = 0;
      return;
    }
    GameplayTest.start(level, player, origin);
    try {
      checks(server, level);
    } catch (RuntimeException e) {
      RatsMod.LOGGER.error(PREFIX + "checks crashed", e);
      failures.add("checks crashed");
    }
    tick = 0;
  }

  /** After every server tick (wired by the loader's test hooks) */
  public static void serverTick(MinecraftServer server) {
    if (tick < 0) {
      return;
    }
    tick++;
    if (StressTest.enabled()) {
      if (StressTest.tick(server)) {
        // the end line tools/selftest.sh waits for
        RatsMod.LOGGER.info(PREFIX + "PASS stress run");
        tick = -1;
        server.halt(false);
      }
      return;
    }
    GameplayTest.tick(server.overworld());
    if (tick == 10) {
      try {
        laterChecks(server.overworld());
      } catch (RuntimeException e) {
        RatsMod.LOGGER.error(PREFIX + "later checks crashed", e);
        failures.add("later checks crashed");
      }
    }
    if (tick >= 20 && (GameplayTest.finished() || tick > Math.max(4000, GameplayTest.deadline()))) {
      GameplayTest.failUnfinished();
      int passed = checks - failures.size();
      if (failures.isEmpty()) {
        RatsMod.LOGGER.info(PREFIX + "PASS {}/{}", passed, checks);
      } else {
        RatsMod.LOGGER.error(PREFIX + "FAILED {}/{}: {}", passed, checks, failures);
      }
      tick = -1;
      server.halt(false);
    }
  }

  private static net.minecraft.world.entity.Entity spawned;
  private static com.github.alexthe666.rats.server.entity.rat.TamedRat tamed;

  private static void checks(MinecraftServer server, ServerLevel level) {
    check("items, blocks and entities registered", () ->
      BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "cheese"))
        && BuiltInRegistries.BLOCK.containsKey(net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "rat_crafting_table"))
        && BuiltInRegistries.ENTITY_TYPE.containsKey(net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "tamed_rat"))
        && BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "ratlantis_helmet")));
    check("enum extensions: rat spawn category and Ratlantis rarity", () ->
      RatsMod.RATS.getName().equals("rats:rats") && RatsMod.RATLANTIS_SPECIAL != null);
    check("rat variants registry has the 13 variants", () ->
      com.github.alexthe666.rats.registry.RatVariantRegistry.RAT_VARIANT_REGISTRY.size() == 13);
    check("recipes loaded (crafting, chef, archeologist)", () -> {
      var manager = server.getRecipeManager();
      long rats = manager.getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(RatsMod.MODID)).count();
      RatsMod.LOGGER.info(PREFIX + "{} rats recipes", rats);
      return rats > 100 && manager.getRecipes().stream().anyMatch(r -> r.value().getType() == com.github.alexthe666.rats.registry.RatsRecipeRegistry.CHEF.get());
    });
    check("#864: plastic recipes by default, the glass ones only with plastic off", () -> {
      var manager = server.getRecipeManager();
      return manager.byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "tubes/rat_tube_plastic"))).isPresent() == com.github.alexthe666.rats.RatConfig.plasticRecipes
        && manager.byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "tubes/rat_tube_glass"))).isPresent() != com.github.alexthe666.rats.RatConfig.plasticRecipes;
    });
    check("rat loot table exists", () -> server.reloadableRegistries().getLootTable(
      net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "entities/rat"))) != net.minecraft.world.level.storage.loot.LootTable.EMPTY);
    check("banner patterns registered", () -> server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
      .containsKey(com.github.alexthe666.rats.registry.RatsBannerPatternRegistry.RAT_PATTERN));
    // 1.21 enchantments go by item tags (1.20.1 by EnchantmentCategory): every armor piece, hat and weapon of the mod
    // takes the enchantments its 1.20.1 category allowed
    check("armor, hats, swords and the bow take their enchantments (1.21 item tags)", () -> {
      var enchantments = server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
      java.util.function.BiPredicate<net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, net.minecraft.world.item.Item> supports =
        (key, item) -> enchantments.getOrThrow(key).value().isSupportedItem(new net.minecraft.world.item.ItemStack(item));
      boolean overworld = supports.test(net.minecraft.world.item.enchantment.Enchantments.PROTECTION, com.github.alexthe666.rats.registry.RatsItemRegistry.TOP_HAT.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.RESPIRATION, com.github.alexthe666.rats.registry.RatsItemRegistry.PLAGUE_DOCTOR_MASK.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, com.github.alexthe666.rats.registry.RatsItemRegistry.PLAGUE_SCYTHE.get());
      if (!com.github.alexthe666.rats.RatsMod.RATLANTIS_DATAPACK_ENABLED) return overworld;
      return overworld
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.PROTECTION, com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATLANTIS_CHESTPLATE.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.SWIFT_SNEAK, com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATLANTIS_LEGGINGS.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.FEATHER_FALLING, com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATLANTIS_BOOTS.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.AQUA_AFFINITY, com.github.alexthe666.rats.registry.RatlantisItemRegistry.MILITARY_HAT.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, com.github.alexthe666.rats.registry.RatlantisItemRegistry.PIRAT_CUTLASS.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.LOOTING, com.github.alexthe666.rats.registry.RatlantisItemRegistry.FERAL_BAGH_NAKHS.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.POWER, com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATLANTIS_BOW.get())
        && supports.test(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATLANTIS_BOW.get());
    });
    check("Ratlantis pack is available", () -> server.getPackRepository().getAvailableIds().stream().anyMatch(id -> id.equals("ratlantis") || id.endsWith(":ratlantis") || id.endsWith("/ratlantis")));
    check("Ratlantis pack on means Ratlantis on (1.20.1 pack id)", () -> com.github.alexthe666.rats.server.events.RatlantisPack.isEnabled(server) == com.github.alexthe666.rats.RatsMod.RATLANTIS_DATAPACK_ENABLED);
    check("wild rat spawns", () -> {
      spawned = com.github.alexthe666.rats.registry.RatsEntityRegistry.RAT.get().spawn(level, at(1, 0, 1), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      return spawned != null && spawned.isAlive();
    });
    check("tamed rat with a combined upgrade holding a speed upgrade", () -> {
      tamed = com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().spawn(level, at(-1, 0, 1), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      if (tamed == null) {
        return false;
      }
      tamed.tame(player);
      ItemStack combined = new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_COMBINED.get());
      net.minecraft.core.NonNullList<ItemStack> inside = net.minecraft.core.NonNullList.withSize(27, ItemStack.EMPTY);
      inside.set(0, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_SPEED.get()));
      com.github.alexthe666.rats.server.misc.StoredItems.set(combined, inside);
      tamed.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, combined);
      return tamed.isTame() && com.github.alexthe666.rats.server.misc.RatUpgradeUtils.hasUpgrade(tamed, com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_SPEED.get());
    });
    check("piglins see a mob carrying a rat with the idol upgrade as wearing gold (common mixin)", () -> {
      net.minecraft.world.entity.monster.zombie.Zombie carrier = net.minecraft.world.entity.EntityType.ZOMBIE.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      com.github.alexthe666.rats.server.entity.rat.TamedRat rider = com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      carrier.setPos(at(-3, 0, 3).getCenter());
      rider.setPos(at(-3, 0, 3).getCenter());
      level.addFreshEntity(carrier);
      level.addFreshEntity(rider);
      boolean before = net.minecraft.world.entity.monster.piglin.PiglinAi.isWearingSafeArmor(carrier);
      rider.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_IDOL.get()));
      rider.startRiding(carrier, true, false);
      boolean after = net.minecraft.world.entity.monster.piglin.PiglinAi.isWearingSafeArmor(carrier);
      rider.discard();
      carrier.discard();
      return !before && after;
    });
    check("wardens ignore rats with the sculked upgrade (common mixin)", () -> {
      net.minecraft.world.entity.monster.warden.Warden warden = net.minecraft.world.entity.EntityType.WARDEN.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      com.github.alexthe666.rats.server.entity.rat.TamedRat rat = com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      warden.setPos(at(-5, 0, 3).getCenter());
      rat.setPos(at(-6, 0, 3).getCenter());
      level.addFreshEntity(warden);
      level.addFreshEntity(rat);
      boolean before = warden.canTargetEntity(rat);
      rat.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_SCULKED.get()));
      boolean after = warden.canTargetEntity(rat);
      rat.discard();
      warden.discard();
      return before && !after;
    });
    check("pick block: a milk cauldron gives a cauldron, a decorated rat cage the cage (vanilla getCloneItemStack)", () -> {
      net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.ZERO;
      return com.github.alexthe666.rats.registry.RatsBlockRegistry.MILK_CAULDRON.get().defaultBlockState().getCloneItemStack(level, pos, false).is(net.minecraft.world.item.Items.CAULDRON)
        && com.github.alexthe666.rats.registry.RatsBlockRegistry.RAT_CAGE_DECORATED.get().defaultBlockState().getCloneItemStack(level, pos, false)
          .is(com.github.alexthe666.rats.registry.RatsBlockRegistry.RAT_CAGE.get().asItem());
    });
    check("thrown blocks are solid for other entities", () ->
      new com.github.alexthe666.rats.server.entity.projectile.ThrownBlock(com.github.alexthe666.rats.registry.RatsEntityRegistry.THROWN_BLOCK.get(), level).canBeCollidedWith(null));
    check("1.20.1 combined upgrade data (Items list in custom data) is read", () -> {
      ItemStack old = new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_COMBINED.get());
      com.github.alexthe666.rats.server.misc.RegistryAccessHolder.setServer(server);
      net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
      net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
      net.minecraft.nbt.CompoundTag entry = new net.minecraft.nbt.CompoundTag();
      entry.putByte("Slot", (byte) 3);
      entry.putString("id", "rats:rat_upgrade_health");
      entry.putByte("Count", (byte) 1);
      list.add(entry);
      tag.put("Items", list);
      com.github.alexthe666.rats.server.misc.ItemNbt.setTag(old, tag);
      return com.github.alexthe666.rats.server.misc.StoredItems.view(old, 27).get(3).is(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_HEALTH.get());
    });
  }

  /** Checks that need a few server ticks first */
  private static void laterChecks(ServerLevel level) {
    check("wild rat still alive after 10 ticks", () -> spawned != null && spawned.isAlive());
    check("tamed rat applied the speed boost", () -> tamed != null && tamed.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
      > tamed.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));
  }
}
