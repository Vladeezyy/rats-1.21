package com.github.alexthe666.rats.data.tags;

import net.minecraft.core.registries.Registries;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsBlockTags {

	public static final TagKey<Block> MARBLED_CHEESE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RatsMod.MODID, "marbled_cheese"));
	public static final TagKey<Block> TRASH_CAN_BLACKLIST = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RatsMod.MODID, "trash_can_blacklist"));
	public static final TagKey<Block> QUARRY_IGNORABLES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RatsMod.MODID, "quarry_ignoreables"));
	public static final TagKey<Block> UNRAIDABLE_CONTAINERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RatsMod.MODID, "unraidable_containers"));
	public static final TagKey<Block> DIGGABLE_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RatsMod.MODID, "diggable_blocks"));
	public static final TagKey<Block> STORAGE_BLOCKS_CHEESE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/cheese"));
	public static final TagKey<Block> STORAGE_BLOCKS_BLUE_CHEESE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/blue_cheese"));
	public static final TagKey<Block> STORAGE_BLOCKS_NETHER_CHEESE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/nether_cheese"));
	public static final TagKey<Block> CRAFTING_TABLES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "crafting_tables"));

	private RatsBlockTags() {
	}
}
