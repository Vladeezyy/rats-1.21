package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.block.RatTrapModel;
import com.github.alexthe666.rats.client.render.PosedModels;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.block.RatTrapBlock;
import com.github.alexthe666.rats.server.block.entity.RatTrapBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RatTrapRenderer extends StateBlockEntityRenderer<RatTrapBlockEntity> {
	private static final RatTrapModel<?> MODEL_RAT_TRAP = new RatTrapModel<>();
	private static final RenderType TEXTURE = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_trap.png"));
	private final SubmitHelper items;

	public RatTrapRenderer(BlockEntityRendererProvider.Context context) {
		this.items = new SubmitHelper(context.itemModelResolver(), context.blockModelResolver());
	}

	/** The item: the trap open, without bait */
	public static void submitItem(PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5D, 1.5D, 0.5D);
		stack.mulPose(Axis.XP.rotationDegrees(180));
		PosedModels.submit(collector, stack, TEXTURE, MODEL_RAT_TRAP, () -> MODEL_RAT_TRAP.animateHinge(0), light, overlay, -1);
		stack.popPose();
	}

	@Override
	protected void submit(RatTrapBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		float rotation = 0;
		float shutProgress = 0;
		ItemStack bait = ItemStack.EMPTY;
		BlockState state = entity.getBlockState();
		if (state.getBlock() instanceof RatTrapBlock) {
			rotation = state.getValue(RatTrapBlock.FACING).toYRot();
			shutProgress = entity.shutProgress;
			bait = entity.getBait();
		}
		float shut = shutProgress;
		stack.pushPose();
		stack.translate(0.5D, 1.5D, 0.5D);
		stack.mulPose(Axis.XP.rotationDegrees(180));
		stack.mulPose(Axis.YP.rotationDegrees(rotation));
		PosedModels.submit(collector, stack, TEXTURE, MODEL_RAT_TRAP, () -> MODEL_RAT_TRAP.animateHinge(shut), light, overlay, -1);
		if (!bait.isEmpty()) {
			stack.scale(0.4F, 0.4F, 0.4F);
			stack.translate(0, 3.4F, -0.5F);
			stack.mulPose(Axis.XP.rotationDegrees(90));
			stack.mulPose(Axis.YP.rotationDegrees(180));
			this.items.item(bait, ItemDisplayContext.FIXED, null, stack, collector, light, overlay);
		}
		stack.popPose();
	}
}
