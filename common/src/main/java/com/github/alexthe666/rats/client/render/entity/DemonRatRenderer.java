package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.server.entity.rat.DemonRat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

public class DemonRatRenderer extends AbstractRatRenderer<DemonRat, RatModel<DemonRat>> {
	public static final Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/demon_rat.png");
	public static final Identifier SOUL_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/soul_demon_rat.png");
	public static final Identifier BASE_EYE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/demon_rat_eye.png");
	public static final Identifier SOUL_EYE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/soul_demon_rat_eye.png");

	public DemonRatRenderer(EntityRendererProvider.Context context) {
		super(context, new RatModel<>());
		this.addLayer(new DemonEyesLayer(this));
	}

	@Override
	protected void scale(RatRenderState state, PoseStack stack) {
		stack.scale(1.5F, 1.5F, 1.5F);
		super.scale(state, stack);
	}

	@Override
	protected Identifier getTexture(DemonRat entity) {
		return entity.isSoulVariant() ? SOUL_TEXTURE : BASE_TEXTURE;
	}

	public static class DemonEyesLayer extends EntityLayer<DemonRat, RatRenderState, RatModel<DemonRat>> {
		public DemonEyesLayer(RenderLayerParent<RatRenderState, RatModel<DemonRat>> parent) {
			super(parent);
		}

		@Override
		protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatRenderState state, DemonRat rat) {
			this.submitParent(stack, collector, state, RenderTypes.eyes(rat.isSoulVariant() ? SOUL_EYE_TEXTURE : BASE_EYE_TEXTURE), LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, -1, 1);
		}
	}
}
