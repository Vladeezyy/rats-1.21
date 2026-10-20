package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.world.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import dev.vladeezyy.rats.platform.Registrar;
import dev.vladeezyy.rats.platform.RegistryEntry;


public class RatlantisFeatureRegistry {
	// 26.3: feature types are the features' codecs (the carvers are data, the Ratlantis caves are a minecraft:cave carver)
	public static final Registrar<MapCodec<? extends Feature>> FEATURES = Registrar.create(Registries.FEATURE_TYPE, RatsMod.MODID);
	public static final Registrar<MapCodec<? extends StructureProcessor>> PROCESSORS = Registrar.create(Registries.STRUCTURE_PROCESSOR, RatsMod.MODID);
	public static final Registrar<TrunkPlacerType<?>> TRUNK_PLACERS = Registrar.create(Registries.TRUNK_PLACER_TYPE, RatsMod.MODID);

	public static final RegistryEntry<MapCodec<? extends Feature>, MapCodec<RatlantisRuinFeature>> RATLANTIS_RUIN = FEATURES.register("ratlantis_ruin", () -> RatlantisRuinFeature.CODEC);
	public static final RegistryEntry<MapCodec<? extends Feature>, MapCodec<MarblePileFeature>> MARBLE_PILE = FEATURES.register("marble_pile", () -> MarblePileFeature.CODEC);

	public static final RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<CopyInputStateRuleProcessor>> COPY_STATE = PROCESSORS.register("copy_state", () -> CopyInputStateRuleProcessor.CODEC);
	public static final RegistryEntry<TrunkPlacerType<?>, TrunkPlacerType<ThickBranchingTrunkPlacer>> THICK_BASE_BRANCHING_TRUNK_PLACER = TRUNK_PLACERS.register("thick_branching_trunk_placer", () -> new TrunkPlacerType<>(ThickBranchingTrunkPlacer.CODEC));

}
