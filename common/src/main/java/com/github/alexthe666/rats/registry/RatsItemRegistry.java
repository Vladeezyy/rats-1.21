package com.github.alexthe666.rats.registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumables;

import dev.vladeezyy.rats.platform.transfer.FluidStack;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.data.tags.RatsBannerPatternTags;
import com.github.alexthe666.rats.server.items.*;
import com.github.alexthe666.rats.server.items.upgrades.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RatsItemRegistry {

	public static final Registrar<Item> ITEMS = Registrar.create(BuiltInRegistries.ITEM, RatsMod.MODID);

	//Currently unused item textures:
	//blue cheese stick
	//blueprint
	//confetti launcher
	//hardhat
	//holocards
	//rat credit card
	//rat diamond
	//rat upgrade fragment
	//rat upgrade nuke
	//rat upgrade redstone
	//rat upgrade shield
	//rat upgrade shield max
	//ratlantean arrow
	//stick of cheese

	public static final RegistryEntry<Item, Item> CHEESE = ITEMS.register("cheese", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build())));
	public static final RegistryEntry<Item, Item> RAW_RAT = ITEMS.register("raw_rat", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build())));
	public static final RegistryEntry<Item, Item> COOKED_RAT = ITEMS.register("cooked_rat", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
	public static final RegistryEntry<Item, Item> RAT_PELT = ITEMS.register("rat_pelt", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_PAW = ITEMS.register("rat_paw", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> CHEESE_STICK = ITEMS.register("cheese_stick", () -> new RatStaffItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RADIUS_STICK = ITEMS.register("radius_stick", () -> new RatStaffItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> PATROL_STICK = ITEMS.register("patrol_stick", () -> new RatStaffItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_WHISTLE = ITEMS.register("rat_whistle", () -> new RatWhistleItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_FLUTE = ITEMS.register("rat_flute", () -> new RatFluteItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> GILDED_RAT_FLUTE = ITEMS.register("gilded_rat_flute", () -> new GildedRatFluteItem(Registrar.itemProps().durability(100).repairable(RatsToolMaterialRegistry.repairs("gilded_rat_flute"))));
	public static final RegistryEntry<Item, Item> CHEF_TOQUE = ITEMS.register("chef_toque", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.CHEF_TOQUE, 0));
	public static final RegistryEntry<Item, Item> PIPER_HAT = ITEMS.register("piper_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.PIPER_HAT, 1));
	public static final RegistryEntry<Item, Item> STRING_CHEESE = ITEMS.register("string_cheese", () -> new LoreTagItem(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).build(), Consumables.defaultFood().consumeSeconds(0.8F).build()), 1));
	public static final RegistryEntry<Item, Item> CREATIVE_CHEESE = ITEMS.register("creative_cheese", () -> new LoreTagItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 1, true));
	public static final RegistryEntry<Item, Item> BLUE_CHEESE = ITEMS.register("blue_cheese", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build())));
	public static final RegistryEntry<Item, Item> NETHER_CHEESE = ITEMS.register("nether_cheese", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build()).fireResistant()) {
		@Override
		public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
			if (!entity.fireImmune()) {
				entity.igniteForSeconds(5);
			}
			return super.finishUsingItem(stack, level, entity);
		}
	});
	public static final RegistryEntry<Item, Item> ASSORTED_VEGETABLES = ITEMS.register("assorted_vegetables", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.3F).build())));
	public static final RegistryEntry<Item, Item> RAT_BURGER = ITEMS.register("rat_burger", () -> new LoreTagItem(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0F).build()), 1));
	public static final RegistryEntry<Item, Item> RAT_SACK = ITEMS.register("rat_sack", () -> new RatSackItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_ARROW = ITEMS.register("rat_arrow", () -> new RatArrowItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_CAPTURE_NET = ITEMS.register("rat_capture_net", () -> new RatCaptureNetItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> FEATHERY_WING = ITEMS.register("feathery_wing", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> DRAGON_WING = ITEMS.register("dragon_wing", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> PLASTIC_WASTE = ITEMS.register("plastic_waste", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAW_PLASTIC = ITEMS.register("raw_plastic", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> CONTAMINATED_FOOD = ITEMS.register("contaminated_food", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.5F)
			.build(), Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(RatsEffectRegistry.PLAGUE.holder(), 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.WITHER, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SLOWNESS, 2400), 0.3F))
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.UNLUCK, 2400), 1.0F)).build())));
	public static final RegistryEntry<Item, Item> PURIFYING_LIQUID = ITEMS.register("purifying_liquid", () -> new PurifyingLiquidItem(Registrar.itemProps().stacksTo(1), false));
	public static final RegistryEntry<Item, Item> CRIMSON_FLUID = ITEMS.register("crimson_liquid", () -> new PurifyingLiquidItem(Registrar.itemProps().stacksTo(1), true));
	public static final RegistryEntry<Item, Item> PLAGUE_ESSENCE = ITEMS.register("plague_essence", () -> new LoreTagItem(Registrar.itemProps().rarity(Rarity.UNCOMMON), 1));
	public static final RegistryEntry<Item, Item> PLAGUE_DOCTORATE = ITEMS.register("plague_doctorate", () -> new LoreTagItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.UNCOMMON), 1));
	public static final RegistryEntry<Item, Item> HERB_BUNDLE = ITEMS.register("herb_bundle", () -> new PlagueHealerItem(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).build()), 0.1F));
	public static final RegistryEntry<Item, Item> TREACLE = ITEMS.register("treacle", () -> new PlagueHealerItem(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F).build()), 0.25F));
	public static final RegistryEntry<Item, Item> PLAGUE_LEECH = ITEMS.register("plague_leech", () -> new PlagueLeechItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> PLAGUE_STEW = ITEMS.register("plague_stew", () -> new PlagueHealerItem(Registrar.itemProps().stacksTo(1).craftRemainder(Items.BOWL).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build()), 1.0F));
	public static final RegistryEntry<Item, Item> RAT_SKULL = ITEMS.register("rat_skull", () -> new LoreTagItem(Registrar.itemProps(), 1));
	public static final RegistryEntry<Item, Item> GOLDEN_RAT_SKULL = ITEMS.register("golden_rat_skull", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> CORRUPT_RAT_SKULL = ITEMS.register("corrupt_rat_skull", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> PLAGUE_TOME = ITEMS.register("plague_tome", () -> new LoreTagItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.RARE).fireResistant(), 2, true));
	public static final RegistryEntry<Item, Item> PLAGUE_SCYTHE = ITEMS.register("plague_scythe", () -> new PlagueScytheItem(Registrar.itemProps().rarity(Rarity.RARE).fireResistant()));
	public static final RegistryEntry<Item, Item> FILTH = ITEMS.register("filth", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> FILTH_CORRUPTION = ITEMS.register("filth_corruption", () -> new LoreTagItem(Registrar.itemProps().rarity(Rarity.RARE), 1));
	public static final RegistryEntry<Item, Item> TANGLED_RAT_TAILS = ITEMS.register("tangled_rat_tails", () -> new Item(Registrar.itemProps().rarity(Rarity.RARE)));
	public static final RegistryEntry<Item, Item> CHARGED_CREEPER_CHUNK = ITEMS.register("charged_creeper_chunk", () -> new Item(Registrar.itemProps().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> RATBOW_ESSENCE = ITEMS.register("ratbow_essence", () -> new RatbowEssenceItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_PAPERS = ITEMS.register("rat_papers", () -> new RatPapersItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item>[] RAT_HAMMOCKS = new RegistryEntry[16];
	public static final RegistryEntry<Item, Item>[] RAT_TUBES = new RegistryEntry[16];
	public static final RegistryEntry<Item, Item>[] RAT_IGLOOS = new RegistryEntry[16];

	static {
		for (int i = 0; i < 16; i++) {
			int finalI = i;
			RAT_TUBES[i] = ITEMS.register("rat_tube_" + DyeColor.byId(i).getName(), () -> new RatTubeItem(Registrar.itemProps(), DyeColor.byId(finalI)));
			RAT_IGLOOS[i] = ITEMS.register("rat_igloo_" + DyeColor.byId(i).getName(), () -> new RatIglooItem(Registrar.itemProps(), DyeColor.byId(finalI)));
			RAT_HAMMOCKS[i] = ITEMS.register("rat_hammock_" + DyeColor.byId(i).getName(), () -> new RatHammockItem(Registrar.itemProps(), DyeColor.byId(finalI)));
		}
	}

	public static final RegistryEntry<Item, Item> RAT_WATER_BOTTLE = ITEMS.register("rat_water_bottle", () -> new RatDecorationItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_SEED_BOWL = ITEMS.register("rat_seed_bowl", () -> new RatDecorationItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_BREEDING_LANTERN = ITEMS.register("rat_breeding_lantern", () -> new RatDecorationItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_WHEEL = ITEMS.register("rat_wheel", () -> new RatDecorationItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> CONFIT_BYALDI = ITEMS.register("confit_byaldi", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(50).saturationModifier(1.0F).alwaysEdible().build(), Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(RatsEffectRegistry.SYNESTHESIA.holder(), 2400), 1.0F)).build())));
	public static final RegistryEntry<Item, Item> POTATO_PANCAKE = ITEMS.register("potato_pancake", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).alwaysEdible().build())));
	public static final RegistryEntry<Item, Item> LITTLE_BLACK_SQUASH_BALLS = ITEMS.register("little_black_squash_balls", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> LITTLE_BLACK_WORM = ITEMS.register("little_black_worm", () -> new LoreTagItem(Registrar.itemProps(), 1));
	public static final RegistryEntry<Item, Item> CENTIPEDE = ITEMS.register("centipede", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> POTATO_KNISHES = ITEMS.register("potato_knishes", () -> new Item(Registrar.itemProps().food(new FoodProperties.Builder().nutrition(50).saturationModifier(1.0F).alwaysEdible().build(), Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(RatsEffectRegistry.SYNESTHESIA.holder(), 2400), 1.0F)).build())));
	public static final RegistryEntry<Item, Item> TINY_COIN = ITEMS.register("tiny_coin", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> TOKEN_FRAGMENT = ITEMS.register("token_fragment", () -> new Item(Registrar.itemProps().rarity(Rarity.UNCOMMON)));
	public static final RegistryEntry<Item, Item> TOKEN_PIECE = ITEMS.register("token_piece", () -> new Item(Registrar.itemProps().rarity(Rarity.RARE)));
	public static final RegistryEntry<Item, Item> ARCHEOLOGIST_HAT = ITEMS.register("archeologist_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.UNCOMMON), RatsArmorMaterialRegistry.GENERIC_HAT, 1));
	public static final RegistryEntry<Item, Item> FARMER_HAT = ITEMS.register("farmer_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.FARMER_HAT, 0));
	public static final RegistryEntry<Item, Item> FISHERMAN_HAT = ITEMS.register("fisherman_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.GENERIC_HAT, 0));
	public static final RegistryEntry<Item, Item> RAT_FEZ = ITEMS.register("rat_fez", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.FEZ, 2));
	public static final RegistryEntry<Item, Item> TOP_HAT = ITEMS.register("top_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.TOP_HAT, 0));
	public static final RegistryEntry<Item, Item> SANTA_HAT = ITEMS.register("santa_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.SANTA_HAT, 0));
	public static final RegistryEntry<Item, Item> HALO_HAT = ITEMS.register("halo_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.HALO, 0));
	public static final RegistryEntry<Item, Item> PARTY_HAT = ITEMS.register("party_hat", () -> new PartyHatItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> PIRAT_HAT = ITEMS.register("pirat_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.GENERIC_HAT, 0));
	public static final RegistryEntry<Item, Item> RAT_KING_CROWN = ITEMS.register("rat_king_crown", () -> new HatItem(Registrar.itemProps().stacksTo(1).rarity(Rarity.RARE), RatsArmorMaterialRegistry.CROWN, 0));
	public static final RegistryEntry<Item, Item> PLAGUE_DOCTOR_MASK = ITEMS.register("plague_doctor_mask", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.PLAGUE_MASK, 1));
	public static final RegistryEntry<Item, Item> BLACK_DEATH_MASK = ITEMS.register("black_death_mask", () -> new HatItem(Registrar.itemProps().stacksTo(1).fireResistant().rarity(Rarity.RARE), RatsArmorMaterialRegistry.PLAGUE_MASK, 1));
	public static final RegistryEntry<Item, Item> EXTERMINATOR_HAT = ITEMS.register("exterminator_hat", () -> new HatItem(Registrar.itemProps().stacksTo(1), RatsArmorMaterialRegistry.GENERIC_HAT, 0));
	public static final RegistryEntry<Item, Item> MUSIC_DISC_MICE_ON_VENUS = ITEMS.register("music_disc_mice_on_venus", () -> new Item(Registrar.itemProps().jukeboxPlayable(RatsSoundRegistry.MICE_ON_VENUS_SONG).stacksTo(1).rarity(Rarity.RARE)));
	public static final RegistryEntry<Item, Item> MUSIC_DISC_LIVING_MICE = ITEMS.register("music_disc_living_mice", () -> new Item(Registrar.itemProps().jukeboxPlayable(RatsSoundRegistry.LIVING_MICE_SONG).stacksTo(1).rarity(Rarity.RARE)));


	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BASIC = ITEMS.register("rat_upgrade_basic", () -> new BaseRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CREATIVE = ITEMS.register("rat_upgrade_creative", () -> new CreativeRatUpgradeItem(Registrar.itemProps().fireResistant()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_JURY_RIGGED = ITEMS.register("rat_upgrade_jury_rigged", () -> new JuryRiggedRatUpgradeItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_SPEED = ITEMS.register("rat_upgrade_speed", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 0, () -> Map.of(Attributes.MOVEMENT_SPEED, 0.5D)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_PLATTER = ITEMS.register("rat_upgrade_platter", () -> new PlatterRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_HEALTH = ITEMS.register("rat_upgrade_health", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 0, () -> Map.of(Attributes.MAX_HEALTH, 20.0D)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ARMOR = ITEMS.register("rat_upgrade_armor", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 0, () -> Map.of(Attributes.ARMOR, 10.0D)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BOW = ITEMS.register("rat_upgrade_bow", () -> new RangedWeaponRatUpgradeItem(Registrar.itemProps(), false));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CROSSBOW = ITEMS.register("rat_upgrade_crossbow", () -> new RangedWeaponRatUpgradeItem(Registrar.itemProps(), true));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_DEMON = ITEMS.register("rat_upgrade_demon", () -> new DemonRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_STRENGTH = ITEMS.register("rat_upgrade_strength", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 0, () -> Map.of(Attributes.ATTACK_DAMAGE, 5.0D)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_REMEDY = ITEMS.register("rat_upgrade_remedy", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 2, 1, Map::of, true));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_WARRIOR = ITEMS.register("rat_upgrade_warrior", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 2, () -> Map.of(Attributes.MAX_HEALTH, RatConfig.warriorHealthUpgrade, Attributes.ARMOR, RatConfig.warriorArmorUpgrade, Attributes.ATTACK_DAMAGE, RatConfig.warriorDamageUpgrade)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_GOD = ITEMS.register("rat_upgrade_god", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps().fireResistant(), 3, () -> Map.of(Attributes.MAX_HEALTH, RatConfig.godHealthUpgrade, Attributes.ARMOR, RatConfig.godArmorUpgrade, Attributes.ATTACK_DAMAGE, RatConfig.godDamageUpgrade)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CHEF = ITEMS.register("rat_upgrade_chef", () -> new ChefRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CRAFTING = ITEMS.register("rat_upgrade_crafting", () -> new CraftingRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BLACKLIST = ITEMS.register("rat_upgrade_blacklist", () -> new RatListUpgradeItem(Registrar.itemProps().stacksTo(1), 0, 3));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_WHITELIST = ITEMS.register("rat_upgrade_whitelist", () -> new RatListUpgradeItem(Registrar.itemProps().stacksTo(1), 0, 3));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_MOB_FILTER = ITEMS.register("rat_upgrade_mob_filter", () -> new MobFilterUpgradeItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_REPLANTER = ITEMS.register("rat_upgrade_replanter", () -> new BaseRatUpgradeItem(Registrar.itemProps(), 0, 1));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_FLIGHT = ITEMS.register("rat_upgrade_flight", () -> new BaseFlightRatUpgradeItem(Registrar.itemProps(), 0, 3) {
		public ItemStack getWing() {
			return RatsItemRegistry.FEATHERY_WING.get().getDefaultInstance();
		}
	});
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_DRAGON = ITEMS.register("rat_upgrade_dragon", () -> new DragonRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BEE = ITEMS.register("rat_upgrade_bee", () -> new BeeRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_AQUATIC = ITEMS.register("rat_upgrade_aquatic", () -> new AquaticRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ENDER = ITEMS.register("rat_upgrade_ender", () -> new EnderRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_LUMBERJACK = ITEMS.register("rat_upgrade_lumberjack", () -> new LumberjackRatUpgradeItem(Registrar.itemProps()));
	//public static final RegistryEntry<Item, Item> RAT_UPGRADE_MINER_ORE = ITEMS.register("rat_upgrade_miner_ore", () -> new OreMinerRatUpgradeItem(Registrar.itemProps()));
	//public static final RegistryEntry<Item, Item> RAT_UPGRADE_MINER = ITEMS.register("rat_upgrade_miner", () -> new MinerRatUpgradeItem(Registrar.itemProps(), 1, 3));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_QUARRY = ITEMS.register("rat_upgrade_quarry", () -> new QuarryRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_FARMER = ITEMS.register("rat_upgrade_farmer", () -> new FarmerRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BREEDER = ITEMS.register("rat_upgrade_breeder", () -> new BreederRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_FISHERMAN = ITEMS.register("rat_upgrade_fisherman", () -> new FishermanRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_TICK_ACCELERATOR = ITEMS.register("rat_upgrade_tick_accelerator", () -> new TickAccelRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_TIME_MANIPULATOR = ITEMS.register("rat_upgrade_time_manipulator", () -> new TimeManipuRatorUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ASBESTOS = ITEMS.register("rat_upgrade_asbestos", () -> new AsbestosRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_UNDERWATER = ITEMS.register("rat_upgrade_underwater", () -> new UnderwaterRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_POISON = ITEMS.register("rat_upgrade_poison", () -> new PoisonRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_DAMAGE_PROTECTION = ITEMS.register("rat_upgrade_damage_protection", () -> new ProtectionRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ORE_DOUBLING = ITEMS.register("rat_upgrade_ore_doubling", () -> new OreDoublingRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BASIC_ENERGY = ITEMS.register("rat_upgrade_basic_energy", () -> new EnergyRatUpgradeItem(Registrar.itemProps(), 0, RatConfig.ratRFTransferBasic, RatConfig.ratChargeBasic));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ADVANCED_ENERGY = ITEMS.register("rat_upgrade_advanced_energy", () -> new EnergyRatUpgradeItem(Registrar.itemProps(), 1, RatConfig.ratRFTransferAdvanced, RatConfig.ratChargeAdvanced));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ELITE_ENERGY = ITEMS.register("rat_upgrade_elite_energy", () -> new EnergyRatUpgradeItem(Registrar.itemProps(), 2, RatConfig.ratRFTransferElite, RatConfig.ratChargeElite));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_EXTREME_ENERGY = ITEMS.register("rat_upgrade_extreme_energy", () -> new EnergyRatUpgradeItem(Registrar.itemProps().fireResistant(), 3, RatConfig.ratRFTransferExtreme, RatConfig.ratChargeExtreme));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BOTTLER = ITEMS.register("rat_upgrade_bottler", () -> new BottlerRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BUCKET = ITEMS.register("rat_upgrade_bucket", () -> new BucketRatUpgradeItem(Registrar.itemProps(), 0, 2, FluidStack.BUCKET_VOLUME));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BIG_BUCKET = ITEMS.register("rat_upgrade_big_bucket", () -> new BucketRatUpgradeItem(Registrar.itemProps(), 2, 2, FluidStack.BUCKET_VOLUME * 5));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_MILKER = ITEMS.register("rat_upgrade_milker", () -> new MilkerRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_SHEARS = ITEMS.register("rat_upgrade_shears", () -> new ShearsRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ARISTOCRAT = ITEMS.register("rat_upgrade_aristocrat", () -> new AristocratRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_TNT = ITEMS.register("rat_upgrade_tnt", () -> new TNTRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_TNT_SURVIVOR = ITEMS.register("rat_upgrade_tnt_survivor", () -> new TNTSurvivorRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_PLACER = ITEMS.register("rat_upgrade_placer", () -> new PlacerRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_VOODOO = ITEMS.register("rat_upgrade_voodoo", () -> new StatBoostingRatUpgradeItem(Registrar.itemProps(), 2, 0, () -> Map.of(Attributes.MAX_HEALTH, RatConfig.voodooHealthUpgrade), true));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ENCHANTER = ITEMS.register("rat_upgrade_enchanter", () -> new EnchanterRatUpgradeItem(Registrar.itemProps(), 1, 2));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_DISENCHANTER = ITEMS.register("rat_upgrade_disenchanter", () -> new EnchanterRatUpgradeItem(Registrar.itemProps(), 1, 1));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CHRISTMAS = ITEMS.register("rat_upgrade_christmas", () -> new ChristmasRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_NO_FLUTE = ITEMS.register("rat_upgrade_no_flute", () -> new BaseRatUpgradeItem(Registrar.itemProps(), 0, 1));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CARRAT = ITEMS.register("rat_upgrade_carrat", () -> new CarratRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_DJ = ITEMS.register("rat_upgrade_dj", () -> new DJRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_SUPPORT = ITEMS.register("rat_upgrade_support", () -> new SupportRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_ANGEL = ITEMS.register("rat_upgrade_angel", () -> new BaseRatUpgradeItem(Registrar.itemProps(), 2, 2));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_UNDEAD = ITEMS.register("rat_upgrade_undead", () -> new UndeadRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_PICKPOCKET = ITEMS.register("rat_upgrade_pickpocket", () -> new PickpocketRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_GARDENER = ITEMS.register("rat_upgrade_gardener", () -> new RatGardenerUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_IDOL = ITEMS.register("rat_upgrade_idol", () -> new IdolRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_SCULKED = ITEMS.register("rat_upgrade_sculked", () -> new SculkedRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BASIC_MOUNT = ITEMS.register("rat_upgrade_basic_mount", () -> new BaseRatUpgradeItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_CHICKEN_MOUNT = ITEMS.register("rat_upgrade_chicken_mount", () -> new MountRatUpgradeItem<>(Registrar.itemProps(), 2, 2, RatsEntityRegistry.RAT_MOUNT_CHICKEN));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_GOLEM_MOUNT = ITEMS.register("rat_upgrade_golem_mount", () -> new MountRatUpgradeItem<>(Registrar.itemProps(), 2, 2, RatsEntityRegistry.RAT_MOUNT_GOLEM));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_STRIDER_MOUNT = ITEMS.register("rat_upgrade_strider_mount", () -> new StriderMountRatUpgradeItem<>(Registrar.itemProps(), 2, 2, RatsEntityRegistry.RAT_STRIDER_MOUNT));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_BEAST_MOUNT = ITEMS.register("rat_upgrade_beast_mount", () -> new MountRatUpgradeItem<>(Registrar.itemProps(), 2, 2, RatsEntityRegistry.RAT_MOUNT_BEAST));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_COMBINED = ITEMS.register("rat_upgrade_combined", () -> new CombinedRatUpgradeItem(Registrar.itemProps().stacksTo(1)));
	public static final RegistryEntry<Item, Item> RAT_UPGRADE_COMBINED_CREATIVE = ITEMS.register("rat_upgrade_combined_creative", () -> new CombinedRatUpgradeItem(Registrar.itemProps().stacksTo(1).fireResistant()));

	public static final RegistryEntry<Item, Item> RAT_NUGGET = ITEMS.register("rat_nugget", () -> new Item(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_NUGGET_ORE = ITEMS.register("rat_nugget_ore", () -> new OreRatNuggetItem(Registrar.itemProps()));
	public static final RegistryEntry<Item, Item> RAT_BANNER_PATTERN = ITEMS.register("rat_banner_pattern", () -> new Item(Registrar.itemProps().stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(RatsBannerPatternTags.RAT_BANNER_PATTERN))));
	public static final RegistryEntry<Item, Item> CHEESE_BANNER_PATTERN = ITEMS.register("cheese_banner_pattern", () -> new Item(Registrar.itemProps().stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(RatsBannerPatternTags.CHEESE_BANNER_PATTERN))));
	public static final RegistryEntry<Item, Item> RAC_BANNER_PATTERN = ITEMS.register("rat_and_crossbones_banner_pattern", () -> new Item(Registrar.itemProps().stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(RatsBannerPatternTags.RAC_BANNER_PATTERN))));
}
