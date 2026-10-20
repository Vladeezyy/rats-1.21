package com.github.alexthe666.rats.registry;

import dev.vladeezyy.rats.platform.Platform;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.misc.RatVariant;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.rats.platform.Registrar;
import dev.vladeezyy.rats.platform.RegistryEntry;


public class RatVariantRegistry {
	public static final ResourceKey<Registry<RatVariant>> RAT_VARIANT_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(RatsMod.MODID, "rat_variant"));
	public static final Registrar<RatVariant> RAT_VARIANTS = Registrar.create(RAT_VARIANT_KEY, RatsMod.MODID);
	// synced: the client resolves the variant names it gets from the server
	public static final Registry<RatVariant> RAT_VARIANT_REGISTRY = Platform.INSTANCE.createRegistry(RAT_VARIANT_KEY, true);

	public static final RegistryEntry<RatVariant, RatVariant> BLACK = RAT_VARIANTS.register("black", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/black.png"))));
	public static final RegistryEntry<RatVariant, RatVariant> BLUE = RAT_VARIANTS.register("blue", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/blue.png"))));
	public static final RegistryEntry<RatVariant, RatVariant> BROWN = RAT_VARIANTS.register("brown", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/brown.png"))));
	public static final RegistryEntry<RatVariant, RatVariant> GREEN = RAT_VARIANTS.register("green", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/green.png"))));

	public static final RegistryEntry<RatVariant, RatVariant> ALBINO = RAT_VARIANTS.register("albino", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/albino.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> BROWN_UNDERCOAT = RAT_VARIANTS.register("brown_undercoat", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/brown_undercoat.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> HOODED = RAT_VARIANTS.register("hooded", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/hooded.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> BROWN_HOODED = RAT_VARIANTS.register("brown_hooded", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/brown_hooded.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> GRAY_HOODED = RAT_VARIANTS.register("gray_hooded", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/gray_hooded.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> YELLOW_HOODED = RAT_VARIANTS.register("yellow_hooded", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/yellow_hooded.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> SIAMESE = RAT_VARIANTS.register("siamese", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/siamese.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> WHITE = RAT_VARIANTS.register("white", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/white.png")).setBreedingExclusive()));
	public static final RegistryEntry<RatVariant, RatVariant> HAIRLESS = RAT_VARIANTS.register("hairless", () -> new RatVariant(new RatVariant.Properties(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/breeding_variants/hairless.png")).setBreedingExclusive()));
}
