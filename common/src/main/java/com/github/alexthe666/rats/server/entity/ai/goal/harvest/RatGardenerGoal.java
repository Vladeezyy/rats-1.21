package com.github.alexthe666.rats.server.entity.ai.goal.harvest;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RatGardenerGoal extends BaseRatHarvestGoal {
	public RatGardenerGoal(TamedRat rat) {
		super(rat);
	}

	@Override
	public boolean canUse() {
		if (!super.canUse() || !this.checkTheBasics(this.rat.getDepositPos().isPresent(), this.rat.getDepositPos().isPresent())) {
			return false;
		}
		this.resetTarget();
		return this.getTargetBlock() != null;
	}

	private void resetTarget() {
		this.findTarget((pos, state) -> (state.canBeReplaced() || state.is(BlockTags.FLOWERS)) && RatUtils.canRatBreakBlock(this.rat.level(), pos, this.rat));
	}

	@Override
	public boolean canContinueToUse() {
		return this.checkTheBasics(false, false) && this.getTargetBlock() != null && this.rat.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
	}

	@Override
	public void tick() {
		if (this.getTargetBlock() != null) {
			this.rat.getNavigation().moveTo(this.getTargetBlock().getX() + 0.5D, this.getTargetBlock().getY(), this.getTargetBlock().getZ() + 0.5D, 1.25D);
			double distance = this.rat.getRatDistanceCenterSq(this.getTargetBlock().getX(), this.getTargetBlock().getY(), this.getTargetBlock().getZ());
			if (distance < this.rat.getRatHarvestDistance(0.0D)) {
				this.rat.level().destroyBlock(this.getTargetBlock(), true);
				this.stop();
			}
		}
	}
}
