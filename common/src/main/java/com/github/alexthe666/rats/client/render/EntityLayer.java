package com.github.alexthe666.rats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

/**
 * A layer of a mob drawn with a Citadel-style model (26.x): the render state carries the entity (Rampart's pose), so
 * the layer reads the entity as the 1.21.1 layers did, and draws by submitting to the collector.
 */
public abstract class EntityLayer<T extends LivingEntity, S extends RampartLivingRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

	protected EntityLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		if (state.pose.entity != null) {
			this.submit(poseStack, collector, light, state, (T) state.pose.entity);
		}
	}

	protected abstract void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, T entity);

	/** The render state the parent model was last posed for here (states are new every frame) */
	private static Object posedFor;

	/**
	 * Poses the parent model for this state now, for layers that read its parts (translateToHead: hats, the item in the
	 * mouth). 1.21.1 posed it before the layers; 26.x poses it when it is drawn, after the layers ran, so they read the
	 * pose of the rat drawn before (hats floated above or beside the head). Once per entity and frame.
	 */
	protected void poseParent(S state) {
		if (posedFor != state) {
			this.getParentModel().setupAnim(state);
			posedFor = state;
		}
	}

	/**
	 * The parent model again (posed from the state when it is drawn) with another render type. {@code order} draws it
	 * after the body (vanilla's overlays use 1; stacked overlays count up).
	 */
	protected void submitParent(PoseStack poseStack, SubmitNodeCollector collector, S state, RenderType type, int light, int overlay, int color, int order) {
		collector.order(order).submitModel(this.getParentModel(), state, poseStack, type, light, overlay, color, null, state.outlineColor, null);
	}

	protected void submitParent(PoseStack poseStack, SubmitNodeCollector collector, S state, RenderType type, int light) {
		this.submitParent(poseStack, collector, state, type, light, OverlayTexture.NO_OVERLAY, -1, 1);
	}
}
