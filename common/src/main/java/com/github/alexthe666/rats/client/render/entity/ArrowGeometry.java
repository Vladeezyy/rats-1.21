package com.github.alexthe666.rats.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/** 1.21.1's arrow geometry (the cross at the back, and the shaft) for the mod's bolts and beams with their own render types */
public final class ArrowGeometry {

	private ArrowGeometry() {
	}

	/**
	 * Submits the arrow at the entity's rotation; {@code shake} as {@code AbstractArrow.shakeTime - partialTick},
	 * {@code color} ARGB per vertex
	 */
	public static void submit(PoseStack stack, SubmitNodeCollector collector, RenderType type, float yRot, float xRot, float shake, float scale, int light, int color, boolean shaft) {
		stack.pushPose();
		stack.rotate(Axis.YP.rotationDegrees(yRot - 90.0F));
		stack.rotate(Axis.ZP.rotationDegrees(xRot));
		if (shake > 0.0F) {
			stack.rotate(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
		}
		stack.rotate(Axis.XP.rotationDegrees(45.0F));
		stack.scale(scale * 0.05625F, scale * 0.05625F, scale * 0.05625F);
		stack.translate(-4.0F, 0.0F, 0.0F);
		collector.submitCustomGeometry(stack, type, (pose, consumer) -> draw(pose, consumer, light, color, shaft));
		stack.popPose();
	}

	private static void draw(PoseStack.Pose pose, VertexConsumer consumer, int light, int color, boolean shaft) {
		vertex(pose, consumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, light, color);
		vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, light, color);
		vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, light, color);
		vertex(pose, consumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, light, color);
		vertex(pose, consumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, light, color);
		vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, light, color);
		vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, light, color);
		vertex(pose, consumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, light, color);
		if (shaft) {
			PoseStack.Pose side = pose.copy();
			for (int j = 0; j < 4; ++j) {
				side.rotate(Axis.XP.rotationDegrees(90.0F));
				vertex(side, consumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light, color);
				vertex(side, consumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light, color);
				vertex(side, consumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light, color);
				vertex(side, consumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light, color);
			}
		}
	}

	// the original's argument order: x, z, then y of the normal
	private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int x, int y, int z, float u, float v, int xNorm, int zNorm, int yNorm, int light, int color) {
		consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, (float) xNorm, (float) yNorm, (float) zNorm);
	}
}
