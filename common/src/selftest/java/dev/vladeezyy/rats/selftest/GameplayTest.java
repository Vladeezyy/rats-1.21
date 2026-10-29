package dev.vladeezyy.rats.selftest;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.registry.RatsEffectRegistry;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsVillagerRegistry;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.RatSackItem;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Gameplay scenarios on the dev server ({@code tools/selftest.sh}, both loaders): rats tamed with cheese, carrying
 * items and milk between storages, cheesemaking, the plague, upgrades, loot modifiers, trades, the rat sack, the
 * Ratlantis portal and the bosses. Each scenario has its own patch of ground and waits for its result up to a timeout.
 */
final class GameplayTest {
  private abstract static class Scenario {
    final String name;
    final int timeout;
    BlockPos base;
    int started;
    boolean finished;

    Scenario(String name, int timeout) {
      this.name = name;
      this.timeout = timeout;
    }

    /** Builds the scene; a scenario that cannot run here (no Ratlantis) returns false and is skipped */
    abstract boolean start(ServerLevel level);

    /** Checked every tick: true once the expected thing happened */
    abstract boolean done(ServerLevel level);

    /** What went wrong, logged on timeout */
    String detail(ServerLevel level) {
      return "";
    }
  }

  private static final List<Scenario> SCENARIOS = new ArrayList<>();
  private static ServerPlayer player;
  private static int tick;

  private GameplayTest() {}

  static boolean finished() {
    return SCENARIOS.stream().allMatch(s -> s.finished);
  }

