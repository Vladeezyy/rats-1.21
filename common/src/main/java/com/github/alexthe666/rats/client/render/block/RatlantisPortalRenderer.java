package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.server.block.entity.RatlantisPortalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** The Ratlantis portal: a cube of the portal shader, the faces not hidden by neighbors */
public class RatlantisPortalRenderer extends StateBlockEntityRenderer<RatlantisPortalBlockEntity> {
	public static final Identifier PORTAL_BG = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/environment/ratlantis_sky_portal.png");
	public static final Identifier PORTAL_FG = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/environment/ratlantis_portal.png");
	private static final Direction[] FACES = {Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP};

	public RatlantisPortalRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void submit(RatlantisPortalBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		// the faces to draw, from the neighbors now (the drawing happens later in the frame)
		int faces = 0;
		for (int i = 0; i < FACES.length; i++) {
			if (entity.shouldRenderFace(FACES[i])) {
				faces |= 1 << i;
			}
		}
		if (faces != 0) {
			int shown = faces;
			collector.submitCustomGeometry(stack, RatsRenderType.getRatlantisPortal(), (pose, consumer) -> renderCube(pose, consumer, shown));
		}
	}

	/** The item: every face */
	public static void submitItem(PoseStack stack, SubmitNodeCollector collector) {
		collector.submitCustomGeometry(stack, RatsRenderType.getRatlantisPortal(), (pose, consumer) -> renderCube(pose, consumer, 0b111111));
	}

	private static void renderCube(PoseStack.Pose pose, VertexConsumer consumer, int faces) {
		renderFace(pose, consumer, faces, 0, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
		renderFace(pose, consumer, faces, 1, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		renderFace(pose, consumer, faces, 2, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
		renderFace(pose, consumer, faces, 3, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F);
		renderFace(pose, consumer, faces, 4, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
		renderFace(pose, consumer, faces, 5, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
	}

	private static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, int faces, int face, float xMin, float xMax, float yMin, float yMax, float z1, float z2, float z3, float z4) {
		if ((faces & (1 << face)) != 0) {
			consumer.addVertex(pose, xMin, yMin, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F);
			consumer.addVertex(pose, xMax, yMin, z2).setColor(1.0F, 1.0F, 1.0F, 1.0F);
			consumer.addVertex(pose, xMax, yMax, z3).setColor(1.0F, 1.0F, 1.0F, 1.0F);
			consumer.addVertex(pose, xMin, yMax, z4).setColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
