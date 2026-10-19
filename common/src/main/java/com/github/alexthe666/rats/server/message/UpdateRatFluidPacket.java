package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.vladeezyy.rats.platform.transfer.FluidStack;


public record UpdateRatFluidPacket(int ratId, FluidStack fluid) implements CustomPacketPayload {

	public static final Type<UpdateRatFluidPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "update_rat_fluid"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRatFluidPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), UpdateRatFluidPacket::decode);

	@Override
	public Type<UpdateRatFluidPacket> type() {
		return TYPE;
	}


	public static UpdateRatFluidPacket decode(RegistryFriendlyByteBuf buf) {
		return new UpdateRatFluidPacket(buf.readInt(), FluidStack.OPTIONAL_STREAM_CODEC.decode(buf));
	}

	public static void encode(UpdateRatFluidPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.ratId);
		FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.fluid == null ? FluidStack.EMPTY : packet.fluid);
	}

	public static class Handler {

		public static void handle(UpdateRatFluidPacket packet, PacketContext context) {
			context.enqueueWork(() -> {
				Player player = context.player();
				if (player != null) {
					Entity entity = player.level().getEntity(packet.ratId());
					if (entity instanceof TamedRat rat) {
						rat.transportingFluid = packet.fluid();

					}
				}
			});
		}
	}
}
