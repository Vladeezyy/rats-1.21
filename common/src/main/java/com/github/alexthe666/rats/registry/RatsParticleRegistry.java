package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsParticleRegistry {
	public static final Registrar<ParticleType<?>> PARTICLES = Registrar.create(BuiltInRegistries.PARTICLE_TYPE, RatsMod.MODID);

	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> BLACK_DEATH = PARTICLES.register("black_death", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> DUTCHRAT_SMOKE = PARTICLES.register("dutchrat_smoke", () -> new SimpleParticleType(true));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> FLEA = PARTICLES.register("flea", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> FLY = PARTICLES.register("fly", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> LIGHTNING = PARTICLES.register("rat_lightning", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> MILK_BUBBLE = PARTICLES.register("milk_bubble", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PIRAT_GHOST = PARTICLES.register("pirat_ghost", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> RAT_GHOST = PARTICLES.register("rat_ghost", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> RAT_KING_SMOKE = PARTICLES.register("rat_king_smoke", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> RUNNING_RAT = PARTICLES.register("running_rat", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> SALIVA = PARTICLES.register("saliva", () -> new SimpleParticleType(false));
	public static final RegistryEntry<ParticleType<?>, SimpleParticleType> UPGRADE_COMBINER = PARTICLES.register("upgrade_combiner", () -> new SimpleParticleType(false));
}
