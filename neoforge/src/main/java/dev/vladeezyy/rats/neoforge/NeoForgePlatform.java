package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.Rats;
import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NeoForgePlatform implements Platform {
  /** Deferred registers per registry, filled by the common code before {@link #registerAll} */
  private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

  static Platform INSTANCE() {
    return Platform.INSTANCE;
  }

  static void registerAll(IEventBus modBus) {
    REGISTERS.values().forEach(register -> register.register(modBus));
  }

  @Override
  public String name() {
    return "neoforge";
  }

  @Override
  public Path configDir() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
    DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry,
      key -> DeferredRegister.create((ResourceKey<? extends Registry<R>>) key, Rats.MOD_ID));
    return register.register(name, factory);
  }

  @Override
  public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
    PacketDistributor.sendToPlayer(player, payload);
  }

  @Override
  public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType(
      java.util.function.BiFunction<BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory, net.minecraft.world.level.block.Block... blocks) {
    return net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(factory::apply, blocks).build(null);
  }

  @Override
  public net.minecraft.world.item.CreativeModeTab.Builder creativeTabBuilder() {
    return net.minecraft.world.item.CreativeModeTab.builder();
  }

  @Override
  public net.minecraft.world.item.Item spawnEgg(Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> type, int background, int highlight) {
    return new net.neoforged.neoforge.common.DeferredSpawnEggItem(type, background, highlight, new net.minecraft.world.item.Item.Properties());
  }
}
