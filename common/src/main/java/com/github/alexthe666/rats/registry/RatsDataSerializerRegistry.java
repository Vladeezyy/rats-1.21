package com.github.alexthe666.rats.registry;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.resources.Identifier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.syncher.EntityDataSerializer;

import java.util.ArrayList;
import java.util.List;

public class RatsDataSerializerRegistry {

	public static final EntityDataSerializer<List<GlobalPos>> GLOBAL_POS_LIST = new EntityDataSerializer<>() {
		private final StreamCodec<RegistryFriendlyByteBuf, List<GlobalPos>> codec = GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list()).cast();

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, List<GlobalPos>> codec() {
			return this.codec;
		}

		@Override
		public List<GlobalPos> copy(List<GlobalPos> list) {
			return new ArrayList<>(list);
		}
	};

	/** NeoForge: its serializer registry; Fabric: vanilla's list (both sides register in the same order) */
	public static void register() {
		Platform.INSTANCE.registerDataSerializer(Identifier.fromNamespaceAndPath(RatsMod.MODID, "global_pos_list"), GLOBAL_POS_LIST);
	}
}
