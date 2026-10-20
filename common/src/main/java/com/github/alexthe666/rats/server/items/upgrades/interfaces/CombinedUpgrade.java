package com.github.alexthe666.rats.server.items.upgrades.interfaces;
import java.util.function.Consumer;

import com.github.alexthe666.rats.server.misc.StoredItems;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface CombinedUpgrade {

	int getUpgradeSlots();

	default void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
		if (StoredItems.has(stack)) {
			NonNullList<ItemStack> nonnulllist = StoredItems.view(stack, this.getUpgradeSlots());
			int i = 0;
			for (ItemStack itemstack : nonnulllist) {
				if (!itemstack.isEmpty()) {
					if (i <= 4) {
						++i;
						tooltip.accept(Component.literal(String.format("%s", itemstack.getDisplayName().getString())));
					} else {
						break;
					}
				}
			}
			if (nonnulllist.stream().filter(stack1 -> !stack1.isEmpty()).toList().size() > 5) {
				tooltip.accept(Component.translatable(RatsLangConstants.AND_MORE, nonnulllist.stream().filter(stack1 -> !stack1.isEmpty()).toList().size() - 5).withStyle(ChatFormatting.GRAY));
			}
		}
	}
}
