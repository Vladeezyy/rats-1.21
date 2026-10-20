package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.github.alexthe666.rats.server.block.entity.UpgradeSeparatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** The upgrade separator: its sawblade floating and turning above it */
public class UpgradeSeparatorRenderer extends StateBlockEntityRenderer<UpgradeSeparatorBlockEntity> {
	private final SubmitHelper items;
	private ItemStack sawblade = ItemStack.EMPTY;

	public UpgradeSeparatorRenderer(BlockEntityRendererProvider.Context context) {
		this.items = new SubmitHelper(context.itemModelResolver(), context.blockModelResolver());
	}

	@Override
	protected void submit(UpgradeSeparatorBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		if (this.sawblade.isEmpty()) {
			this.sawblade = new ItemStack(RatlantisItemRegistry.ANCIENT_SAWBLADE.get());
		}
		stack.pushPose();
		stack.translate(0.5D, 0.15D, 0.5D);
		float f = entity.ratRotationPrev + partialTicks;
		stack.translate(0.0F, 1F + Mth.sin(f * 0.1F) * 0.1F, 0.0F);
		float f1 = Mth.wrapDegrees((entity.ratRotation - entity.ratRotationPrev) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
		float f2 = entity.ratRotationPrev + f1 * partialTicks;
		stack.mulPose(Axis.YP.rotationDegrees(-f2 * 0.1F * (180F / (float) Math.PI)));
		stack.mulPose(Axis.ZP.rotationDegrees(180));
		stack.mulPose(Axis.XP.rotationDegrees(90));
		this.items.item(this.sawblade, ItemDisplayContext.FIXED, null, stack, collector, light, overlay);
		stack.popPose();
	}

	// the renderer gives the render bounds (the original overrode BlockEntity.getRenderBoundingBox)
	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public AABB getRenderBoundingBox(UpgradeSeparatorBlockEntity blockEntity) {
		return blockEntity.getRenderBoundingBox();
	}
}
