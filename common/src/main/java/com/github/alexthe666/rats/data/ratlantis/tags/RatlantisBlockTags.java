package com.github.alexthe666.rats.data.ratlantis.tags;

import net.minecraft.core.registries.Registries;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatlantisBlockTags {

	public static final TagKey<Block> PIRAT_LOGS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "pirat_logs"));
	public static final TagKey<Block> PIRAT_ONLY_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "pirat_blocks"));
	public static final TagKey<Block> STORAGE_BLOCKS_ORATCHALCUM = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/oratchalcum"));
	public static final TagKey<Block> ORES_CHEESE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/cheese"));
	public static final TagKey<Block> ORES_GEM_OF_RATLANTIS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/gem_of_ratlantis"));
	public static final TagKey<Block> ORES_ORATCHALCUM = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/oratchalcum"));

	private RatlantisBlockTags() {
	}
}
