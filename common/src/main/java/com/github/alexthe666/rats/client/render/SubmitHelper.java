package com.github.alexthe666.rats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Draws items and blocks from renderers and layers (26.x): the stack or block state is resolved into a render state that
 * this helper reuses, and submitted at once (submitting copies what the frame needs, so the state can be reused).
 * One helper per renderer or layer; the render thread is the only user.
 */
public final class SubmitHelper {
	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
	/** The state for a Citadel-style model posed by hand (never changed): drawing it only copies the current pose */
	public static final EntityRenderState NO_STATE = new EntityRenderState();
	private final ItemModelResolver items;
	private final BlockModelResolver blocks;
	private final ItemStackRenderState itemState = new ItemStackRenderState();
	private final BlockModelRenderState blockState = new BlockModelRenderState();

	public SubmitHelper(EntityRendererProvider.Context context) {
		this(context.getItemModelResolver(), context.getBlockModelResolver());
	}

	public SubmitHelper(ItemModelResolver items, BlockModelResolver blocks) {
		this.items = items;
		this.blocks = blocks;
	}

	/** A helper for code without a renderer context (block entity renderers, events) */
	public static SubmitHelper fromGame() {
		Minecraft minecraft = Minecraft.getInstance();
		return new SubmitHelper(minecraft.getItemModelResolver(), new BlockModelResolver(minecraft.getModelManager()));
	}

	/** An item as {@code ItemRenderer.renderStatic} drew it; {@code holder} gives the model its owner (and seed) */
	public void item(ItemStack stack, ItemDisplayContext display, @Nullable Entity holder, PoseStack poseStack, SubmitNodeCollector collector, int light) {
		this.item(stack, display, holder, poseStack, collector, light, OverlayTexture.NO_OVERLAY);
	}

	public void item(ItemStack stack, ItemDisplayContext display, @Nullable Entity holder, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
		if (!stack.isEmpty()) {
			this.resolve(stack, display, holder);
			this.submitResolved(poseStack, collector, light, overlay);
		}
	}

	/** Resolves the item's model (for {@link #resolvedIs3d()}); {@link #submitResolved} then draws it */
	public void resolve(ItemStack stack, ItemDisplayContext display, @Nullable Entity holder) {
		this.items.updateForTopItem(this.itemState, stack, display, holder == null ? null : holder.level(), holder instanceof ItemOwner owner ? owner : null, holder == null ? 0 : holder.getId() + display.ordinal());
	}

	/** Whether the resolved item is drawn as a 3D (block-lit) model, 1.21.1's {@code BakedModel.isGui3d} */
	public boolean resolvedIs3d() {
		return this.itemState.usesBlockLight();
	}

	public void submitResolved(PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
		this.itemState.submit(poseStack, collector, light, overlay, 0);
	}

	/** A block model as {@code BlockRenderDispatcher.renderSingleBlock} drew it (the block from 0,0,0 to 1,1,1) */
	public void block(BlockState state, PoseStack poseStack, SubmitNodeCollector collector, int light) {
		this.blocks.update(this.blockState, state, BLOCK_DISPLAY_CONTEXT);
		this.blockState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
	}
}
