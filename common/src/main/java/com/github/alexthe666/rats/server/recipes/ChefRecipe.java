package com.github.alexthe666.rats.server.recipes;

import com.github.alexthe666.rats.registry.RatsRecipeRegistry;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ChefRecipe extends BaseRatRecipe {
	public ChefRecipe(Recipe.CommonInfo commonInfo, String group, Ingredient input, ItemStackTemplate output) {
		super(commonInfo, group, input, output);
	}

	@Override
	public RecipeType<ChefRecipe> getType() {
		return RatsRecipeRegistry.CHEF.get();
	}

	@Override
	public RecipeSerializer<ChefRecipe> getSerializer() {
		return RatsRecipeRegistry.CHEF_SERIALIZER.get();
	}
}
