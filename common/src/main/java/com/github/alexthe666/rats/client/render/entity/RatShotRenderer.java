package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.StaticRatModel;
import com.github.alexthe666.rats.server.entity.projectile.RatShot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.vladeezyy.rampart.client.render.RampartEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;

/** A rat shot from the gilded flute: a running rat in the air (eyes glowing in the dark) */
public class RatShotRenderer extends EntityRenderer<RatShot, RatShotRenderer.State> {
	private static final RenderType TEXTURE_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/eyes/glow.png"));
	private static final StaticRatModel<RatShot> MODEL_STATIC_RAT = new StaticRatModel<>();

	public RatShotRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(RatShot entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		long roundedTime = entity.level().getDefaultClockTime() % 24000;
		boolean night = roundedTime >= 13000 && roundedTime <= 22000;
		BlockPos ratPos = entity.getLightPosition();
		int brightI = entity.level().getBrightness(LightLayer.SKY, ratPos);
		int brightJ = entity.level().getBrightness(LightLayer.BLOCK, ratPos);
		state.glowingEyes = (night ? brightJ : Math.max(brightI, brightJ)) < 7;
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		state.texture = RenderTypes.entityCutout(entity.getColorVariant().getTexture());
		// the model runs in the air: the pose is set when it is drawn, from these values
		float age = entity.tickCount + partialTicks;
		state.pose.set(entity, partialTicks, age * 0.5F, 1.0F, age, 0.0F, 0.0F);
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		stack.pushPose();
		stack.scale(0.6F, -0.6F, 0.6F);
		stack.translate(0F, -1.5F, 0F);
		stack.rotate(Axis.YP.rotationDegrees(state.yRot - 180));
		stack.rotate(Axis.XN.rotationDegrees(state.xRot));
		collector.submitModel(MODEL_STATIC_RAT, state, stack, state.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		if (state.glowingEyes) {
			collector.order(1).submitModel(MODEL_STATIC_RAT, state, stack, TEXTURE_EYES, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		}
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}

	public static class State extends RampartEntityRenderState {
		public float yRot;
		public float xRot;
		public boolean glowingEyes;
		public RenderType texture;
	}
}
