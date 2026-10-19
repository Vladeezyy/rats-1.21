package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public record ClearRatRecipePacket(long blockPos) implements CustomPacketPayload {

	public static final Type<ClearRatRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "clear_rat_recipe"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClearRatRecipePacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), ClearRatRecipePacket::decode);

	@Override
	public Type<ClearRatRecipePacket> type() {
		return TYPE;
	}


	public static ClearRatRecipePacket decode(RegistryFriendlyByteBuf buf) {
		return new ClearRatRecipePacket(buf.readLong());
	}

	public static void encode(ClearRatRecipePacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeLong(packet.blockPos());
	}

	public static class Handler {
		public static void handle(ClearRatRecipePacket packet, IPayloadContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());
				if (player != null) {
					BlockPos pos = BlockPos.of(packet.blockPos());
					if (player.level().getBlockEntity(pos) instanceof RatCraftingTableBlockEntity table) {
						table.matrixHandler.ifPresent(handler -> {
							for (int slot = 0; slot < handler.getSlots(); slot++) {
								handler.setStackInSlot(slot, ItemStack.EMPTY);
							}
						});
						table.setChanged();
					}
				}
			});
		}
	}
}
