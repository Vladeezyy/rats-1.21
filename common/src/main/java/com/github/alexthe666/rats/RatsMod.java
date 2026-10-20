package com.github.alexthe666.rats;
import net.minecraft.world.entity.EntitySpawnReason;
import dev.vladeezyy.rats.platform.RegistryEntry;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.flag.FeatureFlagSet;
import com.mojang.serialization.Codec;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRule;

import dev.vladeezyy.rats.platform.Platform;
import dev.vladeezyy.rats.platform.Platform;
import com.github.alexthe666.rats.registry.*;
import com.github.alexthe666.rats.registry.worldgen.RatlantisFeatureRegistry;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The common part of Rats: constants, registration and setup that both loaders run. The loader entry points are
 * {@code dev.vladeezyy.rats.neoforge.RatsNeoForge} and {@code dev.vladeezyy.rats.fabric.RatsFabric}.
 */
public final class RatsMod {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "rats";
	public static final Rarity RATLANTIS_SPECIAL = Platform.INSTANCE.ratlantisRarity();
	public static final MobCategory RATS = Platform.INSTANCE.ratsCategory();

	public static final BlockSetType PIRAT_WOOD_SET = BlockSetType.register(new BlockSetType(Identifier.fromNamespaceAndPath(MODID, "pirat").toString(), true, true, true, BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.NETHER_WOOD, SoundEvents.NETHER_WOOD_DOOR_CLOSE, SoundEvents.NETHER_WOOD_DOOR_OPEN, SoundEvents.NETHER_WOOD_TRAPDOOR_CLOSE, SoundEvents.NETHER_WOOD_TRAPDOOR_OPEN, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, SoundEvents.NETHER_WOOD_BUTTON_CLICK_OFF, SoundEvents.NETHER_WOOD_BUTTON_CLICK_ON));
	public static final WoodType PIRAT_WOOD_TYPE = WoodType.register(new WoodType(Identifier.fromNamespaceAndPath(MODID, "pirat").toString(), PIRAT_WOOD_SET, SoundType.NETHER_WOOD, SoundType.NETHER_WOOD_HANGING_SIGN, SoundEvents.NETHER_WOOD_FENCE_GATE_CLOSE, SoundEvents.NETHER_WOOD_FENCE_GATE_OPEN));

	// 26.x: game rules are registry entries with snake_case ids (1.21.1: doRatSpawning, doPiperSpawning, doPlagueDoctorSpawning)
	public static final Registrar<GameRule<?>> GAME_RULES = Registrar.create(Registries.GAME_RULE, MODID);
	public static final RegistryEntry<GameRule<?>, GameRule<Boolean>> SPAWN_RATS = booleanRule("do_rat_spawning");
	public static final RegistryEntry<GameRule<?>, GameRule<Boolean>> SPAWN_PIPERS = booleanRule("do_piper_spawning");
	public static final RegistryEntry<GameRule<?>, GameRule<Boolean>> SPAWN_PLAGUE_DOCTORS = booleanRule("do_plague_doctor_spawning");

	private static RegistryEntry<GameRule<?>, GameRule<Boolean>> booleanRule(String name) {
		return GAME_RULES.register(name, () -> new GameRule<>(GameRuleCategory.SPAWNING, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, value -> value ? 1 : 0, true, FeatureFlagSet.of()));
	}

	/** A Rats spawning rule of the level (true where the level has no rules, e.g. during world generation) */
	public static boolean spawnRule(LevelAccessor level, RegistryEntry<GameRule<?>, GameRule<Boolean>> rule) {
		return !(level instanceof ServerLevelAccessor server) || server.getLevel().getGameRules().get(rule.get());
	}

	public static boolean ICEANDFIRE_LOADED;
	public static boolean RATLANTIS_DATAPACK_ENABLED = false;
	public static final List<Item> RATLANTIS_ITEMS = new ArrayList<>();
	private static final List<Pair<String, Component>> MOB_CACHE = new ArrayList<>();

	private RatsMod() {
	}

