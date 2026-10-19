package com.github.alexthe666.rats.client.util;

import com.github.alexthe666.rats.server.capability.SelectedRats;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * The rat the local player selected. Client-only: common classes call it behind a dist check, so the dedicated server
 * never verifies code that mentions the client player.
 */
public final class ClientRatSelection {
	private ClientRatSelection() {
	}

	@Nullable
	public static TamedRat selectedRat() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : SelectedRats.of(player).map(selected -> selected.getSelectedRat()).orElse(null);
	}
}
