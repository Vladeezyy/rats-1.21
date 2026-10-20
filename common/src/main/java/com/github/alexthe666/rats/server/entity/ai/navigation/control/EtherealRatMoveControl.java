package com.github.alexthe666.rats.server.entity.ai.navigation.control;

import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class EtherealRatMoveControl extends RatMoveControl {

	public EtherealRatMoveControl(AbstractRat rat) {
		super(rat);
	}

	// #871: the rat phases through blocks now, so nothing stops it but this: steer the motion toward the wanted point
	// at the rat's speed, slowing down near it (pushing the full speed every tick, without collisions, overshot), and
	// stop pushing when nothing asks for a move any more (the navigation sets the wanted point every tick)
	public void tick() {
		if (this.operation == Operation.MOVE_TO) {
			this.operation = Operation.WAIT;
			Vec3 offset = new Vec3(this.wantedX - this.rat.getX(), this.wantedY - this.rat.getY(), this.wantedZ - this.rat.getZ());
			double distance = offset.length();
			if (distance < 0.05D) {
				this.rat.setDeltaMovement(this.rat.getDeltaMovement().scale(0.5D));
				return;
			}
			double speed = this.speedModifier * this.rat.getAttributeValue(Attributes.MOVEMENT_SPEED);
			Vec3 wanted = offset.scale(Math.min(speed, distance) / distance);
			Vec3 motion = this.rat.getDeltaMovement();
			this.rat.setDeltaMovement(motion.add(wanted.subtract(motion).scale(0.25D)));

			if (distance > this.rat.getBbWidth()) {
				LivingEntity target = this.rat.getTarget();
				Vec3 look = target != null ? target.position().subtract(this.rat.position()) : this.rat.getDeltaMovement();
				this.rat.setYRot(-((float) Mth.atan2(look.x, look.z)) * Mth.RAD_TO_DEG);
				this.rat.yBodyRot = this.rat.getYRot();
			}
		} else if (this.operation == Operation.STRAFE || this.operation == Operation.JUMPING) {
			this.operation = Operation.WAIT;
		}
	}
}
