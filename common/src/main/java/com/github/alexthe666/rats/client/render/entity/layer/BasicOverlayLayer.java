package com.github.alexthe666.rats.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** The parent model again with a translucent texture on top */
public class BasicOverlayLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
	private final RenderType renderType;

	public BasicOverlayLayer(RenderLayerParent<S, M> parent, Identifier texture) {
		super(parent);
		this.renderType = RenderTypes.entityTranslucent(texture, false);
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		collector.order(1).submitModel(this.getParentModel(), state, stack, this.renderType, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
	}
}
