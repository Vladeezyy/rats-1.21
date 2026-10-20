package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.EmptyModel;
import com.github.alexthe666.rats.client.model.entity.RatKingModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.entity.RatsMobRenderState;
import com.github.alexthe666.rats.server.entity.monster.boss.RatKing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;

/** The rats of the rat king, in a ring */
public class RatKingLayer extends EntityLayer<RatKing, RatsMobRenderState, EmptyModel<RatKing>> {
	private static final RenderType TEXTURE_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/eyes/glow.png"));
	private static final RenderType TEXTURE_0 = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/blue.png"));
	private static final RenderType TEXTURE_1 = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/black.png"));
	private static final RenderType TEXTURE_2 = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/brown.png"));
	private static final RenderType TEXTURE_3 = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/green.png"));
	/** One model per rat: each poses itself by its index when it is drawn (26.x draws the models later in the frame) */
	private static final RatKingModel<RatKing>[] RAT_MODELS = createModels();

	public RatKingLayer(RenderLayerParent<RatsMobRenderState, EmptyModel<RatKing>> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	private static RatKingModel<RatKing>[] createModels() {
		RatKingModel<RatKing>[] models = new RatKingModel[RatKing.RAT_COUNT];
		for (int i = 0; i < models.length; i++) {
			models[i] = new RatKingModel<>();
			models[i].setIndex(i);
		}
		return models;
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatsMobRenderState state, RatKing king) {
		long roundedTime = king.level().getDefaultClockTime() % 24000;
		boolean night = roundedTime >= 13000 && roundedTime <= 22000;
		BlockPos ratPos = king.getLightPosition();
		int brightI = king.level().getBrightness(LightLayer.SKY, ratPos);
		int brightJ = king.level().getBrightness(LightLayer.BLOCK, ratPos);
		int brightness = night ? brightJ : Math.max(brightI, brightJ);
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
		for (int i = 0; i < RatKing.RAT_COUNT; i++) {
			int deathTime = Math.min(Math.max(0, king.deathTime - i * 5), 5);
			stack.pushPose();
			stack.rotate(Axis.YP.rotationDegrees(i * RatKing.RAT_ANGLE));
			stack.translate(0, 0.6F + (deathTime * 0.01F), -0.8);
			stack.scale(0.6F, 0.6F, 0.6F);
			RatKingModel<RatKing> model = RAT_MODELS[i];
			collector.submitModel(model, state, stack, getRatTexture(king.getRatColors(i)), light, overlay, -1, null, state.outlineColor);
			if (brightness < 7) {
				collector.order(1).submitModel(model, state, stack, TEXTURE_EYES, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
			}
			stack.popPose();
		}
	}

	private static RenderType getRatTexture(int textureIndex) {
		return switch (textureIndex) {
			case 1 -> TEXTURE_1;
			case 2 -> TEXTURE_2;
			case 3 -> TEXTURE_3;
			default -> TEXTURE_0;
		};
	}
}
