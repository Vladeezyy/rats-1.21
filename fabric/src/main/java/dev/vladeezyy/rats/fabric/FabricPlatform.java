package dev.vladeezyy.rats.fabric;

import dev.vladeezyy.rats.Rats;
import dev.vladeezyy.rats.platform.Platform;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class FabricPlatform implements Platform {
  @Override
  public String name() {
    return "fabric";
  }

  @Override
  public Path configDir() {
    return FabricLoader.getInstance().getConfigDir();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
    Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.get(registry.location());
    T value = Registry.register(target, Rats.id(name), factory.get());
    return () -> value;
  }

  @Override
  public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
    ServerPlayNetworking.send(player, payload);
  }

  @Override
  public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType(
      java.util.function.BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory, net.minecraft.world.level.block.Block... blocks) {
    return net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
  }

  @Override
  public net.minecraft.world.item.CreativeModeTab.Builder creativeTabBuilder() {
    return net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder();
  }

  @Override
  public net.minecraft.world.item.Item spawnEgg(Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> type, int background, int highlight) {
    return new net.minecraft.world.item.SpawnEggItem(type.get(), background, highlight, new net.minecraft.world.item.Item.Properties());
  }
}
