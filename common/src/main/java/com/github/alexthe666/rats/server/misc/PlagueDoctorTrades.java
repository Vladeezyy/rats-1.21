package com.github.alexthe666.rats.server.misc;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;

/**
 * The plague doctor's trades, data in 26.x (data/rats/trade_set/plague_doctor/*.json, from tools/port/gen_trades_26.py):
 * five common ones, three different rare ones, and the Ratlantis machines when the Ratlantis pack is off.
 */
public final class PlagueDoctorTrades {
	public static final ResourceKey<TradeSet> COMMON = key("common");
	public static final ResourceKey<TradeSet> RARE = key("rare");
	public static final ResourceKey<TradeSet> NO_RATLANTIS = key("no_ratlantis");

	private PlagueDoctorTrades() {}

	private static ResourceKey<TradeSet> key(String name) {
		return ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(RatsMod.MODID, "plague_doctor/" + name));
	}
}
