package dev.vladeezyy.rats.selftest;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client scene, inert unless {@code -Drats.scene=main} ({@code tools/scenetest.sh}): builds a small stage with the
 * mod's blocks, takes screenshots, checks the textures (no magenta on screen) and the translations, shows the items in
 * the inventory, logs {@code [scene] CHECK OK/FAIL}, then quits. Add the mod's views as more {@code at(...)} steps.
 */
public final class ClientScene {
  private static final String MODE = System.getProperty("rats.scene", "");
  private static final boolean ENABLED = MODE.equals("main") || MODE.equals("perf");
  /** -Pscene=perf: FPS with this many mobs in view (-Drats.scene.mobs) */
  private static final int PERF_MOBS = Integer.getInteger("rats.scene.mobs", 100);
  static final String LANG = System.getProperty("rats.scene.lang", "en_us");
  private static final String WORLD = RatsMod.MODID + "_scene";
  private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
  private static boolean opened;
  private static int tick, checks, failures;
  private static BlockPos stage;

  static void at(int t, Consumer<Minecraft> action) {
    STEPS.add(Pair.of(t, action));
  }

  static void check(String name, boolean ok, Object detail) {
    checks++;
    if (!ok) {
      failures++;
    }
    RatsMod.LOGGER.info("[scene] CHECK " + (ok ? "OK " : "FAIL ") + name + (ok ? "" : " (" + detail + ")"));
  }

