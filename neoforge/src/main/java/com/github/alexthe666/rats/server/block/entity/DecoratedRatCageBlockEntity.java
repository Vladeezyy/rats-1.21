package com.github.alexthe666.rats.server.block.entity;

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

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.@NotNull Provider registries) {
		this.handleUpdateTag(packet.getTag(), registries);
	}

	public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
		return this.saveWithId(registries);
	}

	public void saveAdditional(CompoundTag compound, HolderLookup.@NotNull Provider registries) {
		ContainerHelper.saveAllItems(compound, this.containedDeco, registries);
		super.saveAdditional(compound, registries);
	}

	protected void loadAdditional(CompoundTag compound, HolderLookup.@NotNull Provider registries) {
		super.loadAdditional(compound, registries);
		containedDeco = NonNullList.withSize(1, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, containedDeco, registries);
	}

	public ItemStack getContainedItem() {
		return containedDeco.get(0);
	}

	public void setContainedItem(ItemStack stack) {
		containedDeco.set(0, stack);
		this.setChanged();
	}


}
