package com.github.alexthe666.rats.server.loot;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RatHasPlagueCondition implements LootItemCondition {

	public static final MapCodec<RatHasPlagueCondition> CODEC = MapCodec.unit(RatHasPlagueCondition::new);

	@Override
	public boolean test(LootContext context) {
		if (!context.hasParameter(LootContextParams.THIS_ENTITY)) return false;
		return context.getOptional(LootContextParams.THIS_ENTITY) instanceof Rat rat && rat.hasPlague();
	}

	public static LootItemCondition.Builder hasPlague() {
		return RatHasPlagueCondition::new;
	}

	@Override
	public MapCodec<RatHasPlagueCondition> codec() {
		return CODEC;
	}
}
