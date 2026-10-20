package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.client.model.hats.AbstractHatModel;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.github.alexthe666.rats.server.items.HatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

/**
 * The hats of Rats (no vanilla equipment asset: the mod draws them, on players and other humanoids through
 * {@link com.github.alexthe666.rats.client.render.entity.layer.HatLayer}, on rats through the helmet layer).
 */
public final class HatRendering {
	/** A humanoid state that is never changed: models drawn with it keep their head at rest (rats move it themselves) */
	public static final HumanoidRenderState REST = new HumanoidRenderState();

	private HatRendering() {
	}

	/** Submits the hat's model posed from {@code state}; false when the stack is no hat with a model */
	public static boolean submitHat(ItemStack stack, HumanoidRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, int outlineColor) {
		if (!(stack.getItem() instanceof HatItem hat)) {
			return false;
		}
		AbstractHatModel model = RatsArmorModels.hatModel(stack.getItem());
		if (model == null) {
			return false;
		}
		// the ghost pirat hat glows (its model's render type is the glowing translucent one)
		int hatLight = stack.is(RatlantisItemRegistry.GHOST_PIRAT_HAT.get()) ? LightCoordsUtil.FULL_BRIGHT : light;
		collector.submitModel(model, state, poseStack, model.renderType(hat.getHatTexture()), hatLight, OverlayTexture.NO_OVERLAY, -1, null, outlineColor, null);
		if (stack.hasFoil()) {
			collector.submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(), hatLight, OverlayTexture.NO_OVERLAY, -1, null, outlineColor, null);
		}
		return true;
	}
}
