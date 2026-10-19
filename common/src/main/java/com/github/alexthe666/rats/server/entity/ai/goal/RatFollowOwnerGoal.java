package com.github.alexthe666.rats.server.entity.ai.goal;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.LivingEntity;
import com.github.alexthe666.rats.server.entity.RatMount;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class RatFollowOwnerGoal extends FollowOwnerGoal {
	private final TamedRat rat;
	private final double speedModifier;
	private final float startDist;
	private final float stopDist;
	private int timeToRecalcPath;

	public RatFollowOwnerGoal(TamedRat rat, double speedModifier, float startDist, float stopDist) {
		super(rat, speedModifier, startDist, stopDist);
		this.rat = rat;
		this.speedModifier = speedModifier;
		this.startDist = startDist;
		this.stopDist = stopDist;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	// 1.21's TamableAnimal.unableToMoveToOwner is final and stops any passenger; the original let rats on their own
	// mounts follow (only sitting, riding a player or a leash stop them), so the checks are done here
	private boolean unableToMove() {
		return this.rat.isOrderedToSit() || this.rat.getVehicle() instanceof Player || this.rat.isLeashed();
	}

	@Override
	public boolean canUse() {
		LivingEntity owner = this.rat.getOwner();
		return this.rat.canMove() && this.rat.isFollowing() && owner != null && !owner.isSpectator() && !this.unableToMove()
				&& this.rat.distanceToSqr(owner) >= (double) (this.startDist * this.startDist);
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity owner = this.rat.getOwner();
		return owner != null && !this.rat.getNavigation().isDone() && !this.unableToMove()
				&& this.rat.distanceToSqr(owner) > (double) (this.stopDist * this.stopDist);
	}

	@Override
	public void start() {
		super.start();
		this.rat.getNavigation().stop();
		this.timeToRecalcPath = 0;
	}

	@Override
	public void tick() {
		LivingEntity owner = this.rat.getOwner();
		if (owner == null) {
			return;
		}
		this.rat.getLookControl().setLookAt(owner, 10.0F, (float) this.rat.getMaxHeadXRot());
		if (this.rat.isFollowing()) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = this.adjustedTickDelay(10);
				if (this.rat.distanceToSqr(owner) >= (this.rat.hasFlightUpgrade() ? 800.0D : 400.0D)) {
					if (!this.maybeTeleportMount(owner)) {
						this.rat.tryToTeleportToOwner();
						// the original kept the fall distance of the old position (#847: "fell from a great height")
						this.rat.resetFallDistance();
					}
				} else {
					if (this.rat.hasFlightUpgrade()) {
						this.rat.getNavigation().moveTo(owner.getX(), owner.getY() + 2.5D, owner.getZ(), this.speedModifier);
						this.rat.setFlying(true);
					} else {
						this.rat.getNavigation().moveTo(owner, this.speedModifier);
					}
				}
			}
		}
	}

	private boolean maybeTeleportMount(LivingEntity owner) {
		if (this.rat.getVehicle() instanceof RatMount mount && mount.shouldTeleportWhenFarAway()) {
			BlockPos blockpos = owner.blockPosition();

			for (int i = 0; i < 10; ++i) {
				int j = this.randomIntInclusive(-3, 3);
				int k = this.randomIntInclusive(-1, 1);
				int l = this.randomIntInclusive(-3, 3);
				boolean flag = this.attemptTeleportEntity(owner, this.rat.getVehicle(), blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
				if (flag) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean attemptTeleportEntity(LivingEntity owner, Entity mount, int x, int y, int z) {
		if (Math.abs((double) x - owner.getX()) < 2.0D && Math.abs((double) z - owner.getZ()) < 2.0D) {
			return false;
		} else if (!this.canTeleportTo(mount, new BlockPos(x, y, z))) {
			return false;
		} else {
			mount.moveTo((double) x + 0.5D, y, (double) z + 0.5D, this.rat.getYRot(), this.rat.getXRot());
			mount.resetFallDistance();
			if (mount instanceof Mob mob) mob.getNavigation().stop();
			this.rat.getNavigation().stop();
			return true;
		}
	}

	private int randomIntInclusive(int min, int max) {
		return this.rat.getRandom().nextInt(max - min + 1) + min;
	}

	/** Vanilla's TamableAnimal.canTeleportTo (private in 1.21): solid floor, no leaves, room for the mount */
	private boolean canTeleportTo(Entity mount, BlockPos pos) {
		PathType type = WalkNodeEvaluator.getPathTypeStatic(this.rat, pos);
		if (type != PathType.WALKABLE) {
			return false;
		}
		BlockState below = this.rat.level().getBlockState(pos.below());
		if (below.getBlock() instanceof LeavesBlock) {
			return false;
		}
		BlockPos offset = pos.subtract(mount.blockPosition());
		return this.rat.level().noCollision(mount, mount.getBoundingBox().move(offset));
	}
}
