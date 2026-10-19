package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.inventory.RatCraftingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;


public record CycleRatRecipePacket(long blockPos, boolean increase) implements CustomPacketPayload {

	public static final Type<CycleRatRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "cycle_rat_recipe"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleRatRecipePacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), CycleRatRecipePacket::decode);

	@Override
	public Type<CycleRatRecipePacket> type() {
		return TYPE;
	}


	public static CycleRatRecipePacket decode(RegistryFriendlyByteBuf buf) {
		return new CycleRatRecipePacket(buf.readLong(), buf.readBoolean());
	}

	public static void encode(CycleRatRecipePacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeLong(packet.blockPos());
		buf.writeBoolean(packet.increase());
	}

	public static class Handler {
		public static void handle(CycleRatRecipePacket packet, IPayloadContext context) {
			context.enqueueWork(() -> {
				Player player = ((ServerPlayer) context.player());
				if (player != null) {
					if (player.containerMenu instanceof RatCraftingTableMenu table) {
						table.incrementRecipeIndex(packet.increase());
					}
				}
			});
		}
	}
}
