package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanSpiritModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.entity.projectile.PlagueShot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** The Black Death's plague shot: the spirit model with the plague cloud texture and a glint */
public class PlagueShotRenderer extends EntityRenderer<PlagueShot, DutchratSwordRenderer.State> {
	private static final RenderType TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/plague_cloud.png"));
	private static final RatlanteanSpiritModel<PlagueShot> MODEL_SPIRIT = new RatlanteanSpiritModel<>();

	public PlagueShotRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public DutchratSwordRenderer.State createRenderState() {
		return new DutchratSwordRenderer.State();
	}

	@Override
	public void extractRenderState(PlagueShot entity, DutchratSwordRenderer.State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(DutchratSwordRenderer.State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		stack.pushPose();
		stack.scale(1.5F, -1.5F, 1.5F);
		stack.mulPose(Axis.YP.rotationDegrees(state.yRot + 180.0F));
		stack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
		stack.translate(0F, -1.5F, 0F);
		collector.submitModel(MODEL_SPIRIT, SubmitHelper.NO_STATE, stack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		collector.order(1).submitModel(MODEL_SPIRIT, SubmitHelper.NO_STATE, stack, RenderTypes.entityGlint(), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}
}
