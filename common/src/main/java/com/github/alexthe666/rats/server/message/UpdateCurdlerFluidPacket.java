package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import dev.vladeezyy.rats.platform.transfer.FluidStack;


public record UpdateCurdlerFluidPacket(long blockPos, FluidStack fluid) implements CustomPacketPayload {

	public static final Type<UpdateCurdlerFluidPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "update_curdler_fluid"));
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
		public static void handle(UpdateCurdlerFluidPacket packet, PacketContext context) {
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
