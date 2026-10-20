package com.github.alexthe666.rats.server.entity.ai.navigation.navigation;

import com.github.alexthe666.rats.server.entity.ai.navigation.evaluator.RatNodeEvaluator;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import java.util.List;
import java.util.Set;

public class EtherealRatNavigation extends FlyingPathNavigation {

	public final AbstractRat rat;

	public EtherealRatNavigation(AbstractRat entity, Level level) {
		super(entity, level);
		this.rat = entity;
	}

	protected PathFinder createPathFinder(int nodes) {
		this.nodeEvaluator = new RatNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		this.nodeEvaluator.setCanFloat(true);
		// #871: an ethereal rat phases through blocks (as in 1.15, where it had noClip), so its path is the straight
		// line to the nearest target: no search around walls, which also spares the server the pathfinding
		return new PathFinder(this.nodeEvaluator, nodes) {
			@Override
			public Path findPath(PathNavigationRegion region, Mob mob, Set<BlockPos> targets, float maxRange, int accuracy, float searchDepthMultiplier) {
				BlockPos nearest = null;
				double nearestDistance = Double.MAX_VALUE;
				for (BlockPos target : targets) {
					double distance = target.distToCenterSqr(mob.position());
					if (distance < nearestDistance) {
						nearest = target;
						nearestDistance = distance;
					}
				}
				return nearest == null ? null : new Path(List.of(new Node(nearest.getX(), nearest.getY(), nearest.getZ())), nearest, true);
			}
		};
	}

	@Override
	public boolean moveTo(Entity entity, double speed) {
		this.rat.getMoveControl().setWantedPosition(entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), speed);
		Path path = this.createPath(entity, 0);
		return path != null && this.moveTo(path, speed);
	}

	@Override
	public boolean moveTo(double x, double y, double z, double speed) {
		this.rat.getMoveControl().setWantedPosition(x, y, z, speed);
		return this.moveTo(this.createPath(BlockPos.containing(x, y, z), 0), speed);
	}

	// 26.3: goals call the overloads with a reach range (MeleeAttackGoal: moveTo(target, 0, speed))
	@Override
	public boolean moveTo(Entity entity, int reachRange, double speed) {
		return this.moveTo(entity, speed);
	}

	@Override
	public boolean moveTo(double x, double y, double z, int reachRange, double speed) {
		return this.moveTo(x, y, z, speed);
	}
}
