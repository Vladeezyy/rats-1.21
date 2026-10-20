package com.github.alexthe666.rats.server.message;

import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.network.FriendlyByteBuf;


public record ChangeRatlantisStatusPacket(boolean enabled) implements CustomPacketPayload {

	public static final Type<ChangeRatlantisStatusPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "change_ratlantis_status"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangeRatlantisStatusPacket> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), ChangeRatlantisStatusPacket::decode);

	@Override
	public Type<ChangeRatlantisStatusPacket> type() {
		return TYPE;
	}


	public static ChangeRatlantisStatusPacket decode(RegistryFriendlyByteBuf buf) {
		return new ChangeRatlantisStatusPacket(buf.readBoolean());
	}

	public static void encode(ChangeRatlantisStatusPacket message, FriendlyByteBuf buf) {
		buf.writeBoolean(message.enabled());
	}

	public static class Handler {
		public static void handle(ChangeRatlantisStatusPacket message, PacketContext context) {
			context.enqueueWork(() -> RatsMod.RATLANTIS_DATAPACK_ENABLED = message.enabled());
		}
	}

}
