package com.github.alexthe666.rats.server.entity.ai.goal.harvest;

import net.minecraft.world.level.block.FarmlandBlock;

import net.minecraft.world.item.BlockItem;

import com.github.alexthe666.rats.data.tags.CommonTags;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class RatFarmGoal extends BaseRatHarvestGoal {
	private final TamedRat rat;

	public RatFarmGoal(TamedRat rat) {
		super(rat);
		this.rat = rat;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!super.canUse() || !this.checkTheBasics(this.rat.getDepositPos().isPresent(), false) || (!this.holdingSeeds() && !this.holdingBonemeal())) {
			return false;
		}
		this.resetTarget();
		return this.getTargetBlock() != null;
	}

	private boolean holdingSeeds() {
		ItemStack stack = this.rat.getItemInHand(InteractionHand.MAIN_HAND);
		return !stack.isEmpty() && stack.is(CommonTags.SEEDS);
	}

	private boolean holdingBonemeal() {
		ItemStack stack = this.rat.getItemInHand(InteractionHand.MAIN_HAND);
		return !stack.isEmpty() && stack.is(Items.BONE_MEAL);
	}

	@Override
	public boolean canContinueToUse() {
		return this.checkTheBasics(false, false) && this.getTargetBlock() != null && (this.holdingSeeds() || this.holdingBonemeal());
	}

	@Override
	public void tick() {
		if (this.getTargetBlock() != null) {
			if (this.holdingSeeds()) {
				BlockState block = this.rat.level().getBlockState(this.getTargetBlock());
				this.rat.getNavigation().moveTo(this.getTargetBlock().getX() + 0.5D, this.getTargetBlock().getY(), this.getTargetBlock().getZ() + 0.5D, 1.25D);
				if (this.canPlantAt(this.getTargetBlock(), block)) {
					double distance = this.rat.getRatDistanceCenterSq(this.getTargetBlock().getX(), this.getTargetBlock().getY(), this.getTargetBlock().getZ());
					if (distance < this.rat.getRatHarvestDistance(0.0F)) {
						// any seed that places a block (modded seeds are often plain BlockItems); the seed is only used up
						// when the plant is placed (the original took it even when nothing was planted)
						if (this.rat.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem item) {
							this.rat.level().setBlockAndUpdate(this.getTargetBlock().above(), item.getBlock().defaultBlockState());
							this.rat.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
						}
						this.stop();
					}
				} else {
					this.stop();
				}
			} else if (this.holdingBonemeal()) {
				BlockState block = this.rat.level().getBlockState(this.getTargetBlock());
				this.rat.getNavigation().moveTo(this.getTargetBlock().getX() + 0.5D, this.getTargetBlock().getY(), this.getTargetBlock().getZ() + 0.5D, 1.25D);
				if (this.canPlantBeBonemealed(this.getTargetBlock(), block)) {
					double distance = this.rat.getRatDistanceCenterSq(this.getTargetBlock().getX(), this.getTargetBlock().getY(), this.getTargetBlock().getZ());
					if (distance < 4.5F) {
						if (this.holdingBonemeal()) {
							this.rat.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
							if (block.getBlock() instanceof BonemealableBlock bonemealable) {
								if (bonemealable.isValidBonemealTarget(this.rat.level(), this.getTargetBlock(), block)) {
									if (!this.rat.level().isClientSide()) {
										this.rat.level().levelEvent(2005, this.getTargetBlock(), 0);
										this.rat.level().playSound(null, this.getTargetBlock(), SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS);
										bonemealable.performBonemeal((ServerLevel) this.rat.level(), this.rat.level().getRandom(), this.getTargetBlock(), block);
									}
								}
							}
						}
						this.stop();
					}
				} else {
					this.stop();
				}
			}
		}
	}

	private void resetTarget() {
		if (this.holdingBonemeal()) {
			this.findTarget((pos, state) -> this.canPlantBeBonemealed(pos, state) && !RatUtils.isBlockProtected(this.rat.level(), pos, this.rat));
		} else if (this.holdingSeeds()) {
			this.findTarget((pos, state) -> this.canPlantAt(pos, state) && !RatUtils.isBlockProtected(this.rat.level(), pos, this.rat));
		}
	}

	/**
	 * #902 / #920: the original planted only on wet farmland (Forge's isFertile), so a farm without water nearby got
	 * nothing. Now any farmland where the held seed's plant can stay.
	 */
	private boolean canPlantAt(BlockPos soil, BlockState soilState) {
		if (!(soilState.getBlock() instanceof FarmlandBlock) || !this.rat.level().isEmptyBlock(soil.above())) {
			return false;
		}
		return this.rat.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem item
			&& item.getBlock().defaultBlockState().canSurvive(this.rat.level(), soil.above());
	}

	private boolean canPlantBeBonemealed(BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof BonemealableBlock bonemealable && state.is(BlockTags.BEE_GROWABLES)) {
			if (bonemealable.isValidBonemealTarget(this.rat.level(), pos, state)) {
				return bonemealable.isBonemealSuccess(this.rat.level(), this.rat.level().getRandom(), pos, state);
			}
		}
		return false;
	}
}
