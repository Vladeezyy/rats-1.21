package com.github.alexthe666.rats.server.block.entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import org.jetbrains.annotations.NotNull;
import net.minecraft.core.HolderLookup;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DecoratedRatCageBlockEntity extends BlockEntity {
	private NonNullList<ItemStack> containedDeco = NonNullList.withSize(1, ItemStack.EMPTY);

	public DecoratedRatCageBlockEntity(BlockPos pos, BlockState state) {
		super(RatsBlockEntityRegistry.RAT_CAGE_DECORATED.get(), pos, state);
	}

	public DecoratedRatCageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
	}


	public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	public void saveAdditional(ValueOutput compound) {
		ContainerHelper.saveAllItems(compound, this.containedDeco);
		super.saveAdditional(compound);
	}

	protected void loadAdditional(ValueInput compound) {
		super.loadAdditional(compound);
		containedDeco = NonNullList.withSize(1, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, containedDeco);
	}

	public ItemStack getContainedItem() {
		return containedDeco.get(0);
	}

	public void setContainedItem(ItemStack stack) {
		containedDeco.set(0, stack);
		this.setChanged();
	}

	// 26.x: the decoration drops when the cage is removed (1.21.1: the block's onRemove)
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null && !this.level.isClientSide() && !this.getContainedItem().isEmpty()) {
			this.level.addFreshEntity(new ItemEntity(this.level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this.getContainedItem()));
		}
	}
}
