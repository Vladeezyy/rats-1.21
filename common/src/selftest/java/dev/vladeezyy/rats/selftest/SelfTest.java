package dev.vladeezyy.rats.selftest;

import dev.vladeezyy.rats.Rats;
import dev.vladeezyy.rats.ModRegistry;
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
    boolean result;
    try {
      result = test.getAsBoolean();
    } catch (RuntimeException e) {
      Rats.LOG.error(PREFIX + "exception in {}", name, e);
      result = false;
    }
    checks++;
    if (result) {
      Rats.LOG.info(PREFIX + "ok   {}", name);
    } else {
      failures.add(name);
      Rats.LOG.error(PREFIX + "FAIL {}", name);
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
    try {
      checks(server, level);
    } catch (RuntimeException e) {
      Rats.LOG.error(PREFIX + "checks crashed", e);
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
    if (tick == 10) {
      try {
        laterChecks(server.overworld());
      } catch (RuntimeException e) {
        Rats.LOG.error(PREFIX + "later checks crashed", e);
        failures.add("later checks crashed");
      }
    }
    if (tick == 20) {
      int passed = checks - failures.size();
      if (failures.isEmpty()) {
        Rats.LOG.info(PREFIX + "PASS {}/{}", passed, checks);
      } else {
        Rats.LOG.error(PREFIX + "FAILED {}/{}: {}", passed, checks, failures);
      }
      tick = -1;
      server.halt(false);
    }
  }

  private static void checks(MinecraftServer server, ServerLevel level) {
    check("example block registered", () -> BuiltInRegistries.BLOCK.getKey(ModRegistry.exampleBlock.get()).equals(Rats.id("example_block")));
    check("example block placed in the world", () -> {
      level.setBlock(at(0, 0, 0), ModRegistry.exampleBlock.get().defaultBlockState(), Block.UPDATE_ALL);
      return level.getBlockState(at(0, 0, 0)).is(ModRegistry.exampleBlock.get());
    });
    check("example block drops itself (loot table)", () -> {
      LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(at(0, 0, 0)))
        .withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_PICKAXE))
        .withParameter(LootContextParams.BLOCK_STATE, ModRegistry.exampleBlock.get().defaultBlockState()).create(LootContextParamSets.BLOCK);
      List<ItemStack> drops = level.getServer().reloadableRegistries().getLootTable(ModRegistry.exampleBlock.get().getLootTable()).getRandomItems(params);
      return drops.size() == 1 && drops.get(0).is(ModRegistry.exampleBlockItem.get());
    });
    check("a pickaxe is the right tool", () -> new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ModRegistry.exampleBlock.get().defaultBlockState()));
  }

  /** Checks that need a few server ticks first */
  private static void laterChecks(ServerLevel level) {
    check("example block still there after 10 ticks", () -> level.getBlockState(at(0, 0, 0)).is(ModRegistry.exampleBlock.get()));
  }
}
