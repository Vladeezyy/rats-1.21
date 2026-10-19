package com.github.alexthe666.rats.server.misc;

import dev.vladeezyy.rats.platform.Platform;
import dev.vladeezyy.rats.platform.transfer.EnergyHandler;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The item, energy and fluid handlers of blocks and items (the 1.20.1 original asked block entities for LazyOptionals;
 * the loader finds them: NeoForge capabilities, Fabric transfer API).
 */
public final class RatsCaps {

	private RatsCaps() {
	}

	public static Optional<ItemHandler> items(@Nullable BlockEntity be, @Nullable Direction side) {
		if (be == null || be.getLevel() == null || be.isRemoved()) {
			return Optional.empty();
		}
		return Platform.INSTANCE.itemHandler(be.getLevel(), be.getBlockPos(), be, side);
	}

	public static Optional<EnergyHandler> energy(@Nullable BlockEntity be, @Nullable Direction side) {
		if (be == null || be.getLevel() == null || be.isRemoved()) {
			return Optional.empty();
		}
		return Platform.INSTANCE.energyHandler(be.getLevel(), be.getBlockPos(), be, side);
	}

	public static Optional<FluidHandler> fluids(@Nullable BlockEntity be, @Nullable Direction side) {
		if (be == null || be.getLevel() == null || be.isRemoved()) {
			return Optional.empty();
		}
		return Platform.INSTANCE.fluidHandler(be.getLevel(), be.getBlockPos(), be, side);
	}

	public static Optional<EnergyHandler> energy(ItemStack stack) {
		return Platform.INSTANCE.energyHandler(stack);
	}
}
