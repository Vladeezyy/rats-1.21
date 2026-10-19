package dev.vladeezyy.examplemod.neoforge.selftest;

import dev.vladeezyy.examplemod.ExampleMod;
import dev.vladeezyy.examplemod.selftest.ClientScene;
import dev.vladeezyy.examplemod.selftest.NetScene;
import dev.vladeezyy.examplemod.selftest.SelfTest;
import dev.vladeezyy.examplemod.selftest.TestHooks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Wires the shared tests (common/src/selftest) to NeoForge events; dev runs only. The outer class has no
 * {@code @EventBusSubscriber}: NeoForge refuses a subscriber class without {@code @SubscribeEvent} methods.
 */
public final class NeoForgeTestHooks {
  private NeoForgeTestHooks() {}

  @EventBusSubscriber(modid = ExampleMod.MOD_ID)
  static final class Server {
    static {
      TestHooks.fakePlayer = FakePlayerFactory::get;
    }

    private Server() {}

    @SubscribeEvent
    static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
      SelfTest.serverStarted(event.getServer());
    }

    @SubscribeEvent
    static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
      SelfTest.serverTick(event.getServer());
    }
  }

  @EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
  static final class Client {
    private Client() {}

    @SubscribeEvent
    static void clientTick(ClientTickEvent.Post event) {
      ClientScene.onClientTick();
      NetScene.onClientTick();
    }
  }
}
