package com.github.alexthe666.rats.server.items;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.alexthe666.rats.registry.RatsToolMaterialRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class PiratCutlassItem extends Item {
	private final boolean ghost;

	public PiratCutlassItem(Item.Properties properties, boolean ghost) {
		// the material for durability, repair and enchanting; the damage and speed are the original's
		super(properties.sword(ghost ? RatsToolMaterialRegistry.GHOST_CUTLASS : RatsToolMaterialRegistry.CUTLASS, 0.0F, 0.0F).attributes(weapon(ghost ? 8D : 6.5D, -1D)));
		this.ghost = ghost;
	}

	/** The weapon's main hand damage and speed (the original replaced the item's attribute map with these values) */
	static ItemAttributeModifiers weapon(double damage, double speed) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.build();
	}
}
