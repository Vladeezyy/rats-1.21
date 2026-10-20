package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;


/** The Ratlantis features (data: worldgen/feature in the Ratlantis pack; 26.3 has no configured features) */
public class RatlantisConfiguredFeatureRegistry {
	public static final ResourceKey<Feature> GHOST_PIRAT_TREE = registerKey("ghost_pirat_tree");
	public static final ResourceKey<Feature> LARGE_GHOST_PIRAT_TREE = registerKey("large_ghost_pirat_tree");
	public static final ResourceKey<Feature> RATGLOVE_FLOWERS = registerKey("ratglove_flowers");
	public static final ResourceKey<Feature> MARBLE_PILE = registerKey("marble_pile");
	public static final ResourceKey<Feature> SMALL_RUINS = registerKey("small_ruins");
	public static final ResourceKey<Feature> LARGE_RUINS = registerKey("large_ruins");
	public static final ResourceKey<Feature> CHEESE_ORE = registerKey("cheese_ore");
	public static final ResourceKey<Feature> GEM_ORE = registerKey("ratlantean_gem_ore");
	public static final ResourceKey<Feature> ORATCHALCUM_ORE = registerKey("oratchalcum_ore");

	public static ResourceKey<Feature> registerKey(String name) {
		return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
