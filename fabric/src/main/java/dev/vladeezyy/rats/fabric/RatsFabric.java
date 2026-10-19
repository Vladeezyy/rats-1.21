package dev.vladeezyy.rats.fabric;

import dev.vladeezyy.rats.Rats;
import net.fabricmc.api.ModInitializer;

public final class RatsFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    Rats.init();
  }
}
