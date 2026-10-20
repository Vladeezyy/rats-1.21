package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.block.TrashCanModel;
import com.github.alexthe666.rats.client.render.PosedModels;
import com.github.alexthe666.rats.server.block.TrashCanBlock;
import com.github.alexthe666.rats.server.block.entity.TrashCanBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class TrashCanRenderer extends StateBlockEntityRenderer<TrashCanBlockEntity> {
	private static final TrashCanModel<?> MODEL_TRASH_CAN = new TrashCanModel<>();
	private static final RenderType TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/trash_can.png"), true);

	public TrashCanRenderer(BlockEntityRendererProvider.Context context) {
	}

	/** The item: the trash can closed (the default pose) */
	public static void submitItem(PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5D, 1.501D, 0.5D);
		stack.mulPose(Axis.XP.rotationDegrees(180));
		PosedModels.submit(collector, stack, TEXTURE, MODEL_TRASH_CAN, MODEL_TRASH_CAN::resetToDefaultPose, light, overlay, -1);
		stack.popPose();
	}

	@Override
	protected void submit(TrashCanBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		float rotation = 0;
		BlockState state = entity.getBlockState();
		if (state.getBlock() instanceof TrashCanBlock) {
			rotation = state.getValue(TrashCanBlock.FACING).toYRot();
		}
		stack.pushPose();
		stack.translate(0.5D, 1.501D, 0.5D);
		stack.mulPose(Axis.XP.rotationDegrees(180));
		stack.mulPose(Axis.YP.rotationDegrees(rotation));
		PosedModels.submit(collector, stack, TEXTURE, MODEL_TRASH_CAN, () -> MODEL_TRASH_CAN.animate(entity), light, overlay, -1);
		stack.popPose();
	}
}
