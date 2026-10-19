package com.github.alexthe666.rats.data.tags;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

/** Tags of the mod (the 1.20.1 original generated their JSON here; the JSON is now kept as is) */
public final class RatsBannerPatternTags {

	public static final TagKey<BannerPattern> RAT_BANNER_PATTERN = create("pattern_item/rat");
	public static final TagKey<BannerPattern> CHEESE_BANNER_PATTERN = create("pattern_item/cheese");
	public static final TagKey<BannerPattern> RAC_BANNER_PATTERN = create("pattern_item/rat_and_crossbones");
	public static final TagKey<BannerPattern> RAS_BANNER_PATTERN = create("pattern_item/rat_and_sickle");

	private RatsBannerPatternTags() {
	}

	private static TagKey<BannerPattern> create(String name) {
		return TagKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
