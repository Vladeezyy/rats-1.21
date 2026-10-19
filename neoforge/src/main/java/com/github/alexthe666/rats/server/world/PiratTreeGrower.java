package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

/** The pirat sapling's trees (1.21: a TreeGrower value; the original subclassed AbstractMegaTreeGrower) */
public final class PiratTreeGrower {
	// keys made here: the feature registry class builds tree configurations from blocks in its static init
	public static final TreeGrower PIRAT = new TreeGrower(RatsMod.MODID + ":pirat",
			Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "large_ghost_pirat_tree"))),
			Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "ghost_pirat_tree"))),
			Optional.empty());

	private PiratTreeGrower() {
	}
}
