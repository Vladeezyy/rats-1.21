package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public record RatCommandPacket(int ratId, int newCommand) implements CustomPacketPayload {

	public static final Type<RatCommandPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "rat_command"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RatCommandPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), RatCommandPacket::decode);

	@Override
	public Type<RatCommandPacket> type() {
		return TYPE;
	}


	public static RatCommandPacket decode(RegistryFriendlyByteBuf buf) {
		return new RatCommandPacket(buf.readInt(), buf.readInt());
	}

	public static void encode(RatCommandPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.ratId());
		buf.writeInt(packet.newCommand());
	}

	public static class Handler {
		public static void handle(RatCommandPacket packet, PacketContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());
				if (player != null) {
					TamedRat rat = RatsNetworkHandler.ownedRat(player, packet.ratId());
					if (rat != null) {
						rat.setCommandInteger(packet.newCommand());
					}
				}
			});
		}
	}
}
