package com.github.alexthe666.rats.server.loot;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RatlantisLoadedLootCondition implements LootItemCondition {

	public static final MapCodec<RatlantisLoadedLootCondition> CODEC = MapCodec.unit(RatlantisLoadedLootCondition::new);


	@Override
	public boolean test(LootContext context) {
		return RatsMod.RATLANTIS_DATAPACK_ENABLED;
	}

	@Override
	public MapCodec<RatlantisLoadedLootCondition> codec() {
		return CODEC;
	}
}
