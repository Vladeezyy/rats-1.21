package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsItemTags {

	public static final TagKey<Item> CHEESE_ITEMS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "cheese"));
	public static final TagKey<Item> CRAFTING_TABLES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "crafting_tables"));
	public static final TagKey<Item> VEGETABLES = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "vegetables"));
	public static final TagKey<Item> PLASTICS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plastics"));
	public static final TagKey<Item> HIDES_RAT_WHISKERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "hides_rat_whiskers"));
	public static final TagKey<Item> IGLOOS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "igloos"));
	public static final TagKey<Item> TUBES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "tubes"));
	public static final TagKey<Item> HAMMOCKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "hammocks"));
	public static final TagKey<Item> MARBLED_CHEESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "marbled_cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_CHEESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_BLUE_CHEESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/blue_cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_NETHER_CHEESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/nether_cheese"));

	private RatsItemTags() {
	}
}
