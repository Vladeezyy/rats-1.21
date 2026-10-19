package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import dev.vladeezyy.rats.platform.RegistryEntry;
import dev.vladeezyy.rats.platform.Registrar;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Armor materials (1.21: registry entries; the 1.20.1 original was an enum). Values as in the original. Durability is
 * an item property in 1.21: {@link #durability} gives the original's value (multiplier x vanilla base per slot).
 */
public final class RatsArmorMaterialRegistry {
	public static final Registrar<ArmorMaterial> MATERIALS = Registrar.create(Registries.ARMOR_MATERIAL, RatsMod.MODID);
	private static final Map<String, Integer> DURABILITY_MULTIPLIERS = new HashMap<>();

	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> PIPER_HAT = register("piper_hat", 25, defense(1, 1, 1, 2), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(RatsBlockRegistry.PIED_WOOL.get()));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> CHEF_TOQUE = register("chef_toque", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Blocks.WHITE_WOOL));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> PLAGUE_MASK = register("plague_mask", 25, defense(1, 1, 1, 3), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 0.0F, () -> Ingredient.of(RatsItemRegistry.PLAGUE_ESSENCE.get()));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> RATLANTIS = register("ratlantis", 40, defense(5, 8, 10, 5), 17, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.1F, () -> Ingredient.of(RatlantisItemRegistry.ORATCHALCUM_INGOT.get()));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> FARMER_HAT = register("farmer_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.HAY_BLOCK));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> TOP_HAT = register("top_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.BLACK_WOOL));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> FEZ = register("fez", 0, defense(1, 1, 1, 1), 100, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.RED_WOOL));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> SANTA_HAT = register("santa_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.RED_WOOL));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> HALO = register("halo", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_NUGGET));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> CROWN = register("crown", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> GHOST_HAT = register("ghost_hat", 0, defense(1, 1, 1, 1), 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(RatlantisItemRegistry.GHOST_PIRAT_ECTOPLASM.get()));
	public static final RegistryEntry<ArmorMaterial, ArmorMaterial> GENERIC_HAT = register("generic_hat", 0, defense(1, 1, 1, 1), 100, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER));

	private RatsArmorMaterialRegistry() {
	}

	/** boots, leggings, chestplate, helmet */
	private static EnumMap<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
		return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
			map.put(ArmorItem.Type.BOOTS, boots);
			map.put(ArmorItem.Type.LEGGINGS, leggings);
			map.put(ArmorItem.Type.CHESTPLATE, chestplate);
			map.put(ArmorItem.Type.HELMET, helmet);
			map.put(ArmorItem.Type.BODY, chestplate);
		});
	}

	private static RegistryEntry<ArmorMaterial, ArmorMaterial> register(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, Holder<SoundEvent> sound, float toughness, float knockback, Supplier<Ingredient> repair) {
		DURABILITY_MULTIPLIERS.put(name, durabilityMultiplier);
		List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, name)));
		return MATERIALS.register(name, () -> new ArmorMaterial(defense, enchantability, sound, repair, layers, toughness, knockback));
	}

	/** The item durability the 1.20.1 material gave (0: no durability) */
	public static int durability(Holder<ArmorMaterial> material, ArmorItem.Type type) {
		int multiplier = DURABILITY_MULTIPLIERS.getOrDefault(material.unwrapKey().orElseThrow().location().getPath(), 0);
		return multiplier == 0 ? 0 : type.getDurability(multiplier);
	}
}
