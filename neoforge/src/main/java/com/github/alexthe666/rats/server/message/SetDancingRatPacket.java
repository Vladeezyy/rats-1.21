package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public record SetDancingRatPacket(int ratId, boolean setDancing, long blockPos, int moves) implements CustomPacketPayload {

	public static final Type<SetDancingRatPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "set_dancing_rat"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetDancingRatPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), SetDancingRatPacket::decode);

	@Override
	public Type<SetDancingRatPacket> type() {
		return TYPE;
	}


	public static SetDancingRatPacket decode(RegistryFriendlyByteBuf buf) {
		return new SetDancingRatPacket(buf.readInt(), buf.readBoolean(), buf.readLong(), buf.readInt());
	}

	public static void encode(SetDancingRatPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.ratId());
		buf.writeBoolean(packet.setDancing());
		buf.writeLong(packet.blockPos());
		buf.writeInt(packet.moves());
	}

	public static class Handler {
		public static void handle(SetDancingRatPacket packet, IPayloadContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());
				if (player != null) {
					Entity entity = player.level().getEntity(packet.ratId());
					if (entity instanceof TamedRat rat) {
						if (!rat.isDancing() && packet.setDancing()) {
							rat.setDanceMoves(packet.moves());
						}
						rat.setDancing(packet.setDancing());
						rat.jukeboxPos = BlockPos.of(packet.blockPos());
					}
				}
			});
		}
	}
}
