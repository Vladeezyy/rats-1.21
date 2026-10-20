package com.github.alexthe666.rats.data.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** The common ("c") tags the game code checks; NeoForge's Tags and Fabric's conventional tags have the same ids */
public final class CommonTags {
	public static final TagKey<Item> SEEDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "seeds"));
	public static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores"));
	public static final TagKey<Block> ORE_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

	private CommonTags() {
	}
}
