package com.github.alexthe666.rats.registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.ArmorType;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.data.tags.RatsBannerPatternTags;
import com.github.alexthe666.rats.server.items.*;
import com.github.alexthe666.rats.server.items.upgrades.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatlantisItemRegistry {

	public static final Registrar<Item> ITEMS = Registrar.create(BuiltInRegistries.ITEM, RatsMod.MODID);

	public static final RegistryEntry<Item, Item> RAS_BANNER_PATTERN = ITEMS.register("rat_and_sickle_banner_pattern", () -> new Item(Registrar.itemProps().stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(RatsBannerPatternTags.RAS_BANNER_PATTERN))));
	public static final RegistryEntry<Item, Item> RATLANTIS_RAT_SKULL = ITEMS.register("ratlantis_rat_skull", () -> new Item(Registrar.itemProps().rarity(RatsMod.RATLANTIS_SPECIAL).fireResistant()));
	public static final RegistryEntry<Item, Item> AVIATOR_HAT = ITEMS.register("aviator_hat", () -> new HatItem(Registrar.itemProps(), RatsArmorMaterialRegistry.GENERIC_HAT, 0));
	public static final RegistryEntry<Item, Item> RAT_TOGA = ITEMS.register("rat_toga", () -> new LoreTagItem(Registrar.itemProps(), 2));
	public static final RegistryEntry<Item, Item> RATGLOVE_PETALS = ITEMS.register("ratglove_petals", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> FERAL_RAT_CLAW = ITEMS.register("feral_rat_claw", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> FERAL_BAGH_NAKHS = ITEMS.register("feral_bagh_nakhs", () -> new BaghNakhsItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> GEM_OF_RATLANTIS = ITEMS.register("gem_of_ratlantis", () -> new Item(Registrar.itemProps().rarity(RatsMod.RATLANTIS_SPECIAL).trimMaterial(RatlantisTrimRegistry.GEM_OF_RATLANTIS)));
	public static final RegistryEntry<Item, Item> ORATCHALCUM_INGOT = ITEMS.register("oratchalcum_ingot", () -> new Item(Registrar.itemProps().fireResistant().trimMaterial(RatlantisTrimRegistry.ORATCHALCUM)));
	public static final RegistryEntry<Item, Item> RAW_ORATCHALCUM = ITEMS.register("raw_oratchalcum", () -> new Item(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> ORATCHALCUM_NUGGET = ITEMS.register("oratchalcum_nugget", () -> new Item(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_HELMET = ITEMS.register("ratlantis_helmet", () -> new RatlantisArmorItem(RatsArmorMaterialRegistry.RATLANTIS, ArmorType.HELMET, Registrar.itemProps().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> RATLANTIS_CHESTPLATE = ITEMS.register("ratlantis_chestplate", () -> new RatlantisArmorItem(RatsArmorMaterialRegistry.RATLANTIS, ArmorType.CHESTPLATE, Registrar.itemProps().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> RATLANTIS_LEGGINGS = ITEMS.register("ratlantis_leggings", () -> new RatlantisArmorItem(RatsArmorMaterialRegistry.RATLANTIS, ArmorType.LEGGINGS, Registrar.itemProps().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> RATLANTIS_BOOTS = ITEMS.register("ratlantis_boots", () -> new RatlantisArmorItem(RatsArmorMaterialRegistry.RATLANTIS, ArmorType.BOOTS, Registrar.itemProps().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> RATLANTIS_SWORD = ITEMS.register("ratlantis_sword", () -> new RatlantisSwordItem(Registrar.itemProps().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_PICKAXE = ITEMS.register("ratlantis_pickaxe", () -> new RatlantisToolItem.Pickaxe(Registrar.itemProps().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_AXE = ITEMS.register("ratlantis_axe", () -> new RatlantisToolItem.Axe(Registrar.itemProps().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_SHOVEL = ITEMS.register("ratlantis_shovel", () -> new RatlantisToolItem.Shovel(Registrar.itemProps().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_HOE = ITEMS.register("ratlantis_hoe", () -> new RatlantisToolItem.Hoe(Registrar.itemProps().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTIS_BOW = ITEMS.register("ratlantis_bow", () -> new RatlantisBowItem(Registrar.itemProps().durability(1500).rarity(Rarity.UNCOMMON).fireResistant()));
	public static final RegistryEntry<Item, Item> ARCANE_TECHNOLOGY = ITEMS.register("arcane_technology", () -> new LoreTagItem(Registrar.itemProps().rarity(Rarity.RARE).fireResistant(), 2));
	public static final RegistryEntry<Item, Item> ANCIENT_SAWBLADE = ITEMS.register("ancient_sawblade", () -> new Item(Registrar.itemProps().rarity(Rarity.RARE).fireResistant()));
	public static final RegistryEntry<Item, Item> RATLANTEAN_FLAME = ITEMS.register("ratlantean_flame", () -> new RatlanteanFlameItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> VIAL_OF_SENTIENCE = ITEMS.register("vial_of_sentience", () -> new VialOfSentienceItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.RARE)));
	public static final RegistryEntry<Item, Item> PSIONIC_RAT_BRAIN = ITEMS.register("psionic_rat_brain", () -> new LoreTagItem(Registrar.itemProps().rarity(Rarity.RARE).fireResistant(), 2));
	public static final RegistryEntry<Item, Item> PIRAT_CUTLASS = ITEMS.register("pirat_cutlass", () -> new PiratCutlassItem(Registrar.itemProps(), false));
	public static final RegistryEntry<Item, Item> CHEESE_CANNONBALL = ITEMS.register("cheese_cannonball", () -> new LoreTagItem(Registrar.itemProps(), 1));
	public static final RegistryEntry<Item, Item> GHOST_PIRAT_HAT = ITEMS.register("ghost_pirat_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1).fireResistant(), RatsArmorMaterialRegistry.GHOST_HAT, 0));
	public static final RegistryEntry<Item, Item> GHOST_PIRAT_ECTOPLASM = ITEMS.register("ghost_pirat_ectoplasm", () -> new EctoplasmItem(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> GHOST_PIRAT_CUTLASS = ITEMS.register("ghost_pirat_cutlass", () -> new PiratCutlassItem(Registrar.itemProps().fireResistant(), true));
	public static final RegistryEntry<Item, Item> DUTCHRAT_WHEEL = ITEMS.register("dutchrat_wheel", () -> new Item(Registrar.itemProps().rarity(Rarity.RARE).fireResistant()));
	public static final RegistryEntry<Item, Item> MILITARY_HAT = ITEMS.register("military_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.GENERIC_HAT, 0));
	public static final RegistryEntry<Item, Item> BIPLANE_WING = ITEMS.register("biplane_wing", () -> new Item(Registrar.itemProps().rarity(Rarity.RARE).fireResistant()));
	public static final RegistryEntry<Item, Item> RATFISH = ITEMS.register("ratfish", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.35F).build())));
	public static final RegistryEntry<Item, Item> RATFISH_BUCKET = ITEMS.register("ratfish_bucket", () -> new MobBucketItem(RatlantisEntityRegistry.RATFISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, Registrar.itemProps().craftRemainder(Items.BUCKET).stacksTo(1)));
	public static final RegistryEntry<Item, Item> RATBOT_BARREL = ITEMS.register("ratbot_barrel", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> CHARGED_RATBOT_BARREL = ITEMS.register("charged_ratbot_barrel", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RATTLING_GUN = ITEMS.register("rattling_gun", () -> new RattlingGunItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> IDOL_OF_RATLANTIS = ITEMS.register("idol_of_ratlantis", () -> new LoreTagItem(Registrar.itemProps().rarity(RatsMod.RATLANTIS_SPECIAL).fireResistant(), 1));

	public static final RegistryEntry<Item, Item> PIRAT_SIGN = ITEMS.register("pirat_sign", () -> new SignItem(RatlantisBlockRegistry.PIRAT_SIGN.get(), RatlantisBlockRegistry.PIRAT_WALL_SIGN.get(), Registrar.itemProps().stacksTo(16).useBlockDescriptionPrefix()));
	public static final RegistryEntry<Item, Item> PIRAT_HANGING_SIGN = ITEMS.register("pirat_hanging_sign", () -> new HangingSignItem(RatlantisBlockRegistry.PIRAT_HANGING_SIGN.get(), RatlantisBlockRegistry.PIRAT_WALL_HANGING_SIGN.get(), Registrar.itemProps().stacksTo(16).useBlockDescriptionPrefix()));
	public static final RegistryEntry<Item, Item> PIRAT_BOAT = ITEMS.register("pirat_boat", () -> new BoatItem(RatlantisEntityRegistry.BOAT.get(), Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> PIRAT_CHEST_BOAT = ITEMS.register("pirat_chest_boat", () -> new BoatItem(RatlantisEntityRegistry.CHEST_BOAT.get(), Registrar.itemProps().stacksTo(1)));

	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ARCHEOLOGIST = ITEMS.register("rat_upgrade_archeologist", () -> new ArcheologistRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_AUTOMATON_MOUNT = ITEMS.register("rat_upgrade_automaton_mount", () -> new MountRatUpgradeItem<>(Registrar.itemProps(), 2, 2, RatlantisEntityRegistry.RAT_MOUNT_AUTOMATON));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BIPLANE_MOUNT = ITEMS.register("rat_upgrade_biplane_mount", () -> new BiplaneMountUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BASIC_RATLANTEAN = ITEMS.register("rat_upgrade_basic_ratlantean", () -> new BaseRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_FERAL_BITE = ITEMS.register("rat_upgrade_feral_bite", () -> new FeralBiteRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BUCCANEER = ITEMS.register("rat_upgrade_buccaneer", () -> new BuccaneerRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_RATINATOR = ITEMS.register("rat_upgrade_ratinator", () -> new RatinatorRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_PSYCHIC = ITEMS.register("rat_upgrade_psychic", () -> new PsychicRatUpgradeItem(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ETHEREAL = ITEMS.register("rat_upgrade_ethereal", () -> new EtherealRatUpgradeItem(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_NONBELIEVER = ITEMS.register("rat_upgrade_nonbeliever", () -> new NonbelieverRatUpgradeItem(Registrar.itemProps().fireResistant()));
}
