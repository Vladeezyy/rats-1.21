package com.github.alexthe666.rats.server.capability;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.message.UpdateSelectedRatPacket;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class SelectedRatCapability {

	private Player host;
	@Nullable
	private TamedRat rat;


	public void setPlayer(Player player) {
		this.host = player;
	}


	public void clearSelectedRat() {
		this.rat = null;
	}

	@Nullable

	public TamedRat getSelectedRat() {
		return this.rat;
	}


	public void setSelectedRat(@Nullable TamedRat rat) {
		this.rat = rat;
		this.sendPacket();
	}

	private void sendPacket() {
		if (!this.host.level().isClientSide()) {
			RatsNetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.host), new UpdateSelectedRatPacket(this.host, this));
		}
	}
}
