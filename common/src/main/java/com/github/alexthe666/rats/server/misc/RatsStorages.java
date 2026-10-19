package com.github.alexthe666.rats.server.misc;

import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCageWheelBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import com.github.alexthe666.rats.server.block.entity.UpgradeCombinerBlockEntity;
import dev.vladeezyy.rats.platform.transfer.EnergyHandler;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The item, fluid and energy storages of the mod's own blocks (the original's capabilities). The loaders expose them to
 * other mods (NeoForge capabilities, Fabric transfer API) and look here first for the mod's blocks.
 */
public final class RatsStorages {

	private RatsStorages() {
	}

	@Nullable
	public static ItemHandler items(BlockEntity be, @Nullable Direction side) {
		if (be instanceof UpgradeCombinerBlockEntity combiner) {
			return combiner.getItemHandler(side);
		} else if (be instanceof RatQuarryBlockEntity quarry) {
			return quarry.getItemHandler(side);
		} else if (be instanceof AutoCurdlerBlockEntity curdler) {
			return curdler.getItemHandler(side);
		} else if (be instanceof RatCraftingTableBlockEntity table) {
			return table.getItemHandler(side);
		}
		return null;
	}

	@Nullable
	public static FluidHandler fluids(BlockEntity be, @Nullable Direction side) {
		return be instanceof AutoCurdlerBlockEntity curdler ? curdler.getFluidHandler(side) : null;
	}

	@Nullable
	public static EnergyHandler energy(BlockEntity be, @Nullable Direction side) {
		return be instanceof RatCageWheelBlockEntity wheel ? wheel.getEnergyStorage(side) : null;
	}
}
