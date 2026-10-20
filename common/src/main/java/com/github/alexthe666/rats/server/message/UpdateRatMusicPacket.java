package com.github.alexthe666.rats.server.message;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.client.util.RatRecordSoundInstance;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;


public record UpdateRatMusicPacket(int id, Item record) implements CustomPacketPayload {

	public static final Type<UpdateRatMusicPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "update_rat_music"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRatMusicPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), UpdateRatMusicPacket::decode);

	@Override
	public Type<UpdateRatMusicPacket> type() {
		return TYPE;
	}


	public static UpdateRatMusicPacket decode(RegistryFriendlyByteBuf buf) {
		return new UpdateRatMusicPacket(buf.readInt(), ByteBufCodecs.registry(Registries.ITEM).decode(buf));
	}

	public static void encode(UpdateRatMusicPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.id());
		ByteBufCodecs.registry(Registries.ITEM).encode(buf, packet.record());
	}

	public static class Handler {
		@SuppressWarnings("Convert2Lambda")
		public static void handle(UpdateRatMusicPacket packet, PacketContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					if (Minecraft.getInstance().level != null) {
						Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
						if (entity instanceof TamedRat rat) {
							// 1.21: discs play jukebox songs
							JukeboxSong.fromStack(new ItemStack(packet.record())).ifPresent(song -> {
								Minecraft.getInstance().getSoundManager().queueTickingSound(new RatRecordSoundInstance(rat, packet.record(), song.value().soundEvent().value()));
								Minecraft.getInstance().gui.hud.setNowPlaying(song.value().description());
							});
						}
					}
				}
			});
		}
	}
}