	/**
	 * Loads the registry classes, which register their entries through {@link dev.vladeezyy.rats.platform.Registrar}
	 * (Fabric: right away, in this order; NeoForge: on its register event).
	 */
	public static void init() {
		ICEANDFIRE_LOADED = Platform.INSTANCE.isModLoaded("iceandfire");
		touch(RatVariantRegistry.RAT_VARIANTS, RatsAdvancementsRegistry.TRIGGERS,
			RatsSoundRegistry.SOUNDS, RatsEffectRegistry.MOB_EFFECTS, RatsParticleRegistry.PARTICLES,
			RatsBlockRegistry.BLOCKS, RatlantisBlockRegistry.BLOCKS, RatsBlockEntityRegistry.BLOCK_ENTITIES, RatlantisBlockEntityRegistry.BLOCK_ENTITIES,
			RatsEntityRegistry.ENTITIES, RatlantisEntityRegistry.ENTITIES, RatsItemRegistry.ITEMS, RatlantisItemRegistry.ITEMS,
			RatsLootRegistry.CONDITIONS, RatsMenuRegistry.MENUS, RatsVillagerRegistry.POIS, RatsVillagerRegistry.PROFESSIONS,
			RatsRecipeRegistry.RECIPES, RatsRecipeRegistry.SERIALIZERS, RatsCreativeTabRegistry.TABS,
			RatlantisFeatureRegistry.CARVERS, RatlantisFeatureRegistry.FEATURES, RatlantisFeatureRegistry.PROCESSORS, RatlantisFeatureRegistry.TRUNK_PLACERS);
		RatsDataSerializerRegistry.register();
	}

	private static void touch(Object... registrars) {
		// reading the static fields loaded the classes; nothing else to do
	}

	/** After registration, on the main thread (NeoForge: common setup, enqueued; Fabric: after init) */
	public static void setup() {
		RatsAdvancementsRegistry.init();
		
		RatsUpgradeConflictRegistry.init();
		{
			RatsCauldronRegistry.init();
			RatsDispenserRegistry.init();


			// 26.x: the party hat is washed by the cauldron_can_remove_dye tag and dyed by recipe/party_hat_dyed.json

			FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
			// the potted plants: vanilla FlowerPotBlock(content, properties) adds them to the empty pot

			ComposterBlock.add(0.3F, RatsItemRegistry.RAT_NUGGET.get());
			ComposterBlock.add(0.3F, RatlantisBlockRegistry.PIRAT_SAPLING.get());
			ComposterBlock.add(0.3F, RatlantisBlockRegistry.PIRAT_LEAVES.get());
			ComposterBlock.add(0.5F, RatsItemRegistry.CONTAMINATED_FOOD.get());
			ComposterBlock.add(0.65F, RatlantisBlockRegistry.RATGLOVE_FLOWER.get());
			ComposterBlock.add(0.65F, RatlantisItemRegistry.RATGLOVE_PETALS.get());
			ComposterBlock.add(0.85F, RatsItemRegistry.POTATO_PANCAKE.get());
			ComposterBlock.add(0.85F, RatsItemRegistry.HERB_BUNDLE.get());
			ComposterBlock.add(1.0F, RatsItemRegistry.CONFIT_BYALDI.get());
			ComposterBlock.add(1.0F, RatsItemRegistry.POTATO_KNISHES.get());

			AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
			AxeItem.STRIPPABLES.put(RatlantisBlockRegistry.PIRAT_LOG.get(), RatlantisBlockRegistry.STRIPPED_PIRAT_LOG.get());
			AxeItem.STRIPPABLES.put(RatlantisBlockRegistry.PIRAT_WOOD.get(), RatlantisBlockRegistry.STRIPPED_PIRAT_WOOD.get());
		}
		//wooooo caches ftw
		if (RATLANTIS_ITEMS.isEmpty()) {
			RatlantisItemRegistry.ITEMS.getEntries().forEach(item -> RATLANTIS_ITEMS.add(item.get()));
		}
	}

	//code take from TelepathicGrunt's gist: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
	//1.18.2 version used and modified so it works in 1.19.4
	/** Before the server starts: the pet shops and garbage heaps of villages */
	public static void addPetShops(net.minecraft.server.MinecraftServer server) {
		Registry<StructureTemplatePool> templatePoolRegistry = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
		Registry<StructureProcessorList> processorListRegistry = server.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);

