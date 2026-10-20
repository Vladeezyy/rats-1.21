package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsBiomeTags {

	public static final TagKey<Biome> RAT_SPAWN_BIOMES = create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "rat_spawn_biomes"));
	public static final TagKey<Biome> PIPER_SPAWN_BIOMES = create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "piper_spawn_biomes"));
	public static final TagKey<Biome> DEMON_RAT_SPAWN_BIOMES = create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "demon_rat_spawn_biomes"));

	private RatsBiomeTags() {
	}

	private static TagKey<Biome> create(Identifier name) {
		return TagKey.create(Registries.BIOME, name);
	}
}
