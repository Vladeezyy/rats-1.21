package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.server.entity.misc.RatProtector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class RatProtectorRenderer extends AbstractRatRenderer<RatProtector, RatModel<RatProtector>> {
	public static final Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat_protector.png");

	public RatProtectorRenderer(EntityRendererProvider.Context context) {
		super(context, new RatModel<>());
		this.addLayer(new Overlay(this));
	}

	@Override
	protected Identifier getTexture(RatProtector entity) {
		return BASE_TEXTURE;
	}

	private static class Overlay extends EntityLayer<RatProtector, RatRenderState, RatModel<RatProtector>> {
		public Overlay(RenderLayerParent<RatRenderState, RatModel<RatProtector>> parent) {
			super(parent);
		}

		@Override
		protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatRenderState state, RatProtector rat) {
			this.submitParent(stack, collector, state, RatsRenderType.getYellowGlint(), light, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 0.5F), 1);
		}
	}
}
