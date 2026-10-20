package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.StaticRatModel;
import com.github.alexthe666.rats.client.render.PosedModels;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.client.render.entity.RatProtectorRenderer;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** The protector rats circling a player in Ratlantis armor, one per piece (the original drew them after the player) */
public class RatProtectorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private static final StaticRatModel<?> RAT_MODEL = new StaticRatModel<>();

	public RatProtectorLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	private static int protectorCount(HumanoidRenderState state) {
		int protectors = 0;
		if (state.headEquipment.is(RatlantisItemRegistry.RATLANTIS_HELMET.get())) {
			protectors++;
		}
		if (state.chestEquipment.is(RatlantisItemRegistry.RATLANTIS_CHESTPLATE.get())) {
			protectors++;
		}
		if (state.legsEquipment.is(RatlantisItemRegistry.RATLANTIS_LEGGINGS.get())) {
			protectors++;
		}
		if (state.feetEquipment.is(RatlantisItemRegistry.RATLANTIS_BOOTS.get())) {
			protectors++;
		}
		return protectors;
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		if (!(state instanceof AvatarRenderState)) {
			return;
		}
		int protectorCount = protectorCount(state);
		if (protectorCount == 0) {
			return;
		}
		stack.pushPose();
		// back from the player model's space to the player's (LivingEntityRenderer.submit, the player's 0.9375 scale)
		stack.translate(0.0F, 1.501F, 0.0F);
		stack.scale(1.0F / 0.9375F, 1.0F / 0.9375F, 1.0F / 0.9375F);
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.mulPose(Axis.YP.rotationDegrees(-(180.0F - state.bodyRot)));
		float tick = state.ageInTicks - 1.0F;
		float age = state.ageInTicks;
		for (int i = 0; i < protectorCount; i++) {
			float offsetRot = 30 + 360 * (i / (float) protectorCount);
			float bob = (float) ((Math.sin(tick * 0.1F) * 0.2F + Math.cos(tick * 0.4F + i)) * 0.2);
			float scale = 0.4F;
			float rotation = Mth.wrapDegrees((tick * 8) % 360.0F + offsetRot);
			stack.pushPose();
			stack.mulPose(Axis.YP.rotationDegrees(rotation));
			stack.translate(0.0D, state.boundingBoxHeight + 0.5D + bob, state.boundingBoxWidth + 0.5F);
			stack.mulPose(Axis.YP.rotationDegrees(90));
			stack.mulPose(Axis.XP.rotationDegrees(75.0F));
			stack.scale(scale, scale, scale);
			stack.mulPose(Axis.XP.rotationDegrees(90.0F));
			PosedModels.submit(collector, stack, RatsRenderType.getGlowingTranslucent(RatProtectorRenderer.BASE_TEXTURE), RAT_MODEL, () -> RAT_MODEL.setupAnim(null, age * 0.5F, 1.0F, age, 0.0F, 0.0F), light, OverlayTexture.NO_OVERLAY, -1);
			stack.popPose();
		}
		stack.popPose();
	}
}
