package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesOverlayUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EtherealRatUpgradeItem extends BaseRatUpgradeItem implements ChangesTextureUpgrade, ChangesOverlayUpgrade {
	public EtherealRatUpgradeItem(Properties properties) {
		super(properties, 1, 2);
	}

	@Override
	public @Nullable RenderType getOverlayTexture(ItemStack stack, TamedRat rat, float partialTicks) {
		float f = (float) rat.tickCount + partialTicks;
		return RenderTypes.energySwirl(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/psychic.png"), f * 0.01F, f * 0.01F);
	}

	@Override
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ghost_pirat/ghost_pirat.png");
	}

	@Override
	public boolean makesEyesGlowByDefault() {
		return false;
	}
}
