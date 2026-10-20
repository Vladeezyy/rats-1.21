package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.block.AutoCurdlerModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.client.util.FluidSprites;
import com.github.alexthe666.rats.server.block.AutoCurdlerBlock;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The auto curdler and the milk in its tank */
public class AutoCurdlerRenderer extends StateBlockEntityRenderer<AutoCurdlerBlockEntity> {
	private static final AutoCurdlerModel<?> MODEL_AUTO_CURDLER = new AutoCurdlerModel<>();
	private static final RenderType TEXTURE = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/auto_curdler.png"));
	private static final RenderType TEXTURE_BLOCKS = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);

	public AutoCurdlerRenderer(BlockEntityRendererProvider.Context context) {
	}

	/** The milk: a box of the fluid's still sprite, as high as the tank is full */
	public static void submitMilk(SubmitNodeCollector collector, PoseStack stack, FluidStack fluidStack, int combinedLight, int overlay) {
		stack.pushPose();
		stack.mulPose(Axis.XP.rotationDegrees(180));
		stack.translate(-0.5F, -1.6F, -0.5F);
		collector.submitCustomGeometry(stack, TEXTURE_BLOCKS, (pose, vertexbuffer) -> drawMilk(pose, vertexbuffer, fluidStack, combinedLight, overlay));
		stack.popPose();
	}

	private static void drawMilk(PoseStack.Pose pose, VertexConsumer vertexbuffer, FluidStack fluidStack, int combinedLight, int overlay) {
		float textureYPos = (0.6F * (fluidStack.getAmount() / 5000F));
		AABB boundingBox = new AABB(0.25F, 0.35F, 0.25F, 0.75F, 0.6F + textureYPos, 0.75F);
		TextureAtlasSprite sprite = FluidSprites.still(fluidStack.getFluid());
		double avgY = boundingBox.maxY - boundingBox.minY;
		double avgX = Math.abs(boundingBox.maxX - boundingBox.minX);
		double avgZ = Math.abs(boundingBox.maxZ - boundingBox.minZ);
		float f1 = sprite.getU0();
		float f2_alt_x = (float) Math.min(sprite.getU1(), f1 + avgX * Math.abs(sprite.getU1() - sprite.getU0()));
		float f2_alt_z = (float) Math.min(sprite.getU1(), f1 + avgZ * Math.abs(sprite.getU1() - sprite.getU0()));
		float f3 = sprite.getV0();
		float f4_alt = (float) Math.min(sprite.getV1(), f3 + avgY * Math.abs(sprite.getV1() - sprite.getV0()));
		float f4_alt_z = (float) Math.min(sprite.getV1(), f3 + avgZ * Math.abs(sprite.getV1() - sprite.getV0()));
		//back
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
		//front
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
		//tops
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt_z).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f4_alt_z).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, -1.0F, 0.0F);

		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt_z).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f4_alt_z).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_x, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
		//sides
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_z, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f2_alt_z, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.minX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, -1.0F, 0.0F, 0.0F);

		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_z, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.minZ).setColor(255, 255, 255, 255).setUv(f2_alt_z, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.maxY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f3).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexbuffer.addVertex(pose, (float) boundingBox.maxX, (float) boundingBox.minY, (float) boundingBox.maxZ).setColor(255, 255, 255, 255).setUv(f1, f4_alt).setOverlay(overlay).setLight(combinedLight).setNormal(pose, 1.0F, 0.0F, 0.0F);
	}

	/** The item: the curdler facing north, empty */
	public static void submitItem(PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5D, 1.5D, 0.5D);
		stack.mulPose(Axis.ZP.rotationDegrees(180));
		collector.submitModel(MODEL_AUTO_CURDLER, SubmitHelper.NO_STATE, stack, TEXTURE, light, overlay, -1, null, 0, null);
		stack.popPose();
	}

	@Override
	protected void submit(AutoCurdlerBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		float f = 0;
		BlockState state = entity.getBlockState();
		if (state.getBlock() instanceof AutoCurdlerBlock) {
			f = state.getValue(AutoCurdlerBlock.FACING).getClockWise().toYRot() + 90;
		}
		stack.pushPose();
		stack.translate(0.5D, 1.5D, 0.5D);
		stack.mulPose(Axis.YP.rotationDegrees(-f));
		stack.mulPose(Axis.ZP.rotationDegrees(180));
		if (!entity.getTank().getFluid().isEmpty()) {
			submitMilk(collector, stack, entity.getTank().getFluid().copy(), light, overlay);
		}
		collector.submitModel(MODEL_AUTO_CURDLER, SubmitHelper.NO_STATE, stack, TEXTURE, light, overlay, -1, null, 0, null);
		stack.popPose();
	}
}
