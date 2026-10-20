package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsVillagerRegistry {

	public static final Registrar<PoiType> POIS = Registrar.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, RatsMod.MODID);
	public static final Registrar<VillagerProfession> PROFESSIONS = Registrar.create(BuiltInRegistries.VILLAGER_PROFESSION, RatsMod.MODID);

	public static final RegistryEntry<PoiType, PoiType> RATLANTIS_PORTAL = POIS.register("ratlantis_portal", () -> new PoiType(ImmutableSet.copyOf(RatlantisBlockRegistry.RATLANTIS_PORTAL.get().getStateDefinition().getPossibleStates()), 1, 1));
	public static final RegistryEntry<PoiType, PoiType> TRASH_CAN = POIS.register("trash_can", () -> new PoiType(ImmutableSet.copyOf(RatsBlockRegistry.TRASH_CAN.get().getStateDefinition().getPossibleStates()), 1, 1));
	public static final RegistryEntry<VillagerProfession, VillagerProfession> PET_SHOP_OWNER = PROFESSIONS.register("pet_shop_owner", () -> new VillagerProfession(net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.rats.pet_shop_owner"), (poiType) -> RatConfig.villagePetShops && poiType.is(TRASH_CAN.getKey()), (poiType) -> RatConfig.villagePetShops && poiType.is(TRASH_CAN.getKey()), ImmutableSet.of(), ImmutableSet.of(), RatsSoundRegistry.TRASH_CAN.get(), petShopTrades()));

	// 26.x: the trades are data (data/rats/trade_set/pet_shop_owner/level_n.json, from tools/port/gen_trades_26.py)
	private static it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraft.resources.ResourceKey<net.minecraft.world.item.trading.TradeSet>> petShopTrades() {
		it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraft.resources.ResourceKey<net.minecraft.world.item.trading.TradeSet>> trades = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
		for (int level = 1; level <= 5; level++) {
			trades.put(level, net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.TRADE_SET, net.minecraft.resources.Identifier.fromNamespaceAndPath(com.github.alexthe666.rats.RatsMod.MODID, "pet_shop_owner/level_" + level)));
		}
		return trades;
	}
}