  static void server(Minecraft mc, Consumer<ServerPlayer> action) {
    var server = mc.getSingleplayerServer();
    server.execute(() -> {
      try {
        action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()));
      } catch (RuntimeException | LinkageError e) {
        check("server step ran", false, e);
        RatsMod.LOGGER.error("[scene] server step failed", e);
      }
    });
  }

  static void shot(Minecraft mc, String name) {
    Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), 1, c -> RatsMod.LOGGER.info("[scene] " + c.getString()));
  }

  static void lookAt(ServerPlayer player, double x, double y, double z, double tx, double ty, double tz) {
    double dx = tx - x, dy = ty - (y + player.getEyeHeight()), dz = tz - z;
    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
    float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    player.teleportTo(player.level(), stage.getX() + x, stage.getY() + y, stage.getZ() + z, java.util.Set.of(), yaw, pitch, true);
  }

  static {
    if (MODE.equals("perf")) {
      perfSteps();
    } else {
      mainSteps();
    }
  }

  // FPS of the client: the stage in a glass pen, empty, then with PERF_MOBS rats wandering in it, then as many wolves;
  // each measured for 10 s (one sample a second), "[scene] PERF" lines
  private static final List<Integer> fps = new ArrayList<>();
  private static final List<String> perf = new ArrayList<>();
  private static final List<net.minecraft.world.entity.Entity> perfMobs = new ArrayList<>();

  private static void measure(int from, String what) {
    for (int t = from + 20; t <= from + 200; t += 20) {
      at(t, mc -> fps.add(mc.getFps()));
    }
    at(from + 201, mc -> {
      double mean = fps.stream().mapToInt(Integer::intValue).average().orElse(0);
      int min = fps.stream().mapToInt(Integer::intValue).min().orElse(0);
      String line = String.format("%s: %.0f FPS mean, %d lowest second (%d samples)", what, mean, min, fps.size());
      perf.add(line);
      RatsMod.LOGGER.info("[scene] PERF " + line);
      fps.clear();
    });
  }

  private static void perfMobs(ServerPlayer player, boolean rats) {
    ServerLevel level = player.level();
    perfMobs.forEach(net.minecraft.world.entity.Entity::discard);
    perfMobs.clear();
    for (int i = 0; i < PERF_MOBS; i++) {
      Mob mob = rats ? com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND) : EntityType.WOLF.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      mob.snapTo(stage.getX() - 4 + (i % 10) * 1.5, stage.getY(), stage.getZ() - 4 + (i / 10) * 1.5, i * 37, 0);
      mob.setPersistenceRequired();
      if (mob instanceof com.github.alexthe666.rats.server.entity.rat.TamedRat rat) {
        rat.tame(player);
        rat.setCommand(com.github.alexthe666.rats.server.entity.rat.RatCommand.WANDER);
      }
      level.addFreshEntity(mob);
      perfMobs.add(mob);
    }
  }

  private static void perfSteps() {
    int t = 40;
    at(t, mc -> {
      mc.options.framerateLimit().set(260);
      mc.options.enableVsync().set(false);
      server(mc, ClientScene::build);
    });
    at(t += 40, mc -> server(mc, p -> {
      ServerLevel level = p.level();
      // a glass pen over the stage, so the mobs stay in view; the blocks of the main scene are cleared
      for (BlockPos pos : BlockPos.betweenClosed(stage.offset(-6, 0, -6), stage.offset(12, 6, 12))) {
        boolean edge = pos.getX() == stage.getX() - 6 || pos.getX() == stage.getX() + 12 || pos.getZ() == stage.getZ() - 6 || pos.getZ() == stage.getZ() + 12;
        level.setBlock(pos, edge && pos.getY() <= stage.getY() + 1 ? Blocks.GLASS.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
      }
      p.setGameMode(GameType.SPECTATOR);
      lookAt(p, 3, 9, -12, 3, 0, 3);
    }));
    measure(t += 40, "empty stage");
    at(t += 210, mc -> server(mc, p -> perfMobs(p, true)));
    measure(t += 60, PERF_MOBS + " rats wandering");
    at(t + 205, mc -> shot(mc, "perf_rats"));
    at(t += 210, mc -> server(mc, p -> perfMobs(p, false)));
    measure(t += 60, PERF_MOBS + " wolves wandering");
    at(t += 210, mc -> {
      shot(mc, "perf");
      RatsMod.LOGGER.info("[scene] PERF DONE " + String.join(" | ", perf));
      RatsMod.LOGGER.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
      mc.disconnectWithSavingScreen();
      mc.stop();
    });
  }

  private static void mainSteps() {
    int t = 40;
    at(t, mc -> server(mc, ClientScene::build));
    at(t += 60, mc -> server(mc, p -> lookAt(p, 3.5, 0, 3.5, 3.5, 0, 7)));
    at(t += 25, mc -> {
      shot(mc, "blocks");
      checkNoMissingTexture(mc, "blocks");
    });
    // every mob of the mod (rendered through Rampart's models), frozen in rows
    at(t += 5, mc -> server(mc, ClientScene::spawnMobs));
    at(t += 20, mc -> server(mc, p -> lookAt(p, 5, 5, 6, 5, 0, -8)));
    at(t += 30, mc -> {
      shot(mc, "mobs");
      checkNoMissingTexture(mc, "mobs");
    });
    at(t += 5, mc -> server(mc, p -> {
      p.setGameMode(GameType.SURVIVAL);
      p.getInventory().clearContent();
      int slot = 0;
      for (var item : BuiltInRegistries.ITEM) {
        if (slot < 36 && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(RatsMod.MODID)) {
          p.getInventory().setItem(slot++, new ItemStack(item));
        }
      }
    }));
    at(t += 10, mc -> mc.setScreen(new InventoryScreen(mc.player)));
    at(t += 15, mc -> {
      shot(mc, "inventory");
      checkNoMissingTexture(mc, "inventory");
      mc.setScreen(null);
    });
    at(t += 5, ClientScene::checkTranslations);
    at(t += 10, mc -> {
      RatsMod.LOGGER.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
      mc.disconnectWithSavingScreen();
      mc.stop();
    });
  }

  /** After every client tick (wired by the loader's test hooks) */
  public static void onClientTick() {
    if (!ENABLED) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    if (!opened && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
      mc.options.onboardAccessibility = false;
      mc.options.save();
      mc.setScreen(new TitleScreen());
      return;
    }
    if (!opened && mc.getOverlay() != null) {
      return;
    }
    if (!opened && mc.screen instanceof TitleScreen) {
      if (!mc.options.languageCode.equals(LANG)) {
        mc.options.languageCode = LANG;
        mc.getLanguageManager().setSelected(LANG);
        mc.options.save();
        mc.reloadResourcePacks();
        return;
      }
      opened = true;
      GLFW.glfwSetWindowSize(mc.getWindow().handle(), 1600, 900);
      mc.options.guiScale().set(3);
      mc.options.cloudStatus().set(CloudStatus.OFF);
      mc.options.pauseOnLostFocus = false;
      mc.resizeGui();
      LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
      mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261016L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
      return;
    }
    if (mc.player == null || mc.getSingleplayerServer() == null) {
      return;
    }
    mc.options.pauseOnLostFocus = false;
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
    mc.getToastManager().clear();
    if (mc.screen instanceof PauseScreen) {
      mc.setScreen(null);
    }
    tick++;
    for (Pair<Integer, Consumer<Minecraft>> step : STEPS) {
      if (step.getFirst() == tick) {
        try {
          step.getSecond().accept(mc);
        } catch (RuntimeException | LinkageError e) {
          check("step at tick " + tick + " ran", false, e);
          RatsMod.LOGGER.error("[scene] step failed", e);
        }
      }
    }
  }

  private static void spawnMobs(ServerPlayer player) {
    ServerLevel level = player.level();
    int i = 0;
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      if (!BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(RatsMod.MODID)) {
        continue;
      }
      var entity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      if (!(entity instanceof Mob mob)) {
        continue;
      }
      mob.setNoAi(true);
      mob.setPersistenceRequired();
      mob.snapTo(stage.getX() + (i % 6) * 2.5, stage.getY(), stage.getZ() - 3 - (i / 6) * 3.0, 180, 0);
      level.addFreshEntity(mob);
      i++;
    }
    check("mobs spawned for the picture (" + i + ")", i > 15, i);
  }

  private static void build(ServerPlayer player) {
    ServerLevel level = player.level();
    BlockPos spawn = level.getRespawnData().pos();
    stage = new BlockPos(spawn.getX(), 140, spawn.getZ());
    level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
    level.getGameRules().set(GameRules.ADVANCE_WEATHER, false, level.getServer());
    level.getGameRules().set(GameRules.SPAWN_MOBS, false, level.getServer());
    // the day time moved to clocks in 26.1, the command works on every version
    level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(), "time set noon");
    for (BlockPos pos : BlockPos.betweenClosed(stage.offset(-6, -1, -6), stage.offset(12, 6, 12))) {
      level.setBlock(pos, pos.getY() == stage.getY() - 1 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    // the mod's blocks in a row, in front of the camera
    List<Block> blocks = new ArrayList<>();
    for (Block block : BuiltInRegistries.BLOCK) {
      if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(RatsMod.MODID)) {
        blocks.add(block);
      }
    }
    for (int i = 0; i < blocks.size(); i++) {
      level.setBlock(stage.offset(i % 8, i / 8, 7), blocks.get(i).defaultBlockState(), Block.UPDATE_ALL);
    }
    player.getAbilities().flying = true;
    player.onUpdateAbilities();
  }

  /** Every key the mod shows is in the language, and the language file has no other keys */
  private static void checkTranslations(Minecraft mc) {
    List<String> needed = new ArrayList<>();
    for (Item item : BuiltInRegistries.ITEM) {
      if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(RatsMod.MODID)) {
        needed.add(item.getDescriptionId());
      }
    }
    for (Block block : BuiltInRegistries.BLOCK) {
      if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(RatsMod.MODID)) {
        needed.add(block.getDescriptionId());
      }
    }
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(RatsMod.MODID)) {
        needed.add(type.getDescriptionId());
      }
    }
    // other texts the mod shows (messages, death messages, effects, ...)
    // the creative tabs' titles
    for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
      if (BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getNamespace().equals(RatsMod.MODID)
        && tab.getDisplayName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
        needed.add(translatable.getKey());
      }
    }
    Language language = Language.getInstance();
    List<String> missing = needed.stream().distinct().filter(key -> !language.has(key)).toList();
    check("translations: every item, block, entity and tab in " + LANG + " (" + needed.size() + " keys)", missing.isEmpty(), missing);
  }

  private static void checkNoMissingTexture(Minecraft mc, String name) {
    // the screenshot is read back from the GPU asynchronously in 26.x
    Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
      int magenta = 0;
      try (image) {
        for (int pixel : image.getPixelsABGR()) {
          int r = pixel & 0xFF, g = pixel >> 8 & 0xFF, b = pixel >> 16 & 0xFF;
          if (r > 200 && b > 200 && g < 40) {
            magenta++;
          }
        }
      }
      check(name + ": no missing texture on screen", magenta < 50, magenta + " magenta pixels");
    });
  }
}
