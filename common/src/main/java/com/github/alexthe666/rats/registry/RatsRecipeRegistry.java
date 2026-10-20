package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.recipes.ArcheologistRecipe;
import com.github.alexthe666.rats.server.recipes.ChefRecipe;
import com.github.alexthe666.rats.server.recipes.DemonRatSwitchRecipe;
import com.github.alexthe666.rats.server.recipes.BaseRatRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsRecipeRegistry {

	public static final Registrar<RecipeType<?>> RECIPES = Registrar.create(BuiltInRegistries.RECIPE_TYPE, RatsMod.MODID);
	public static final Registrar<RecipeSerializer<?>> SERIALIZERS = Registrar.create(BuiltInRegistries.RECIPE_SERIALIZER, RatsMod.MODID);

	public static final RegistryEntry<RecipeType<?>, RecipeType<ArcheologistRecipe>> ARCHEOLOGIST = RECIPES.register("archeologist", () -> recipeType("archeologist"));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<ArcheologistRecipe>> ARCHEOLOGIST_SERIALIZER = SERIALIZERS.register("archeologist", () -> BaseRatRecipe.serializer(ArcheologistRecipe::new));
	public static final RegistryEntry<RecipeType<?>, RecipeType<ChefRecipe>> CHEF = RECIPES.register("chef", () -> recipeType("chef"));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<ChefRecipe>> CHEF_SERIALIZER = SERIALIZERS.register("chef", () -> BaseRatRecipe.serializer(ChefRecipe::new));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<DemonRatSwitchRecipe>> SWITCH_DEMON = SERIALIZERS.register("switch_demon", () -> new RecipeSerializer<>(MapCodec.unit(DemonRatSwitchRecipe::new), StreamCodec.of((buf, recipe) -> {}, buf -> new DemonRatSwitchRecipe())));


	/** A recipe type named by its id (NeoForge's RecipeType.simple) */
	private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> recipeType(String name) {
		String id = RatsMod.MODID + ":" + name;
		return new RecipeType<>() {
			@Override
			public String toString() {
				return id;
			}
		};
	}
}
