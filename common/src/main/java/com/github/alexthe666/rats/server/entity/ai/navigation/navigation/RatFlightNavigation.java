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
		this.takeOffFor(y);
		this.rat.getMoveControl().setWantedPosition(x, y, z, speed);
		return true;
	}

	public boolean moveTo(Entity entity, double speed) {
		this.takeOffFor(entity.getY());
		this.rat.getMoveControl().setWantedPosition(entity.getX(), entity.getY(), entity.getZ(), speed);
		return true;
	}

	// #922: a working rat only flew when it followed its owner or wandered; on the ground, with gravity, it pushed
	// against the wall under a chest it could not jump to. A goal more than a block up makes it take off
	private void takeOffFor(double y) {
		if (!this.rat.isFlying() && !this.rat.isOrderedToSit() && y > this.rat.getY() + 1.1D) {
			this.rat.setFlying(true);
		}
	}
}
