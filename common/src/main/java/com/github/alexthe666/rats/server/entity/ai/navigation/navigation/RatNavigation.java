package com.github.alexthe666.rats.server.entity.ai.navigation.navigation;

import com.github.alexthe666.rats.server.entity.ai.navigation.evaluator.RatNodeEvaluator;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class RatNavigation extends GroundPathNavigation {
	public RatNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int nodes) {
		this.nodeEvaluator = new RatNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		this.nodeEvaluator.setCanOpenDoors(true);
		this.nodeEvaluator.setCanFloat(true);
		return new PathFinder(this.nodeEvaluator, nodes);
	}

	// #907: in a tube shaft the rat climbs with no ground under it; without this every moveTo of its goals there found
	// no path and dropped the one it was following
	@Override
	protected boolean canUpdatePath() {
		return super.canUpdatePath() || this.mob instanceof TamedRat rat && rat.isInTube();
	}

	// #844: a rat with a work area (radius staff) paths across all of it. Paths end at the follow range (12 blocks from
	// the rat), so a lumberjack only went for the trees close to it and left the drops further away to despawn; the
	// node budget grows with the range
	@Nullable
	@Override
	protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy) {
		float followRange = (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
		float range = followRange;
		if (this.mob instanceof TamedRat rat && rat.getRadiusCenter().isPresent()) {
			range = Math.max(followRange, (float) Math.sqrt(rat.blockPosition().distSqr(rat.getSearchCenter())) + rat.getRadius() + 2.0F);
		}
		this.setMaxVisitedNodesMultiplier(range / followRange);
		return this.createPath(targets, regionOffset, offsetUpward, accuracy, range);
	}
}
