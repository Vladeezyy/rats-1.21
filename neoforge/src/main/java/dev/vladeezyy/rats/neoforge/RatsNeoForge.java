package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.Rats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Rats.MOD_ID)
public final class RatsNeoForge {
  public RatsNeoForge(IEventBus modBus) {
    Rats.init();
    NeoForgePlatform.registerAll(modBus);
  }
}
