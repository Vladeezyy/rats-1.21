package com.github.alexthe666.rats.server.items.upgrades;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesOverlayUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class IdolRatUpgradeItem extends BaseRatUpgradeItem implements ChangesTextureUpgrade, ChangesOverlayUpgrade {
	public IdolRatUpgradeItem(Properties properties) {
		super(properties, 1, 2);
	}

	@Override
	public @Nullable RenderType getOverlayTexture(ItemStack stack, TamedRat rat, float partialTicks) {
		return RatsRenderType.getGoldGlint();
	}

	@Override
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/upgrades/idol.png");
	}

	@Override
	public boolean makesEyesGlowByDefault() {
		return false;
	}
}
