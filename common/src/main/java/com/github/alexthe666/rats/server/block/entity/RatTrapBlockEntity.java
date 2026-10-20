package com.github.alexthe666.rats.server.block.entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import org.jetbrains.annotations.NotNull;
import net.minecraft.core.HolderLookup;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.block.RatTrapBlock;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RatTrapBlockEntity extends BlockEntity {

	public float shutProgress;
	private NonNullList<ItemStack> baitStack = NonNullList.withSize(1, ItemStack.EMPTY);

	public RatTrapBlockEntity(BlockPos pos, BlockState state) {
		super(RatsBlockEntityRegistry.RAT_TRAP.get(), pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, RatTrapBlockEntity te) {
		if (level.isClientSide()) {
			if (state.getValue(RatTrapBlock.SHUT) && te.shutProgress < 6.0F) {
				te.shutProgress += 1.5F;
			} else if (!state.getValue(RatTrapBlock.SHUT) && te.shutProgress > 0.0F) {
				te.shutProgress -= 1.5F;
			}
		} else {
			if (RatUtils.isRatFood(te.baitStack.get(0))) {
				te.killRats(level, pos, state);
			}
		}
	}

	private void killRats(Level level, BlockPos pos, BlockState state) {
		if (!state.getValue(RatTrapBlock.SHUT)) {
			for (Rat rat : level.getEntitiesOfClass(Rat.class, new AABB(pos).inflate(0.25D))) {
				if (!rat.isDead() && !rat.isDeadInTrap()) {
					level.setBlockAndUpdate(pos, state.setValue(RatTrapBlock.SHUT, true));
					rat.setKilledInTrap();
					level.playSound(null, this.getBlockPos(), RatsSoundRegistry.RAT_TRAP_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
					this.decreaseBait();
					level.sendBlockUpdated(pos, state, state, 3);
					//cause rats to scatter when one is killed
					level.getEntitiesOfClass(Rat.class, new AABB(pos).inflate(8.0D)).forEach(rat1 -> rat1.setFleePos(pos));
					break;
				}
			}

		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
	}


	@Override
	public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public void saveAdditional(ValueOutput compound) {
		ContainerHelper.saveAllItems(compound, this.baitStack);
		compound.putFloat("ShutProgress", this.shutProgress);
		super.saveAdditional(compound);
	}

	@Override
	protected void loadAdditional(ValueInput compound) {
		super.loadAdditional(compound);
		this.baitStack = NonNullList.withSize(1, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.baitStack);
		this.shutProgress = compound.getFloatOr("ShutProgress", 0.0F);
	}

	public ItemStack getBait() {
		return this.baitStack.get(0);
	}

	private void decreaseBait() {
		this.baitStack.get(0).shrink(1);
	}

	public void setBaitStack(ItemStack stack) {
		this.baitStack.set(0, stack);
	}

	// 26.x: the bait drops when the trap is removed (1.21.1: the block's onRemove)
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null && !this.level.isClientSide() && !this.getBait().isEmpty()) {
			this.level.addFreshEntity(new ItemEntity(this.level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.getBait()));
		}
	}
}
