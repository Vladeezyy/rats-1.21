package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsBiomeTags {

	public static final TagKey<Biome> RAT_SPAWN_BIOMES = create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "rat_spawn_biomes"));
	public static final TagKey<Biome> PIPER_SPAWN_BIOMES = create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "piper_spawn_biomes"));
	public static final TagKey<Biome> DEMON_RAT_SPAWN_BIOMES = create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "demon_rat_spawn_biomes"));

	private RatsBiomeTags() {
	}
}
