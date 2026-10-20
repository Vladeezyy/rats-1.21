package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.StoredItems;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsUpgradeConflictRegistry;
import com.github.alexthe666.rats.server.inventory.RatUpgradeMenu;
import com.github.alexthe666.rats.server.inventory.container.RatUpgradeContainer;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.CombinedUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class CombinedRatUpgradeItem extends BaseRatUpgradeItem implements CombinedUpgrade {

	public CombinedRatUpgradeItem(Item.Properties properties) {
		super(properties, 1, 1);
	}

	public static boolean canCombineWithUpgrade(ItemStack combiner, ItemStack stack) {
		if (stack.getItem() instanceof CombinedUpgrade) return false;
		if (StoredItems.has(combiner)) {
			NonNullList<ItemStack> nonnulllist = StoredItems.view(combiner, 27);
			for (ItemStack contained : nonnulllist) {
				if (!(stack.getItem() instanceof BaseRatUpgradeItem) || stack.getItem() == contained.getItem() || RatsUpgradeConflictRegistry.doesConflict(contained, stack)) {
					return false;
				}
			}
		}
		return combiner.is(RatsItemRegistry.RAT_UPGRADE_JURY_RIGGED.get()) || combiner.is(RatsItemRegistry.RAT_UPGRADE_COMBINED.get()) || combiner.is(RatsItemRegistry.RAT_UPGRADE_COMBINED_CREATIVE.get());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		if (stack.is(RatsItemRegistry.RAT_UPGRADE_COMBINED_CREATIVE.get())) {
			tooltip.accept(Component.translatable("item.rats.rat_upgrade_combined.desc").withStyle(ChatFormatting.GRAY));
		}
		this.addTooltip(stack, tooltip);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (stack.is(RatsItemRegistry.RAT_UPGRADE_COMBINED_CREATIVE.get())) {
			return true;
		}
		return StoredItems.has(stack);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (this == RatsItemRegistry.RAT_UPGRADE_COMBINED_CREATIVE.get()) {
			//FIXME move this behavior to a right click on stack option.
			//basically, right clicking an upgrade into it or right clicking it on top of another upgrade will add the upgrade to it.
			//this is very similar to bundle logic, I think its better than a gui
			ItemStack stack = player.getItemInHand(hand);
			if (!player.isShiftKeyDown()) {
				if (!level.isClientSide()) {
					player.openMenu(new MenuProvider() {
						@Override
						public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player1) {
							return new RatUpgradeMenu(id, new RatUpgradeContainer(stack), player.getInventory(), stack);
						}

						@Override
						public Component getDisplayName() {
							return CombinedRatUpgradeItem.this.getName(stack);
						}
					});
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		} else {
			return super.use(level, player, hand);
		}
	}

	@Override
	public int getUpgradeSlots() {
		return 27;
	}
}