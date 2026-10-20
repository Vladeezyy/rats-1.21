package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;


public class RatlantisDimensionRegistry {
	public static final Identifier DIMENSION = Identifier.fromNamespaceAndPath(RatsMod.MODID, "ratlantis");
	public static final ResourceKey<Level> DIMENSION_KEY = ResourceKey.create(Registries.DIMENSION, DIMENSION);

	public static final ResourceKey<NoiseGeneratorSettings> RATLANTIS_NOISE_GEN = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(RatsMod.MODID, "ratlantis_noise_gen"));
	public static final ResourceKey<DimensionType> RATLANTIS_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(RatsMod.MODID, "ratlantis_type"));
	public static final ResourceKey<LevelStem> RATLANTIS_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, DIMENSION);
}
