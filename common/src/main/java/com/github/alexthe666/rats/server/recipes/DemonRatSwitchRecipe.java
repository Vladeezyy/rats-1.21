package com.github.alexthe666.rats.server.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingInput;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsRecipeRegistry;
import com.github.alexthe666.rats.server.items.upgrades.DemonRatUpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DemonRatSwitchRecipe extends CustomRecipe {
	public DemonRatSwitchRecipe() {
	}

	@Override
	public boolean matches(CraftingInput container, Level level) {
		ItemStack upgrade = null;
		for (int i = 0; i < container.size(); ++i) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				if (upgrade != null) {
					return false;
				} else {
					if (stack.is(RatsItemRegistry.RAT_UPGRADE_DEMON.get())) {
						upgrade = stack;
					} else {
						return false;
					}
				}
			}
		}
		return upgrade != null;
	}

	@Override
	public ItemStack assemble(CraftingInput container) {
		for (int i = 0; i < container.size(); ++i) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && stack.is(RatsItemRegistry.RAT_UPGRADE_DEMON.get())) {
				return DemonRatUpgradeItem.getDemonUpgrade(!DemonRatUpgradeItem.isSoulVersion(stack));
			}
		}
		return ItemStack.EMPTY;
	}


	@Override
	public RecipeSerializer<DemonRatSwitchRecipe> getSerializer() {
		return RatsRecipeRegistry.SWITCH_DEMON.get();
	}
}
