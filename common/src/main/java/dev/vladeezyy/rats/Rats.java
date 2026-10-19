package dev.vladeezyy.rats;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The mod's shared code (both loaders) */
public final class Rats {
  public static final String MOD_ID = "rats";
  public static final Logger LOG = LoggerFactory.getLogger("Rats");

  private Rats() {}

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
  }

  /** Called by the loader's entry point */
  public static void init() {
    ModRegistry.register();
  }
}
