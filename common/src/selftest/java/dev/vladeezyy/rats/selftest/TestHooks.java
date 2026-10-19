package dev.vladeezyy.rats.selftest;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.BiFunction;

/** What the shared tests need from the loader, set by its test bootstrap */
public final class TestHooks {
  public static BiFunction<ServerLevel, GameProfile, ServerPlayer> fakePlayer;

  static final GameProfile TESTER = new GameProfile(UUID.fromString("5f0c8d1a-2b7e-4f3a-9c61-3e2d4a7b9c10"), "[Rats test]");

  private TestHooks() {}
}
