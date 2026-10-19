package com.github.alexthe666.rats.server.inventory.slot;

import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import dev.vladeezyy.rats.platform.transfer.HandlerSlot;
import org.jetbrains.annotations.NotNull;

public class ImprovedSlotItemHandler extends HandlerSlot {
	private final RatCraftingTableBlockEntity table;
	private final boolean ghostSlot;

	public ImprovedSlotItemHandler(RatCraftingTableBlockEntity table, ItemHandler itemHandler, int index, int xPosition, int yPosition, boolean ghostSlot) {
		super(itemHandler, index, xPosition, yPosition);
		this.table = table;
		this.ghostSlot = ghostSlot;
	}

	@Override
	public boolean mayPickup(Player player) {
		return !this.ghostSlot && super.mayPickup(player);
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return !this.ghostSlot && super.mayPlace(stack);
	}

	@Override
	public @NotNull ItemStack remove(int amount) {
		return this.ghostSlot ? ItemStack.EMPTY : super.remove(amount);
	}

	@Override
	public void setChanged() {
		if (!this.ghostSlot) {
			this.table.updateHelper();
		}
		this.table.updateRecipe();
		this.table.setChanged();
		super.setChanged();
	}

	@Override
	public void set(ItemStack stack) {
		if (this.ghostSlot && !stack.isEmpty()) {
			stack = stack.copy();
		}
		super.set(stack);
	}
}
