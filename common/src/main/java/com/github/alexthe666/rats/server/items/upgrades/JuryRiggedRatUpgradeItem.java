package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.StoredItems;
import com.github.alexthe666.rats.server.inventory.JuryRiggedRatUpgradeMenu;
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


public class JuryRiggedRatUpgradeItem extends BaseRatUpgradeItem implements CombinedUpgrade {

	public JuryRiggedRatUpgradeItem(Item.Properties properties) {
		super(properties, 1, 2);
	}

	@Override
	public int getUpgradeSlots() {
		return 2;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable("item.rats.rat_upgrade_combined.desc").withStyle(ChatFormatting.GRAY));
		this.addTooltip(stack, tooltip);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return this.isUpgradeLocked(stack);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && !this.isUpgradeLocked(stack)) {
			if (!level.isClientSide()) {
				player.openMenu(new MenuProvider() {
					@Override
					public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player1) {
						return new JuryRiggedRatUpgradeMenu(id, new RatUpgradeContainer(stack), player1.getInventory(), stack);
					}

					@Override
					public Component getDisplayName() {
						return JuryRiggedRatUpgradeItem.this.getName(stack);
					}
				});
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	private boolean isUpgradeLocked(ItemStack stack) {
		if (StoredItems.has(stack)) {
			NonNullList<ItemStack> nonnulllist = StoredItems.view(stack, this.getUpgradeSlots());
			return !nonnulllist.get(0).isEmpty() && !nonnulllist.get(1).isEmpty();
		}
		return false;
	}
}