package com.github.alexthe666.rats.server.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import com.github.alexthe666.rats.server.misc.ItemNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PartyHatItem extends Item {
	public PartyHatItem(Properties properties) {
		super(properties);
	}

	/** 1.21: the color is the dyed_color component (the item is in #minecraft:dyeable) */
	public static int getColor(ItemStack stack) {
		return DyedItemColor.getOrDefault(stack, 0x25C9E7);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, List<Component> tooltip, TooltipFlag flag) {
		if (!stack.has(DataComponents.DYED_COLOR)) {
			tooltip.add(Component.translatable("item.rats.party_hat.desc").withStyle(ChatFormatting.GRAY));
		}
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
		return slot == EquipmentSlot.HEAD;
	}
}
