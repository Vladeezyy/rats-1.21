package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.server.entity.monster.GhostPirat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class GhostPiratRenderer extends AbstractRatRenderer<GhostPirat, RatModel<GhostPirat>> {
	private static final Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ghost_pirat/ghost_pirat.png");

	public GhostPiratRenderer(EntityRendererProvider.Context context) {
		super(context, new RatModel<>());
		this.shadowRadius = 0.35F;
		this.addLayer(new GhostPiratLayer(this));
	}

	@Override
	protected void scale(RatRenderState state, PoseStack stack) {
		super.scale(state, stack);
		stack.scale(2.0F, 2.0F, 2.0F);
	}

	@Override
	protected Identifier getTexture(GhostPirat entity) {
		return BASE_TEXTURE;
	}

	private static class GhostPiratLayer extends EntityLayer<GhostPirat, RatRenderState, RatModel<GhostPirat>> {
		private static final Identifier GHOST_OVERLAY = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ghost_pirat/ghost_pirat_overlay.png");

		public GhostPiratLayer(RenderLayerParent<RatRenderState, RatModel<GhostPirat>> parent) {
			super(parent);
		}

		@Override
		protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatRenderState state, GhostPirat rat) {
			float f = state.ageInTicks;
			this.submitParent(stack, collector, state, RenderTypes.energySwirl(GHOST_OVERLAY, f * 0.01F, f * 0.01F), light, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 0.5F), 1);
		}
	}
}
