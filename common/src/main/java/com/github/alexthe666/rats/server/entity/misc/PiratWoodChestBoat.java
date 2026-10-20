package com.github.alexthe666.rats.server.entity.misc;

import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

/** Pirat wood chest boat: vanilla's chest boat with its own drop item */
public class PiratWoodChestBoat extends ChestBoat {

	public PiratWoodChestBoat(EntityType<? extends ChestBoat> type, Level level) {
		super(type, level, RatlantisItemRegistry.PIRAT_CHEST_BOAT::get);
	}
}
