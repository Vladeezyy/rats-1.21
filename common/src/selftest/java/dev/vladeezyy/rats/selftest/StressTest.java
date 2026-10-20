package dev.vladeezyy.rats.selftest;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Server load of working rats ({@code -Drats.stress=<rats>} with {@code tools/selftest.sh}): the dev server's mean tick
 * time (MSPT) with the stage empty, with as many vanilla wolves wandering, then with as many tamed rats at work
 * (half carrying items between chests, a quarter farming wheat, a quarter wandering). Logs "[Rats stress]" lines.
 */
final class StressTest {
  static final int COUNT = Integer.getInteger("rats.stress", 0);
  private static final String PREFIX = "[Rats stress] ";
  private static final int CELL = 7;
  private static final int EMPTY_FROM = 100;
  private static final int WOLVES_AT = 700;
  private static final int RATS_AT = 1400;
  private static final int END = 2800;

  private static BlockPos origin;
  private static ServerPlayer player;
  private static int tick;
  private static final List<Entity> mobs = new ArrayList<>();
  private static final List<ChestBlockEntity> sources = new ArrayList<>();
  private static final List<ChestBlockEntity> targets = new ArrayList<>();
  private static final List<Long> samples = new ArrayList<>();
  private static final List<String> results = new ArrayList<>();

  private StressTest() {}

  static boolean enabled() {
    return COUNT > 0;
  }

  private static int side() {
    return (int) Math.ceil(Math.sqrt(COUNT));
  }

  private static BlockPos cell(int i) {
    return origin.offset((i % side()) * CELL, 0, (i / side()) * CELL);
  }

  static void start(ServerLevel level, ServerPlayer tester, BlockPos at) {
    player = tester;
    origin = at;
    int size = side() * CELL;
    for (int cx = (origin.getX() - 16) >> 4; cx <= (origin.getX() + size + 16) >> 4; cx++) {
      for (int cz = (origin.getZ() - 16) >> 4; cz <= (origin.getZ() + size + 16) >> 4; cz++) {
        level.setChunkForced(cx, cz, true);
      }
    }
    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -1, -2), origin.offset(size + 2, 4, size + 2))) {
      level.setBlock(pos, pos.getY() == origin.getY() - 1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    for (int i = 0; i < COUNT; i++) {
      BlockPos cell = cell(i);
      switch (i % 4) {
        case 0, 2 -> {
          ChestBlockEntity from = chest(level, cell);
          sources.add(from);
          targets.add(chest(level, cell.offset(4, 0, 4)));
        }
        case 1 -> {
          targets.add(chest(level, cell));
          for (BlockPos pos : BlockPos.betweenClosed(cell.offset(2, -1, 2), cell.offset(4, -1, 4))) {
            boolean middle = pos.getX() == cell.getX() + 3 && pos.getZ() == cell.getZ() + 3;
            level.setBlock(pos, middle ? Blocks.WATER.defaultBlockState() : Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (!middle) {
              level.setBlock(pos.above(), ((CropBlock) Blocks.WHEAT).getStateForAge(7), Block.UPDATE_CLIENTS);
            }
          }
        }
        default -> {
        }
      }
    }
    refill();
    RatsMod.LOGGER.info(PREFIX + "stage for {} rats: {}x{} cells of {} blocks", COUNT, side(), side(), CELL);
    tick = 0;
  }

  private static ChestBlockEntity chest(ServerLevel level, BlockPos pos) {
    level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
    return (ChestBlockEntity) level.getBlockEntity(pos);
  }

  private static long delivered;

  /** Keeps the carriers busy: full source chests, empty target chests (what they hold is counted first) */
  private static void refill() {
    for (ChestBlockEntity chest : targets) {
      for (int i = 0; i < chest.getContainerSize(); i++) {
        delivered += chest.getItem(i).getCount();
      }
    }
    for (ChestBlockEntity chest : sources) {
      chest.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
      chest.setItem(1, new ItemStack(Items.OAK_PLANKS, 64));
    }
    for (ChestBlockEntity chest : targets) {
      chest.clearContent();
    }
  }

  private static void spawnWolves(ServerLevel level) {
    for (int i = 0; i < COUNT; i++) {
      // wild: a tamed wolf whose owner is not online sits (vanilla's sit goal), so it would cost nothing
      Wolf wolf = EntityType.WOLF.spawn(level, cell(i).offset(2, 0, 2), MobSpawnType.COMMAND);
      if (wolf != null) {
        mobs.add(wolf);
      }
    }
  }

  private static void spawnRats(ServerLevel level) {
    for (int i = 0; i < COUNT; i++) {
      BlockPos cell = cell(i);
      TamedRat rat = RatsEntityRegistry.TAMED_RAT.get().spawn(level, cell.offset(2, 0, 2), MobSpawnType.COMMAND);
      if (rat == null) {
        continue;
      }
      rat.tame(player);
      switch (i % 4) {
        case 0, 2 -> {
          rat.setCommand(RatCommand.TRANSPORT);
          rat.setPickupPos(GlobalPos.of(level.dimension(), cell));
          rat.setDepositPos(GlobalPos.of(level.dimension(), cell.offset(4, 0, 4)));
        }
        case 1 -> {
          rat.setCommand(RatCommand.HARVEST);
          rat.setDepositPos(GlobalPos.of(level.dimension(), cell));
        }
        default -> rat.setCommand(RatCommand.WANDER);
      }
      mobs.add(rat);
    }
  }

  private static void sample(MinecraftServer server, int from, int to, String phase) {
    if (tick > from && tick <= to && (tick - from) % 100 == 0) {
      samples.add(server.getAverageTickTimeNanos());
    }
    if (tick == to) {
      double mean = samples.stream().mapToLong(Long::longValue).average().orElse(0) / 1.0E6;
      double max = samples.stream().mapToLong(Long::longValue).max().orElse(0) / 1.0E6;
      String line = String.format("%s: %.2f ms mean, %.2f ms worst 100-tick window (%d windows, %d mobs)", phase, mean, max, samples.size(), mobs.size());
      results.add(line);
      RatsMod.LOGGER.info(PREFIX + line);
      samples.clear();
    }
  }

  /** After every server tick; true once done */
  static boolean tick(MinecraftServer server) {
    ServerLevel level = server.overworld();
    tick++;
    if (tick % 400 == 0) {
      refill();
    }
    sample(server, EMPTY_FROM, WOLVES_AT, "empty stage");
    if (tick == WOLVES_AT) {
      spawnWolves(level);
    }
    // a 100-tick settling time after each spawn
    sample(server, WOLVES_AT + 100, RATS_AT, COUNT + " vanilla wolves");
    if (tick == RATS_AT) {
      mobs.forEach(Entity::discard);
      mobs.clear();
      spawnRats(level);
    }
    sample(server, RATS_AT + 200, END, COUNT + " working rats");
    if (tick == RATS_AT) {
      delivered = 0;
    }
    if (tick == END) {
      refill();
      RatsMod.LOGGER.info(PREFIX + "items the rats put in chests: {}, rats alive: {}", delivered, mobs.stream().filter(Entity::isAlive).count());
      RatsMod.LOGGER.info(PREFIX + "DONE " + String.join(" | ", results));
      return true;
    }
    return false;
  }
}
