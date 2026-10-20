package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Armor materials (26.x records; the 1.20.1 original's enum values). Durability is the original's multiplier (0: the
 * item has none), the repair items a tag (data/rats/tags/item/repairs/*.json, the original's ingredients).
 */
public final class RatsArmorMaterialRegistry {
	public static final ArmorMaterial PIPER_HAT = material("piper_hat", 25, defense(1, 1, 1, 2), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial CHEF_TOQUE = material("chef_toque", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial PLAGUE_MASK = material("plague_mask", 25, defense(1, 1, 1, 3), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 0.0F);
	public static final ArmorMaterial RATLANTIS = material("ratlantis", 40, defense(5, 8, 10, 5), 17, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.1F);
	public static final ArmorMaterial FARMER_HAT = material("farmer_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial TOP_HAT = material("top_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial FEZ = material("fez", 0, defense(1, 1, 1, 1), 100, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial SANTA_HAT = material("santa_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial HALO = material("halo", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial CROWN = material("crown", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial GHOST_HAT = material("ghost_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);
	public static final ArmorMaterial GENERIC_HAT = material("generic_hat", 0, defense(1, 1, 1, 1), 100, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F);

	private RatsArmorMaterialRegistry() {
	}

	/** boots, leggings, chestplate, helmet */
	private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
		Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
		map.put(ArmorType.BOOTS, boots);
		map.put(ArmorType.LEGGINGS, leggings);
		map.put(ArmorType.CHESTPLATE, chestplate);
		map.put(ArmorType.HELMET, helmet);
		map.put(ArmorType.BODY, chestplate);
		return map;
	}

	private static ArmorMaterial material(String name, int durability, Map<ArmorType, Integer> defense, int enchantability, Holder<SoundEvent> sound, float toughness, float knockback) {
		ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
		return new ArmorMaterial(durability, defense, enchantability, sound, toughness, knockback, RatsToolMaterialRegistry.repairs(name), asset);
	}

	/** Properties of armor drawn by the game (equipment asset assets/rats/equipment/name.json) */
	public static Item.Properties armor(Item.Properties properties, ArmorMaterial material, ArmorType type) {
		return properties.humanoidArmor(material, type);
	}

	/**
	 * Properties of a hat: worn on the head with the material's armor, sound, enchantability and repair items, drawn by
	 * Rats (no equipment asset); durability only for the materials that had it (the hats of the original had none)
	 */
	public static Item.Properties hat(Item.Properties properties, ArmorMaterial material) {
		Item.Properties hat = properties.attributes(material.createAttributes(ArmorType.HELMET))
				.enchantable(material.enchantmentValue())
				.repairable(material.repairIngredient())
				.component(DataComponents.EQUIPPABLE, Equippable.builder(ArmorType.HELMET.getSlot()).setEquipSound(material.equipSound()).setDamageOnHurt(material.durability() > 0).build());
		return material.durability() > 0 ? hat.durability(ArmorType.HELMET.getDurability(material.durability())) : hat;
	}
}
