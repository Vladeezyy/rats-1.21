package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

/** Banner patterns (1.21: data, in data/rats/banner_pattern; the original registered them in code) */
public class RatsBannerPatternRegistry {
	public static final ResourceKey<BannerPattern> RAT_PATTERN = key("rat");
	public static final ResourceKey<BannerPattern> CHEESE_PATTERN = key("cheese");
	public static final ResourceKey<BannerPattern> RAT_AND_CROSSBONES_BANNER = key("rat_and_crossbones");
	public static final ResourceKey<BannerPattern> RAT_AND_SICKLE_BANNER = key("rat_and_sickle");

	private static ResourceKey<BannerPattern> key(String name) {
		return ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
