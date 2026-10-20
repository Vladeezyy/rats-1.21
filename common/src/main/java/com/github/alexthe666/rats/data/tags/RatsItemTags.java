package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsItemTags {

	public static final TagKey<Item> CHEESE_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "cheese"));
	public static final TagKey<Item> CRAFTING_TABLES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "crafting_tables"));
	public static final TagKey<Item> VEGETABLES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "vegetables"));
	public static final TagKey<Item> PLASTICS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "plastics"));
	public static final TagKey<Item> HIDES_RAT_WHISKERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RatsMod.MODID, "hides_rat_whiskers"));
	public static final TagKey<Item> IGLOOS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RatsMod.MODID, "igloos"));
	public static final TagKey<Item> TUBES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RatsMod.MODID, "tubes"));
	public static final TagKey<Item> HAMMOCKS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RatsMod.MODID, "hammocks"));
	public static final TagKey<Item> MARBLED_CHEESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RatsMod.MODID, "marbled_cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_CHEESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_BLUE_CHEESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/blue_cheese"));
	public static final TagKey<Item> STORAGE_BLOCKS_NETHER_CHEESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/nether_cheese"));

	private RatsItemTags() {
	}
}
