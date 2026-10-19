package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.world.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

import java.util.function.Supplier;

public class RatlantisFeatureRegistry {
	public static final Registrar<WorldCarver<?>> CARVERS = Registrar.create(Registries.CARVER, RatsMod.MODID);
	public static final Registrar<Feature<?>> FEATURES = Registrar.create(BuiltInRegistries.FEATURE, RatsMod.MODID);
	public static final Registrar<StructureProcessorType<?>> PROCESSORS = Registrar.create(Registries.STRUCTURE_PROCESSOR, RatsMod.MODID);
	public static final Registrar<TrunkPlacerType<?>> TRUNK_PLACERS = Registrar.create(Registries.TRUNK_PLACER_TYPE, RatsMod.MODID);

	public static final RegistryEntry<WorldCarver<?>, WorldCarver<CaveCarverConfiguration>> RATLANTIS_CAVES = CARVERS.register("ratlantis_caves", () -> new RatsCaveCarver(CaveCarverConfiguration.CODEC));

	public static final RegistryEntry<Feature<?>, Feature<RatlantisRuinConfiguration>> RATLANTIS_RUIN = FEATURES.register("ratlantis_ruin", () -> new RatlantisRuinFeature(RatlantisRuinConfiguration.CODEC));
	public static final RegistryEntry<Feature<?>, Feature<NoneFeatureConfiguration>> MARBLE_PILE = FEATURES.register("marble_pile", () -> new MarblePileFeature(NoneFeatureConfiguration.CODEC));

	public static final RegistryEntry<StructureProcessorType<?>, StructureProcessorType<CopyInputStateRuleProcessor>> COPY_STATE = registerProcessor("copy_state", () -> () -> CopyInputStateRuleProcessor.CODEC);
	public static final RegistryEntry<TrunkPlacerType<?>, TrunkPlacerType<ThickBranchingTrunkPlacer>> THICK_BASE_BRANCHING_TRUNK_PLACER = TRUNK_PLACERS.register("thick_branching_trunk_placer", () -> new TrunkPlacerType<>(ThickBranchingTrunkPlacer.CODEC));

	private static <P extends StructureProcessor> RegistryEntry<StructureProcessorType<?>, StructureProcessorType<P>> registerProcessor(String name, Supplier<StructureProcessorType<P>> factory) {
		return PROCESSORS.register(name, factory);
	}
}
