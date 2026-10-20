package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RattlingGunBaseModel;
import com.github.alexthe666.rats.client.model.entity.RattlingGunModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.entity.misc.RattlingGun;
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
import net.minecraft.resources.Identifier;

/** The rattling gun: its base, and the gun turning to its yaw (spinning while it fires) */
public class RattlingGunRenderer extends EntityRenderer<RattlingGun, RattlingGunRenderer.State> {
	private static final RenderType TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rattling_gun/rattling_gun.png"));
	private static final RenderType TEXTURE_FIRING = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rattling_gun/rattling_gun_firing.png"));
	/** Animated from the state when drawn (firing); the idle gun is another instance that keeps its default pose */
	private static final RattlingGunModel<RattlingGun> GUN_MODEL = new RattlingGunModel<>();
	private static final RattlingGunModel<RattlingGun> IDLE_GUN_MODEL = new RattlingGunModel<>();
	private static final RattlingGunBaseModel<RattlingGun> GUN_BASE_MODEL = new RattlingGunBaseModel<>();

	public RattlingGunRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(RattlingGun entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot();
		state.firing = entity.isFiring();
		state.pose.set(entity, partialTicks, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		int light = state.lightCoords;
		stack.pushPose();
		stack.pushPose();
		stack.translate(0, 1.5F, 0);
		stack.rotate(Axis.XP.rotationDegrees(180));
		collector.submitModel(GUN_BASE_MODEL, SubmitHelper.NO_STATE, stack, TEXTURE, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		stack.rotate(Axis.YP.rotationDegrees(state.yRot));
		if (state.firing) {
			collector.submitModel(GUN_MODEL, state, stack, TEXTURE, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		} else {
			collector.submitModel(IDLE_GUN_MODEL, SubmitHelper.NO_STATE, stack, TEXTURE, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		}
		stack.popPose();

		if (state.firing) {
			stack.pushPose();
			stack.translate(0, 1.6F, 0);
			stack.rotate(Axis.XP.rotationDegrees(180));
			stack.rotate(Axis.YP.rotationDegrees(state.yRot));
			collector.order(1).submitModel(GUN_MODEL, state, stack, TEXTURE_FIRING, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
			stack.popPose();
		}
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}

	public static class State extends RampartEntityRenderState {
		public float yRot;
		public boolean firing;
	}
}
