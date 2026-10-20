package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.advancements.BlackDeathSummonedTrigger;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import dev.vladeezyy.rats.platform.RegistryEntry;
import dev.vladeezyy.rats.platform.Registrar;

/** Advancement triggers (1.21: a registry) */
public class RatsAdvancementsRegistry {
	public static final Registrar<CriterionTrigger<?>> TRIGGERS = Registrar.create(Registries.TRIGGER_TYPE, RatsMod.MODID);
	public static final RegistryEntry<CriterionTrigger<?>, BlackDeathSummonedTrigger> BLACK_DEATH_SUMMONED = TRIGGERS.register("black_death_summoned", BlackDeathSummonedTrigger::new);

	public static void init() {
	}
}
