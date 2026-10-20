package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.CubeModel;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.server.entity.projectile.ThrownBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** A block thrown by a psychic rat: the block, wrapped in the psychic swirl */
public class ThrownBlockRenderer extends EntityRenderer<ThrownBlock, ThrownBlockRenderer.State> {
	private static final Identifier LIGHTNING_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/psychic.png");
	private final CubeModel cube;
	private final SubmitHelper blocks;

	public ThrownBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.cube = new CubeModel(context.bakeLayer(RatsModelLayers.THROWN_BLOCK));
		this.blocks = new SubmitHelper(context);
		this.shadowRadius = 0.5F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ThrownBlock entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.block = entity.getHeldBlockState();
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		float f = state.ageInTicks;
		if (state.block != null) {
			stack.pushPose();
			stack.translate(-0.5D, 0, 0.5D);
			stack.mulPose(Axis.YP.rotationDegrees(90.0F));
			this.blocks.block(state.block, stack, collector, state.lightCoords);
			stack.popPose();
		}
		stack.pushPose();
		stack.scale(1F, -1F, 1F);
		stack.translate(0F, -0.5F, 0F);
		stack.mulPose(Axis.YP.rotationDegrees(state.yRot - 180));
		collector.submitModel(this.cube, Unit.INSTANCE, stack, RenderTypes.energySwirl(LIGHTNING_TEXTURE, f * 0.01F, f * 0.01F), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}

	public static class State extends EntityRenderState {
		public float yRot;
		@Nullable
		public BlockState block;
	}
}
