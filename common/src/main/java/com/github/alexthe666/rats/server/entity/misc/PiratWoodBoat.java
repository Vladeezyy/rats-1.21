package com.github.alexthe666.rats.server.entity.misc;

import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;

/** Pirat wood boat: vanilla's boat with its own drop item (the original's wood type field had only one value) */
public class PiratWoodBoat extends Boat {

	public PiratWoodBoat(EntityType<? extends Boat> type, Level level) {
		super(type, level, RatlantisItemRegistry.PIRAT_BOAT::get);
	}
}
