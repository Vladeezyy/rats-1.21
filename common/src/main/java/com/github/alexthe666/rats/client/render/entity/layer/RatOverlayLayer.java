package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Wild rats: the plague overlay and the toga */
public class RatOverlayLayer<S extends RampartLivingRenderState> extends EntityLayer<Rat, S, RatModel<Rat>> {
	private static final RenderType PLAGUE_TEX = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/plague_overlay.png"), false);
	private static final RenderType TOGA_TEX = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/upgrades/toga.png"));

	public RatOverlayLayer(RenderLayerParent<S, RatModel<Rat>> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, Rat rat) {
		if (rat.hasPlague()) {
			this.submitParent(stack, collector, state, PLAGUE_TEX, light);
		}
		if (rat.hasToga()) {
			this.submitParent(stack, collector, state, TOGA_TEX, light, OverlayTexture.NO_OVERLAY, -1, 2);
		}
	}
}
