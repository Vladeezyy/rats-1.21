package com.github.alexthe666.rats.data.ratlantis.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatlantisItemTags {

	public static final TagKey<Item> ORATCHALCUM_NUGGETS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/oratchalcum"));
	public static final TagKey<Item> ORATCHALCUM_INGOTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/oratchalcum"));
	public static final TagKey<Item> RAW_ORATCHALCUM_INGOTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/oratchalcum"));
	public static final TagKey<Item> RATLANTIS_GEMS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/ratlantis_gem"));
	public static final TagKey<Item> PIRAT_LOGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "pirat_logs"));
	public static final TagKey<Item> STORAGE_BLOCKS_ORATCHALCUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/oratchalcum"));
	public static final TagKey<Item> ORES_CHEESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/cheese"));
	public static final TagKey<Item> ORES_GEM_OF_RATLANTIS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/gem_of_ratlantis"));
	public static final TagKey<Item> ORES_ORATCHALCUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/oratchalcum"));

	private RatlantisItemTags() {
	}
}
