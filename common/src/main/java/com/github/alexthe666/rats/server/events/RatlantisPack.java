package com.github.alexthe666.rats.server.events;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.message.ChangeRatlantisStatusPacket;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Whether the built-in Ratlantis datapack is on, told to the clients (the loaders call these from their events) */
public final class RatlantisPack {

	/** The pack's id in a world's data packs: "ratlantis", as in 1.20.1 (the port first named it rats:ratlantis) */
	public static final String PACK_ID = "ratlantis";

	private RatlantisPack() {
	}

	/** Whether the world has the pack on; Fabric's built-in packs carry the mod's namespace (rats:ratlantis) */
	public static boolean isEnabled(net.minecraft.server.MinecraftServer server) {
		for (String id : server.getWorldData().getDataConfiguration().dataPacks().getEnabled()) {
			if (id.equals(PACK_ID) || id.endsWith(":" + PACK_ID) || id.endsWith("/" + PACK_ID)) {
				return true;
			}
		}
		return false;
	}

	public static void onLevelLoad(ServerLevel server) {
		if (server.dimension().equals(Level.OVERWORLD) && server.getServer() != null) {
			boolean enabled = isEnabled(server.getServer());
			RatsMod.RATLANTIS_DATAPACK_ENABLED = enabled;
			RatsNetworkHandler.toAll(new ChangeRatlantisStatusPacket(enabled));
			RatsMod.LOGGER.debug("Overworld loaded! Ratlantis datapack is {} for this world.", enabled ? "enabled" : "disabled");
		}
	}

	/** Datapacks sent to one player (joining) or, with null, to everyone (after /reload) */
	public static void onDatapackSync(@Nullable ServerPlayer player, PlayerList playerList) {
		if (player != null) {
			boolean enabled = isEnabled(player.level().getServer());
			RatsMod.RATLANTIS_DATAPACK_ENABLED = enabled;
			RatsNetworkHandler.toPlayer(player, new ChangeRatlantisStatusPacket(enabled));
			RatsMod.LOGGER.debug("Ratlantis datapack flag has been set to {} for {} ({})", enabled, player.getDisplayName().getString(), player.getStringUUID());
		} else {
			playerList.getPlayers().forEach(p -> onDatapackSync(p, playerList));
		}
	}
}
