package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** The item, energy and fluid handlers of blocks and items (the 1.20.1 original asked block entities for LazyOptionals) */
public final class RatsCaps {

	private RatsCaps() {
	}

	public static Optional<IItemHandler> items(@Nullable BlockEntity be, @Nullable Direction side) {
		return block(Capabilities.ItemHandler.BLOCK, be, side);
	}

	public static Optional<IEnergyStorage> energy(@Nullable BlockEntity be, @Nullable Direction side) {
		return block(Capabilities.EnergyStorage.BLOCK, be, side);
	}

	public static Optional<IFluidHandler> fluids(@Nullable BlockEntity be, @Nullable Direction side) {
		return block(Capabilities.FluidHandler.BLOCK, be, side);
	}

	public static Optional<IEnergyStorage> energy(ItemStack stack) {
		return Optional.ofNullable(stack.getCapability(Capabilities.EnergyStorage.ITEM));
	}

	private static <T> Optional<T> block(BlockCapability<T, @Nullable Direction> capability, @Nullable BlockEntity be, @Nullable Direction side) {
		if (be == null || be.getLevel() == null || be.isRemoved()) {
			return Optional.empty();
		}
		return Optional.ofNullable(be.getLevel().getCapability(capability, be.getBlockPos(), be.getBlockState(), be, side));
	}
}
