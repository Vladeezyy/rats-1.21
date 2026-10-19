package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/** The registries of the running server, for code that reads item data without a level at hand */
public final class RegistryAccessHolder {
	@Nullable
	private static MinecraftServer server;

	/** The client's level, set by the client setup (null on a server or before a world is loaded) */
	private static java.util.function.Supplier<net.minecraft.world.level.Level> clientLevel = () -> null;

	private RegistryAccessHolder() {
	}

	public static void setClientLevel(java.util.function.Supplier<net.minecraft.world.level.Level> level) {
		clientLevel = level;
	}

	@Nullable
	public static net.minecraft.world.level.Level clientLevel() {
		return clientLevel.get();
	}

	public static void setServer(@Nullable MinecraftServer server) {
		RegistryAccessHolder.server = server;
	}

	/** The running server (NeoForge's RegistryAccessHolder.server()) */
	@Nullable
	public static MinecraftServer server() {
		return server;
	}

	@Nullable
	public static HolderLookup.Provider get() {
		return server == null ? null : server.registryAccess();
	}
}
