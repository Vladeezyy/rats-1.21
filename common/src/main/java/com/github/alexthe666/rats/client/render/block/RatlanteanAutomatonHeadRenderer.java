package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanAutomatonModel;
import com.github.alexthe666.rats.server.block.RatlanteanAutomatonHeadBlock;
import com.github.alexthe666.rats.server.block.entity.RatlanteanAutomatonHeadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/** The marbled cheese rat head: the automaton's head, bobbing, with its glow */
public class RatlanteanAutomatonHeadRenderer extends StateBlockEntityRenderer<RatlanteanAutomatonHeadBlockEntity> {
	private static final RatlanteanAutomatonModel<?> AUTOMATON_MODEL = new RatlanteanAutomatonModel<>(false);
	private static final RenderType GOLEM_TEXTURE = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_automaton/ratlantean_automaton.png"));
	private static final RenderType GLOW_TEXTURE = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_automaton/ratlantean_automaton_glow.png"));

	public RatlanteanAutomatonHeadRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void submit(RatlanteanAutomatonHeadBlockEntity te, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		BlockState state = te.getBlockState();
		if (state.getBlock() instanceof RatlanteanAutomatonHeadBlock) {
			submitHead(state.getValue(RatlanteanAutomatonHeadBlock.FACING).toYRot(), te.tickCount + partialTicks, stack, collector, light, overlay);
		}
	}

	/** The head at a rotation and age (the block, and the item through the special item model) */
	public static void submitHead(float rotation, float tickCount, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5F, -0.5F, 0.5F);
		stack.mulPose(Axis.XP.rotationDegrees(180));
		stack.mulPose(Axis.YP.rotationDegrees(rotation));
		for (RenderType type : new RenderType[] {GOLEM_TEXTURE, GLOW_TEXTURE}) {
			collector.submitCustomGeometry(stack, type, (pose, consumer) -> {
				AUTOMATON_MODEL.setTERotationAngles(tickCount);
				PoseStack drawStack = new PoseStack();
				drawStack.last().set(pose);
				AUTOMATON_MODEL.renderHead(drawStack, consumer, light, overlay, -1);
			});
		}
		stack.popPose();
	}
}
