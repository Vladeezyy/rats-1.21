package dev.vladeezyy.examplemod.fabric.selftest;

import dev.vladeezyy.examplemod.selftest.SelfTest;
import dev.vladeezyy.examplemod.selftest.TestHooks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Wires the shared tests (common/src/selftest) to Fabric API events; dev runs only */
public final class FabricTestHooks implements ModInitializer {
  @Override
  public void onInitialize() {
    TestHooks.fakePlayer = FakePlayer::get;
    ServerLifecycleEvents.SERVER_STARTED.register(SelfTest::serverStarted);
    ServerTickEvents.END_SERVER_TICK.register(SelfTest::serverTick);
  }
}
