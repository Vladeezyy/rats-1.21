package com.github.alexthe666.rats.server.items;

import com.github.alexthe666.rats.registry.RatsToolMaterialRegistry;
import net.minecraft.world.item.Item;

public class BaghNakhsItem extends Item {

	public BaghNakhsItem(Item.Properties properties) {
		super(properties.sword(RatsToolMaterialRegistry.BAGHNAKHS, 0.0F, 0.0F).attributes(PiratCutlassItem.weapon(6D, 6D)));
	}


}