package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsEntityTags {

	public static final TagKey<EntityType<?>> RATS = create("rats");
	public static final TagKey<EntityType<?>> RAT_MOUNTS = create("rat_mounts");
	public static final TagKey<EntityType<?>> PLAGUE_IMMUNE = create("plague_immune");
	public static final TagKey<EntityType<?>> PLAGUE_LEGION = create("plague_legion");

	private RatsEntityTags() {
	}

	public static TagKey<EntityType<?>> create(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
