package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.render.HatRendering;
import com.github.alexthe666.rats.client.render.PartyHats;
import com.github.alexthe666.rats.server.items.PartyHatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

/**
 * The mod's hats on players, armor stands and other humanoids (they have no vanilla equipment asset): the hat's model
 * posed from the wearer's state, as vanilla's armor layer poses armor; the party hat on the parent model's head.
 */
public class HatLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

	public HatLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		ItemStack hat = state.headEquipment;
		if (hat.isEmpty()) {
			return;
		}
		if (hat.getItem() instanceof PartyHatItem) {
			stack.pushPose();
			this.getParentModel().head.translateAndRotate(stack);
			stack.translate(0.0F, -0.875F, 0.0F);
			PartyHats.submit(hat, stack, collector, light, state.outlineColor);
			stack.popPose();
		} else {
			HatRendering.submitHat(hat, state, stack, collector, light, state.outlineColor);
		}
	}
}
