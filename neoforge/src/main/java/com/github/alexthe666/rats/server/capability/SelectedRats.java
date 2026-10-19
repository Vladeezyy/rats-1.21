package com.github.alexthe666.rats.server.capability;

import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * The rat each player has selected with a staff. The original kept it in a Forge capability; it was never saved, so a
 * map from the player object is enough (one entry per player object: the client's and the server's players are
 * different objects, and a player that leaves is collected with its entry).
 */
public final class SelectedRats {
	private static final Map<Player, SelectedRatCapability> SELECTED = Collections.synchronizedMap(new WeakHashMap<>());

	private SelectedRats() {
	}

	public static Optional<SelectedRatCapability> of(Player player) {
		return Optional.of(SELECTED.computeIfAbsent(player, p -> {
			SelectedRatCapability selected = new SelectedRatCapability();
			selected.setPlayer(p);
			return selected;
		}));
	}
}
