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
        return count(this.to, Items.BONE) > 0;
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
