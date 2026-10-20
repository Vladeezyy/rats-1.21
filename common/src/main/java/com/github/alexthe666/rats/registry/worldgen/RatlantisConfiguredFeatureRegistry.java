package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.server.world.ThickBranchingTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;


public class RatlantisConfiguredFeatureRegistry {
	public static final ResourceKey<ConfiguredFeature<?, ?>> GHOST_PIRAT_TREE = registerKey("ghost_pirat_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_GHOST_PIRAT_TREE = registerKey("large_ghost_pirat_tree");
	public static final ResourceKey<ConfiguredFeature<?, ?>> RATGLOVE_FLOWERS = registerKey("ratglove_flowers");
	public static final ResourceKey<ConfiguredFeature<?, ?>> MARBLE_PILE = registerKey("marble_pile");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_RUINS = registerKey("small_ruins");
	public static final ResourceKey<ConfiguredFeature<?, ?>> LARGE_RUINS = registerKey("large_ruins");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CHEESE_ORE = registerKey("cheese_ore");
	public static final ResourceKey<ConfiguredFeature<?, ?>> GEM_ORE = registerKey("ratlantean_gem_ore");
	public static final ResourceKey<ConfiguredFeature<?, ?>> ORATCHALCUM_ORE = registerKey("oratchalcum_ore");

	public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

	public static final TreeConfiguration GHOST_PIRAT_TREE_CONFIG = new TreeConfiguration.TreeConfigurationBuilder(
			BlockStateProvider.simple(RatlantisBlockRegistry.PIRAT_LOG.get()),
			new ThickBranchingTrunkPlacer(5, 1, 1, 1, 1, 2),
			BlockStateProvider.simple(RatlantisBlockRegistry.PIRAT_LEAVES.get()),
			new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.5F, 0.75F),
			new TwoLayersFeatureSize(1, 0, 2),
			TreeConfiguration.defaultPlaceBelowTreeTrunkProvider(null)).ignoreVines().build();

	public static final TreeConfiguration LARGE_GHOST_PIRAT_TREE_CONFIG = new TreeConfiguration.TreeConfigurationBuilder(
			BlockStateProvider.simple(RatlantisBlockRegistry.PIRAT_LOG.get()),
			new ThickBranchingTrunkPlacer(8, 1, 1, 2, 2, 4),
			BlockStateProvider.simple(RatlantisBlockRegistry.PIRAT_LEAVES.get()),
			new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.5F, 0.75F),
			new TwoLayersFeatureSize(1, 0, 2),
			TreeConfiguration.defaultPlaceBelowTreeTrunkProvider(null)).ignoreVines().build();

}
