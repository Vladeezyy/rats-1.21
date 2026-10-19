package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/** The registries of the running server, for code that reads item data without a level at hand */
public final class RegistryAccessHolder {
	@Nullable
	private static MinecraftServer server;

	private RegistryAccessHolder() {
	}

	public static void setServer(@Nullable MinecraftServer server) {
		RegistryAccessHolder.server = server;
	}

	@Nullable
	public static HolderLookup.Provider get() {
		return server == null ? null : server.registryAccess();
	}
}
