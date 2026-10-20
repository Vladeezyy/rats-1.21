package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.server.capability.SelectedRats;
import com.github.alexthe666.rats.server.capability.SelectedRatCapability;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public record UpdateSelectedRatPacket(int entityId, int ratId) implements CustomPacketPayload {

	public static final Type<UpdateSelectedRatPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "update_selected_rat"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSelectedRatPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), UpdateSelectedRatPacket::decode);

	@Override
	public Type<UpdateSelectedRatPacket> type() {
		return TYPE;
	}


	public UpdateSelectedRatPacket(Entity entity, SelectedRatCapability cap) {
		// -1: the selection was cleared (the original crashed here with a null rat)
		this(entity.getId(), cap.getSelectedRat() == null ? -1 : cap.getSelectedRat().getId());
	}

	public static UpdateSelectedRatPacket decode(RegistryFriendlyByteBuf buf) {
		return new UpdateSelectedRatPacket(buf.readInt(), buf.readInt());
	}

	public static void encode(UpdateSelectedRatPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.entityId());
		buf.writeInt(packet.ratId());
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static void handle(UpdateSelectedRatPacket packet, PacketContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
					if (entity instanceof LivingEntity) {
						if (entity instanceof net.minecraft.world.entity.player.Player player && Minecraft.getInstance().level.getEntity(packet.ratId()) instanceof TamedRat rat) {
							SelectedRats.of(player).ifPresent(cap -> cap.setSelectedRat(rat));
						} else if (entity instanceof net.minecraft.world.entity.player.Player player) {
							SelectedRats.of(player).ifPresent(SelectedRatCapability::clearSelectedRat);
						}
					}
				}
			});
		}
	}
}
