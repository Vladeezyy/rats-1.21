package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.server.entity.mount.RatChickenMount;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** The chicken mount: vanilla's temperate chicken */
public class RatChickenMountRenderer extends MobRenderer<RatChickenMount, ChickenRenderState, ChickenModel> {
	private static final Identifier CHICKEN_TEXTURES = Identifier.withDefaultNamespace("textures/entity/chicken/chicken_temperate.png");

	public RatChickenMountRenderer(EntityRendererProvider.Context context) {
		super(context, new AdultChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
	}

	@Override
	public ChickenRenderState createRenderState() {
		return new ChickenRenderState();
	}

	@Override
	public void extractRenderState(RatChickenMount entity, ChickenRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flap = Mth.lerp(partialTicks, entity.oFlap, entity.wingRotation);
		state.flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.destPos);
	}

	@Override
	public Identifier getTextureLocation(ChickenRenderState state) {
		return CHICKEN_TEXTURES;
	}
}
