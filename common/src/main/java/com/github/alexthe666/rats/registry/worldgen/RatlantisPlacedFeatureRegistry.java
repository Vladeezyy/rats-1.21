package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.*;


public class RatlantisPlacedFeatureRegistry {

	public static final ResourceKey<PlacedFeature> RATGLOVE_FLOWERS = registerKey("ratglove_flowers");
	public static final ResourceKey<PlacedFeature> MARBLE_PILE = registerKey("marble_pile");
	public static final ResourceKey<PlacedFeature> SMALL_RUINS = registerKey("small_ruins");
	public static final ResourceKey<PlacedFeature> LARGE_RUINS = registerKey("large_ruins");
	public static final ResourceKey<PlacedFeature> CHEESE_ORE = registerKey("cheese_ore");
	public static final ResourceKey<PlacedFeature> GEM_ORE = registerKey("ratlantean_gem_ore");
	public static final ResourceKey<PlacedFeature> ORATCHALCUM_ORE = registerKey("oratchalcum_ore");

	public static ResourceKey<PlacedFeature> registerKey(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

}
