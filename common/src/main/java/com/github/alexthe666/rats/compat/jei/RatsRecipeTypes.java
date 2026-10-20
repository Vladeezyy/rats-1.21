package com.github.alexthe666.rats.compat.jei;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.recipes.ArcheologistRecipe;
import com.github.alexthe666.rats.server.recipes.ChefRecipe;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;

public class RatsRecipeTypes {
	public static final IRecipeType<ArcheologistRecipe> ARCHEOLOGIST = IRecipeType.create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "archeologist"), ArcheologistRecipe.class);
	public static final IRecipeType<CauldronInfoHolder> CAULDRON = IRecipeType.create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "cauldron"), CauldronInfoHolder.class);
	public static final IRecipeType<ChefRecipe> CHEF = IRecipeType.create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "chef"), ChefRecipe.class);
}
