package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.world.item.ItemStack;


public record SyncArmSwingPacket(ItemStack stack) implements CustomPacketPayload {

	public static final Type<SyncArmSwingPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "sync_arm_swing"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncArmSwingPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), SyncArmSwingPacket::decode);

	@Override
	public Type<SyncArmSwingPacket> type() {
		return TYPE;
	}


	// the stack is not sent: the server uses the item the player holds (the original trusted the client's stack,
	// so a modified client could shoot plague shots with any damage)
	public static SyncArmSwingPacket decode(RegistryFriendlyByteBuf buf) {
		return new SyncArmSwingPacket(ItemStack.EMPTY);
	}

	public static void encode(SyncArmSwingPacket packet, RegistryFriendlyByteBuf buf) {
	}

	public static class Handler {
		public static void handle(SyncArmSwingPacket packet, PacketContext context) {
			context.enqueueWork(() -> {
				ServerPlayer player = (ServerPlayer) context.player();
				RatsEvents.handleArmSwing(player.getMainHandItem(), player);
			});
		}
	}
}
