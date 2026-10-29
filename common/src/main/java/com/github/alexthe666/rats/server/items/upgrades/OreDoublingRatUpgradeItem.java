package com.github.alexthe666.rats.server.items.upgrades;

import com.github.alexthe666.rats.data.tags.CommonTags;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.OreRatNuggetItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OreDoublingRatUpgradeItem extends BaseRatUpgradeItem {

	public OreDoublingRatUpgradeItem(Item.Properties properties) {
		super(properties, 2, 4);
	}

	@Override
	public boolean shouldDepositItem(TamedRat rat, ItemStack stack) {
		return !isProcessable(rat.level(), stack);
	}

	@Override
	public boolean isRatHoldingFood(TamedRat rat) {
		return isProcessable(rat.level(), rat.getMainHandItem());
	}

	public static boolean isProcessable(Level level, ItemStack stack) {
		// #918: raw ores too (since 1.17 a mined ore drops raw iron, gold and copper, not the ore block)
		return (stack.is(CommonTags.ORES) || stack.is(CommonTags.RAW_MATERIALS)) && !OreRatNuggetItem.getIngot(level, stack).isEmpty();
	}
}
