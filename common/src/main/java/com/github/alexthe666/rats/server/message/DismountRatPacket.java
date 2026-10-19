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
import net.minecraft.world.phys.Vec3;


public record DismountRatPacket(int ratId) implements CustomPacketPayload {

	public static final Type<DismountRatPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "dismount_rat"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DismountRatPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), DismountRatPacket::decode);

	@Override
	public Type<DismountRatPacket> type() {
		return TYPE;
	}


	public static DismountRatPacket decode(RegistryFriendlyByteBuf buf) {
		return new DismountRatPacket(buf.readInt());
	}

	public static void encode(DismountRatPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.ratId());
	}

	public static class Handler {
		public static void handle(DismountRatPacket packet, PacketContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());
				if (player != null) {
					Entity entity = player.level().getEntity(packet.ratId());
					if (entity instanceof TamedRat rat) {
						rat.stopRiding();
						Vec3 dismountPos = rat.getDismountLocationForPassenger(player);
						rat.setPos(dismountPos.x(), dismountPos.y(), dismountPos.z());
					}
				}
			});
		}
	}
}
