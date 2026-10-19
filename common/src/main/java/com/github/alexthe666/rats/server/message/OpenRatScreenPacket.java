package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.client.gui.RatScreen;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.inventory.RatMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;


public record OpenRatScreenPacket(int containerId, int entityId) implements CustomPacketPayload {

	public static final Type<OpenRatScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "open_rat_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenRatScreenPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), OpenRatScreenPacket::decode);

	@Override
	public Type<OpenRatScreenPacket> type() {
		return TYPE;
	}


	public static OpenRatScreenPacket decode(RegistryFriendlyByteBuf buf) {
		return new OpenRatScreenPacket(buf.readInt(), buf.readInt());
	}

	public static void encode(OpenRatScreenPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.containerId());
		buf.writeInt(packet.entityId());
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static void handle(OpenRatScreenPacket packet, PacketContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
					if (entity instanceof TamedRat rat) {
						LocalPlayer localplayer = Minecraft.getInstance().player;
						SimpleContainer container = new SimpleContainer(6);
						RatMenu menu = new RatMenu(packet.containerId(), container, localplayer.getInventory());
						localplayer.containerMenu = menu;
						Minecraft.getInstance().setScreen(new RatScreen(menu, localplayer.getInventory(), rat));
					}
				}
			});
		}
	}
}
