package com.github.alexthe666.rats.server.items.upgrades;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import net.minecraft.resources.Identifier;

public class UndeadRatUpgradeItem extends BaseRatUpgradeItem implements ChangesTextureUpgrade {
	public UndeadRatUpgradeItem(Properties properties) {
		super(properties, 2, 2);
	}

	@Override
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/upgrades/undead.png");
	}

	@Override
	public boolean makesEyesGlowByDefault() {
		return false;
	}
}
