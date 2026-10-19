package com.github.alexthe666.rats.server.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * The packets of Rats and who receives them. The original sent the state of single entities and blocks (a rat's
 * fluid, a thrown block, the curdler's tank) to every player on the server; here it goes to the players that see them.
 */
public final class RatsNetworkHandler {

	private RatsNetworkHandler() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		// server -> client
		toClient(registrar, ChangeRatlantisStatusPacket.TYPE, ChangeRatlantisStatusPacket.CODEC, ChangeRatlantisStatusPacket.Handler::handle);
		toClient(registrar, ManageRatStaffPacket.TYPE, ManageRatStaffPacket.CODEC, ManageRatStaffPacket.Handler::handle);
		toClient(registrar, OpenRatScreenPacket.TYPE, OpenRatScreenPacket.CODEC, OpenRatScreenPacket.Handler::handle);
		toClient(registrar, SyncPlaguePacket.TYPE, SyncPlaguePacket.CODEC, SyncPlaguePacket.Handler::handle);
		toClient(registrar, UpdateCurdlerFluidPacket.TYPE, UpdateCurdlerFluidPacket.CODEC, UpdateCurdlerFluidPacket.Handler::handle);
		toClient(registrar, UpdateRatMusicPacket.TYPE, UpdateRatMusicPacket.CODEC, UpdateRatMusicPacket.Handler::handle);
		toClient(registrar, UpdateSelectedRatPacket.TYPE, UpdateSelectedRatPacket.CODEC, UpdateSelectedRatPacket.Handler::handle);
		toClient(registrar, UpdateRatFluidPacket.TYPE, UpdateRatFluidPacket.CODEC, UpdateRatFluidPacket.Handler::handle);
		toClient(registrar, SyncThrownBlockPacket.TYPE, SyncThrownBlockPacket.CODEC, SyncThrownBlockPacket.Handler::handle);
		// client -> server
		toServer(registrar, ClearRatRecipePacket.TYPE, ClearRatRecipePacket.CODEC, ClearRatRecipePacket.Handler::handle);
		toServer(registrar, CycleRatRecipePacket.TYPE, CycleRatRecipePacket.CODEC, CycleRatRecipePacket.Handler::handle);
		toServer(registrar, DismountRatPacket.TYPE, DismountRatPacket.CODEC, DismountRatPacket.Handler::handle);
		toServer(registrar, RatCommandPacket.TYPE, RatCommandPacket.CODEC, RatCommandPacket.Handler::handle);
		toServer(registrar, RatUpgradeVisibilityPacket.TYPE, RatUpgradeVisibilityPacket.CODEC, RatUpgradeVisibilityPacket.Handler::handle);
		toServer(registrar, SetDancingRatPacket.TYPE, SetDancingRatPacket.CODEC, SetDancingRatPacket.Handler::handle);
		toServer(registrar, SyncArmSwingPacket.TYPE, SyncArmSwingPacket.CODEC, SyncArmSwingPacket.Handler::handle);
		toServer(registrar, SyncRatStaffPacket.TYPE, SyncRatStaffPacket.CODEC, SyncRatStaffPacket.Handler::handle);
		toServer(registrar, SyncRatTagPacket.TYPE, SyncRatTagPacket.CODEC, SyncRatTagPacket.Handler::handle);
		toServer(registrar, UpdateMobFilterPacket.TYPE, UpdateMobFilterPacket.CODEC, UpdateMobFilterPacket.Handler::handle);
	}

	private static <T extends CustomPacketPayload> void toClient(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
		registrar.playToClient(type, codec, handler);
	}

	private static <T extends CustomPacketPayload> void toServer(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
		registrar.playToServer(type, codec, handler);
	}

	public static void toServer(CustomPacketPayload payload) {
		PacketDistributor.sendToServer(payload);
	}

	public static void toPlayer(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}

	public static void toTracking(Entity entity, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
	}

	public static void toTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
	}

	public static void toTrackingBlock(Level level, BlockPos pos, CustomPacketPayload payload) {
		if (level instanceof ServerLevel server) {
			PacketDistributor.sendToPlayersTrackingChunk(server, new ChunkPos(pos), payload);
		}
	}

	public static void toAll(CustomPacketPayload payload) {
		PacketDistributor.sendToAllPlayers(payload);
	}
}
