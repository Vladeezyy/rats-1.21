package com.github.alexthe666.rats.registry.worldgen;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;


public class RatlantisStructureRegistry {

	public static final ResourceKey<StructureTemplatePool> BARON_RUNWAY_START = registerPoolKey("baron_runway");
	public static final ResourceKey<StructureTemplatePool> DUTCHRAT_SHIP_START = registerPoolKey("dutchrat_ship");

	public static final ResourceKey<Structure> BARON_RUNWAY = registerStructureKey("baron_runway");
	public static final ResourceKey<Structure> DUTCHRAT_SHIP = registerStructureKey("dutchrat_ship");

	public static final ResourceKey<StructureSet> BARON_RUNWAY_SET = registerSetKey("baron_runway");
	public static final ResourceKey<StructureSet> DUTCHRAT_SHIP_SET = registerSetKey("dutchrat_ship");

	public static final ResourceKey<StructureProcessorList> RUIN_RUINS = registerProcessorKey("ruin_ruins");

	private static ResourceKey<Structure> registerStructureKey(String name) {
		return ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

	private static ResourceKey<StructureTemplatePool> registerPoolKey(String name) {
		return ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

	private static ResourceKey<StructureSet> registerSetKey(String name) {
		return ResourceKey.create(Registries.STRUCTURE_SET, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}

	private static ResourceKey<StructureProcessorList> registerProcessorKey(String name) {
		return ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(RatsMod.MODID, name));
	}
}
