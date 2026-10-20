package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.LaserPortalModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.entity.misc.LaserPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** The Neo-Ratlantean's laser portal: its floating pivot, growing and spinning, with a glint */
public class LaserPortalRenderer extends EntityRenderer<LaserPortal, LaserPortalRenderer.State> {
	private static final RenderType PORTAL = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/neo_ratlantean/neo_ratlantean_glow.png"));
	private static final LaserPortalModel MODEL_NEO_RATLANTEAN = new LaserPortalModel();

	public LaserPortalRenderer(EntityRendererProvider.Context context) {
		super(context);
		MODEL_NEO_RATLANTEAN.floatyPivot.setRotationPoint(0, 0, 0);
		MODEL_NEO_RATLANTEAN.floatyPivot.rotateAngleY = 0.7853981633974483F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(LaserPortal entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.scale = entity.scaleOfPortalPrev + (entity.scaleOfPortal - entity.scaleOfPortalPrev) * partialTicks;
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		float d1 = state.scale;
		stack.pushPose();
		stack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
		stack.pushPose();
		stack.scale(1.5F * d1, 1.5F * d1, 1.5F * d1);
		stack.translate(0, 0.5F, 0);
		stack.translate(0, 1 - d1, 0);
		stack.mulPose(Axis.YP.rotationDegrees(90));
		stack.mulPose(Axis.XP.rotationDegrees(90));
		stack.mulPose(Axis.ZP.rotationDegrees(state.yRot - 90.0F));
		stack.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 10));
		collector.submitModel(MODEL_NEO_RATLANTEAN, SubmitHelper.NO_STATE, stack, PORTAL, 240, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		collector.order(1).submitModel(MODEL_NEO_RATLANTEAN, SubmitHelper.NO_STATE, stack, RenderTypes.entityGlint(), 240, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		stack.popPose();
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}

	public static class State extends EntityRenderState {
		public float scale;
		public float yRot;
		public float xRot;
	}
}
