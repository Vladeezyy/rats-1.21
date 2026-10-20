package com.github.alexthe666.rats.server.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** A block's tooltip lines, shown by its {@link com.github.alexthe666.rats.server.items.RatsBlockItem} (26.x blocks have no tooltips) */
public interface BlockTooltip {

	void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag);
}
