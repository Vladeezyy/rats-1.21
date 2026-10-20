package com.github.alexthe666.rats.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

/** The staffs' markers in the world (icons facing the camera, scrolling boxes, patrol lines), submitted with the world */
public class RatsIconRenderUtil {

	/** An icon over a block, facing the camera */
	public static void submitPOIIcon(Identifier icon, Vec3 viewVec, @Nullable BlockPos renderPos, float bob, Quaternionf cameraOrientation, PoseStack stack, SubmitNodeCollector collector) {
		if (renderPos != null && renderPos != BlockPos.ZERO) {
			stack.pushPose();
			stack.translate(-viewVec.x(), -viewVec.y(), -viewVec.z());
			stack.translate(renderPos.getX() + 0.5F, renderPos.getY() + bob, renderPos.getZ() + 0.5F);
			stack.rotate(cameraOrientation);
			collector.submitCustomGeometry(stack, RenderTypes.entityTranslucent(icon), (pose, buffer) -> {
				iconVertex(buffer, pose, -0.5F, -0.5F, 1.0F, 1.0F);
				iconVertex(buffer, pose, -0.5F, 0.5F, 1.0F, 0.0F);
				iconVertex(buffer, pose, 0.5F, 0.5F, 0.0F, 0.0F);
				iconVertex(buffer, pose, 0.5F, -0.5F, 0.0F, 1.0F);
			});
			stack.popPose();
		}
	}

	private static void iconVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
		buffer.addVertex(pose, x, y, 0).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	/** A box around a center with the texture scrolling over it */
	public static void submitBox(Identifier texture, Vec3 viewPos, Vec3 centerPos, AABB boxSize, PoseStack stack, SubmitNodeCollector collector) {
		stack.pushPose();
		stack.translate(-viewPos.x(), -viewPos.y(), -viewPos.z());
		stack.translate(centerPos.x(), centerPos.y(), centerPos.z());
		float f3 = Minecraft.getInstance().isPaused() ? 0.0F : (float) (System.currentTimeMillis() % 3000L) / 3000.0F;
		collector.submitCustomGeometry(stack, RenderTypes.entityTranslucent(texture), (pose, buffer) -> drawMovingAABB(boxSize, pose, buffer, f3));
		stack.popPose();
	}

	/** A line from the pose's origin to (x, y, z) */
	public static void submitLine(PoseStack stack, SubmitNodeCollector collector, float x, float y, float z, int color) {
		float length = (float) Math.sqrt(x * x + y * y + z * z);
		float nx = length > 0 ? x / length : 0;
		float ny = length > 0 ? y / length : 1;
		float nz = length > 0 ? z / length : 0;
		collector.submitCustomGeometry(stack, RenderTypes.lines(), (pose, buffer) -> {
			buffer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
			buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
		});
	}

	private static void drawMovingAABB(AABB boundingBox, PoseStack.Pose pose, VertexConsumer vertexbuffer, float f3) {
		float maxX = (float) boundingBox.maxX * 0.125F;
		float minX = (float) boundingBox.minX * 0.125F;
		float maxY = (float) boundingBox.maxY * 0.125F;
		float minY = (float) boundingBox.minY * 0.125F;
		float maxZ = (float) boundingBox.maxZ * 0.125F;
		float minZ = (float) boundingBox.minZ * 0.125F;
		//north
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F);

		//south
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);

		//bottom
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxZ - minZ).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxZ - minZ).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);

		//top
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxZ - minZ).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxZ - minZ).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);

		//west
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, -1.0F, 0.0F, 0.0F);

		//east
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(-1).setUv(f3 + minX - maxX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + maxY - minY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(-1).setUv(f3 + maxX - minX, f3 + minY - maxY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 1.0F, 0.0F, 0.0F);
	}
}
