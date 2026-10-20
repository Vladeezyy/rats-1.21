package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.hats.PartyHatModel;
import com.github.alexthe666.rats.server.items.PartyHatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

/** The party hat: two layers, the hat's dye color and its inverse, at rest where the pose stack puts it */
public final class PartyHats {
	private static final RenderType LAYER_1 = RenderTypes.armorCutoutNoCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/model/hat/party_hat_layer_1.png"));
	private static final RenderType LAYER_2 = RenderTypes.armorCutoutNoCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/model/hat/party_hat_layer_2.png"));
	private static PartyHatModel model;

	private PartyHats() {
	}

	public static void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int outlineColor) {
		if (model == null) {
			model = new PartyHatModel(Minecraft.getInstance().getEntityModels().bakeLayer(RatsModelLayers.PARTY_HAT));
		}
		int color = PartyHatItem.getColor(stack);
		collector.submitModel(model, HatRendering.REST, poseStack, LAYER_1, light, OverlayTexture.NO_OVERLAY, ARGB.opaque(color), null, outlineColor, null);
		collector.submitModel(model, HatRendering.REST, poseStack, LAYER_2, light, OverlayTexture.NO_OVERLAY, ARGB.opaque(invertColor(color)), null, outlineColor, null);
		if (stack.hasFoil()) {
			collector.submitModel(model, HatRendering.REST, poseStack, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, outlineColor, null);
		}
	}

	private static int invertColor(int color) {
		int a = (color >> 24) & 0xff;
		int r = 255 - ((color >> 16) & 0xff);
		int g = 255 - ((color >> 8) & 0xff);
		int b = 255 - (color & 0xff);
		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}
}
