package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.github.alexthe666.rats.server.entity.projectile.DutchratSword;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The Dutchrat's thrown sword: the ghost pirat cutlass, spinning */
public class DutchratSwordRenderer extends EntityRenderer<DutchratSword, DutchratSwordRenderer.State> {
	private final SubmitHelper items;
	private ItemStack sword = ItemStack.EMPTY;

	public DutchratSwordRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.items = new SubmitHelper(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DutchratSword entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (this.sword.isEmpty()) {
			this.sword = new ItemStack(RatlantisItemRegistry.GHOST_PIRAT_CUTLASS.get());
		}
		stack.pushPose();
		stack.mulPose(Axis.YP.rotationDegrees(state.yRot - 180.0F));
		stack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
		stack.translate(0.0F, 0.5F, 0.0F);
		stack.scale(3.0F, 3.0F, 3.0F);
		stack.mulPose(Axis.YP.rotationDegrees(90.0F));
		stack.mulPose(Axis.ZP.rotationDegrees(state.ageInTicks * 20.0F));
		stack.translate(0.0F, -0.15F, 0.0F);
		this.items.item(this.sword, ItemDisplayContext.GROUND, null, stack, collector, state.lightCoords);
		stack.popPose();
		super.submit(state, stack, collector, camera);
	}

	public static class State extends EntityRenderState {
		public float yRot;
		public float xRot;
	}
}
