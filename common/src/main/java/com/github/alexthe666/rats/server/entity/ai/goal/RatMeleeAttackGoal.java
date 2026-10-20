package com.github.alexthe666.rats.server.entity.ai.goal;

import com.github.alexthe666.rats.server.entity.ai.navigation.navigation.RatFlightNavigation;
import dev.vladeezyy.rampart.animation.IAnimatedEntity;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

// 1.21: the reach of 1.20.1 (getAttackReachSqr, wider on a mount) is vanilla's isWithinMeleeAttackRange now, which
// already counts the vehicle's box
public class RatMeleeAttackGoal extends MeleeAttackGoal implements RatWorkGoal {
	private final TamedRat rat;

	public RatMeleeAttackGoal(TamedRat rat, double speed, boolean memory) {
		super(rat, speed, memory);
		this.rat = rat;
	}

	public boolean canUse() {
		if (this.rat.isInCage()) {
			return false;
		}
		// #822: a flying rat in the air gets no path (its navigation only paths from the ground), so the vanilla check
		// let it attack only once it was in reach already, and bee or winged rats hovered by their owner instead; it
		// flies straight at the target (its navigation needs no path for that)
		if (this.rat.getNavigation() instanceof RatFlightNavigation) {
			LivingEntity target = this.rat.getTarget();
			return target != null && target.isAlive() && this.rat.isWithinRestriction(target.blockPosition());
		}
		return super.canUse();
	}

	@Override
	public void tick() {
		this.rat.setAnimation(IAnimatedEntity.NO_ANIMATION);
		super.tick();
	}

	@Override
	public TaskType getRatTaskType() {
		return TaskType.ATTACK;
	}
}
