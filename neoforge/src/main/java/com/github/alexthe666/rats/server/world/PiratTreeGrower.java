package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.worldgen.RatlantisConfiguredFeatureRegistry;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

/** The pirat sapling's trees (1.21: a TreeGrower value; the original subclassed AbstractMegaTreeGrower) */
public final class PiratTreeGrower {
	public static final TreeGrower PIRAT = new TreeGrower(RatsMod.MODID + ":pirat",
			Optional.of(RatlantisConfiguredFeatureRegistry.LARGE_GHOST_PIRAT_TREE),
			Optional.of(RatlantisConfiguredFeatureRegistry.GHOST_PIRAT_TREE),
			Optional.empty());

	private PiratTreeGrower() {
	}
}
