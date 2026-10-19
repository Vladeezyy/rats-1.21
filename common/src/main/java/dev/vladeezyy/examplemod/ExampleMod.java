package dev.vladeezyy.examplemod;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The mod's shared code (both loaders) */
public final class ExampleMod {
  public static final String MOD_ID = "examplemod";
  public static final Logger LOG = LoggerFactory.getLogger("Example Mod");

  private ExampleMod() {}

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  }

  /** Called by the loader's entry point */
  public static void init() {
    ModRegistry.register();
  }
}
