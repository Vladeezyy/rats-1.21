package com.github.alexthe666.rats.server.entity.ai.navigation.evaluator;

import net.minecraft.world.level.pathfinder.PathfindingContext;
import com.github.alexthe666.rats.server.block.*;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class RatNodeEvaluator extends WalkNodeEvaluator {

	// 1.21: the per-block hook of 1.20.1 (evaluateBlockPathType) is gone; the vanilla door and rail rules now run in
	// getPathTypeWithinMobBB, the rat's own rules here
	@Override
	public PathType getPathType(PathfindingContext context, int x, int y, int z) {
		PathType types = super.getPathType(context, x, y, z);
		BlockGetter getter = context.level();
		BlockPos pos = new BlockPos(x, y, z);
		Block block = getter.getBlockState(pos).getBlock();
		if (this.mob instanceof TamedRat rat) {
			if (block instanceof RatHoleBlock || block instanceof RatTrapBlock || block instanceof RatCageBlock || RatUtils.isOpenRatTube(getter, pos)) {
				types = PathType.WALKABLE;
			}
			if (block instanceof RatQuarryPlatformBlock) {
				types = PathType.OPEN;
			}
			if (block instanceof SlabBlock) {
				types = PathType.WALKABLE;
			}

			if (rat.isInCage()) {
				if (block instanceof RatCageBlock || block instanceof RatTubeBlock) {
					types = PathType.WALKABLE;
				} else {
					types = PathType.BLOCKED;
				}
			}
		}
		return types;
	}
}