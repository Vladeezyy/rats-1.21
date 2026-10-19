package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.recipes.ArcheologistRecipe;
import com.github.alexthe666.rats.server.recipes.ChefRecipe;
import com.github.alexthe666.rats.server.recipes.DemonRatSwitchRecipe;
import com.github.alexthe666.rats.server.recipes.RatsRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsRecipeRegistry {

	public static final Registrar<RecipeType<?>> RECIPES = Registrar.create(BuiltInRegistries.RECIPE_TYPE, RatsMod.MODID);
	public static final Registrar<RecipeSerializer<?>> SERIALIZERS = Registrar.create(BuiltInRegistries.RECIPE_SERIALIZER, RatsMod.MODID);

	public static final RegistryEntry<RecipeType<?>, RecipeType<ArcheologistRecipe>> ARCHEOLOGIST = RECIPES.register("archeologist", () -> recipeType("archeologist"));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<ArcheologistRecipe>> ARCHEOLOGIST_SERIALIZER = SERIALIZERS.register("archeologist", () -> new RatsRecipeSerializer<>(ArcheologistRecipe::new));
	public static final RegistryEntry<RecipeType<?>, RecipeType<ChefRecipe>> CHEF = RECIPES.register("chef", () -> recipeType("chef"));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<ChefRecipe>> CHEF_SERIALIZER = SERIALIZERS.register("chef", () -> new RatsRecipeSerializer<>(ChefRecipe::new));
	public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<DemonRatSwitchRecipe>> SWITCH_DEMON = SERIALIZERS.register("switch_demon", () -> new SimpleCraftingRecipeSerializer<>(DemonRatSwitchRecipe::new));


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
