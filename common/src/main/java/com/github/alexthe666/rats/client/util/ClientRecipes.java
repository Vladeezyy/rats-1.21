package com.github.alexthe666.rats.client.util;

import com.github.alexthe666.rats.server.recipes.ArcheologistRecipe;
import com.github.alexthe666.rats.server.recipes.ChefRecipe;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * The rat recipes the server sent (26.x clients have no recipe manager): the loaders sync them (NeoForge
 * OnDatapackSyncEvent.sendRecipes, Fabric RecipeSynchronization) and pass them here, for JEI.
 */
public final class ClientRecipes {
	private static volatile List<ChefRecipe> chef = List.of();
	private static volatile List<ArcheologistRecipe> archeologist = List.of();

	private ClientRecipes() {
	}

	public static List<ChefRecipe> chef() {
		return chef;
	}

	public static List<ArcheologistRecipe> archeologist() {
		return archeologist;
	}

	public static void received(Iterable<? extends Recipe<?>> recipes) {
		List<ChefRecipe> chefRecipes = new ArrayList<>();
		List<ArcheologistRecipe> archeologistRecipes = new ArrayList<>();
		for (Recipe<?> recipe : recipes) {
			if (recipe instanceof ChefRecipe chefRecipe) {
				chefRecipes.add(chefRecipe);
			} else if (recipe instanceof ArcheologistRecipe archeologistRecipe) {
				archeologistRecipes.add(archeologistRecipe);
			}
		}
		chef = List.copyOf(chefRecipes);
		archeologist = List.copyOf(archeologistRecipes);
	}
}
