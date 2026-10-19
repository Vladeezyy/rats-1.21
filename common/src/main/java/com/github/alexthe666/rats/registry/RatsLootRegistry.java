package com.github.alexthe666.rats.registry;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.loot.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsLootRegistry {

	public static final ResourceKey<LootTable> CHRISTMAS_GIFTS = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "christmas_rat_gifts"));
	public static final ResourceKey<LootTable> PET_SHOP_HOTV = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/hero_of_the_village/pet_shop_owner"));
	public static final ResourceKey<LootTable> RATLANTIS_RAT_EXCLUSIVE_DROPS = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/ratlantis_exclusive_rat_loot"));

	public static final ResourceKey<LootTable> RATLANTIS_FISH = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/fishing/ratlantis"));
	public static final ResourceKey<LootTable> RATLANTIS_FISHING_JUNK = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/fishing/ratlantis_junk"));
	public static final ResourceKey<LootTable> RATLANTIS_FISHING_TREASURE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/fishing/ratlantis_treasure"));
	public static final ResourceKey<LootTable> RATLANTIS_FISHING_FISH = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "gameplay/fishing/ratlantis_fish"));


	public static final Registrar<LootItemConditionType> CONDITIONS = Registrar.create(Registries.LOOT_CONDITION_TYPE, RatsMod.MODID);

	public static final RegistryEntry<LootItemConditionType, LootItemConditionType> RATLANTIS_LOADED = CONDITIONS.register("ratlantis_loaded", () -> new LootItemConditionType(RatlantisLoadedLootCondition.CODEC));
	public static final RegistryEntry<LootItemConditionType, LootItemConditionType> KILLER_HAS_UPGRADE = CONDITIONS.register("killer_has_upgrade", () -> new LootItemConditionType(RatKilledAndHasUpgradeCondition.CODEC));
	public static final RegistryEntry<LootItemConditionType, LootItemConditionType> RAT_HAS_PLAGUE = CONDITIONS.register("rat_has_plague", () -> new LootItemConditionType(RatHasPlagueCondition.CODEC));
	public static final RegistryEntry<LootItemConditionType, LootItemConditionType> HAS_TOGA_AND_IN_RATLANTIS = CONDITIONS.register("has_toga_and_in_ratlantis", () -> new LootItemConditionType(RatHasTogaInRatlantisCondition.CODEC));
}
