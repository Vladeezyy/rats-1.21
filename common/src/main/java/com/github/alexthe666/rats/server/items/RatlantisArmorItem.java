package com.github.alexthe666.rats.server.items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.registry.RatsArmorMaterialRegistry;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RatlantisArmorItem extends Item {

	public RatlantisArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
		super(RatsArmorMaterialRegistry.armor(properties, material, type));
	}


	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable("item.rats.ratlantis_armor.desc0").withStyle(ChatFormatting.YELLOW));
		tooltip.accept(Component.translatable("item.rats.ratlantis_armor.desc1").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.rats.ratlantis_armor.desc2").withStyle(ChatFormatting.GRAY));
	}

}
