package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/** 26.3: features are records with their own codec (was Feature<NoneFeatureConfiguration>) */
public record MarblePileFeature() implements Feature {
	public static final MapCodec<MarblePileFeature> CODEC = MapCodec.unit(MarblePileFeature::new);

	private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH};

	@Override
	public MapCodec<MarblePileFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		for (int i = 0; i < 4 + random.nextInt(6); ++i) {
			BlockPos blockpos = origin.offset(random.nextInt(15), 0, random.nextInt(15));
			blockpos = new BlockPos(blockpos.getX(), level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(), blockpos.getZ()), blockpos.getZ());
			if (level.getBlockState(blockpos.below()).canOcclude()) {
				if (random.nextInt(3) != 0) {
					if (random.nextInt(2) == 0) {
						Direction randFacing = HORIZONTALS[random.nextInt(HORIZONTALS.length - 1)];
						Direction.Axis axis = randFacing.getAxis();
						level.setBlock(blockpos, RatlantisBlockRegistry.MARBLED_CHEESE_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), 2);
						level.setBlock(blockpos.relative(randFacing), RatlantisBlockRegistry.MARBLED_CHEESE_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), 2);
					} else {
						level.setBlock(blockpos, RatlantisBlockRegistry.MARBLED_CHEESE_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.values()[random.nextInt(Direction.Axis.values().length - 1)]), 2);
					}
				} else {
					BlockPos height = blockpos.immutable();
					int h = 0;
					level.setBlock(height.below(), RatlantisBlockRegistry.MARBLED_CHEESE_TILE.get().defaultBlockState(), 2);

					while (h < 3 + random.nextInt(3)) {
						level.setBlock(height.above(h), RatlantisBlockRegistry.MARBLED_CHEESE_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
						++h;
					}

					level.setBlock(height.above(h), RatlantisBlockRegistry.MARBLED_CHEESE_CHISELED.get().defaultBlockState(), 2);
					level.setBlock(height.above(h + 1), RatlantisBlockRegistry.MARBLED_CHEESE_TILE.get().defaultBlockState(), 2);

					for (Direction facing : HORIZONTALS) {
						BlockPos stairPos = height.above(h + 1).relative(facing);
						BlockState stairState = RatlantisBlockRegistry.MARBLED_CHEESE_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, facing.getOpposite()).setValue(StairBlock.HALF, Half.TOP);
						FluidState fluidState = level.getFluidState(stairPos);
						stairState = stairState.setValue(StairBlock.WATERLOGGED, fluidState.is(Fluids.WATER));
						level.setBlock(stairPos, stairState, 2);
					}
				}
			}
		}
		return true;
	}
}
