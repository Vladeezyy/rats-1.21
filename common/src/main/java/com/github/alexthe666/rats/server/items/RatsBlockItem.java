package com.github.alexthe666.rats.server.items;

import net.minecraft.world.entity.LivingEntity;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.RatsBEWLR;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.server.block.CustomItemRarity;
import com.github.alexthe666.rats.server.block.WearableOnHead;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class RatsBlockItem extends BlockItem {

	public RatsBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return this.getBlock() instanceof WearableOnHead && EquipmentSlot.HEAD == armorType;
	}


	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (this.getBlock() == RatlantisBlockRegistry.CHUNKY_CHEESE_TOKEN.get() && !RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			return InteractionResult.PASS;
		}
		return super.useOn(context);
	}

}
