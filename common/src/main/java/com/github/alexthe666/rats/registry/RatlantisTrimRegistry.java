package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class RatlantisTrimRegistry {

	public static final ResourceKey<TrimMaterial> GEM_OF_RATLANTIS = registerKey("gem_of_ratlantis");
	public static final ResourceKey<TrimMaterial> ORATCHALCUM = registerKey("oratchalcum");

	private static ResourceKey<TrimMaterial> registerKey(String name) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
