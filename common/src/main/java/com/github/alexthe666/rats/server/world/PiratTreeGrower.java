package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;

/** The pirat sapling's trees (1.21: a TreeGrower value; the original subclassed AbstractMegaTreeGrower) */
public final class PiratTreeGrower {
	// keys made here: the feature registry class builds tree configurations from blocks in its static init
	private static final ResourceKey<Feature> TREE = ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, "ghost_pirat_tree"));
	private static final ResourceKey<Feature> LARGE_TREE = ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, "large_ghost_pirat_tree"));

	// 26.3: weighted lists of features (trees, 2x2 trees, trees next to flowers) and the smallest tree
	public static final TreeGrower PIRAT = new TreeGrower(RatsMod.MODID + ":pirat",
			WeightedList.of(TREE), WeightedList.of(LARGE_TREE), WeightedList.of(), TREE);

	private PiratTreeGrower() {
	}
}
