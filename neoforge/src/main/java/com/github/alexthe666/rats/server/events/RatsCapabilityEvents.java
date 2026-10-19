package com.github.alexthe666.rats.server.events;

import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCageWheelBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import com.github.alexthe666.rats.server.block.entity.UpgradeCombinerBlockEntity;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Item, fluid and energy handlers of the mod's blocks and of tamed rats (the 1.20.1 original overrode getCapability) */
public final class RatsCapabilityEvents {

	private RatsCapabilityEvents() {
	}

	public static void register(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RatsBlockEntityRegistry.UPGRADE_COMBINER.get(), UpgradeCombinerBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RatsBlockEntityRegistry.RAT_QUARRY.get(), RatQuarryBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RatsBlockEntityRegistry.AUTO_CURDLER.get(), AutoCurdlerBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RatsBlockEntityRegistry.AUTO_CURDLER.get(), AutoCurdlerBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RatsBlockEntityRegistry.RAT_CRAFTING_TABLE.get(), RatCraftingTableBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, RatsBlockEntityRegistry.RAT_CAGE_WHEEL.get(), RatCageWheelBlockEntity::getEnergyStorage);
		event.registerEntity(Capabilities.ItemHandler.ENTITY, RatsEntityRegistry.TAMED_RAT.get(), (TamedRat rat, Void context) -> rat.getItemHandler());
	}
}
