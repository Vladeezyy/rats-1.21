package dev.vladeezyy.examplemod.selftest;

import dev.vladeezyy.examplemod.ExampleMod;
import dev.vladeezyy.examplemod.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Multiplayer check ({@code tools/nettest.sh}), inert unless {@code examplemod.scene} is {@code net}: the dev client joins
 * a dev dedicated server as an operator and plays like a player (here: gets the example block and places it, the
 * server confirms the block). Add the mod's multiplayer checks as more steps. Logs {@code [scene] CHECK/DONE}, quits.
 */
public final class NetScene {
  private static final boolean ENABLED = System.getProperty("examplemod.scene", "").equals("net");
  private static final int Y = 200;
  private static final BlockPos PLACE = new BlockPos(2, Y, 2);
  private static int tick, checks, failures;
  private static boolean joining;
  private static boolean quitting;

  static void check(String name, boolean ok, Object detail) {
    checks++;
    if (!ok) {
      failures++;
    }
    ExampleMod.LOG.info("[scene] CHECK " + (ok ? "OK " : "FAIL ") + name + (ok ? "" : " (" + detail + ")"));
  }

  private static void command(Minecraft mc, String command) {
    mc.player.connection.sendCommand(command);
  }

  /** After every client tick (wired by the loader's test hooks) */
  public static void onClientTick() {
    if (!ENABLED) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
      mc.options.onboardAccessibility = false;
      mc.options.save();
      mc.setScreen(new TitleScreen());
      return;
    }
    // 26.x ignores --quickPlayMultiplayer in the dev run (the client waits on the title screen): join from code
    if (!joining && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
      joining = true;
      net.minecraft.client.gui.screens.ConnectScreen.startConnecting(mc.screen, mc,
        net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("localhost:25565"),
        new net.minecraft.client.multiplayer.ServerData("Test server", "localhost:25565", net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
      return;
    }
    // kicked or the server went away: fail and quit instead of waiting on the screen until the script's timeout
    if (mc.screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen && !quitting) {
      quitting = true;
      check("stayed connected to the server", false, "disconnected");
      done(mc);
      return;
    }
    if (quitting || mc.player == null || mc.getConnection() == null) {
      return;
    }
    mc.options.pauseOnLostFocus = false;
    mc.getToasts().clear();
    tick++;
    try {
      step(mc);
    } catch (RuntimeException e) {
      check("net step at tick " + tick + " ran", false, e);
      ExampleMod.LOG.error("[scene] net step failed", e);
    }
  }

  private static void step(Minecraft mc) {
    switch (tick) {
      case 40 -> {
        check("joined the dedicated server with the mod", mc.getSingleplayerServer() == null && mc.getConnection().getConnection().isConnected(), "no connection");
        command(mc, "time set day");
        command(mc, "gamerule doMobSpawning false");
        command(mc, "forceload add -4 -4 8 8");
      }
      case 50 -> {
        command(mc, "fill -4 " + (Y - 1) + " -4 8 " + (Y - 1) + " 8 minecraft:stone");
        command(mc, "fill -4 " + Y + " -4 8 " + (Y + 3) + " 8 minecraft:air");
        command(mc, "gamemode survival");
        command(mc, "tp @s 0.5 " + Y + " 0.5 -45 30");
        command(mc, "item replace entity @s weapon.mainhand with " + ExampleMod.MOD_ID + ":example_block");
      }
      // after the teleport the client needs a moment to get the chunks
      case 100 -> check("the item arrived in the hand", mc.player.getMainHandItem().is(ModRegistry.exampleBlockItem.get()), mc.player.getMainHandItem());
      case 105 -> mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
        new BlockHitResult(Vec3.atCenterOf(PLACE.below()).add(0, 0.5, 0), Direction.UP, PLACE.below(), false));
      case 125 -> check("the server confirmed the placed block", mc.level.getBlockState(PLACE).is(ModRegistry.exampleBlock.get()), mc.level.getBlockState(PLACE));
      case 130 -> check("still connected", mc.getConnection() != null && mc.getConnection().getConnection().isConnected(), "");
      case 135 -> {
        quitting = true;
        done(mc);
      }
      default -> {
      }
    }
  }

  private static void done(Minecraft mc) {
    ExampleMod.LOG.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
    if (mc.level != null) {
      mc.level.disconnect();
    }
    mc.disconnect(new TitleScreen());
    mc.stop();
  }
}
