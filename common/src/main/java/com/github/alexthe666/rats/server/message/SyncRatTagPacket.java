package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record SyncRatTagPacket(int ratId, List<GlobalPos> nodes) implements CustomPacketPayload {

	public static final Type<SyncRatTagPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "sync_rat_tag"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncRatTagPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), SyncRatTagPacket::decode);

	@Override
	public Type<SyncRatTagPacket> type() {
		return TYPE;
	}


	public static SyncRatTagPacket decode(RegistryFriendlyByteBuf buf) {
		return new SyncRatTagPacket(buf.readInt(), buf.readList(FriendlyByteBuf::readGlobalPos));
	}

	public static void encode(SyncRatTagPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.ratId());
		buf.writeCollection(packet.nodes(), FriendlyByteBuf::writeGlobalPos);
	}

	public static class Handler {
		public static void handle(SyncRatTagPacket packet, PacketContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());

				if (player != null) {
					TamedRat rat = RatsNetworkHandler.ownedRat(player, packet.ratId());
					if (rat != null) {
						rat.setPatrolNodes(packet.nodes());
					}
				}

			});
		}
	}
}
