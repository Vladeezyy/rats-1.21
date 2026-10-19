package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.client.util.RatRecordSoundInstance;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.core.registries.BuiltInRegistries;


public record UpdateRatMusicPacket(int id, RecordItem record) implements CustomPacketPayload {

	public static final Type<UpdateRatMusicPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "update_rat_music"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRatMusicPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), UpdateRatMusicPacket::decode);

	@Override
	public Type<UpdateRatMusicPacket> type() {
		return TYPE;
	}


	public static UpdateRatMusicPacket decode(RegistryFriendlyByteBuf buf) {
		return new UpdateRatMusicPacket(buf.readInt(), (RecordItem) buf.readRegistryIdUnsafe(BuiltInRegistries.ITEM));
	}

	public static void encode(UpdateRatMusicPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.id());
		buf.writeRegistryIdUnsafe(BuiltInRegistries.ITEM, packet.record());
	}

	public static class Handler {
		@SuppressWarnings("Convert2Lambda")
		public static void handle(UpdateRatMusicPacket packet, IPayloadContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					if (Minecraft.getInstance().level != null) {
						Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
						if (entity instanceof TamedRat rat) {
							Minecraft.getInstance().getSoundManager().queueTickingSound(new RatRecordSoundInstance(rat, packet.record()));
							Minecraft.getInstance().gui.setNowPlaying(packet.record().getDisplayName());
						}
					}
				}
			});
		}
	}
}
