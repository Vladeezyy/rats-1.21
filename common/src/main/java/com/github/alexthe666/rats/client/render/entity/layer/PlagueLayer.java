package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartState;
import dev.vladeezyy.rats.platform.ClientPlatform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** The plague overlay every living renderer gets (rats draw their own) */
public class PlagueLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

	private static final RenderType TEXTURE = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/plague_overlay.png"));

	public PlagueLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		if (ClientPlatform.INSTANCE.isPlagued(state) && !(state instanceof RampartState rampart && rampart.rampart$pose().entity instanceof AbstractRat)) {
			collector.order(1).submitModel(this.getParentModel(), state, stack, TEXTURE, light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		}
	}
}
