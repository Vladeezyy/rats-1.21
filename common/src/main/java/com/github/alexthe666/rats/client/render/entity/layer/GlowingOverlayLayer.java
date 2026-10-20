package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.RatlanteanAutomatonModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Glowing parts (vanilla's eyes render type); the automaton also holds its sawblade */
public class GlowingOverlayLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
	private final RenderType renderType;
	private SubmitHelper items;
	private ItemStack sawblade = ItemStack.EMPTY;

	public GlowingOverlayLayer(RenderLayerParent<S, M> parent, Identifier texture) {
		super(parent);
		this.renderType = RenderTypes.eyes(texture);
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		collector.order(1).submitModel(this.getParentModel(), state, stack, this.renderType, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		if (this.getParentModel() instanceof RatlanteanAutomatonModel<?> automaton) {
			if (this.items == null) {
				this.items = SubmitHelper.fromGame();
				this.sawblade = new ItemStack(RatlantisItemRegistry.ANCIENT_SAWBLADE.get());
			}
			stack.pushPose();
			automaton.armLeft1.translateAndRotate(stack);
			automaton.armLeft2.translateAndRotate(stack);
			automaton.drillArm2.translateAndRotate(stack);
			automaton.drilArm3.translateAndRotate(stack);
			automaton.blade.translateAndRotate(stack);
			stack.mulPose(Axis.YP.rotationDegrees(90));
			this.items.item(this.sawblade, ItemDisplayContext.FIXED, null, stack, collector, light);
			stack.popPose();
		}
	}
}
