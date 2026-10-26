package com.github.alexthe666.rats.server.entity.ai.navigation.evaluator;

import net.minecraft.world.level.pathfinder.PathfindingContext;
import com.github.alexthe666.rats.server.block.*;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class RatNodeEvaluator extends WalkNodeEvaluator {

	private boolean ratInCage;

	@Override
	public void prepare(PathNavigationRegion level, Mob mob) {
		super.prepare(level, mob);
		// once per search, not for every block it looks at
		this.ratInCage = mob instanceof TamedRat rat && rat.isInCage();
	}

	// 1.21: the per-block hook of 1.20.1 (evaluateBlockPathType) is gone; the vanilla door and rail rules now run in
	// getPathTypeWithinMobBB, the rat's own rules here
	@Override
	public PathType getPathType(PathfindingContext context, int x, int y, int z) {
		PathType types = super.getPathType(context, x, y, z);
		BlockGetter getter = context.level();
		BlockPos pos = new BlockPos(x, y, z);
		Block block = getter.getBlockState(pos).getBlock();
		if (this.mob instanceof TamedRat) {
			// #907: every tube is a corridor for a tamed rat, as in 1.12 (1.20.1 opened only the tubes with an opening,
			// so a rat outside could not plan a way through a network); getNeighbors keeps it to the tubes' open sides
			if (block instanceof RatHoleBlock || block instanceof RatTrapBlock || block instanceof RatCageBlock || block instanceof RatTubeBlock) {
				types = PathType.WALKABLE;
			}
			// quarry platforms stay blocked (the original made them open): the walk evaluator only tries a step up where
			// the cell ahead is blocked, so with open platforms a quarry rat walked under the staircase, never up it, and
			// stayed in the pit with its blocks
			if (block instanceof SlabBlock) {
				types = PathType.WALKABLE;
			}

			if (this.ratInCage) {
				if (block instanceof RatCageBlock || block instanceof RatTubeBlock) {
					types = PathType.WALKABLE;
				} else {
					types = PathType.BLOCKED;
				}
			}
		}
		return types;
	}

	// #907: into and out of a tube only straight through a side that is open or joined to the next tube, and up and
	// down tube shafts (walking nodes only go sideways, up a step or down a drop)
	@Override
	public int getNeighbors(Node[] outputArray, Node node) {
		int count = super.getNeighbors(outputArray, node);
		if (!(this.mob instanceof TamedRat)) {
			return count;
		}
		BlockPos from = node.asBlockPos();
		BlockState fromState = this.currentContext.getBlockState(from);
		int kept = 0;
		for (int i = 0; i < count; i++) {
			if (this.tubeAllows(from, fromState, outputArray[i])) {
				outputArray[kept++] = outputArray[i];
			}
		}
		if (fromState.getBlock() instanceof RatTubeBlock) {
			for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN}) {
				BlockPos to = from.relative(direction);
				if (passes(fromState, direction) && passes(this.currentContext.getBlockState(to), direction.getOpposite())) {
					Node next = this.getNode(to.getX(), to.getY(), to.getZ());
					next.type = PathType.WALKABLE;
					next.costMalus = Math.max(next.costMalus, this.mob.getPathfindingMalus(PathType.WALKABLE));
					if (this.isNeighborValid(next, node)) {
						outputArray[kept++] = next;
					}
				}
			}
		}
		return kept;
	}

	private boolean tubeAllows(BlockPos from, BlockState fromState, Node next) {
		BlockPos to = next.asBlockPos();
		BlockState toState = this.currentContext.getBlockState(to);
		boolean fromTube = fromState.getBlock() instanceof RatTubeBlock;
		boolean toTube = toState.getBlock() instanceof RatTubeBlock;
		if (!fromTube && !toTube) {
			return true;
		}
		int dx = to.getX() - from.getX();
		int dy = to.getY() - from.getY();
		int dz = to.getZ() - from.getZ();
		if (Math.abs(dx) + Math.abs(dz) != 1 || dy > 0 || dy < 0 && toTube) {
			return false;
		}
		// out of an open end the rat may drop down; in and along tubes it stays level
		Direction direction = dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
		return (!fromTube || passes(fromState, direction)) && (!toTube || passes(toState, direction.getOpposite()));
	}

	private static boolean passes(BlockState state, Direction side) {
		return state.getBlock() instanceof RatTubeBlock && RatTubeBlock.isPassable(state, side);
	}
}
