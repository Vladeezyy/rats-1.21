package com.github.alexthe666.rats.server.items;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;

public class RatDecorationItem extends Item implements RatCageDecoration {

	public RatDecorationItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Direction getSupportedFace(Direction inputDir) {
		if (this == RatsItemRegistry.RAT_WATER_BOTTLE.get()) {
			return inputDir;
		} else if (this == RatsItemRegistry.RAT_SEED_BOWL.get() || this == RatsItemRegistry.RAT_WHEEL.get()) {
			return Direction.DOWN;
		} else if (this == RatsItemRegistry.RAT_BREEDING_LANTERN.get()) {
			return Direction.UP;
		} else {
			return inputDir;
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable(RatsLangConstants.CAGE_DECORATION).withStyle(ChatFormatting.GRAY));
		if (this == RatsItemRegistry.RAT_WHEEL.get()) {
			tooltip.accept(Component.translatable("item.rats.rat_wheel.desc").withStyle(ChatFormatting.GRAY));
		}
	}
}
