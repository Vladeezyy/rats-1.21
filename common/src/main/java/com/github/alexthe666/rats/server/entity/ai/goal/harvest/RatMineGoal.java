package com.github.alexthe666.rats.server.entity.ai.goal.harvest;

import com.github.alexthe666.rats.data.tags.CommonTags;
import net.minecraft.world.level.Level;
import com.github.alexthe666.rats.server.misc.StoredItems;
public class RatMineGoal {
}/* extends BaseRatHarvestGoal {
	private final TamedRat rat;
	private int breakingTime;
	private int previousBreakProgress;
	private BlockState prevMiningState = null;

	public RatMineGoal(TamedRat rat) {
		super(rat);
		this.rat = rat;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!super.canUse() || !this.checkTheBasics(false, this.rat.getDepositPos().isPresent())) {
			return false;
		}
		this.resetTarget();
		return this.getTargetBlock() != null;
	}

	private void resetTarget() {
		// the upgrades once per search and the block checks first; the protection check (the loader's event) only for
		// the blocks the rat wants (the original asked it, and made a stack, for every block of the area)
		NonNullList<ItemStack> list = RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get()) ? this.getMiningList() : null;
		NonNullList<ItemStack> mining = list != null && !list.isEmpty() ? list : null;
		boolean ores = RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER_ORE.get());
		if (mining == null && !ores) {
			return;
		}
		this.findTarget((pos, state) -> !state.isAir() && this.isWanted(this.rat.level(), mining, ores, pos, state) && !RatUtils.isBlockProtected(this.rat.level(), pos, this.rat));
	}

	@Nullable
	private NonNullList<ItemStack> getMiningList() {
		if (RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get())) {
			return StoredItems.view(RatUpgradeUtils.getUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get()), 27);
		} else {
			return null;
		}
	}

	private boolean isWanted(Level level, @Nullable NonNullList<ItemStack> list, boolean ores, BlockPos pos, BlockState state) {
		if (list != null) {
			ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
			for (ItemStack currentStack : list) {
				if (currentStack.sameItem(stack)) {
					return true;
				}
			}
		}
		return ores && state.is(CommonTags.ORE_BLOCKS);
	}


	@Override
	public boolean canContinueToUse() {
		return this.checkTheBasics(false, true) && this.getTargetBlock() != null;
	}

	@Override
	public void tick() {
		if (this.getTargetBlock() != null) {
			BlockPos rayPos = RatPathingHelper.clipWithConditions(this.rat.level(), new ClipContext(this.rat.position(), Vec3.atCenterOf(this.getTargetBlock()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.rat), false).getBlockPos();
			this.rat.getNavigation().moveTo(rayPos.getX() + 0.5D, rayPos.getY(), rayPos.getZ() + 0.5D, 1.25D);
			if (!this.rat.getMoveControl().hasWanted() && (this.rat.isOnGround() || this.rat.isRidingSpecialMount())) {
				BlockState block = this.rat.level().getBlockState(rayPos);
				SoundType soundType = block.getBlock().getSoundType(block, this.rat.level(), rayPos, null);
				if (this.rat.blockPosition().getY() < rayPos.getY()) {
					if (this.rat.level().getBlockState(this.rat.blockPosition()).canBeReplaced() && this.rat.level().isEmptyBlock(this.rat.blockPosition().above())) {
						this.rat.getJumpControl().jump();
						this.rat.level().setBlockAndUpdate(this.rat.blockPosition(), Blocks.COBBLESTONE.defaultBlockState());
					}
				}
				if (block.getMaterial().blocksMotion() && block.getMaterial() != Material.AIR) {
					double distance = this.rat.getRatDistanceCenterSq(rayPos.getX() + 0.5D, rayPos.getY() + 0.5D, rayPos.getZ() + 0.5D);
					if (distance < this.rat.getRatHarvestDistance(2.0D)) {
						this.rat.level().broadcastEntityEvent(this.rat, (byte) 85);
						this.rat.crafting = true;
						if (block == this.prevMiningState) {
							this.rat.level().broadcastEntityEvent(this.rat, (byte) 85);
							this.rat.crafting = true;
						} else {
							this.rat.level().broadcastEntityEvent(this.rat, (byte) 86);
							this.rat.crafting = false;
						}
						if (distance < this.rat.getRatHarvestDistance(-1.0D)) {
							this.rat.setDeltaMovement(Vec3.ZERO);
							this.rat.getNavigation().stop();
						}
						this.breakingTime++;
						int hardness = (int) (block.getDestroySpeed(this.rat.level(), rayPos) * (RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get()) ? 10 : 20));
						int i = (int) ((float) this.breakingTime / hardness * 10.0F);
						if (this.breakingTime % 5 == 0) {
							this.rat.playSound(soundType.getHitSound(), soundType.volume + 1, soundType.pitch);
						}
						if (i != this.previousBreakProgress) {
							this.rat.level().destroyBlockProgress(this.rat.getId(), rayPos, i);
							this.previousBreakProgress = i;
						}
						if (this.breakingTime >= hardness) {
							this.rat.level().broadcastEntityEvent(this.rat, (byte) 86);
							this.rat.playSound(soundType.getBreakSound(), soundType.volume, soundType.pitch);
							this.breakingTime = 0;
							this.previousBreakProgress = -1;
							if (!RatUtils.isBlockProtected(this.rat.level(), rayPos, this.rat)) {
								this.rat.level().destroyBlock(rayPos, true);
							}
							if (this.rat.level().isEmptyBlock(this.getTargetBlock())) {
								this.stop();
							}
						}
						this.prevMiningState = block;
					}
				}
			}
		}
	}
}
*/
