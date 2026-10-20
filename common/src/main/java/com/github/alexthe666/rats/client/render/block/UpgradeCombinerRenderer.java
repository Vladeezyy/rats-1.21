package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanSpiritModel;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.block.entity.UpgradeCombinerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** The upgrade combiner: its spirit floating and turning above it */
public class UpgradeCombinerRenderer extends StateBlockEntityRenderer<UpgradeCombinerBlockEntity> {
	private static final RatlanteanSpiritModel<?> MODEL_SPIRIT = new RatlanteanSpiritModel<>();
	private static final RenderType TEXTURE = RatsRenderType.getGlowingTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/upgrade_combiner.png"));

	public UpgradeCombinerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void submit(UpgradeCombinerBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5D, 0.0D, 0.5D);
		float f = (float) entity.tickCount + partialTicks;
		stack.translate(0.0F, 3.25F + Mth.sin(f * 0.1F) * 0.1F, 0.0F);
		float f1 = Mth.wrapDegrees((entity.ratRotation - entity.ratRotationPrev) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
		float f2 = entity.ratRotationPrev + f1 * partialTicks;
		stack.rotate(Axis.YP.rotationDegrees(-f2 * (180F / (float) Math.PI) - 90F));
		stack.rotate(Axis.ZP.rotationDegrees(180));
		stack.scale(1.5F, 1.5F, 1.5F);
		collector.submitModel(MODEL_SPIRIT, SubmitHelper.NO_STATE, stack, TEXTURE, 244, overlay, ARGB.colorFromFloat(0.5F, 1.0F, 1.0F, 1.0F), null, 0);
		stack.popPose();
	}

	// the renderer gives the render bounds (the original overrode BlockEntity.getRenderBoundingBox)
	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public AABB getRenderBoundingBox(UpgradeCombinerBlockEntity blockEntity) {
		return blockEntity.getRenderBoundingBox();
	}
}
