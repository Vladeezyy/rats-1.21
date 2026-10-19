package com.github.alexthe666.rats.registry;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class RatsDataSerializerRegistry {

	public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = DeferredRegister.create(Registries.ENTITY_DATA_SERIALIZERS, RatsMod.MODID);

	public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<List<GlobalPos>>> GLOBAL_POS_LIST = DATA_SERIALIZERS.register("global_pos_list", () -> new EntityDataSerializer<>() {
		private final StreamCodec<RegistryFriendlyByteBuf, List<GlobalPos>> codec = GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()).cast();

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, List<GlobalPos>> codec() {
			return this.codec;
		}

		@Override
		public List<GlobalPos> copy(List<GlobalPos> list) {
			return new ArrayList<>(list);
		}
	});
}
