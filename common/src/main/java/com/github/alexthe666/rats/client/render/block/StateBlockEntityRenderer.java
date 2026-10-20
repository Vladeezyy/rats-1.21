package com.github.alexthe666.rats.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** A block entity renderer that submits from the block entity itself (1.21.1's render arguments) */
public abstract class StateBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, BlockEntityState<T>> {

	@Override
	public BlockEntityState<T> createRenderState() {
		return new BlockEntityState<>();
	}

	@Override
	public void extractRenderState(T blockEntity, BlockEntityState<T> state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTick = partialTicks;
	}

	@Override
	public void submit(BlockEntityState<T> state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		this.submit(state.blockEntity, state.partialTick, stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
	}

	protected abstract void submit(T blockEntity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay);
}
