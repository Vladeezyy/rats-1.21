package com.github.alexthe666.rats.server.loot;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.server.entity.mount.RatMountBase;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.core.registries.BuiltInRegistries;

public record RatKilledAndHasUpgradeCondition(Item upgrade) implements LootItemCondition {

	public static final MapCodec<RatKilledAndHasUpgradeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("upgrade").forGetter(RatKilledAndHasUpgradeCondition::upgrade)
	).apply(instance, RatKilledAndHasUpgradeCondition::new));

	@Override
	public MapCodec<? extends LootItemCondition> codec() {
		return CODEC;
	}

	@Override
	public boolean test(LootContext context) {
		if (!context.hasParameter(LootContextParams.ATTACKING_ENTITY)) return false;
		if (context.getParameter(LootContextParams.ATTACKING_ENTITY) instanceof RatMountBase base) {
			return base.getRat() != null && RatUpgradeUtils.hasUpgrade(base.getRat(), this.upgrade());
		}
		return context.getParameter(LootContextParams.ATTACKING_ENTITY) instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, this.upgrade());
	}

	public static LootItemCondition.Builder hasUpgrade(Item upgrade) {
		return () -> new RatKilledAndHasUpgradeCondition(upgrade);
	}
}
