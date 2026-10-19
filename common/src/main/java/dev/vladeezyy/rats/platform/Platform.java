package dev.vladeezyy.rats.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** What the common code needs from the mod loader (NeoForge or Fabric) */
public interface Platform {
  Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
    .orElseThrow(() -> new IllegalStateException("No Rats platform for this mod loader"));

  /** "neoforge" or "fabric" */
  String name();

  Path configDir();

  /** Registers an entry (NeoForge: deferred register, Fabric: right away); the supplier gives it once registered */
  <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

  void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

  /** A spawn egg for a type registered later (NeoForge: deferred spawn egg) */
  net.minecraft.world.item.Item spawnEgg(Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> type, int background, int highlight);

  /** A creative tab builder (NeoForge: vanilla builder, Fabric: its own builder) */
  net.minecraft.world.item.CreativeModeTab.Builder creativeTabBuilder();

  /** A block entity type (the vanilla builder's factory interface is not public outside NeoForge) */
  <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);
}