  private static void flat(ServerLevel level, BlockPos base) {
    for (int cx = -1; cx <= 1; cx++) {
      for (int cz = -1; cz <= 1; cz++) {
        level.setChunkForced((base.getX() >> 4) + cx, (base.getZ() >> 4) + cz, true);
      }
    }
    for (BlockPos pos : BlockPos.betweenClosed(base.offset(-9, -1, -9), base.offset(9, 5, 9))) {
      level.setBlock(pos, pos.getY() == base.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
  }

  private static TamedRat tamedRat(ServerLevel level, BlockPos pos, RatCommand command) {
    TamedRat rat = RatsEntityRegistry.TAMED_RAT.get().spawn(level, pos, EntitySpawnReason.COMMAND);
    rat.tame(player);
    rat.setCommand(command);
    return rat;
  }

  private static ChestBlockEntity chest(ServerLevel level, BlockPos pos) {
    level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
    return (ChestBlockEntity) level.getBlockEntity(pos);
  }

  private static int count(Container container, net.minecraft.world.item.Item item) {
    int count = 0;
    for (int i = 0; i < container.getContainerSize(); i++) {
      if (container.getItem(i).is(item)) {
        count += container.getItem(i).getCount();
      }
    }
    return count;
  }

  private static <T extends Entity> List<T> around(ServerLevel level, Class<T> type, BlockPos base) {
    return level.getEntitiesOfClass(type, new AABB(base).inflate(12));
  }

  static void start(ServerLevel level, ServerPlayer tester, BlockPos origin) {
    player = tester;
    SCENARIOS.clear();
    SCENARIOS.add(new Scenario("a wild rat is tamed by cheese a player threw", 3600) {
      @Override
      boolean start(ServerLevel level) {
        // a glass pen: a wild rat wanders off between two bites and stops seeing food further than its search range
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(-5, 0, -5), this.base.offset(5, 1, 5))) {
          if (Math.abs(pos.getX() - this.base.getX()) == 5 || Math.abs(pos.getZ() - this.base.getZ()) == 5) {
            level.setBlock(pos, Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
          }
        }
        Rat rat = RatsEntityRegistry.RAT.get().spawn(level, this.base, EntitySpawnReason.COMMAND);
        rat.setPlagued(false);
        // a baby rat does not move (canMove)
        rat.setBaby(false);
        ItemEntity cheese = new ItemEntity(level, this.base.getX() + 3.5, this.base.getY(), this.base.getZ() + 0.5, new ItemStack(RatsItemRegistry.CHEESE.get(), 32));
        cheese.setThrower(player);
        level.addFreshEntity(cheese);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // #838: the fed cheese does not stay in the tamed rat's paw
        return around(level, TamedRat.class, this.base).stream().anyMatch(rat -> rat.isOwnedBy(player) && !RatsItemRegistry.CHEESE.get().equals(rat.getMainHandItem().getItem()));
      }

      @Override
      String detail(ServerLevel level) {
        return "tamed: " + around(level, TamedRat.class, this.base).stream().map(r -> "hand " + r.getMainHandItem()).toList() + ", wild rats: " + around(level, Rat.class, this.base).stream().map(r -> "trust " + r.wildTrust + " fed " + r.cheeseFeedings
          + " baby " + r.isBaby() + " ticks " + r.tickCount + " at " + r.position().subtract(Vec3.atLowerCornerOf(this.base)) + " hand " + r.getMainHandItem()).toList()
          + ", cheese on the ground: " + around(level, ItemEntity.class, this.base).stream().map(item -> item.getItem() + " at " + item.position().subtract(Vec3.atLowerCornerOf(this.base))).toList();
      }
    });
    SCENARIOS.add(new Scenario("creative cheese tames a wild rat at once", 20) {
      @Override
      boolean start(ServerLevel level) {
        Rat rat = RatsEntityRegistry.RAT.get().spawn(level, this.base, EntitySpawnReason.COMMAND);
        rat.setPlagued(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RatsItemRegistry.CREATIVE_CHEESE.get()));
        rat.mobInteract(player, InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return around(level, TamedRat.class, this.base).stream().anyMatch(rat -> rat.isOwnedBy(player) && rat.getCommand() == RatCommand.SIT);
      }
    });
    SCENARIOS.add(new Scenario("#838: a working rat does not stay stuck with food it may not eat", 300) {
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        this.rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        this.rat.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RatsItemRegistry.CHEESE.get()));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.rat.getMainHandItem().isEmpty();
      }
    });
    SCENARIOS.add(new Scenario("#902: a farmer rat plants seeds on dry farmland", 1800) {
      @Override
      boolean start(ServerLevel level) {
        for (int x = 2; x <= 4; x++) {
          for (int z = -1; z <= 1; z++) {
            level.setBlock(this.base.offset(x, -1, z), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
          }
        }
        chest(level, this.base.offset(-3, 0, 0));
        TamedRat rat = tamedRat(level, this.base, RatCommand.HARVEST);
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_FARMER.get()));
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        rat.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS, 8));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        for (int x = 2; x <= 4; x++) {
          for (int z = -1; z <= 1; z++) {
            if (level.getBlockState(this.base.offset(x, 0, z)).is(Blocks.WHEAT)) {
              return true;
            }
          }
        }
        return false;
      }
    });
    SCENARIOS.add(new Scenario("#844: a lumberjack rat fells a tree across its work area, 15 blocks away", 1800) {
      BlockPos tree;
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        this.tree = this.base.offset(7, 0, 0);
        level.setBlock(this.tree.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        for (BlockPos pos : BlockPos.betweenClosed(this.tree.offset(-2, 3, -2), this.tree.offset(2, 5, 2))) {
          level.setBlock(pos, leaves, Block.UPDATE_CLIENTS);
        }
        for (int y = 0; y < 5; y++) {
          level.setBlock(this.tree.above(y), Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        chest(level, this.base.offset(-8, 0, 2));
        this.rat = tamedRat(level, this.base.offset(-8, 0, 0), RatCommand.HARVEST);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_LUMBERJACK.get()));
        this.rat.onUpgradeChanged();
        this.rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(-8, 0, 2)));
        this.rat.setRadiusCenter(GlobalPos.of(level.dimension(), this.base));
        this.rat.setRadius(9);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return !level.getBlockState(this.tree).is(Blocks.OAK_LOG);
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position() + ", tree at " + this.tree;
      }
    });
    SCENARIOS.add(new Scenario("#787: a patrol path survives saving and reaches joining players", 1) {
      @Override
      boolean start(ServerLevel level) {
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        TamedRat rat = tamedRat(level, this.base, RatCommand.PATROL);
        rat.setPatrolNodes(List.of(GlobalPos.of(level.dimension(), this.base.east(3)), GlobalPos.of(level.dimension(), this.base.west(3))));
        net.minecraft.world.level.storage.TagValueOutput saved = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        rat.addAdditionalSaveData(saved);
        TamedRat loaded = RatsEntityRegistry.TAMED_RAT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        loaded.readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        var sent = loaded.getEntityData().getNonDefaultValues();
        return loaded.getPatrolNodes().size() == 2 && sent != null && sent.stream().anyMatch(v -> v.value() instanceof List<?> list && list.size() == 2);
      }
    });
    SCENARIOS.add(new Scenario("a rat carries items from one chest to another", 1800) {
      ChestBlockEntity to;

      @Override
      boolean start(ServerLevel level) {
        ChestBlockEntity from = chest(level, this.base.offset(-3, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        this.to = chest(level, this.base.offset(3, 0, 0));
        TamedRat rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(3, 0, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }
    });
    SCENARIOS.add(new Scenario("#922: a flying rat carries items up to a chest on a pillar it cannot walk to", 2400) {
      ChestBlockEntity to;

      @Override
      boolean start(ServerLevel level) {
        ChestBlockEntity from = chest(level, this.base.offset(-3, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        for (int y = 0; y < 4; y++) level.setBlock(this.base.offset(3, y, 0), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        this.to = chest(level, this.base.offset(3, 4, 0));
        TamedRat rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_FLIGHT.get()));
        rat.onUpgradeChanged();
        rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(3, 4, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }

      @Override
      String detail(ServerLevel level) {
        return around(level, TamedRat.class, this.base).stream().map(r -> "rat at " + r.position().subtract(Vec3.atBottomCenterOf(this.base)) + " flying " + r.isFlying() + " hand " + r.getMainHandItem() + " nav " + r.getNavigation().getClass().getSimpleName() + " path " + r.getNavigation().getPath()).toList().toString();
      }
    });
    SCENARIOS.add(new Scenario("#860: an aquatic rat carries items between two chests under water", 2400) {
      ChestBlockEntity to;

      @Override
      boolean start(ServerLevel level) {
        // a pool 3 deep with glass walls, a chest at each end on its floor
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(-6, 0, -3), this.base.offset(6, 3, 3))) {
          boolean wall = Math.abs(pos.getX() - this.base.getX()) == 6 || Math.abs(pos.getZ() - this.base.getZ()) == 3;
          level.setBlock(pos, wall ? Blocks.GLASS.defaultBlockState() : pos.getY() - this.base.getY() < 3 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        ChestBlockEntity from = chest(level, this.base.offset(-4, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        this.to = chest(level, this.base.offset(4, 0, 0));
        TamedRat rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_AQUATIC.get()));
        rat.onUpgradeChanged();
        rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-4, 0, 0)));
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(4, 0, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }

      @Override
      String detail(ServerLevel level) {
        return around(level, TamedRat.class, this.base).stream().map(r -> "rat at " + r.position().subtract(Vec3.atBottomCenterOf(this.base)) + " in water " + r.isInWater() + " air " + r.getAirSupply() + " health " + r.getHealth() + " hand " + r.getMainHandItem() + " nav " + r.getNavigation().getClass().getSimpleName() + " path " + r.getNavigation().getPath()).toList().toString();
      }
    });
    SCENARIOS.add(new Scenario("#871: an ethereal rat carries items through stone into a walled-in chest", 1800) {
      ChestBlockEntity to;
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        if (!RatsMod.RATLANTIS_DATAPACK_ENABLED) {
          return false;
        }
        ChestBlockEntity from = chest(level, this.base.offset(-3, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        BlockPos box = this.base.offset(4, 1, 0);
        for (BlockPos pos : BlockPos.betweenClosed(box.offset(-2, -2, -2), box.offset(2, 2, 2))) {
          level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        this.to = chest(level, box);
        this.rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(com.github.alexthe666.rats.registry.RatlantisItemRegistry.RAT_UPGRADE_ETHEREAL.get()));
        this.rat.onUpgradeChanged();
        this.rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        this.rat.setDepositPos(GlobalPos.of(level.dimension(), box));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position() + ", noPhysics " + this.rat.noPhysics + ", noGravity " + this.rat.isNoGravity() + ", holds " + this.rat.getMainHandItem();
      }
    });
    SCENARIOS.add(new Scenario("#907: a rat carries items through a rat tube into a walled-in room", 600) {
      ChestBlockEntity to;
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        ChestBlockEntity from = chest(level, this.base.offset(-3, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        // a stone room (inside x 4..6, z -1..1, two high) whose only way in is a tube through its west wall
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(3, 0, -2), this.base.offset(7, 2, 2))) {
          level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(4, 0, -1), this.base.offset(6, 1, 1))) {
          level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockState tube = RatsBlockRegistry.RAT_TUBE_COLOR.get().defaultBlockState();
        level.setBlock(this.base.offset(2, 0, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.OPEN_WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.EAST, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(3, 0, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.EAST, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(4, 0, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.OPEN_EAST, true), Block.UPDATE_CLIENTS);
        this.to = chest(level, this.base.offset(6, 0, 0));
        this.rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        this.rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        this.rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(6, 0, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }

      @Override
      String detail(ServerLevel level) {
        StringBuilder text = new StringBuilder("rat at " + this.rat.position() + ", holds " + this.rat.getMainHandItem() + ", path " + (this.rat.getNavigation().getPath() == null ? "none" : this.rat.getNavigation().getPath().getTarget() + " reach " + this.rat.getNavigation().getPath().canReach()));
        for (int x = 2; x <= 4; x++) {
          text.append("\n  tube ").append(x).append(": ").append(level.getBlockState(this.base.offset(x, 0, 0)));
        }
        TamedRat probe = RatsEntityRegistry.TAMED_RAT.get().spawn(level, this.base.offset(1, 0, 0), EntitySpawnReason.COMMAND);
        probe.tame(player);
        probe.setOnGround(true);
        net.minecraft.world.level.pathfinder.Path path = probe.getNavigation().createPath(this.base.offset(5, 0, 0), 0);
        text.append("\n  probe from the entrance: ").append(path == null ? "no path" : "reach " + path.canReach());
        if (path != null) {
          for (int i = 0; i < path.getNodeCount(); i++) {
            text.append(" ").append(path.getNode(i).asBlockPos().subtract(this.base).toShortString()).append(path.getNode(i).type);
          }
        }
        var evaluator = probe.getNavigation().getNodeEvaluator();
        text.append("\n  evaluator ").append(evaluator.getClass().getSimpleName());
        var region = new net.minecraft.world.level.PathNavigationRegion(level, this.base.offset(-10, -2, -10), this.base.offset(10, 4, 10));
        evaluator.prepare(region, probe);
        for (int x = 1; x <= 5; x++) {
          BlockPos at = this.base.offset(x, 0, 0);
          text.append("\n  ").append(x).append(": ").append(evaluator.getPathTypeOfMob(new net.minecraft.world.level.pathfinder.PathfindingContext(region, probe), at.getX(), at.getY(), at.getZ(), probe));
          net.minecraft.world.level.pathfinder.Node[] out = new net.minecraft.world.level.pathfinder.Node[32];
          int n = evaluator.getNeighbors(out, new net.minecraft.world.level.pathfinder.Node(at.getX(), at.getY(), at.getZ()));
          text.append(" ->");
          for (int i = 0; i < n; i++) {
            text.append(" ").append(out[i].asBlockPos().subtract(this.base).toShortString()).append(out[i].type);
          }
        }
        evaluator.done();
        probe.discard();
        net.minecraft.world.level.pathfinder.Path own = this.rat.getNavigation().getPath();
        if (own != null) {
          text.append("\n  the rat's path:");
          for (int i = 0; i < own.getNodeCount(); i++) {
            text.append(" ").append(own.getNode(i).asBlockPos().subtract(this.base).toShortString());
          }
        }
        return text.toString();
      }
    });
    SCENARIOS.add(new Scenario("#907: a rat climbs a rat tube shaft into a room three blocks up", 1200) {
      ChestBlockEntity to;
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        ChestBlockEntity from = chest(level, this.base.offset(-3, 0, 0));
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        // a stone room on stone pillars (inside x 4..6, z -1..1, y 3..4); the way in: a tube up from the ground at x 2,
        // then east through the room's west wall
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(3, 0, -2), this.base.offset(7, 5, 2))) {
          level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(4, 3, -1), this.base.offset(6, 4, 1))) {
          level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockState tube = RatsBlockRegistry.RAT_TUBE_COLOR.get().defaultBlockState();
        level.setBlock(this.base.offset(2, 0, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.OPEN_WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.UP, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(2, 1, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.DOWN, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.UP, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(2, 2, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.DOWN, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.UP, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(2, 3, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.DOWN, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.EAST, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(3, 3, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.EAST, true), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.offset(4, 3, 0), tube.setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.WEST, true).setValue(com.github.alexthe666.rats.server.block.RatTubeBlock.OPEN_EAST, true), Block.UPDATE_CLIENTS);
        this.to = chest(level, this.base.offset(6, 3, 0));
        this.rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        this.rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        this.rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(6, 3, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.to, Items.COBBLESTONE) > 0;
      }

      @Override
      String detail(ServerLevel level) {
        StringBuilder text = new StringBuilder("rat at " + this.rat.position().subtract(Vec3.atLowerCornerOf(this.base)) + ", holds " + this.rat.getMainHandItem());
        net.minecraft.world.level.pathfinder.Path own = this.rat.getNavigation().getPath();
        text.append(", path:");
        if (own != null) {
          for (int i = 0; i < own.getNodeCount(); i++) {
            text.append(" ").append(own.getNode(i).asBlockPos().subtract(this.base).toShortString());
          }
          text.append(" reach ").append(own.canReach()).append(" next ").append(own.getNextNodeIndex());
        }
        return text.toString();
      }
    });
    SCENARIOS.add(new Scenario("a rat gathers items from the ground into a chest", 1800) {
      ChestBlockEntity to;

      @Override
      boolean start(ServerLevel level) {
        this.to = chest(level, this.base.offset(3, 0, 0));
        TamedRat rat = tamedRat(level, this.base, RatCommand.GATHER);
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(3, 0, 0)));
        level.addFreshEntity(new ItemEntity(level, this.base.getX() - 3.5, this.base.getY(), this.base.getZ() + 2.5, new ItemStack(Items.BONE, 5)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // any item from the ground counts: on 26.x the lumberjack scenario next door drops dozens of leaf litter
        // items here, and the rat picks those up first (seen on CI, 47 stacks)
        return !this.to.isEmpty();
      }

      @Override
      String detail(ServerLevel level) {
        StringBuilder why = new StringBuilder();
        for (TamedRat r : around(level, TamedRat.class, this.base)) {
          for (ItemEntity item : around(level, ItemEntity.class, this.base)) {
            var path = r.getNavigation().createPath(item, 1);
            why.append(" canMove=").append(r.canMove()).append(" inCage=").append(r.isInCage()).append(" targetCmd=").append(r.isTargetCommand())
              .append(" collect=").append(r.shouldCollectItem(item.getItem())).append(" pickup=").append(r.canRatPickupItem(item.getItem()))
              .append(" path=").append(path == null ? "null" : path.canReach() + "/" + path.getNodeCount()).append(" nav=").append(r.getNavigation().getClass().getSimpleName())
              .append(" goals=").append(r.targetSelector.getAvailableGoals().stream().filter(g -> g.isRunning()).map(g -> g.getGoal().getClass().getSimpleName()).toList());
          }
        }
        return why + " rats: " + around(level, TamedRat.class, this.base).stream().map(r -> r.getCommand() + " hand " + r.getMainHandItem() + " at " + r.blockPosition()
          + " sit " + r.isOrderedToSit() + " working " + r.isCurrentlyWorking).toList() + ", items: " + around(level, ItemEntity.class, this.base).stream()
          .map(i -> i.getItem() + " ground " + i.onGround() + " at " + i.blockPosition()).toList() + ", base " + this.base;
      }
    });
    SCENARIOS.add(new Scenario("a rat with a bucket upgrade carries milk between curdlers", 1800) {
      AutoCurdlerBlockEntity to;

      @Override
      boolean start(ServerLevel level) {
        level.setBlock(this.base.offset(-3, 0, 0), RatsBlockRegistry.AUTO_CURDLER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(this.base.offset(3, 0, 0), RatsBlockRegistry.AUTO_CURDLER.get().defaultBlockState(), Block.UPDATE_ALL);
        AutoCurdlerBlockEntity from = (AutoCurdlerBlockEntity) level.getBlockEntity(this.base.offset(-3, 0, 0));
        this.to = (AutoCurdlerBlockEntity) level.getBlockEntity(this.base.offset(3, 0, 0));
        from.getTank().fill(new FluidStack(dev.vladeezyy.rats.platform.Platform.INSTANCE.milkFluid(), 4000), FluidHandler.FluidAction.EXECUTE);
        TamedRat rat = tamedRat(level, this.base, RatCommand.TRANSPORT);
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_BUCKET.get()));
        rat.setPickupPos(GlobalPos.of(level.dimension(), this.base.offset(-3, 0, 0)));
        rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(3, 0, 0)));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.to.getTank().getFluidAmount() > 0;
      }

      @Override
      String detail(ServerLevel level) {
        return "target tank " + this.to.getTank().getFluidAmount() + " mB, rats: " + around(level, TamedRat.class, this.base).stream().map(r -> r.transportingFluid.toString()).toList();
      }
    });
    SCENARIOS.add(new Scenario("the auto curdler turns a milk bucket into a block of cheese", 1200) {
      AutoCurdlerBlockEntity curdler;

      @Override
      boolean start(ServerLevel level) {
        level.setBlock(this.base, RatsBlockRegistry.AUTO_CURDLER.get().defaultBlockState(), Block.UPDATE_ALL);
        this.curdler = (AutoCurdlerBlockEntity) level.getBlockEntity(this.base);
        this.curdler.setItem(0, new ItemStack(Items.MILK_BUCKET));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.curdler.getItem(1).is(RatsBlockRegistry.BLOCK_OF_CHEESE.get().asItem()) && this.curdler.getItem(0).is(Items.BUCKET);
      }

      @Override
      String detail(ServerLevel level) {
        return "input " + this.curdler.getItem(0) + ", output " + this.curdler.getItem(1) + ", tank " + this.curdler.getTank().getFluidAmount();
      }
    });
    SCENARIOS.add(new Scenario("milk poured into a cauldron curdles into cheese", 400) {
      @Override
      boolean start(ServerLevel level) {
        level.setBlock(this.base, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
        level.getBlockState(this.base).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND,
          new BlockHitResult(Vec3.atCenterOf(this.base), Direction.UP, this.base, false));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return level.getBlockState(this.base).is(RatsBlockRegistry.MILK_CAULDRON.get());
      }

      @Override
      boolean done(ServerLevel level) {
        return level.getBlockState(this.base).is(RatsBlockRegistry.CHEESE_CAULDRON.get());
      }
    });
    SCENARIOS.add(new Scenario("the plague stops healing and spreads to the mob that is hit", 40) {
      Cow sick;
      Cow victim;
      float healthBeforeHeal;

      @Override
      boolean start(ServerLevel level) {
        this.sick = EntityType.COW.spawn(level, this.base, EntitySpawnReason.COMMAND);
        this.victim = EntityType.COW.spawn(level, this.base.offset(1, 0, 0), EntitySpawnReason.COMMAND);
        this.sick.addEffect(new MobEffectInstance(RatsEffectRegistry.PLAGUE.holder(), 6000));
        this.sick.setHealth(5);
        this.healthBeforeHeal = this.sick.getHealth();
        this.sick.heal(4);
        this.victim.hurt(level.damageSources().mobAttack(this.sick), 1);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.sick.getHealth() == this.healthBeforeHeal && this.victim.hasEffect(RatsEffectRegistry.PLAGUE.holder());
      }

      @Override
      String detail(ServerLevel level) {
        return "health " + this.healthBeforeHeal + " -> " + this.sick.getHealth() + ", victim plagued " + this.victim.hasEffect(RatsEffectRegistry.PLAGUE.holder());
      }
    });
    SCENARIOS.add(new Scenario("#822: a bee rat in the air flies down and bites its target", 400) {
      TamedRat rat;
      Mob pig;

      @Override
      boolean start(ServerLevel level) {
        this.pig = EntityType.PIG.spawn(level, this.base.offset(5, 0, 0), EntitySpawnReason.COMMAND);
        this.pig.setNoAi(true);
        // hunting rats do not wander, so it stays in the air as a following one does (wandering, it lands and paths)
        this.rat = tamedRat(level, this.base.above(4), RatCommand.HUNT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_BEE.get()));
        this.rat.onUpgradeChanged();
        this.rat.setFlying(true);
        this.rat.setNoGravity(true);
        this.rat.setTarget(this.pig);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        if (this.rat.getTarget() == null && this.pig.isAlive()) {
          this.rat.setTarget(this.pig);
        }
        return this.pig.getLastHurtByMob() == this.rat;
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position() + " flying " + this.rat.isFlying() + ", target " + this.rat.getTarget() + ", pig at " + this.pig.position();
      }
    });
    SCENARIOS.add(new Scenario("#935: a rat with the flight upgrade in the air flies down and bites its target", 400) {
      TamedRat rat;
      Mob pig;

      @Override
      boolean start(ServerLevel level) {
        this.pig = EntityType.PIG.spawn(level, this.base.offset(5, 0, 0), EntitySpawnReason.COMMAND);
        this.pig.setNoAi(true);
        this.rat = tamedRat(level, this.base.above(4), RatCommand.HUNT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_FLIGHT.get()));
        this.rat.onUpgradeChanged();
        this.rat.setFlying(true);
        this.rat.setNoGravity(true);
        this.rat.setTarget(this.pig);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        if (this.rat.getTarget() == null && this.pig.isAlive()) {
          this.rat.setTarget(this.pig);
        }
        return this.pig.getLastHurtByMob() == this.rat;
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position() + " flying " + this.rat.isFlying() + ", target " + this.rat.getTarget() + ", pig at " + this.pig.position();
      }
    });
    SCENARIOS.add(new Scenario("#822: a winged rat hovering by its owner may start a melee attack", 1) {
      boolean canAttack;

      @Override
      boolean start(ServerLevel level) {
        // as a following rat: in the air, where its navigation has no path (owners cannot be fake players here)
        Mob pig = EntityType.PIG.spawn(level, this.base.offset(5, 0, 0), EntitySpawnReason.COMMAND);
        pig.setNoAi(true);
        TamedRat rat = tamedRat(level, this.base.above(4), RatCommand.FOLLOW);
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_FLIGHT.get()));
        rat.onUpgradeChanged();
        rat.setFlying(true);
        rat.setNoGravity(true);
        rat.setTarget(pig);
        this.canAttack = new com.github.alexthe666.rats.server.entity.ai.goal.RatMeleeAttackGoal(rat, 1.45D, true).canUse();
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.canAttack;
      }
    });
    SCENARIOS.add(new Scenario("health and armor upgrades raise the rat's stats", 40) {
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        this.rat = tamedRat(level, this.base, RatCommand.SIT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_HEALTH.get()));
        this.rat.setItemSlot(EquipmentSlot.LEGS, new ItemStack(RatsItemRegistry.RAT_UPGRADE_ARMOR.get()));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.rat.getAttributeValue(Attributes.MAX_HEALTH) > this.rat.getAttributeBaseValue(Attributes.MAX_HEALTH)
          && this.rat.getAttributeValue(Attributes.ARMOR) > this.rat.getAttributeBaseValue(Attributes.ARMOR);
      }
    });
    SCENARIOS.add(new Scenario("loot modifiers: charged creepers drop a chunk, cats bring rat gifts", 1) {
      boolean chunk;
      boolean gift;

      @Override
      boolean start(ServerLevel level) {
        Creeper creeper = EntityType.CREEPER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        creeper.setPos(Vec3.atCenterOf(this.base));
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        creeper.saveWithoutId(out);
        net.minecraft.nbt.CompoundTag powered = out.buildResult();
        powered.putBoolean("powered", true);
        creeper.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), powered));
        LootTable creeperLoot = level.getServer().reloadableRegistries().getLootTable(EntityType.CREEPER.getDefaultLootTable().orElseThrow());
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, creeper)
          .withParameter(LootContextParams.ORIGIN, creeper.position()).withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
          .create(LootContextParamSets.ENTITY);
        this.chunk = creeperLoot.getRandomItems(params).stream().anyMatch(s -> s.is(RatsItemRegistry.CHARGED_CREEPER_CHUNK.get()));
        LootTable gifts = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("gameplay/cat_morning_gift")));
        LootParams giftParams = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.base))
          .withParameter(LootContextParams.THIS_ENTITY, creeper).create(LootContextParamSets.GIFT);
        for (int i = 0; i < 200 && !this.gift; i++) {
          this.gift = gifts.getRandomItems(giftParams).stream().anyMatch(s -> s.is(RatsItemRegistry.RAW_RAT.get()) || s.is(RatsItemRegistry.TINY_COIN.get()));
        }
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.chunk && this.gift;
      }

      @Override
      String detail(ServerLevel level) {
        return "creeper chunk " + this.chunk + ", cat gift " + this.gift;
      }
    });
    SCENARIOS.add(new Scenario("the pet shop owner has its five trade levels", 1) {
      @Override
      boolean start(ServerLevel level) {
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // 26.x: trade sets are data, one per level
        var sets = level.registryAccess().lookupOrThrow(Registries.TRADE_SET);
        var byLevel = RatsVillagerRegistry.PET_SHOP_OWNER.get().tradeSetsByLevel();
        for (int tradeLevel = 1; tradeLevel <= 5; tradeLevel++) {
          if (byLevel.get(tradeLevel) == null || sets.get(byLevel.get(tradeLevel)).isEmpty()) {
            return false;
          }
        }
        return sets.get(byLevel.get(1)).get().value().getTrades().size() == 7 && sets.get(byLevel.get(5)).get().value().getTrades().size() == 5;
      }
    });
    SCENARIOS.add(new Scenario("a rat sack that despawns lets its rats out", 40) {
      @Override
      boolean start(ServerLevel level) {
        ItemStack sack = new ItemStack(RatsItemRegistry.RAT_SACK.get());
        TamedRat rat = tamedRat(level, this.base.offset(4, 0, 4), RatCommand.SIT);
        RatSackItem.packRatIntoSack(sack, rat, 1);
        rat.discard();
        ItemEntity item = new ItemEntity(level, this.base.getX() + 0.5, this.base.getY(), this.base.getZ() + 0.5, sack);
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        item.saveWithoutId(out);
        net.minecraft.nbt.CompoundTag age = out.buildResult();
        age.putShort("Age", (short) 5990);
        item.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), age));
        level.addFreshEntity(item);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return !around(level, TamedRat.class, this.base).isEmpty();
      }
    });
    SCENARIOS.add(new Scenario("the Ratlantis portal takes a mob to Ratlantis", 200) {
      Mob pig;

      @Override
      boolean start(ServerLevel level) {
        if (!RatsMod.RATLANTIS_DATAPACK_ENABLED || level.getServer().getLevel(RatlantisDimensionRegistry.DIMENSION_KEY) == null) {
          return false;
        }
        // the portal as the game builds it: raw marbled cheese, two portal blocks, raw marbled cheese (it breaks without);
        // a glass pen around it, so the pig moves (an entity only touches blocks while it moves) but stays in it
        for (Direction side : Direction.Plane.HORIZONTAL) {
          level.setBlock(this.base.relative(side), Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
          level.setBlock(this.base.relative(side).above(), Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(this.base.below(), RatsBlockRegistry.MARBLED_CHEESE_RAW.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.above(2), RatsBlockRegistry.MARBLED_CHEESE_RAW.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(this.base, RatlantisBlockRegistry.RATLANTIS_PORTAL.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(this.base.above(), RatlantisBlockRegistry.RATLANTIS_PORTAL.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        this.pig = EntityType.PIG.spawn(level, this.base, EntitySpawnReason.COMMAND);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        Entity moved = level.getServer().getLevel(RatlantisDimensionRegistry.DIMENSION_KEY).getEntity(this.pig.getUUID());
        return moved != null;
      }

      @Override
      String detail(ServerLevel level) {
        return "pig in " + this.pig.level().dimension().identifier() + " at " + this.pig.position() + " removed " + this.pig.getRemovalReason()
          + " cooldown " + this.pig.getPortalCooldown() + " block " + level.getBlockState(this.pig.blockPosition()) + " portal at " + this.base
          + " enabled " + RatsMod.RATLANTIS_DATAPACK_ENABLED;
      }
    });
    SCENARIOS.add(new Scenario("#848: the Flying Dutchrat stays within a player's reach while it fights in melee", 1000) {
      Mob dutchrat;
      Mob target;
      int inReach;
      int counted;
      Boolean verdict;
      int throwChance;

      @Override
      boolean start(ServerLevel level) {
        if (!RatsMod.RATLANTIS_DATAPACK_ENABLED) {
          return false;
        }
        this.target = EntityType.ZOMBIE.spawn(level, this.base, EntitySpawnReason.COMMAND);
        this.target.setNoAi(true);
        // 26.x mobs drop invulnerable targets (Mob.getTarget -> canAttack -> canBeSeenAsEnemy): unhurt by resistance V instead
        this.target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, -1, 4));
        // melee only: the sword throws (1 in 5 hits) send the boss off for 5 s on purpose and made the share random
        this.throwChance = com.github.alexthe666.rats.RatConfig.dutchratSwordThrowChance;
        com.github.alexthe666.rats.RatConfig.dutchratSwordThrowChance = Integer.MAX_VALUE;
        this.dutchrat = com.github.alexthe666.rats.registry.RatlantisEntityRegistry.DUTCHRAT.get().spawn(level, this.base.offset(8, 6, 0), EntitySpawnReason.COMMAND);
        this.dutchrat.setTarget(this.target);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        this.dutchrat.setTarget(this.target);
        // its own ticks (a busy test server ticks the boss's chunk late), and the melee part of the fight: after a sword
        // throw the boss flies off for 5 s on purpose
        if (this.dutchrat.tickCount > 60 && !((com.github.alexthe666.rats.server.entity.monster.boss.Dutchrat) this.dutchrat).hasThrownSword()) {
          // a player standing there reaches 3 blocks from the eyes; 1.20 hung about 4 blocks over the head
          this.counted++;
          if (this.dutchrat.getBoundingBox().distanceToSqr(this.target.getEyePosition()) < 9.0D) {
            this.inReach++;
          }
        }
        // judged once, over the first 300 melee ticks
        if (this.counted == 300 && this.verdict == null) {
          this.verdict = this.inReach * 2 >= this.counted;
          com.github.alexthe666.rats.RatConfig.dutchratSwordThrowChance = this.throwChance;
        }
        return Boolean.TRUE.equals(this.verdict);
      }

      @Override
      String detail(ServerLevel level) {
        var move = this.dutchrat.getMoveControl();
        return "in reach " + this.inReach + " of " + this.counted + " melee ticks, boss at " + this.dutchrat.position() + ", target at " + this.target.position()
          + ", wanted " + (move.hasWanted() ? move.getWantedX() + " " + move.getWantedY() + " " + move.getWantedZ() : "none") + ", motion " + this.dutchrat.getDeltaMovement()
          + ", target " + this.dutchrat.getTarget();
      }
    });
    SCENARIOS.add(new Scenario("bosses and Ratlantis mobs fight for 10 seconds without errors", 200) {
      final List<Mob> mobs = new ArrayList<>();

      @Override
      boolean start(ServerLevel level) {
        Mob target = EntityType.IRON_GOLEM.spawn(level, this.base.offset(0, 0, 6), EntitySpawnReason.COMMAND);
        for (EntityType<? extends Mob> type : List.of(RatsEntityRegistry.RAT_KING.get(), RatsEntityRegistry.BLACK_DEATH.get(), RatsEntityRegistry.PLAGUE_BEAST.get(),
          com.github.alexthe666.rats.registry.RatlantisEntityRegistry.RATLANTEAN_AUTOMATON.get(), com.github.alexthe666.rats.registry.RatlantisEntityRegistry.DUTCHRAT.get(),
          com.github.alexthe666.rats.registry.RatlantisEntityRegistry.NEO_RATLANTEAN.get(), com.github.alexthe666.rats.registry.RatlantisEntityRegistry.RAT_BARON.get(),
          com.github.alexthe666.rats.registry.RatlantisEntityRegistry.FERAL_RATLANTEAN.get(), RatsEntityRegistry.PIED_PIPER.get())) {
          Mob mob = type.spawn(level, this.base.offset(this.mobs.size() - 4, 0, -4), EntitySpawnReason.COMMAND);
          if (mob != null) {
            mob.setTarget(target);
            this.mobs.add(mob);
          }
        }
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return level.getServer().getTickCount() - this.started >= 199 && this.mobs.size() == 9;
      }

      @Override
      String detail(ServerLevel level) {
        return this.mobs.size() + " mobs spawned";
      }
    });
    SCENARIOS.add(new Scenario("1.20.1 items stored in a combined upgrade keep their count and name", 40) {
      net.minecraft.core.NonNullList<ItemStack> items;

      @Override
      boolean start(ServerLevel level) {
        // an upgrade from a 1.20.1 world: its "Items" list stays in custom data as the old version wrote it
        ItemStack upgrade = new ItemStack(RatsItemRegistry.RAT_UPGRADE_COMBINED.get());
        net.minecraft.nbt.CompoundTag tag;
        try {
          tag = net.minecraft.nbt.TagParser.parseCompoundFully("{Items:[{Slot:0b,id:\"rats:rat_upgrade_speed\",Count:1b},{Slot:1b,id:\"minecraft:diamond\",Count:5b,tag:{display:{Name:'{\"text\":\"Shiny\"}'}}}]}");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
          throw new IllegalStateException(e);
        }
        upgrade.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        this.items = com.github.alexthe666.rats.server.misc.StoredItems.view(upgrade, 2);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        ItemStack diamonds = this.items.get(1);
        return this.items.get(0).is(RatsItemRegistry.RAT_UPGRADE_SPEED.get()) && diamonds.is(Items.DIAMOND) && diamonds.getCount() == 5 && "Shiny".equals(diamonds.getHoverName().getString());
      }

      @Override
      String detail(ServerLevel level) {
        return "stored: " + this.items;
      }
    });
    SCENARIOS.add(new Scenario("a plague beast keeps its owner after a save and load", 40) {
      java.util.UUID owner;
      java.util.UUID loaded;

      @Override
      boolean start(ServerLevel level) {
        com.github.alexthe666.rats.server.entity.monster.PlagueBeast beast = RatsEntityRegistry.PLAGUE_BEAST.get().create(level, EntitySpawnReason.COMMAND);
        this.owner = java.util.UUID.randomUUID();
        beast.setOwnerId(this.owner);
        net.minecraft.world.level.storage.TagValueOutput saved = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        beast.saveWithoutId(saved);
        com.github.alexthe666.rats.server.entity.monster.PlagueBeast copy = RatsEntityRegistry.PLAGUE_BEAST.get().create(level, EntitySpawnReason.COMMAND);
        copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        this.loaded = copy.getOwnerId();
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.owner.equals(this.loaded);
      }

      @Override
      String detail(ServerLevel level) {
        return "owner " + this.owner + ", after load " + this.loaded;
      }
    });
    SCENARIOS.add(new Scenario("marbled cheese grass spreads onto marbled cheese dirt, not vanilla dirt", 40) {
      @Override
      boolean start(ServerLevel level) {
        BlockState grass = RatlantisBlockRegistry.MARBLED_CHEESE_GRASS.get().defaultBlockState();
        level.setBlock(this.base, grass, Block.UPDATE_ALL);
        level.setBlock(this.base.east(), RatlantisBlockRegistry.MARBLED_CHEESE_DIRT.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(this.base.west(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        // light for the spreading, whatever the time of day
        level.setBlock(this.base.above(3), Blocks.GLOWSTONE.defaultBlockState(), Block.UPDATE_ALL);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // ~200 random ticks: the east block is one of 45 picks, 4 picks a tick
        for (int i = 0; i < 20; i++) {
          BlockState state = level.getBlockState(this.base);
          state.randomTick(level, this.base, level.getRandom());
        }
        return level.getBlockState(this.base.east()).is(RatlantisBlockRegistry.MARBLED_CHEESE_GRASS.get()) && level.getBlockState(this.base.west()).is(Blocks.DIRT);
      }

      @Override
      String detail(ServerLevel level) {
        return "east " + level.getBlockState(this.base.east()) + ", west " + level.getBlockState(this.base.west());
      }
    });
    // mount upgrades: the rat rides its mount around, and the mount is kept like the rat (the original let it despawn
    // 128 blocks from the player, the rat then fell and died)
    for (var upgrade : List.of(RatsItemRegistry.RAT_UPGRADE_GOLEM_MOUNT, RatsItemRegistry.RAT_UPGRADE_CHICKEN_MOUNT, RatsItemRegistry.RAT_UPGRADE_BEAST_MOUNT)) {
      String name = upgrade.getId().getPath().replace("rat_upgrade_", "");
      // 3600 ticks: the stroll goal starts about once in 120 ticks and a third of its targets are reachable for the
      // mount, so 1200 ticks without a 3-block walk happened now and then
      SCENARIOS.add(new Scenario("a rat on its " + name + " wanders around, the mount never despawns", 3600) {
        TamedRat rat;

        @Override
        boolean start(ServerLevel level) {
          // a glass pen: the scenes float at y 120 and a wandering rat must not walk off the edge. It is wider than the
          // stroll goal's 10 blocks (the floor is 12 out here): in a 7-block pen most stroll targets were behind the
          // glass, and the golem mount sometimes did not move in 1200 ticks (CI)
          for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(-12, -1, -12), this.base.offset(12, 2, 12))) {
            boolean edge = Math.abs(pos.getX() - this.base.getX()) == 12 || Math.abs(pos.getZ() - this.base.getZ()) == 12;
            level.setBlock(pos, pos.getY() == this.base.getY() - 1 ? Blocks.STONE.defaultBlockState() : edge ? Blocks.GLASS.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
          }
          this.rat = tamedRat(level, this.base, RatCommand.WANDER);
          this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(upgrade.get()));
          this.rat.onUpgradeChanged();
          return true;
        }

        @Override
        boolean done(ServerLevel level) {
          // as if a player stood by: the test player is far away, and from 26.3 vanilla resets a persistent mob's
          // idle time only with a player within 32 blocks (idle past 100 ticks, the stroll goal never starts)
          this.rat.setNoActionTime(0);
          if (!(this.rat.getVehicle() instanceof com.github.alexthe666.rats.server.entity.mount.RatMountBase mount)) {
            return false;
          }
          return mount.requiresCustomPersistence() && !mount.removeWhenFarAway(200 * 200) && mount.position().subtract(Vec3.atBottomCenterOf(this.base)).horizontalDistance() > 3;
        }

        @Override
        String detail(ServerLevel level) {
          Entity vehicle = this.rat.getVehicle();
          return "rat at " + this.rat.position().subtract(Vec3.atBottomCenterOf(this.base)) + ", alive " + this.rat.isAlive() + ", vehicle " + (vehicle == null ? "none" : vehicle.getType() + " at "
            + vehicle.position().subtract(Vec3.atBottomCenterOf(this.base)) + (vehicle instanceof Mob mob ? ", persistent " + mob.requiresCustomPersistence() + ", path " + mob.getNavigation().getPath() : ""))
            + ", canMove " + this.rat.canMove() + ", wander " + this.rat.shouldWander() + ", command " + this.rat.getCommand() + ", idle " + this.rat.getNoActionTime()
            + ", stroll targets " + strollTargets();
        }

        // what the stroll goal finds: 20 tries of its random position, and whether the mount's navigation reaches it
        String strollTargets() {
          int found = 0, reached = 0;
          for (int i = 0; i < 20; i++) {
            Vec3 pos = net.minecraft.world.entity.ai.util.LandRandomPos.getPos(this.rat, 10, 7);
            if (pos == null) continue;
            found++;
            var path = this.rat.getNavigation().createPath(BlockPos.containing(pos), 0);
            if (path != null && path.canReach()) reached++;
          }
          return found + "/20 found, " + reached + " reachable";
        }
      });
    }
    SCENARIOS.add(new Scenario("a tamed rat keeps its upgrades, hat and held item through a save and load", 1) {
      @Override
      boolean start(ServerLevel level) {
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        TamedRat rat = tamedRat(level, this.base, RatCommand.SIT);
        rat.setItemSlot(EquipmentSlot.HEAD, new ItemStack(RatsItemRegistry.TOP_HAT.get()));
        rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_HEALTH.get()));
        rat.setItemSlot(EquipmentSlot.LEGS, new ItemStack(RatsItemRegistry.RAT_UPGRADE_SPEED.get()));
        rat.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
        rat.onUpgradeChanged();
        double health = rat.getAttributeValue(Attributes.MAX_HEALTH);
        net.minecraft.world.level.storage.TagValueOutput saved = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        rat.saveWithoutId(saved);
        rat.discard();
        TamedRat loaded = RatsEntityRegistry.TAMED_RAT.get().create(level, EntitySpawnReason.COMMAND);
        loaded.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        level.addFreshEntity(loaded);
        boolean ok = loaded.getItemBySlot(EquipmentSlot.HEAD).is(RatsItemRegistry.TOP_HAT.get()) && loaded.getItemBySlot(EquipmentSlot.CHEST).is(RatsItemRegistry.RAT_UPGRADE_HEALTH.get())
          && loaded.getItemBySlot(EquipmentSlot.LEGS).is(RatsItemRegistry.RAT_UPGRADE_SPEED.get()) && loaded.getMainHandItem().is(Items.BREAD) && loaded.getMainHandItem().getCount() == 3
          && loaded.getAttributeValue(Attributes.MAX_HEALTH) == health;
        RatsMod.LOGGER.info("[Rats selftest] save and load: head {} chest {} legs {} hand {} health {}/{}", loaded.getItemBySlot(EquipmentSlot.HEAD), loaded.getItemBySlot(EquipmentSlot.CHEST), loaded.getItemBySlot(EquipmentSlot.LEGS), loaded.getMainHandItem(), loaded.getAttributeValue(Attributes.MAX_HEALTH), health);
        loaded.discard();
        return ok;
      }
    });
    SCENARIOS.add(new Scenario("#910: an angel rat comes back with the health of its upgrades", 100) {
      TamedRat rat;
      double health;

      @Override
      boolean start(ServerLevel level) {
        this.rat = tamedRat(level, this.base, RatCommand.SIT);
        this.rat.setItemSlot(EquipmentSlot.LEGS, new ItemStack(RatsItemRegistry.RAT_UPGRADE_ANGEL.get()));
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_HEALTH.get()));
        this.rat.onUpgradeChanged();
        this.health = this.rat.getAttributeValue(Attributes.MAX_HEALTH);
        this.rat.hurt(level.damageSources().genericKill(), 1000.0F);
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.health > this.rat.getAttributeBaseValue(Attributes.MAX_HEALTH) && around(level, TamedRat.class, this.base).stream()
          .anyMatch(r -> r != this.rat && r.isAlive() && r.getAttributeValue(Attributes.MAX_HEALTH) == this.health && r.getHealth() == (float) this.health);
      }

      @Override
      String detail(ServerLevel level) {
        return "before " + this.health + ", rats " + around(level, TamedRat.class, this.base).stream().map(r -> r.getHealth() + "/" + r.getAttributeValue(Attributes.MAX_HEALTH) + (r.isAlive() ? "" : " dead") + " chest " + r.getItemBySlot(EquipmentSlot.CHEST) + " legs " + r.getItemBySlot(EquipmentSlot.LEGS) + " countdown " + r.getRespawnCountdown()).toList();
      }
    });
    // #916: what a rat picks up keeps its data (enchantments, damage, name); with and without the platter upgrade
    for (boolean platter : new boolean[] { false, true }) {
      SCENARIOS.add(new Scenario("#916: a rat carries an enchanted, named, damaged sword into a chest unchanged" + (platter ? " (platter)" : ""), 1800) {
        ChestBlockEntity to;
        ItemStack sword;

        @Override
        boolean start(ServerLevel level) {
          this.to = chest(level, this.base.offset(3, 0, 0));
          TamedRat rat = tamedRat(level, this.base, RatCommand.GATHER);
          if (platter) {
            rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_PLATTER.get()));
            rat.onUpgradeChanged();
          }
          rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(3, 0, 0)));
          this.sword = new ItemStack(Items.IRON_SWORD);
          this.sword.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS), 3);
          this.sword.setDamageValue(57);
          this.sword.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Squeaker"));
          // clear the ground the lumberjack next door may have covered
          around(level, ItemEntity.class, this.base).forEach(Entity::discard);
          level.addFreshEntity(new ItemEntity(level, this.base.getX() - 2.5, this.base.getY(), this.base.getZ() + 2.5, this.sword.copy()));
          return true;
        }

        @Override
        boolean done(ServerLevel level) {
          for (int i = 0; i < this.to.getContainerSize(); i++) {
            if (this.to.getItem(i).is(Items.IRON_SWORD)) return ItemStack.isSameItemSameComponents(this.to.getItem(i), this.sword);
          }
          return false;
        }

        @Override
        String detail(ServerLevel level) {
          StringBuilder chest = new StringBuilder();
          for (int i = 0; i < this.to.getContainerSize(); i++) if (!this.to.getItem(i).isEmpty()) chest.append(this.to.getItem(i)).append(this.to.getItem(i).getComponentsPatch()).append(' ');
          return "chest " + chest + ", rats " + around(level, TamedRat.class, this.base).stream().map(r -> r.getMainHandItem() + " " + r.getMainHandItem().getComponentsPatch()).toList();
        }
      });
    }
    SCENARIOS.add(new Scenario("#918: an ore doubling rat turns raw iron into two ore nuggets", 1200) {
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        this.rat = tamedRat(level, this.base, RatCommand.SIT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_ORE_DOUBLING.get()));
        this.rat.onUpgradeChanged();
        this.rat.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON, 4));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return around(level, ItemEntity.class, this.base).stream().filter(i -> i.getItem().is(RatsItemRegistry.RAT_NUGGET_ORE.get())).mapToInt(i -> i.getItem().getCount()).sum() >= 2
          && com.github.alexthe666.rats.server.items.OreRatNuggetItem.getStoredItem(around(level, ItemEntity.class, this.base).stream().filter(i -> i.getItem().is(RatsItemRegistry.RAT_NUGGET_ORE.get())).findFirst().get().getItem(), ItemStack.EMPTY).is(Items.IRON_INGOT);
      }

      @Override
      String detail(ServerLevel level) {
        return "hand " + this.rat.getMainHandItem() + ", items " + around(level, ItemEntity.class, this.base).stream().map(i -> i.getItem().toString()).toList();
      }
    });
    // with Farmer's Delight (-Pwith_compat): young budding tomatoes stay, a ripe tomato vine is harvested
    var budding = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.Identifier.fromNamespaceAndPath("farmersdelight", "budding_tomatoes"));
    var vine = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.Identifier.fromNamespaceAndPath("farmersdelight", "tomatoes"));
    if (budding.isPresent() && vine.isPresent()) {
      SCENARIOS.add(new Scenario("#926, #868: a farmer rat leaves young Farmer's Delight tomatoes and harvests a ripe one", 1800) {
        TamedRat rat;
        ChestBlockEntity to;

        @Override
        boolean start(ServerLevel level) {
          for (int x = -2; x <= 2; x++) {
            level.setBlock(this.base.offset(x, -1, 2), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(this.base.offset(x, 0, 2), budding.get().defaultBlockState(), Block.UPDATE_ALL);
          }
          level.setBlock(this.base.offset(0, -1, -2), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
          var ripe = vine.get().defaultBlockState();
          for (var property : ripe.getProperties()) {
            if (property.getName().equals("age") && property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty age) {
              ripe = ripe.setValue(age, age.getPossibleValues().stream().mapToInt(Integer::intValue).max().getAsInt());
            }
          }
          level.setBlock(this.base.offset(0, 0, -2), ripe, Block.UPDATE_ALL);
          this.to = chest(level, this.base.offset(4, 0, 0));
          this.rat = tamedRat(level, this.base, RatCommand.HARVEST);
          this.rat.setDepositPos(GlobalPos.of(level.dimension(), this.base.offset(4, 0, 0)));
          return true;
        }

        @Override
        boolean done(ServerLevel level) {
          for (int x = -2; x <= 2; x++) {
            if (!level.getBlockState(this.base.offset(x, 0, 2)).is(budding.get())) return false;
          }
          // harvested: gone, or replanted young (the replanter upgrade, or the config that keeps the plant)
          var spot = level.getBlockState(this.base.offset(0, 0, -2));
          boolean harvested = !spot.is(vine.get()) || spot.equals(vine.get().defaultBlockState());
          return harvested && this.rat.tickCount > 600;
        }

        @Override
        String detail(ServerLevel level) {
          StringBuilder row = new StringBuilder();
          for (int x = -2; x <= 2; x++) row.append(level.getBlockState(this.base.offset(x, 0, 2))).append(' ');
          return "young row " + row + ", ripe spot " + level.getBlockState(this.base.offset(0, 0, -2)) + ", rat hand " + this.rat.getMainHandItem() + ", chest " + this.to.getItem(0);
        }
      });
    }
    SCENARIOS.add(new Scenario("tamed rats are persistent, so they leave the rats spawn cap to wild rats", 1) {
      @Override
      boolean start(ServerLevel level) {
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // vanilla's spawner counts a category without the persistent mobs; the garbage piles read the same counts
        TamedRat rat = tamedRat(level, this.base, RatCommand.SIT);
        Rat wild = RatsEntityRegistry.RAT.get().spawn(level, this.base.east(2), EntitySpawnReason.COMMAND);
        boolean result = rat.requiresCustomPersistence() && !wild.requiresCustomPersistence();
        rat.discard();
        wild.discard();
        return result;
      }
    });
    SCENARIOS.add(new Scenario("a chef rat cooks the food in its paws (on the server)", 400) {
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        this.rat = tamedRat(level, this.base, RatCommand.SIT);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_CHEF.get()));
        this.rat.onUpgradeChanged();
        this.rat.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return this.rat.getMainHandItem().is(Items.COOKED_BEEF);
      }

      @Override
      String detail(ServerLevel level) {
        return "holds " + this.rat.getMainHandItem() + ", progress " + this.rat.cookingProgress;
      }
    });
    SCENARIOS.add(new Scenario("a wild rat raids crops: it eats a wheat plant", 1800) {
      Rat rat;

      @Override
      boolean start(ServerLevel level) {
        for (int x = 2; x <= 4; x++) {
          for (int z = -1; z <= 1; z++) {
            level.setBlock(this.base.offset(x, -1, z), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(this.base.offset(x, 0, z), Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7), Block.UPDATE_ALL);
          }
        }
        this.rat = RatsEntityRegistry.RAT.get().spawn(level, this.base, EntitySpawnReason.COMMAND);
        this.rat.setPlagued(false);
        this.rat.setBaby(false);
        this.rat.raidCooldown = 0;
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        for (int x = 2; x <= 4; x++) {
          for (int z = -1; z <= 1; z++) {
            if (!level.getBlockState(this.base.offset(x, 0, z)).is(Blocks.WHEAT)) {
              return true;
            }
          }
        }
        return false;
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position().subtract(Vec3.atBottomCenterOf(this.base)) + ", alive " + this.rat.isAlive() + ", holds " + this.rat.getMainHandItem() + ", raid cooldown " + this.rat.raidCooldown;
      }
    });
    SCENARIOS.add(new Scenario("a wild rat raids a chest: it takes food out", 1800) {
      ChestBlockEntity chest;
      Rat rat;

      @Override
      boolean start(ServerLevel level) {
        this.chest = chest(level, this.base.offset(4, 0, 0));
        this.chest.setItem(0, new ItemStack(Items.BREAD, 16));
        this.rat = RatsEntityRegistry.RAT.get().spawn(level, this.base, EntitySpawnReason.COMMAND);
        this.rat.setPlagued(false);
        this.rat.setBaby(false);
        this.rat.raidCooldown = 0;
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return count(this.chest, Items.BREAD) < 16;
      }

      @Override
      String detail(ServerLevel level) {
        return "rat at " + this.rat.position().subtract(Vec3.atBottomCenterOf(this.base)) + ", alive " + this.rat.isAlive() + ", holds " + this.rat.getMainHandItem();
      }
    });
    SCENARIOS.add(new Scenario("a quarry rat digs down its quarry and brings the blocks back up", 6000) {
      Container quarry;
      TamedRat rat;

      @Override
      boolean start(ServerLevel level) {
        // the scenes float at y 120: solid ground under the quarry, wider than its 5 x 5 pit
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(-5, -12, -5), this.base.offset(5, -2, 5))) {
          level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(this.base, RatsBlockRegistry.RAT_QUARRY.get().defaultBlockState(), Block.UPDATE_ALL);
        this.quarry = (Container) level.getBlockEntity(this.base);
        this.rat = tamedRat(level, this.base.offset(-4, 0, 0), RatCommand.HARVEST);
        this.rat.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RatsItemRegistry.RAT_UPGRADE_QUARRY.get()));
        this.rat.onUpgradeChanged();
        this.rat.setDepositPos(GlobalPos.of(level.dimension(), this.base));
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        // more blocks than the first two layers of a radius 2 quarry have (25 each): the rat climbed the platforms
        int items = 0;
        for (int i = 0; i < this.quarry.getContainerSize(); i++) {
          items += this.quarry.getItem(i).getCount();
        }
        return items > 50;
      }

      @Override
      String detail(ServerLevel level) {
        int items = 0;
        for (int i = 0; i < this.quarry.getContainerSize(); i++) {
          items += this.quarry.getItem(i).getCount();
        }
        List<String> platforms = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(this.base.offset(-2, -12, -2), this.base.offset(2, -1, 2))) {
          if (level.getBlockState(pos).is(RatsBlockRegistry.RAT_QUARRY_PLATFORM.get())) {
            platforms.add(pos.subtract(this.base).toShortString());
          }
        }
        net.minecraft.world.level.pathfinder.Path back = this.rat.getNavigation().createPath(this.base.relative(this.rat.depositFacing), 0);
        List<String> nodes = new ArrayList<>();
        if (back != null) {
          for (int n = 0; n < back.getNodeCount(); n++) {
            nodes.add(back.getNode(n).asBlockPos().subtract(this.base).toShortString());
          }
        }
        List<String> layers = new ArrayList<>();
        for (int y = -1; y >= -4; y--) {
          StringBuilder layer = new StringBuilder("y" + y + ":");
          for (int z = -2; z <= 2; z++) {
            layer.append(' ');
            for (int x = -2; x <= 2; x++) {
              BlockState state = level.getBlockState(this.base.offset(x, y, z));
              layer.append(state.isAir() ? '.' : state.is(RatsBlockRegistry.RAT_QUARRY_PLATFORM.get()) ? 'P' : '#');
            }
          }
          layers.add(layer.toString());
        }
        List<String> legs = new ArrayList<>();
        for (BlockPos leg : List.of(this.base.offset(-1, -2, -1), this.base.offset(-2, -1, -1), this.base.offset(-2, 0, -2), this.base.offset(0, 0, -2), this.base.offset(0, 0, -1), this.base.offset(-3, 0, 0))) {
          net.minecraft.world.level.pathfinder.Path legPath = this.rat.getNavigation().createPath(leg, 0);
          legs.add(leg.subtract(this.base).toShortString() + "=" + (legPath == null ? "none" : legPath.canReach() + "/" + legPath.getNodeCount()));
        }
        return "legs " + legs + ", items in the quarry " + items + ", rat at " + this.rat.position().subtract(Vec3.atBottomCenterOf(this.base)) + ", holds " + this.rat.getMainHandItem() + ", facing " + this.rat.depositFacing
          + ", working " + this.rat.isCurrentlyWorking
          + ", path back " + (back == null ? "none" : "reaches " + back.canReach() + " " + nodes) + ", layers " + layers;
      }
    });
    SCENARIOS.add(new Scenario("two tamed rats in a cage with a breeding lantern have babies", 1200) {
      @Override
      boolean start(ServerLevel level) {
        level.setBlock(this.base.west(), RatsBlockRegistry.RAT_CAGE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(this.base, RatsBlockRegistry.RAT_CAGE_BREEDING_LANTERN.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(this.base.east(), RatsBlockRegistry.RAT_CAGE.get().defaultBlockState(), Block.UPDATE_ALL);
        for (boolean male : new boolean[] {true, false}) {
          TamedRat rat = tamedRat(level, male ? this.base.west() : this.base.east(), RatCommand.SIT);
          rat.setMale(male);
          rat.setAge(0);
        }
        return true;
      }

      @Override
      boolean done(ServerLevel level) {
        return around(level, TamedRat.class, this.base).stream().anyMatch(TamedRat::isBaby);
      }

      @Override
      String detail(ServerLevel level) {
        return around(level, TamedRat.class, this.base).stream().map(r -> (r.isMale() ? "male" : "female") + " in cage " + r.isInCage() + " cooldown " + r.breedCooldown + " at " + r.position().subtract(Vec3.atBottomCenterOf(this.base))).toList().toString();
      }
    });
    // the night changes the whole world, so this one runs on its own: -Drats.selftest.only=plague,doctor,night
    if (System.getProperty("rats.selftest.only", "").contains("night")) {
      SCENARIOS.add(new Scenario("a plague doctor drinks invisibility once a night, not over and over", 1000) {
        com.github.alexthe666.rats.server.entity.misc.PlagueDoctor doctor;
        int ticks;
        int drinks;
        boolean wasDrinking;

        @Override
        boolean start(ServerLevel level) {
          level.dimensionType().defaultClock().ifPresent(clock -> level.clockManager().moveToTimeMarker(clock, net.minecraft.world.clock.ClockTimeMarkers.MIDNIGHT));
          this.doctor = RatsEntityRegistry.PLAGUE_DOCTOR.get().spawn(level, this.base, EntitySpawnReason.COMMAND);
          return true;
        }

        @Override
        boolean done(ServerLevel level) {
          boolean drinking = this.doctor.isUsingItem() && this.doctor.getUseItem().is(Items.POTION);
          if (drinking && !this.wasDrinking) {
            this.drinks++;
          }
          this.wasDrinking = drinking;
          return ++this.ticks >= 900 && this.drinks == 1 && this.doctor.isInvisible();
        }

        @Override
        String detail(ServerLevel level) {
          return "drinks " + this.drinks + " in " + this.ticks + " ticks, invisible " + this.doctor.isInvisible() + ", effects " + this.doctor.getActiveEffects() + ", night " + level.isDarkOutside();
        }
      });
    }
    // -Drats.selftest.only=<part of a name>[,<part>...] runs only the matching scenarios (JAVA_TOOL_OPTIONS reaches the
    // dev run; it splits on spaces, so the parts have none)
    List<String> only = List.of(System.getProperty("rats.selftest.only", "").split(","));
    SCENARIOS.removeIf(scenario -> only.stream().noneMatch(scenario.name::contains));
    int i = 0;
    for (Scenario scenario : SCENARIOS) {
      scenario.base = origin.offset(40 * (i % 5) - 80, 0, 40 + 40 * (i / 5));
      i++;
      flat(level, scenario.base);
    }
    tick = 0;
  }

  /** Ticks of the self-test until every scenario had its whole timeout (they all start at tick 20) */
  static int deadline() {
    return 60 + SCENARIOS.stream().mapToInt(s -> s.timeout).max().orElse(0);
  }

  /** A scenario still running when the self-test stops is a failure, not a silent skip */
  static void failUnfinished() {
    for (Scenario scenario : SCENARIOS) {
      if (!scenario.finished) {
        scenario.finished = true;
        SelfTest.check("gameplay: " + scenario.name + " (did not finish)", () -> false);
      }
    }
  }

  static void tick(ServerLevel level) {
    tick++;
    if (tick == 20) {
      // the forced chunks are loaded and tick entities by now
      for (Scenario scenario : SCENARIOS) {
        scenario.started = level.getServer().getTickCount();
        boolean run;
        try {
          run = scenario.start(level);
        } catch (RuntimeException e) {
          RatsMod.LOGGER.error("[Rats selftest] scenario {} could not start", scenario.name, e);
          SelfTest.check("gameplay: " + scenario.name, () -> false);
          scenario.finished = true;
          continue;
        }
        if (!run) {
          RatsMod.LOGGER.info("[Rats selftest] skip {} (needs the Ratlantis datapack)", scenario.name);
          scenario.finished = true;
        }
      }
      return;
    }
    if (tick < 20) {
      return;
    }
    for (Scenario scenario : SCENARIOS) {
      if (scenario.finished) {
        continue;
      }
      boolean passed;
      try {
        passed = scenario.done(level);
      } catch (RuntimeException e) {
        RatsMod.LOGGER.error("[Rats selftest] scenario {} crashed", scenario.name, e);
        passed = false;
        scenario.finished = true;
        SelfTest.check("gameplay: " + scenario.name, () -> false);
        continue;
      }
      int age = level.getServer().getTickCount() - scenario.started;
      if (passed || age >= scenario.timeout) {
        scenario.finished = true;
        if (!passed) {
          RatsMod.LOGGER.error("[Rats selftest] {} after {} ticks: {}", scenario.name, age, scenario.detail(level));
        } else {
          RatsMod.LOGGER.info("[Rats selftest] {} after {} ticks", scenario.name, age);
        }
        boolean result = passed;
        SelfTest.check("gameplay: " + scenario.name, () -> result);
      }
    }
  }
}
