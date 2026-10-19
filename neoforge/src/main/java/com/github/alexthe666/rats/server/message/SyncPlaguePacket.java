package com.github.alexthe666.rats.server.message;

import net.minecraft.server.level.ServerPlayer;
import com.github.alexthe666.rats.RatsMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public record SyncPlaguePacket(int entityId, byte effectId, byte amplifier, int duration, byte flags) implements CustomPacketPayload {

	public static final Type<SyncPlaguePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "sync_plague"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlaguePacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), SyncPlaguePacket::decode);

	@Override
	public Type<SyncPlaguePacket> type() {
		return TYPE;
	}

	private static final int FLAG_AMBIENT = 1;
	private static final int FLAG_VISIBLE = 2;
	private static final int FLAG_SHOW_ICON = 4;

	public SyncPlaguePacket(int id, MobEffectInstance effect) {
		this(id, (byte) (MobEffect.getId(effect.getEffect()) & 255), (byte) (effect.getAmplifier() & 255), Math.min(effect.getDuration(), 32767), getFlags(effect));
	}

	private static byte getFlags(MobEffectInstance mobEffectInstance) {
		byte flags = 0;
		if (mobEffectInstance.isAmbient()) {
			flags = (byte) (flags | FLAG_AMBIENT);
		}

		if (mobEffectInstance.isVisible()) {
			flags = (byte) (flags | FLAG_VISIBLE);
		}

		if (mobEffectInstance.showIcon()) {
			flags = (byte) (flags | FLAG_SHOW_ICON);
		}

		return flags;
	}

	public boolean isEffectVisible() {
		return (this.flags() & 2) == 2;
	}

	public boolean isEffectAmbient() {
		return (this.flags() & 1) == 1;
	}

	public boolean effectShowsIcon() {
		return (this.flags() & 4) == 4;
	}

	public static SyncPlaguePacket decode(RegistryFriendlyByteBuf buf) {
		return new SyncPlaguePacket(buf.readVarInt(), buf.readByte(), buf.readByte(), buf.readVarInt(), buf.readByte());
	}

	public static void encode(SyncPlaguePacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(packet.entityId());
		buf.writeByte(packet.effectId());
		buf.writeByte(packet.amplifier());
		buf.writeVarInt(packet.duration());
		buf.writeByte(packet.flags());
	}

	public static class Handler {
		@SuppressWarnings("Convert2Lambda")
		public static void handle(SyncPlaguePacket packet, IPayloadContext context) {
			context.enqueueWork(new Runnable() {
				@Override
				public void run() {
					if (Minecraft.getInstance().level == null) {
						return;
					}

					Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
					if (entity instanceof LivingEntity living) {
						MobEffect mobeffect = MobEffect.byId(packet.effectId() & 0xFF);
						if (mobeffect != null) {
							if (packet.duration() == 0) {
								living.removeEffect(mobeffect);
							} else {
								MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, packet.duration(), packet.amplifier(), packet.isEffectAmbient(), packet.isEffectVisible(), packet.effectShowsIcon());
								living.forceAddEffect(mobeffectinstance, null);
							}
						}
					}
				}
			});
		}
	}
}
