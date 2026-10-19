package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;


public record UpdateCurdlerFluidPacket(long blockPos, FluidStack fluid) implements CustomPacketPayload {

	public static final Type<UpdateCurdlerFluidPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "update_curdler_fluid"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCurdlerFluidPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), UpdateCurdlerFluidPacket::decode);

	@Override
	public Type<UpdateCurdlerFluidPacket> type() {
		return TYPE;
	}


	public static UpdateCurdlerFluidPacket decode(RegistryFriendlyByteBuf buf) {
		return new UpdateCurdlerFluidPacket(buf.readLong(), FluidStack.OPTIONAL_STREAM_CODEC.decode(buf));
	}

	public static void encode(UpdateCurdlerFluidPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeLong(packet.blockPos());
		FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.fluid());
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static void handle(UpdateCurdlerFluidPacket packet, IPayloadContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					BlockPos pos = BlockPos.of(packet.blockPos());
					Level level = Minecraft.getInstance().level;
					if (level != null && level.getBlockEntity(pos) instanceof AutoCurdlerBlockEntity curdler) {
						curdler.getTank().setFluid(packet.fluid());
					}
				}
			});
		}
	}

}
