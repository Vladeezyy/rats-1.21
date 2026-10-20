package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.*;

public class RatlantisBiomeRegistry {

	//new biome ideas:
	//make the ocean its own biome
	//ratglove fields. No trees, barely any ruins, just ratglove flowers and maybe even compressed rat blocks. Ratlanteans also spawn in abundance here
	//sparser jungle biome
	//ratlantis plains, spawns ruins more frequently. Maybe could also spawn an ancient city-like structure?
	//marble mushroom biome
	public static final ResourceKey<Biome> RATLANTIS = makeKey("ratlantis");

	private static ResourceKey<Biome> makeKey(String name) {
		return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

	public static BiomeSource buildBiomeSource(HolderGetter<Biome> biomes) {
		return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(ImmutableList.of(
				Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomes.getOrThrow(RATLANTIS))
		)));
	}
}
