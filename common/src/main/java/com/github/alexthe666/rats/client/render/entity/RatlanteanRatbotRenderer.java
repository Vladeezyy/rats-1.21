package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanRatbotModel;
import com.github.alexthe666.rats.client.render.entity.layer.RatbotEyesLayer;
import com.github.alexthe666.rats.server.entity.monster.RatlanteanRatbot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RatlanteanRatbotRenderer extends RatsMobRenderer<RatlanteanRatbot, RatlanteanRatbotModel<RatlanteanRatbot>> {
	private static final Identifier RATBOT_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_ratbot/ratlantean_ratbot.png");

	public RatlanteanRatbotRenderer(EntityRendererProvider.Context context) {
		super(context, new RatlanteanRatbotModel<>(0.0F), 0.5F);
		this.addLayer(new RatbotEyesLayer(this));
	}

	@Override
	protected Identifier getTexture(RatlanteanRatbot entity) {
		return RATBOT_TEXTURE;
	}

	@Override
	protected void scale(RatsMobRenderState state, PoseStack stack) {
		super.scale(state, stack);
		stack.scale(1.9F, 1.9F, 1.9F);
		if (!((double) state.walkAnimationSpeed < 0.01D)) {
			// the state's walk position is already the partial tick's (1.21.1: position - speed * (1 - partialTick))
			float f1 = state.walkAnimationPos + 6.0F;
			float f2 = (Math.abs(f1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
			stack.rotate(Axis.ZP.rotationDegrees(6.5F * f2));
		}
	}
}
