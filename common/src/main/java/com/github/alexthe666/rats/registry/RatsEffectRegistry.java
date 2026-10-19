package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.effect.ConfitByaldiMobEffect;
import com.github.alexthe666.rats.server.effect.PlagueMobEffect;
import net.minecraft.world.effect.MobEffect;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsEffectRegistry {

	public static final Registrar<MobEffect> MOB_EFFECTS = Registrar.create(BuiltInRegistries.MOB_EFFECT, RatsMod.MODID);

	public static final RegistryEntry<MobEffect, MobEffect> SYNESTHESIA = MOB_EFFECTS.register("synesthesia", ConfitByaldiMobEffect::new);
	public static final RegistryEntry<MobEffect, MobEffect> PLAGUE = MOB_EFFECTS.register("plague", PlagueMobEffect::new);
}
