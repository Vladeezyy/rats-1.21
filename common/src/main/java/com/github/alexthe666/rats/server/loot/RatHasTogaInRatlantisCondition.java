package com.github.alexthe666.rats.server.loot;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RatHasTogaInRatlantisCondition implements LootItemCondition {

	public static final MapCodec<RatHasTogaInRatlantisCondition> CODEC = MapCodec.unit(RatHasTogaInRatlantisCondition::new);


	@Override
	public boolean test(LootContext context) {
		if (!context.hasParameter(LootContextParams.THIS_ENTITY)) return false;
		return context.getOptional(LootContextParams.THIS_ENTITY) instanceof Rat rat && rat.hasToga() && rat.level().dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY);
	}

	@Override
	public MapCodec<RatHasTogaInRatlantisCondition> codec() {
		return CODEC;
	}
}
