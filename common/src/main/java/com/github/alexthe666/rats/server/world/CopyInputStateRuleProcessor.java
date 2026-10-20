package com.github.alexthe666.rats.server.world;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.registry.worldgen.RatlantisFeatureRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.List;

public class CopyInputStateRuleProcessor implements StructureProcessor {

	public static final MapCodec<CopyInputStateRuleProcessor> CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(CopyInputStateRuleProcessor::new, config -> config.rules);

	private final ImmutableList<ProcessorRule> rules;

	public CopyInputStateRuleProcessor(List<? extends ProcessorRule> list) {
		this.rules = ImmutableList.copyOf(list);
	}

	@Override
	public StructureTemplate.StructureBlockInfo processBlock(LevelReader reader, BlockPos targetPos, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo newInfo, StructurePlaceSettings settings) {
		RandomSource randomsource = RandomSource.create(Mth.getSeed(newInfo.pos()));

		for (ProcessorRule processorrule : this.rules) {
			// 26.2: the rule reads the world state itself; the template's own state is the processed info's (no earlier processor changes it)
			if (processorrule.test(reader, newInfo.state(), templateRelativePos, newInfo.pos(), referencePos, randomsource)) {
				return new StructureTemplate.StructureBlockInfo(newInfo.pos(), processorrule.getOutputState().getBlock().withPropertiesOf(newInfo.state()), processorrule.getOutputTag(randomsource, newInfo.nbt()));
			}
		}

		return newInfo;
	}

	@Override
	public MapCodec<CopyInputStateRuleProcessor> codec() {
		return CODEC;
	}
}
