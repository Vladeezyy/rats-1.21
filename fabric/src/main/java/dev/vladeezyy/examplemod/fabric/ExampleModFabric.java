package dev.vladeezyy.examplemod.fabric;

import dev.vladeezyy.examplemod.ExampleMod;
import net.fabricmc.api.ModInitializer;

public final class ExampleModFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    ExampleMod.init();
  }
}