		if (RatConfig.villagePetShops) {
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/plains/houses"), "rats:pet_shops/plains", RatConfig.villagePetShopWeight, ProcessorLists.MOSSIFY_10_PERCENT);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/snowy/houses"), "rats:pet_shops/snowy", RatConfig.villagePetShopWeight, ProcessorLists.EMPTY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/savanna/houses"), "rats:pet_shops/savanna", RatConfig.villagePetShopWeight, ProcessorLists.EMPTY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/taiga/houses"), "rats:pet_shops/taiga", RatConfig.villagePetShopWeight, ProcessorLists.MOSSIFY_10_PERCENT);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/desert/houses"), "rats:pet_shops/desert", RatConfig.villagePetShopWeight, ProcessorLists.EMPTY);

			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/plains/zombie/houses"), "rats:pet_shops/zombie_plains", RatConfig.zombieVillagePetShopWeight, ProcessorLists.ZOMBIE_PLAINS);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/snowy/zombie/houses"), "rats:pet_shops/zombie_snowy", RatConfig.zombieVillagePetShopWeight, ProcessorLists.ZOMBIE_SNOWY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/savanna/zombie/houses"), "rats:pet_shops/zombie_savanna", RatConfig.zombieVillagePetShopWeight, ProcessorLists.ZOMBIE_SAVANNA);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/taiga/zombie/houses"), "rats:pet_shops/zombie_taiga", RatConfig.zombieVillagePetShopWeight, ProcessorLists.ZOMBIE_TAIGA);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/desert/zombie/houses"), "rats:pet_shops/zombie_desert", RatConfig.zombieVillagePetShopWeight, ProcessorLists.ZOMBIE_DESERT);
		}

		if (RatConfig.villageGarbageHeaps) {
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/plains/houses"), "rats:garbage_heaps/plains", RatConfig.villageGarbageHeapWeight, ProcessorLists.MOSSIFY_10_PERCENT);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/snowy/houses"), "rats:garbage_heaps/snowy", RatConfig.villageGarbageHeapWeight, ProcessorLists.EMPTY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/savanna/houses"), "rats:garbage_heaps/savanna", RatConfig.villageGarbageHeapWeight, ProcessorLists.EMPTY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/taiga/houses"), "rats:garbage_heaps/taiga", RatConfig.villageGarbageHeapWeight, ProcessorLists.MOSSIFY_10_PERCENT);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/desert/houses"), "rats:garbage_heaps/desert", RatConfig.villageGarbageHeapWeight, ProcessorLists.EMPTY);

			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/plains/zombie/houses"), "rats:garbage_heaps/plains", RatConfig.zombieVillageGarbageHeapWeight, ProcessorLists.ZOMBIE_PLAINS);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/snowy/zombie/houses"), "rats:garbage_heaps/snowy", RatConfig.zombieVillageGarbageHeapWeight, ProcessorLists.ZOMBIE_SNOWY);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/savanna/zombie/houses"), "rats:garbage_heaps/savanna", RatConfig.zombieVillageGarbageHeapWeight, ProcessorLists.ZOMBIE_SAVANNA);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/taiga/zombie/houses"), "rats:garbage_heaps/taiga", RatConfig.zombieVillageGarbageHeapWeight, ProcessorLists.ZOMBIE_TAIGA);
			addBuildingToPool(templatePoolRegistry, processorListRegistry, Identifier.parse("minecraft:village/desert/zombie/houses"), "rats:garbage_heaps/desert", RatConfig.zombieVillageGarbageHeapWeight, ProcessorLists.ZOMBIE_DESERT);
		}
	}

	//code take from TelepathicGrunt's gist: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
	//1.18.2 version used, and modified, so it works in 1.19.4
	//additions: a StructureProcessorList parameter to allow us to add a processor. (original code always used an empty processor, but some houses actually use processors)
	private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, Identifier poolRL, String nbtPieceRL, int weight, ResourceKey<StructureProcessorList> processor) {
		Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getOrThrow(processor);

		StructureTemplatePool pool = templatePoolRegistry.getValue(poolRL);
		if (pool == null) return;

		SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

		for (int i = 0; i < weight; i++) {
			pool.templates.add(piece);
		}

		List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
		listOfPieceEntries.add(new Pair<>(piece, weight));
		pool.rawTemplates = listOfPieceEntries;
		LOGGER.debug("Rats: Successfully added {} to village pool {}", nbtPieceRL, poolRL.toString());
	}

	public static List<Pair<String, Component>> getCachedMobList(@Nullable Level level) {
		if (level != null && MOB_CACHE.isEmpty()) {
			List<Pair<String, Component>> unsortedCache = new ArrayList<>();
			for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
				try {
					Entity entity = entry.getValue().create(level, EntitySpawnReason.MOB_SUMMONED);
					if (entry.getValue() == EntityType.PLAYER || entity instanceof Mob) {
						unsortedCache.add(Pair.of(entry.getKey().identifier().toString(), entry.getValue().getDescription()));
					}
				} catch (RuntimeException e) {
					RatsMod.LOGGER.error("Couldnt cache an instance of the mob {}", entry.getKey().identifier(), e);
				}
			}
			MOB_CACHE.addAll(unsortedCache.stream().sorted(Comparator.comparing(o -> o.getSecond().getString())).toList());
			LOGGER.debug("Cached {} mob ids for later use.", MOB_CACHE.size());
		}
		return MOB_CACHE;
	}
}
