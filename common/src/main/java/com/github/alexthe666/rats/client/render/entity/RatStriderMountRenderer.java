package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.entity.RatStriderMountModel;
import com.github.alexthe666.rats.server.entity.mount.RatStriderMount;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.StriderRenderState;
import net.minecraft.resources.Identifier;

public class RatStriderMountRenderer extends MobRenderer<RatStriderMount, StriderRenderState, RatStriderMountModel> {
	private static final Identifier STRIDER_LOCATION = Identifier.withDefaultNamespace("textures/entity/strider/strider.png");
	private static final Identifier COLD_LOCATION = Identifier.withDefaultNamespace("textures/entity/strider/strider_cold.png");
	private static final Identifier SADDLE_LOCATION = Identifier.withDefaultNamespace("textures/entity/equipment/strider_saddle/saddle.png");

	public RatStriderMountRenderer(EntityRendererProvider.Context context) {
		super(context, new RatStriderMountModel(context.bakeLayer(RatsModelLayers.RAT_STRIDER_MOUNT)), 0.5F);
		this.addLayer(new AlwaysSaddledLayer(this));
	}

	@Override
	public StriderRenderState createRenderState() {
		return new StriderRenderState();
	}

	@Override
	public void extractRenderState(RatStriderMount entity, StriderRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.isSuffocating = entity.isSuffocating();
		state.isRidden = entity.isVehicle();
	}

	@Override
	public Identifier getTextureLocation(StriderRenderState state) {
		return state.isSuffocating ? COLD_LOCATION : STRIDER_LOCATION;
	}

	@Override
	protected boolean isShaking(StriderRenderState state) {
		return super.isShaking(state) || state.isSuffocating;
	}

	/** The saddle, always on (the mount is ridden by its rat) */
	public static class AlwaysSaddledLayer extends RenderLayer<StriderRenderState, RatStriderMountModel> {
		public AlwaysSaddledLayer(RenderLayerParent<StriderRenderState, RatStriderMountModel> parent) {
			super(parent);
		}

		@Override
		public void submit(PoseStack stack, SubmitNodeCollector collector, int light, StriderRenderState state, float yRot, float xRot) {
			coloredCutoutModelCopyLayerRender(this.getParentModel(), SADDLE_LOCATION, stack, collector, light, state, -1, 1);
		}
	}
}
