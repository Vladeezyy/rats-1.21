package com.github.alexthe666.rats.server.misc;

import com.github.alexthe666.rats.registry.RatVariantRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RatVariant {

	private final Identifier texture;
	private final boolean breedingExclusive;

	public RatVariant(RatVariant.Properties properties) {
		this(properties.texture, properties.breedingExclusive);
	}

	private RatVariant(Identifier texture, boolean breedingExclusive) {
		this.texture = texture;
		this.breedingExclusive = breedingExclusive;
	}

	public Identifier getTexture() {
		return this.texture;
	}

	public boolean isBreedingExclusive() {
		return this.breedingExclusive;
	}

	public static class Properties {
		private final Identifier texture;
		private boolean breedingExclusive = false;

		public Properties(Identifier texture) {
			this.texture = texture;
		}

		public RatVariant.Properties setBreedingExclusive() {
			this.breedingExclusive = true;
			return this;
		}
	}

	public static RatVariant getRandomVariant(RandomSource random, boolean fromBreeding) {
		List<RatVariant> validVariants = new ArrayList<>(RatVariantRegistry.RAT_VARIANT_REGISTRY.stream().toList());
		validVariants.removeIf(variant -> fromBreeding != variant.isBreedingExclusive());
		return validVariants.toArray(RatVariant[]::new)[random.nextInt(validVariants.size())];
	}

	public static RatVariant getRandomBreedingExclusiveVariant(RandomSource random) {
		List<RatVariant> validVariants = new ArrayList<>(RatVariantRegistry.RAT_VARIANT_REGISTRY.stream().toList());
		validVariants.removeIf(variant -> !variant.isBreedingExclusive());
		return validVariants.toArray(RatVariant[]::new)[random.nextInt(validVariants.size())];
	}

	public static RatVariant getVariant(String id) {
		return Optional.ofNullable(RatVariantRegistry.RAT_VARIANT_REGISTRY.getValue(Identifier.parse(id))).orElse(RatVariantRegistry.BLUE.get());
	}

	public static String getVariantId(RatVariant variant) {
		return Optional.ofNullable(RatVariantRegistry.RAT_VARIANT_REGISTRY.getKey(variant)).orElse(RatVariantRegistry.RAT_VARIANT_REGISTRY.getKey(RatVariantRegistry.BLUE.get())).toString();
	}
}
