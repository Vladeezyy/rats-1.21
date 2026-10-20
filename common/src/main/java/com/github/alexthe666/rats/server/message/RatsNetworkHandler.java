package com.github.alexthe666.rats.server.message;

import dev.vladeezyy.rats.platform.PacketContext;
import dev.vladeezyy.rats.platform.Platform;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * The packets of Rats and who receives them. The original sent the state of single entities and blocks (a rat's
 * fluid, a thrown block, the curdler's tank) to every player on the server; here it goes to the players that see them.
 */
public final class RatsNetworkHandler {

	private RatsNetworkHandler() {
	}

	/** Receives the packets; each loader registers them with its network API */
	public interface PacketRegistrar {
		<T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler);

		<T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler);
	}

	public static void register(PacketRegistrar registrar) {
		// server -> client
		registrar.toClient(ChangeRatlantisStatusPacket.TYPE, ChangeRatlantisStatusPacket.CODEC, ChangeRatlantisStatusPacket.Handler::handle);
		registrar.toClient(ManageRatStaffPacket.TYPE, ManageRatStaffPacket.CODEC, ManageRatStaffPacket.Handler::handle);
		registrar.toClient(OpenRatScreenPacket.TYPE, OpenRatScreenPacket.CODEC, OpenRatScreenPacket.Handler::handle);
		registrar.toClient(SyncPlaguePacket.TYPE, SyncPlaguePacket.CODEC, SyncPlaguePacket.Handler::handle);
		registrar.toClient(UpdateCurdlerFluidPacket.TYPE, UpdateCurdlerFluidPacket.CODEC, UpdateCurdlerFluidPacket.Handler::handle);
		registrar.toClient(UpdateRatMusicPacket.TYPE, UpdateRatMusicPacket.CODEC, UpdateRatMusicPacket.Handler::handle);
		registrar.toClient(UpdateSelectedRatPacket.TYPE, UpdateSelectedRatPacket.CODEC, UpdateSelectedRatPacket.Handler::handle);
		registrar.toClient(UpdateRatFluidPacket.TYPE, UpdateRatFluidPacket.CODEC, UpdateRatFluidPacket.Handler::handle);
		registrar.toClient(SyncThrownBlockPacket.TYPE, SyncThrownBlockPacket.CODEC, SyncThrownBlockPacket.Handler::handle);
		// client -> server
		registrar.toServer(ClearRatRecipePacket.TYPE, ClearRatRecipePacket.CODEC, ClearRatRecipePacket.Handler::handle);
		registrar.toServer(CycleRatRecipePacket.TYPE, CycleRatRecipePacket.CODEC, CycleRatRecipePacket.Handler::handle);
		registrar.toServer(DismountRatPacket.TYPE, DismountRatPacket.CODEC, DismountRatPacket.Handler::handle);
		registrar.toServer(RatCommandPacket.TYPE, RatCommandPacket.CODEC, RatCommandPacket.Handler::handle);
		registrar.toServer(RatUpgradeVisibilityPacket.TYPE, RatUpgradeVisibilityPacket.CODEC, RatUpgradeVisibilityPacket.Handler::handle);
		registrar.toServer(SetDancingRatPacket.TYPE, SetDancingRatPacket.CODEC, SetDancingRatPacket.Handler::handle);
		registrar.toServer(SyncArmSwingPacket.TYPE, SyncArmSwingPacket.CODEC, SyncArmSwingPacket.Handler::handle);
		registrar.toServer(SyncRatStaffPacket.TYPE, SyncRatStaffPacket.CODEC, SyncRatStaffPacket.Handler::handle);
		registrar.toServer(SyncRatTagPacket.TYPE, SyncRatTagPacket.CODEC, SyncRatTagPacket.Handler::handle);
		registrar.toServer(UpdateMobFilterPacket.TYPE, UpdateMobFilterPacket.CODEC, UpdateMobFilterPacket.Handler::handle);
	}

	public static void toServer(CustomPacketPayload payload) {
		Platform.INSTANCE.sendToServer(payload);
	}

	public static void toPlayer(ServerPlayer player, CustomPacketPayload payload) {
		Platform.INSTANCE.sendToPlayer(player, payload);
	}

	public static void toTracking(Entity entity, CustomPacketPayload payload) {
		Platform.INSTANCE.sendToTracking(entity, payload);
	}

	public static void toTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
		Platform.INSTANCE.sendToTrackingAndSelf(entity, payload);
	}

	public static void toTrackingBlock(Level level, BlockPos pos, CustomPacketPayload payload) {
		if (level instanceof ServerLevel server) {
			Platform.INSTANCE.sendToTrackingChunk(server, pos, payload);
		}
	}

	public static void toAll(CustomPacketPayload payload) {
		Platform.INSTANCE.sendToAll(payload);
	}

	/**
	 * The tamed rat a player may control from a packet: theirs (or any, for operators). The original applied commands,
	 * staff positions, patrol paths and upgrade visibility to any rat id a client sent.
	 */
	@org.jetbrains.annotations.Nullable
	public static com.github.alexthe666.rats.server.entity.rat.TamedRat ownedRat(net.minecraft.world.entity.player.Player player, int id) {
		Entity entity = player.level().getEntity(id);
		if (entity instanceof com.github.alexthe666.rats.server.entity.rat.TamedRat rat && (rat.isOwnedBy(player) || player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))) {
			return rat;
		}
		return null;
	}
}
