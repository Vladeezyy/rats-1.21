package com.github.alexthe666.rats.data.ratlantis.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatlantisBiomeTags {

	public static final TagKey<Biome> DUTCHRAT_SHIP_SPAWNS = create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "has_structure/dutchrat_ship"));
	public static final TagKey<Biome> BARON_RUNWAY_SPAWNS = create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "has_structure/baron_runway"));

	private RatlantisBiomeTags() {
	}
}
