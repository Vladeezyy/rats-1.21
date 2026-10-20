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
    BlockPos spawn = level.getSharedSpawnPos();
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
    if (tick >= 20 && (GameplayTest.finished() || tick > 4000)) {
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
      BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "cheese"))
        && BuiltInRegistries.BLOCK.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "rat_crafting_table"))
        && BuiltInRegistries.ENTITY_TYPE.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "tamed_rat"))
        && BuiltInRegistries.ITEM.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "ratlantis_helmet")));
    check("enum extensions: rat spawn category and Ratlantis rarity", () ->
      RatsMod.RATS.getName().equals("rats:rats") && RatsMod.RATLANTIS_SPECIAL != null);
    check("rat variants registry has the 13 variants", () ->
      com.github.alexthe666.rats.registry.RatVariantRegistry.RAT_VARIANT_REGISTRY.size() == 13);
    check("recipes loaded (crafting, chef, archeologist)", () -> {
      var manager = server.getRecipeManager();
      long rats = manager.getRecipes().stream().filter(r -> r.id().getNamespace().equals(RatsMod.MODID)).count();
      RatsMod.LOGGER.info(PREFIX + "{} rats recipes", rats);
      return rats > 100 && !manager.getAllRecipesFor(com.github.alexthe666.rats.registry.RatsRecipeRegistry.CHEF.get()).isEmpty();
    });
    check("#864: plastic recipes by default, the glass ones only with plastic off", () -> {
      var manager = server.getRecipeManager();
      return manager.byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "tubes/rat_tube_plastic")).isPresent() == com.github.alexthe666.rats.RatConfig.plasticRecipes
        && manager.byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "tubes/rat_tube_glass")).isPresent() != com.github.alexthe666.rats.RatConfig.plasticRecipes;
    });
    check("rat loot table exists", () -> server.reloadableRegistries().getLootTable(
      net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "entities/rat"))) != net.minecraft.world.level.storage.loot.LootTable.EMPTY);
    check("banner patterns registered", () -> server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
      .containsKey(com.github.alexthe666.rats.registry.RatsBannerPatternRegistry.RAT_PATTERN));
    check("Ratlantis pack is available", () -> server.getPackRepository().getAvailableIds().stream().anyMatch(id -> id.equals("ratlantis") || id.endsWith(":ratlantis") || id.endsWith("/ratlantis")));
    check("Ratlantis pack on means Ratlantis on (1.20.1 pack id)", () -> com.github.alexthe666.rats.server.events.RatlantisPack.isEnabled(server) == com.github.alexthe666.rats.RatsMod.RATLANTIS_DATAPACK_ENABLED);
    check("wild rat spawns", () -> {
      spawned = com.github.alexthe666.rats.registry.RatsEntityRegistry.RAT.get().spawn(level, at(1, 0, 1), net.minecraft.world.entity.MobSpawnType.COMMAND);
      return spawned != null && spawned.isAlive();
    });
    check("tamed rat with a combined upgrade holding a speed upgrade", () -> {
      tamed = com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().spawn(level, at(-1, 0, 1), net.minecraft.world.entity.MobSpawnType.COMMAND);
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
