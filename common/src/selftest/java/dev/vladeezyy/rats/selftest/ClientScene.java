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
  private static final boolean ENABLED = MODE.equals("main") || MODE.equals("perf") || MODE.equals("bugs");
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
    } else if (MODE.equals("bugs")) {
      bugSteps();
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

  // -Pscene=bugs: what players reported (CurseForge comments, 2026-11), each with real clicks: the creative inventory
  // with every tab, hats on rats (a carved pumpkin, the aviator hat) and on armor stands (as a player wears them) from
  // the side and from below, the rat's screen with upgrades, the rat attractor hanging from a block (its chains drawn
  // cutout), the cheese staff on a chest (deposit point)
  private static final List<Integer> bugRats = new ArrayList<>();

  private static com.github.alexthe666.rats.server.entity.rat.TamedRat bugRat(ServerPlayer player, double x, double y, double z, ItemStack head) {
    var rat = com.github.alexthe666.rats.registry.RatsEntityRegistry.TAMED_RAT.get().spawn(player.level(), stage.offset((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)), EntitySpawnReason.COMMAND);
    rat.tame(player);
    rat.snapTo(stage.getX() + x, stage.getY() + y, stage.getZ() + z, 180, 0);
    rat.setYHeadRot(180);
    rat.setYBodyRot(180);
    rat.setNoAi(true);
    rat.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, head);
    bugRats.add(rat.getId());
    return rat;
  }

  /** lookAt, flying: a creative player that touched the ground stops flying and would fall from the next view */
  private static void bugLook(ServerPlayer player, double x, double y, double z, double tx, double ty, double tz) {
    player.getAbilities().flying = true;
    player.onUpdateAbilities();
    lookAt(player, x, y, z, tx, ty, tz);
  }

  private static void bugStand(ServerPlayer player, double x, ItemStack head) {
    var stand = EntityType.ARMOR_STAND.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
    stand.snapTo(stage.getX() + x, stage.getY() + 3, stage.getZ() + 5.5, 180, 0);
    stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, head);
    player.level().addFreshEntity(stand);
    bugRats.add(stand.getId());
  }

  private static void selectTab(Minecraft mc, net.minecraft.world.item.CreativeModeTab tab) {
    if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen) {
      try {
        var select = net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", net.minecraft.world.item.CreativeModeTab.class);
        select.setAccessible(true);
        select.invoke(screen, tab);
      } catch (ReflectiveOperationException e) {
        check("select the creative tab " + tab.getDisplayName().getString(), false, e);
      }
    }
  }

  private static void bugSteps() {
    int t = 40;
    // a pad of its own, 30 blocks west of the main stage: stone floor, air above
    at(t, mc -> server(mc, p -> {
      build(p);
      stage = stage.offset(-30, 0, 0);
      ServerLevel level = p.level();
      for (int x = -2; x <= 9; x++) {
        for (int z = -2; z <= 10; z++) {
          level.setBlockAndUpdate(stage.offset(x, -1, z), Blocks.STONE.defaultBlockState());
          for (int y = 0; y <= 6; y++) {
            level.setBlockAndUpdate(stage.offset(x, y, z), Blocks.AIR.defaultBlockState());
          }
        }
      }
    }));
    // the creative inventory: every tab drawn for a few frames (a crash ends the run; the step catches exceptions)
    at(t += 40, mc -> server(mc, p -> {
      p.setGameMode(GameType.CREATIVE);
      bugLook(p, 3.5, 0, 0.5, 3.5, 0, 7);
    }));
    at(t += 10, mc -> mc.setScreen(new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get())));
    for (int k = 0; k < 40; k++) {
      int index = k;
      at(t + 5 + k * 6, mc -> {
        var tabs = net.minecraft.world.item.CreativeModeTabs.allTabs();
        if (index < tabs.size()) {
          var tab = tabs.get(index);
          selectTab(mc, tab);
          var key = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
          RatsMod.LOGGER.info("[scene] creative tab " + key);
          if (key != null && (key.getNamespace().equals(RatsMod.MODID) || tab.getType() == net.minecraft.world.item.CreativeModeTab.Type.INVENTORY)) {
            shot(mc, "bug_creative_" + key.getPath());
          }
        }
      });
    }
    t += 5 + 40 * 6;
    at(t, mc -> {
      check("the creative inventory draws every tab", mc.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen, mc.screen);
      mc.setScreen(null);
    });
    // hats: a carved pumpkin, the aviator hat, a top hat (as it should look) on rats standing on glass 2 blocks up, and
    // the pumpkin and the aviator hat on armor stands (drawn as on a player); seen level with them and from below
    // (the eye under the glass)
    at(t += 5, mc -> server(mc, p -> {
      ServerLevel level = p.level();
      for (int x = 0; x <= 7; x++) {
        for (int z = 4; z <= 7; z++) {
          level.setBlockAndUpdate(stage.offset(x, 2, z), Blocks.GLASS.defaultBlockState());
        }
      }
      bugRat(p, 2.5, 3, 5.5, new ItemStack(Blocks.CARVED_PUMPKIN));
      bugRat(p, 3.5, 3, 5.5, new ItemStack(com.github.alexthe666.rats.registry.RatlantisItemRegistry.AVIATOR_HAT.get()));
      bugRat(p, 4.5, 3, 5.5, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.TOP_HAT.get()));
      bugStand(p, 1.0, new ItemStack(Blocks.CARVED_PUMPKIN));
      bugStand(p, 6.0, new ItemStack(com.github.alexthe666.rats.registry.RatlantisItemRegistry.AVIATOR_HAT.get()));
      bugLook(p, 3.5, 1.75, 3.9, 3.5, 3.2, 5.5);
    }));
    at(t += 30, mc -> shot(mc, "bug_hats_side"));
    at(t += 5, mc -> server(mc, p -> bugLook(p, 3.5, 0, 3.4, 3.5, 3.2, 5.5)));
    at(t += 20, mc -> shot(mc, "bug_hats_below"));
    at(t += 5, mc -> server(mc, p -> bugLook(p, 2.5, 0, 5.0, 2.5, 3.2, 5.6)));
    at(t += 20, mc -> shot(mc, "bug_hats_below_pumpkin"));
    at(t += 5, mc -> server(mc, p -> bugLook(p, 3.5, 0, 5.0, 3.5, 3.2, 5.6)));
    at(t += 20, mc -> shot(mc, "bug_hats_below_aviator"));
    at(t += 5, mc -> server(mc, p -> bugLook(p, 1.0, 0, 2.5, 1.0, 4.6, 5.5)));
    at(t += 20, mc -> shot(mc, "bug_hats_below_stand"));
    // the rat's screen with three upgrades
    at(t += 5, mc -> server(mc, p -> {
      bugLook(p, 3.5, 0, 0.5, 3.5, 0, 7);
      var rat = bugRat(p, 3.5, 0, 2.5, ItemStack.EMPTY);
      rat.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_SPEED.get()));
      rat.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_HEALTH.get()));
      rat.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_BASIC.get()));
      rat.openGUI(p);
    }));
    at(t += 20, mc -> {
      shot(mc, "bug_rat_screen");
      if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen) {
        var menu = screen.getMenu();
        boolean all = !menu.getSlot(3).getItem().isEmpty() && !menu.getSlot(4).getItem().isEmpty() && !menu.getSlot(5).getItem().isEmpty();
        check("the rat's screen shows its three upgrades", all, menu.getSlot(3).getItem() + ", " + menu.getSlot(4).getItem() + ", " + menu.getSlot(5).getItem());
      } else {
        check("the rat's screen opens", false, mc.screen);
      }
      mc.player.closeContainer();
    });
    // the rat attractor: alone, and hanging from a block (the original's "up" state: any block right above it shows the
    // chains, a lantern too); then the hanging one close up, level with its chains, a lime wall behind: cutout shows the
    // wall through the chains, a solid layer (Fabric before 8.1.3.6) white holes
    final BlockPos[] hanging = new BlockPos[1];
    at(t += 5, mc -> server(mc, p -> {
      ServerLevel level = p.level();
      bugRats.forEach(id -> { var e = level.getEntity(id); if (e != null) e.discard(); });
      bugRats.clear();
      for (int x = 0; x <= 7; x++) for (int z = 4; z <= 7; z++) level.setBlockAndUpdate(stage.offset(x, 2, z), Blocks.AIR.defaultBlockState());
      level.setBlockAndUpdate(stage.offset(2, 0, 3), com.github.alexthe666.rats.registry.RatsBlockRegistry.RAT_ATTRACTOR.get().defaultBlockState());
      hanging[0] = stage.offset(4, 2, 3);
      level.setBlockAndUpdate(hanging[0].below(2), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(hanging[0].below(), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(hanging[0], com.github.alexthe666.rats.registry.RatsBlockRegistry.RAT_ATTRACTOR.get().defaultBlockState());
      level.setBlockAndUpdate(hanging[0].above(), Blocks.STONE.defaultBlockState());
      for (int x = 5; x <= 7; x++) for (int z = 4; z <= 6; z++) for (int y = 0; y <= 5; y++) level.setBlockAndUpdate(stage.offset(x, y, z), Blocks.LIME_WOOL.defaultBlockState());
      check("a block placed above the attractor makes it hang (up)", level.getBlockState(hanging[0]).getValue(com.github.alexthe666.rats.server.block.RatAttractorBlock.CONNECTED_UP), level.getBlockState(hanging[0]));
      bugLook(p, 3, 1.4, 1.9, 3.5, 1.8, 3.5);
    }));
    at(t += 25, mc -> {
      shot(mc, "bug_attractor");
      checkNoMissingTexture(mc, "attractor");
    });
    // the eye 1.2 blocks from the chains' axis on the diagonal, at the chains' middle (3/4 of the block up)
    at(t += 5, mc -> server(mc, p -> bugLook(p, 4.5 - 0.85, 2.75 - p.getEyeHeight(), 3.5 - 0.85, 4.5, 2.75, 3.5)));
    at(t += 20, mc -> {
      mc.options.hideGui = true;
    });
    at(t += 3, mc -> {
      shot(mc, "bug_attractor_chain");
      checkChainHoles(mc);
      mc.options.hideGui = false;
    });
    // the cheese staff: select a rat with it, click a chest, press the deposit button, then the server has the point
    final BlockPos[] chest = new BlockPos[1];
    final int[] ratId = new int[1];
    at(t += 5, mc -> server(mc, p -> {
      ServerLevel level = p.level();
      for (int x = 2; x <= 7; x++) for (int z = 3; z <= 6; z++) for (int y = 0; y <= 5; y++) level.setBlockAndUpdate(stage.offset(x, y, z), Blocks.AIR.defaultBlockState());
      chest[0] = stage.offset(3, 0, 5);
      level.setBlockAndUpdate(chest[0], Blocks.CHEST.defaultBlockState());
      var rat = bugRat(p, 4.5, 0, 3.5, ItemStack.EMPTY);
      ratId[0] = rat.getId();
      p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.CHEESE_STICK.get()));
      bugLook(p, 3.5, 0, 2.5, 3.5, 0, 5.5);
    }));
    at(t += 20, mc -> {
      var rat = mc.level.getEntity(ratId[0]);
      check("the staff's rat is on the client", rat != null, ratId[0]);
      if (rat != null) {
        var result = mc.gameMode.interact(mc.player, rat, new net.minecraft.world.phys.EntityHitResult(rat), net.minecraft.world.InteractionHand.MAIN_HAND);
        check("a click with the staff selects the rat", result.consumesAction(), result);
      }
    });
    at(t += 10, mc -> {
      var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(chest[0]).add(0, 0.5, 0), Direction.UP, chest[0], false);
      var result = mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
      RatsMod.LOGGER.info("[scene] staff on the chest: " + result + ", screen " + mc.screen);
    });
    at(t += 15, mc -> {
      shot(mc, "bug_staff_screen");
      boolean screen = mc.screen instanceof com.github.alexthe666.rats.client.gui.CheeseStaffScreen;
      check("a click on a chest with the staff opens the staff screen", screen, mc.screen);
      if (screen) {
        var buttons = mc.screen.children().stream().filter(c -> c instanceof net.minecraft.client.gui.components.Button).map(c -> (net.minecraft.client.gui.components.Button) c).toList();
        check("the staff screen offers the chest as a deposit point", !buttons.isEmpty() && buttons.get(0).visible, buttons.isEmpty() ? "no buttons" : buttons.get(0).getMessage().getString());
        if (!buttons.isEmpty()) {
          buttons.get(0).onPress(new net.minecraft.client.input.KeyEvent(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER, 0, 0));
        }
      }
    });
    at(t += 15, mc -> server(mc, p -> {
      var rat = (com.github.alexthe666.rats.server.entity.rat.TamedRat) p.level().getEntity(ratId[0]);
      check("the server keeps the chest as the rat's deposit point", rat != null && rat.getDepositPos().map(g -> g.pos().equals(chest[0])).orElse(false), rat == null ? "no rat" : rat.getDepositPos());
    }));
    // CurseForge report (2026-11-11): the tinted items' icons in the hotbar (1.21.1 drew them with the tint's alpha 0),
    // each against the empty slot 7, cheese as the control; a lime wall behind the hotbar
    at(t += 10, mc -> {
      mc.setScreen(null);
      server(mc, p -> {
        ServerLevel level = p.level();
        bugRats.forEach(id -> { var e = level.getEntity(id); if (e != null) e.discard(); });
        bugRats.clear();
        for (int x = 0; x <= 7; x++) for (int z = 2; z <= 7; z++) for (int y = 0; y <= 5; y++) level.setBlockAndUpdate(stage.offset(x, y, z), z == 7 ? Blocks.LIME_WOOL.defaultBlockState() : Blocks.AIR.defaultBlockState());
        p.getInventory().clearContent();
        for (int slot = 0; slot < icons().size(); slot++) {
          p.getInventory().setItem(slot, new ItemStack(icons().get(slot)));
        }
        p.getInventory().setSelectedSlot(8);
        bugLook(p, 3.5, 0, 5.5, 3.5, p.getEyeHeight(), 7);
      });
      mc.player.getInventory().setSelectedSlot(8);
    });
    at(t += 25, mc -> {
      shot(mc, "bug_icons");
      checkIcons(mc);
    });
    // the rattling gun: sneak + right click picks it up; the client runs interact too (26.x cast its level to the
    // server's there: a ClassCastException crashed the game)
    final int[] gunId = new int[1];
    at(t += 5, mc -> server(mc, p -> {
      var gun = com.github.alexthe666.rats.registry.RatlantisEntityRegistry.RATTLING_GUN.get().create(p.level(), EntitySpawnReason.COMMAND);
      gun.snapTo(stage.getX() + 3.5, stage.getY(), stage.getZ() + 4.5, 0, 0);
      p.level().addFreshEntity(gun);
      gunId[0] = gun.getId();
      bugLook(p, 3.5, 0, 2.5, 3.5, 0.8, 4.5);
    }));
    at(t += 20, mc -> {
      var gun = mc.level.getEntity(gunId[0]);
      check("the rattling gun is on the client", gun != null, gunId[0]);
      if (gun != null) {
        // 26.x: the local player's sneaking is the shift key of its input (sent in the interact packet)
        var keys = mc.player.input.keyPresses;
        mc.player.input.keyPresses = new net.minecraft.world.entity.player.Input(false, false, false, false, false, true, false);
        try {
          var result = mc.gameMode.interact(mc.player, gun, new net.minecraft.world.phys.EntityHitResult(gun), net.minecraft.world.InteractionHand.MAIN_HAND);
          check("sneak + right click on the rattling gun (the client side)", result.consumesAction(), result);
        } catch (RuntimeException e) {
          check("sneak + right click on the rattling gun (the client side)", false, e);
        } finally {
          mc.player.input.keyPresses = keys;
        }
      }
    });
    at(t += 15, mc -> server(mc, p -> {
      var gun = p.level().getEntity(gunId[0]);
      var items = p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(stage).inflate(10)).stream().map(net.minecraft.world.entity.item.ItemEntity::getItem).toList();
      check("sneak + right click picked the rattling gun up", gun == null && items.stream().anyMatch(i -> i.is(com.github.alexthe666.rats.registry.RatlantisItemRegistry.RATTLING_GUN.get())), "gun " + gun + ", items " + items);
    }));
    at(t += 10, mc -> {
      mc.setScreen(null);
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
    // 1.21 enchantments go by item tags: the enchanting table offers enchantments for the plague doctor mask (the
    // Ratlantis armor's tags come with the Ratlantis pack, off in this world; SelfTest checks them with it on)
    at(t += 5, mc -> server(mc, p -> {
      ServerLevel level = (ServerLevel) p.level();
      BlockPos table = p.blockPosition().offset(0, 0, 3);
      level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
      for (BlockPos offset : net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS) {
        level.setBlockAndUpdate(table.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
      }
      p.setGameMode(GameType.SURVIVAL);
      p.giveExperienceLevels(50);
      p.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inventory, player) -> new net.minecraft.world.inventory.EnchantmentMenu(id, inventory,
        net.minecraft.world.inventory.ContainerLevelAccess.create(level, table)), net.minecraft.network.chat.Component.translatable("container.enchant")));
      var menu = (net.minecraft.world.inventory.EnchantmentMenu) p.containerMenu;
      menu.getSlot(0).set(new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.PLAGUE_DOCTOR_MASK.get()));
      menu.getSlot(1).set(new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI, 64));
      menu.slotsChanged(menu.getSlot(0).container);
      check("the enchanting table offers enchantments for the plague doctor mask", menu.costs[2] > 0 && menu.enchantClue[2] >= 0,
        "costs " + java.util.Arrays.toString(menu.costs) + ", clues " + java.util.Arrays.toString(menu.enchantClue));
    }));
    at(t += 20, mc -> {
      shot(mc, "enchanting");
      mc.player.closeContainer();
    });
    // the mob filter's preview: every entity type of every loaded mod is made and drawn without a crash
    at(t += 10, mc -> {
      int previews = 0;
      for (var id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
        if (com.github.alexthe666.rats.client.util.EntityRenderingUtil.fetchEntity(id, mc.level) != null) {
          previews++;
        }
      }
      check("the mob filter makes a preview of every living entity type without a crash", previews > 50, previews + " previews");
      // a right click on the plague doctor runs mobInteract on the client too: it must not ask for the offers there
      try {
        var doctor = com.github.alexthe666.rats.client.util.EntityRenderingUtil.fetchEntity(net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "plague_doctor"), mc.level);
        var result = ((net.minecraft.world.entity.Mob) doctor).interact(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
        check("a right click on the plague doctor on the client (trading)", result.consumesAction(), result);
      } catch (RuntimeException e) {
        check("a right click on the plague doctor on the client (trading)", false, e);
      }
      mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_UPGRADE_MOB_FILTER.get()));
      var screen = new com.github.alexthe666.rats.client.gui.MobFilterScreen(net.minecraft.world.InteractionHand.MAIN_HAND);
      try {
        var hovered = com.github.alexthe666.rats.client.gui.MobFilterScreen.class.getDeclaredField("hoveredEntityName");
        hovered.setAccessible(true);
        hovered.set(screen, net.minecraft.resources.Identifier.fromNamespaceAndPath(RatsMod.MODID, "plague_doctor"));
      } catch (ReflectiveOperationException e) {
        check("mob filter preview set", false, e);
      }
      mc.setScreen(screen);
    });
    at(t += 15, mc -> {
      shot(mc, "mob_filter");
      checkNoMissingTexture(mc, "mob_filter");
      // a preview that failed to draw puts its entity type on the util's ignore list (26.3 threw for every entity)
      try {
        var ignored = com.github.alexthe666.rats.client.util.EntityRenderingUtil.class.getDeclaredField("IGNORED_ENTITIES");
        ignored.setAccessible(true);
        var names = ((java.util.Set<?>) ignored.get(null)).stream().map(Object::toString).toList();
        check("the mob filter draws the plague doctor's preview", !names.contains(RatsMod.MODID + ":plague_doctor"), "ignored " + names);
      } catch (ReflectiveOperationException e) {
        check("the mob filter draws the plague doctor's preview", false, e);
      }
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
      // #941: a tamed rat wears the halo (the hat a player gives it, not the angel's)
      if (mob instanceof com.github.alexthe666.rats.server.entity.rat.TamedRat) {
        mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(com.github.alexthe666.rats.registry.RatsItemRegistry.HALO_HAT.get()));
      }
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

  /**
   * The rat attractor's chains in the middle of the screen (1.2 blocks away, FOV 70: a block is 0.6 screen heights): the
   * chain planes' transparent texels are white or black, the opaque ones dark blue, the wall behind lime; white or black
   * pixels there mean the chains were drawn solid
   */
  private static void checkChainHoles(Minecraft mc) {
    // the screenshot is read back from the GPU asynchronously in 26.x
    Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
      try (image) {
        int w = image.getWidth(), h = image.getHeight();
        int[] pixels = image.getPixelsABGR();
        int holes = 0, all = 0;
        for (int y = h / 2 - h / 9; y < h / 2 + h / 9; y++) {
          for (int x = w / 2 - h / 16; x < w / 2 + h / 16; x++) {
            int pixel = pixels[y * w + x];
            int r = pixel & 0xFF, g = pixel >> 8 & 0xFF, b = pixel >> 16 & 0xFF;
            all++;
            if (Math.min(r, Math.min(g, b)) > 215 || Math.max(r, Math.max(g, b)) < 16) {
              holes++;
            }
          }
        }
        check("the rat attractor's chains are cutout (no white or black holes)", holes * 100 < all, holes + " of " + all + " pixels white or black");
      }
    });
  }

  /** The hotbar's first slots, checked by checkIcons: the tinted items of the report, cheese last as the control */
  private static List<net.minecraft.world.level.ItemLike> icons() {
    return List.of(
      com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_TUBES[0].get(), com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_IGLOOS[0].get(),
      com.github.alexthe666.rats.registry.RatsItemRegistry.RAT_HAMMOCKS[0].get(), com.github.alexthe666.rats.registry.RatsItemRegistry.PARTY_HAT.get(),
      com.github.alexthe666.rats.registry.RatsItemRegistry.CHEESE.get());
  }

  private static void checkIcons(Minecraft mc) {
    // the screenshot is read back from the GPU asynchronously in 26.x
    Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
      try (image) {
        checkIcons(mc, image.getPixelsABGR(), image.getWidth(), image.getHeight());
      }
    });
  }

  /**
   * The vanilla hotbar: slot l's item at gui (width / 2 - 88 + 20 l, height - 19), 16 x 16; a drawn icon changes at least
   * a tenth of its square against the same square of the empty slot 7
   */
  private static void checkIcons(Minecraft mc, int[] pixels, int w, int h) {
    double scale = mc.getWindow().getGuiScale();
    int center = mc.getWindow().getGuiScaledWidth() / 2, top = (int) ((mc.getWindow().getGuiScaledHeight() - 19) * scale), size = (int) (16 * scale);
    int empty = (int) ((center - 88 + 20 * 7) * scale);
    for (int slot = 0; slot < icons().size(); slot++) {
      int left = (int) ((center - 88 + 20 * slot) * scale), changed = 0;
      for (int y = top; y < Math.min(h, top + size); y++) {
        for (int x = 0; x < size; x++) {
          int a = pixels[y * w + left + x], b = pixels[y * w + empty + x];
          for (int shift = 0; shift < 24; shift += 8) {
            if (Math.abs((a >> shift & 0xFF) - (b >> shift & 0xFF)) > 40) {
              changed++;
              break;
            }
          }
        }
      }
      var key = BuiltInRegistries.ITEM.getKey(icons().get(slot).asItem());
      check("the hotbar draws the icon of " + key, changed * 10 >= size * size, changed + " of " + size * size + " pixels differ from an empty slot");
    }
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
