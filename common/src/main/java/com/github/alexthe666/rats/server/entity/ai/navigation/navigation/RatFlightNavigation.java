package com.github.alexthe666.rats.server.entity.ai.navigation.navigation;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

public class RatFlightNavigation extends GroundPathNavigation {

	public final TamedRat rat;

	public RatFlightNavigation(TamedRat rat, Level level) {
		super(rat, level);
		this.rat = rat;
	}

	public void tick() {
		this.tick++;
	}

	public boolean moveTo(double x, double y, double z, double speed) {
		this.rat.getMoveControl().setWantedPosition(x, y, z, speed);
		return true;
	}

	public boolean moveTo(Entity entity, double speed) {
		this.rat.getMoveControl().setWantedPosition(entity.getX(), entity.getY(), entity.getZ(), speed);
		return true;
	}

	// 26.3: goals call the overloads with a reach range (MeleeAttackGoal: moveTo(target, 0, speed)); flying, a rat
	// goes straight to the spot, as through the overloads above
	@Override
	public boolean moveTo(Entity entity, int reachRange, double speed) {
		return this.moveTo(entity, speed);
	}

	@Override
	public boolean moveTo(double x, double y, double z, int reachRange, double speed) {
		return this.moveTo(x, y, z, speed);
	}
}
