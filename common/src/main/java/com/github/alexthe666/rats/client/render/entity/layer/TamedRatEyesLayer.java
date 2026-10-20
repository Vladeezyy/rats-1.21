package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.GlowingEyesUpgrade;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Tamed rats: the eye texture of an upgrade, else the default eyes */
public class TamedRatEyesLayer<S extends RampartLivingRenderState> extends RatEyesLayer<TamedRat, S, AbstractRatModel<TamedRat>> {

	public TamedRatEyesLayer(RenderLayerParent<S, AbstractRatModel<TamedRat>> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, TamedRat rat) {
		if (RatUpgradeUtils.forEachUpgradeBool(rat, item -> item instanceof GlowingEyesUpgrade, false)) {
			AtomicBoolean skip = new AtomicBoolean(false);
			AtomicReference<RenderType> tex = new AtomicReference<>(EYES);
			RatUpgradeUtils.forEachUpgrade(rat, item -> item instanceof GlowingEyesUpgrade, (stack1, slot) -> {
				if (rat.isSlotVisible(slot)) {
					tex.set(((GlowingEyesUpgrade) stack1.getItem()).getEyeTexture(stack1));
				} else {
					skip.set(true);
				}
			});
			if (!skip.get()) {
				if (tex.get() != null || RatUpgradeUtils.forEachUpgradeBool(rat, upgrade -> upgrade instanceof ChangesTextureUpgrade eyeTex && eyeTex.makesEyesGlowByDefault(), false)) {
					this.submitParent(stack, collector, state, tex.get() != null ? tex.get() : EYES, light);
				}
			}
		} else {
			super.submit(stack, collector, light, state, rat);
		}
	}
}
