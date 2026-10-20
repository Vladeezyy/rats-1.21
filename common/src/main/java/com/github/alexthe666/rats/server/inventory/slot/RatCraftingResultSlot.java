package com.github.alexthe666.rats.server.inventory.slot;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import dev.vladeezyy.rats.platform.transfer.HandlerSlot;

public class RatCraftingResultSlot extends HandlerSlot {

	private final RatCraftingTableBlockEntity table;
	private final Player player;
	private int amountCrafted;

	public RatCraftingResultSlot(ItemHandler handler, Player player, RatCraftingTableBlockEntity te, int slotIndex, int xPosition, int yPosition) {
		super(handler, slotIndex, xPosition, yPosition);
		this.table = te;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(int amount) {
		ItemStack stack = this.getItem();
		this.amountCrafted += stack.getCount();
		this.table.resultHandler.ifPresent(h -> h.setStackInSlot(0, ItemStack.EMPTY));
		return stack;
	}

	@Override
	public void setChanged() {
		this.table.updateHelper();
		this.table.updateRecipe();
		this.table.setChanged();
		super.setChanged();
	}

	@Override
	public boolean mayPickup(Player player) {
		return true;
	}

	@Override
	protected void onQuickCraft(ItemStack stack, int amount) {
		this.amountCrafted += amount;
		this.checkTakeAchievements(stack);
	}

	@Override
	protected void onSwapCraft(int amount) {
		this.amountCrafted += amount;
	}

	@Override
	public void onQuickCraft(ItemStack stack, ItemStack other) {
		int i = other.getCount() - stack.getCount();
		if (i > 0) {
			this.onQuickCraft(other, i);
		}
	}

	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		if (this.amountCrafted > 0) {
			stack.onCraftedBy(this.player, this.amountCrafted);
			this.table.matrixWrapper.ifPresent(h -> Platform.INSTANCE.firePlayerCraftingEvent(this.player, stack, h));
		}
		this.amountCrafted = 0;

		RecipeHolder<?> recipe = this.table.getRecipeUsed();
		if (recipe != null && !recipe.value().isSpecial()) {
			this.player.awardRecipes(java.util.List.of(recipe));
		}
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		this.checkTakeAchievements(stack);
		Platform.INSTANCE.setCraftingPlayer(player);
		this.table.updateRecipe();
		Platform.INSTANCE.setCraftingPlayer(null);
	}
}
