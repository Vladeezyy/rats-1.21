package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.PiratBoatModel;
import com.github.alexthe666.rats.client.model.entity.PiratCannonModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.client.render.entity.PiratBoatRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/** The pirat boat's sail (its banner) and cannon */
public class PiratBoatSailLayer extends RenderLayer<PiratBoatRenderer.State, PiratBoatModel> {
	public static final PiratCannonModel<?> MODEL_PIRAT_CANNON = new PiratCannonModel<>();
	public static final Identifier TEXTURE_PIRATE_CANNON = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/pirat/pirat_cannon.png");
	public static final Identifier TEXTURE_PIRATE_CANNON_FIRE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/pirat/pirat_cannon_fire.png");

	/** The cannon on a buccaneer rat's back (called from the upgrade item: the model types stay out of server classes) */
	public static void submitRatCannon(PoseStack stack, SubmitNodeCollector collector, int light, boolean firing) {
		collector.submitModel(MODEL_PIRAT_CANNON, SubmitHelper.NO_STATE, stack, RenderTypes.entityCutout(TEXTURE_PIRATE_CANNON), light, OverlayTexture.NO_OVERLAY, -1, null, 0);
		if (firing) {
			collector.submitModel(MODEL_PIRAT_CANNON, SubmitHelper.NO_STATE, stack, RenderTypes.eyes(TEXTURE_PIRATE_CANNON_FIRE), light, OverlayTexture.NO_OVERLAY, -1, null, 0);
		}
	}
	private final SubmitHelper items;

	public PiratBoatSailLayer(RenderLayerParent<PiratBoatRenderer.State, PiratBoatModel> parent, EntityRendererProvider.Context context) {
		super(parent);
		this.items = new SubmitHelper(context);
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, PiratBoatRenderer.State state, float yRot, float xRot) {
		int overlay = PiratBoatRenderer.overlay(state);
		stack.pushPose();
		stack.rotate(Axis.XP.rotationDegrees(180));
		stack.rotate(Axis.YP.rotationDegrees(90));
		stack.translate(0F, -0.8F, -0.9F);
		stack.scale(4F, 4F, 4F);
		this.items.item(state.banner, ItemDisplayContext.GROUND, null, stack, collector, light, overlay);
		stack.popPose();
		stack.pushPose();
		stack.rotate(Axis.YN.rotationDegrees(90));
		stack.translate(0, 0.1F, -0.6F);
		stack.scale(0.75F, 0.75F, 0.75F);
		collector.submitModel(MODEL_PIRAT_CANNON, SubmitHelper.NO_STATE, stack, RenderTypes.entityCutout(TEXTURE_PIRATE_CANNON), light, overlay, -1, null, state.outlineColor);
		if (state.firing) {
			collector.order(1).submitModel(MODEL_PIRAT_CANNON, SubmitHelper.NO_STATE, stack, RenderTypes.entityCutout(TEXTURE_PIRATE_CANNON_FIRE), 240, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		}
		stack.popPose();
	}
}
