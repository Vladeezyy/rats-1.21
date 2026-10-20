package com.github.alexthe666.rats.server.items;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;


public class RatHammockItem extends Item implements RatCageDecoration {
	public final DyeColor color;

	public RatHammockItem(Item.Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
	}

	@Override
	public Direction getSupportedFace(Direction inputDir) {
		return Direction.UP;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable(RatsLangConstants.CAGE_DECORATION).withStyle(ChatFormatting.GRAY));
	}
}