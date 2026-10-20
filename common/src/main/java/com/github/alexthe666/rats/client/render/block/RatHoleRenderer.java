package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.server.block.RatHoleBlock;
import com.github.alexthe666.rats.server.block.entity.RatHoleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** A rat hole: the boxes of the hole in the texture of the block it imitates */
public class RatHoleRenderer extends StateBlockEntityRenderer<RatHoleBlockEntity> {
	private static final AABB TOP_AABB = new AABB(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
	private static final AABB WEST_CONNECT_AABB = new AABB(0.0F, 0.0F, 0.25F, 0.25F, 0.5F, 0.75F);
	private static final AABB EAST_CONNECT_AABB = new AABB(0.75F, 0.0F, 0.25F, 1.0F, 0.5F, 0.75F);
	private static final AABB NORTH_CONNECT_AABB = new AABB(0.25F, 0.0F, 0.0F, 0.75F, 0.5F, 0.25F);
	private static final AABB SOUTH_CONNECT_AABB = new AABB(0.25F, 0.0F, 0.75F, 0.75F, 0.5F, 1.0F);

	private static final AABB NORTH_CORNER_AABB = new AABB(0.0F, 0.0F, -0.0F, 0.25F, 0.5F, 0.25F);
	private static final AABB EAST_CORNER_AABB = new AABB(0.75F, 0.0F, -0.0F, 1.0F, 0.5F, 0.25F);
	private static final AABB SOUTH_CORNER_AABB = new AABB(0.0F, 0.0F, 0.75F, 0.25F, 0.5F, 1.0F);
	private static final AABB WEST_CORNER_AABB = new AABB(0.75F, 0.0F, 0.75F, 1.0F, 0.5F, 1.0F);

	private static final RenderType TEXTURE = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);

	public RatHoleRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	private static void renderAABB(VertexConsumer vertexbuffer, AABB boundingBox, PoseStack.Pose normalPose, TextureAtlasSprite sprite, int light, int overlay) {
		double avgY = boundingBox.maxY - boundingBox.minY;
		double avgX = boundingBox.maxX - boundingBox.minX;
		double avgZ = boundingBox.maxZ - boundingBox.minZ;

		float f1 = (float) ((sprite.getX() + (boundingBox.minX * 16.0D)) / (sprite.getX() / sprite.getU0()));
		float maxUX = (float) Math.min(sprite.getU1(), f1 + avgX * Math.abs(sprite.getU1() - sprite.getU0()));
		float f2 = (float) ((sprite.getX() + (boundingBox.minZ * 16.0D)) / (sprite.getX() / sprite.getU0()));
		float maxUZ = (float) Math.min(sprite.getU1(), f2 + avgZ * Math.abs(sprite.getU1() - sprite.getU0()));
		float f3 = (float) ((sprite.getY() + (sprite.contents().height() - (boundingBox.maxY * 16.0D))) / (sprite.getY() / sprite.getV0()));
		float maxVY = (float) Math.min(sprite.getV1(), f3 + avgY * Math.abs(sprite.getV1() - sprite.getV0()));
		float maxVZ = (float) Math.min(sprite.getV1(), f3 + avgZ * Math.abs(sprite.getV1() - sprite.getV0()));
		//back
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUX, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUX, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, 1.0F);
		//front
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUX, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUX, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 0.0F, -1.0F);
		//tops
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, maxVZ).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUX, maxVZ).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUX, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, -1.0F, 0.0F);

		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, maxVZ).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUX, maxVZ).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUX, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 0.0F, 1.0F, 0.0F);
		//sides
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUZ, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(maxUZ, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, -1.0F, 0.0F, 0.0F);

		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUZ, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(maxUZ, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2, f3).setOverlay(overlay).setLight(light).setNormal(normalPose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(normalPose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2, maxVY).setOverlay(overlay).setLight(light).setNormal(normalPose, 1.0F, 0.0F, 0.0F);
	}

	@Override
	protected void submit(RatHoleBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		BlockState state = Blocks.OAK_PLANKS.defaultBlockState();
		List<AABB> boxes = new ArrayList<>(9);
		boxes.add(TOP_AABB);
		boxes.add(EAST_CORNER_AABB);
		boxes.add(WEST_CORNER_AABB);
		boxes.add(NORTH_CORNER_AABB);
		boxes.add(SOUTH_CORNER_AABB);
		BlockState actualState = entity.getBlockState();
		if (actualState.getBlock() instanceof RatHoleBlock) {
			if (actualState.getValue(RatHoleBlock.EAST)) {
				boxes.add(EAST_CONNECT_AABB);
			}
			if (actualState.getValue(RatHoleBlock.WEST)) {
				boxes.add(WEST_CONNECT_AABB);
			}
			if (actualState.getValue(RatHoleBlock.NORTH)) {
				boxes.add(NORTH_CONNECT_AABB);
			}
			if (actualState.getValue(RatHoleBlock.SOUTH)) {
				boxes.add(SOUTH_CONNECT_AABB);
			}
			state = entity.getImitatedBlockState();
		}
		TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).particleMaterial().sprite();
		collector.submitCustomGeometry(stack, TEXTURE, (pose, consumer) -> {
			for (AABB box : boxes) {
				renderAABB(consumer, box, pose, sprite, light, overlay);
			}
		});
	}
}
