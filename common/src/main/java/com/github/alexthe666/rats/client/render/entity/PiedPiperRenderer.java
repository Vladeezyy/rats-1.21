package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.entity.PiedPiperModel;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.monster.PiedPiper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;

public class PiedPiperRenderer extends MobRenderer<PiedPiper, PiedPiperRenderer.State, PiedPiperModel> {
	private static final Identifier PIPER = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/pied_piper.png");

	public PiedPiperRenderer(EntityRendererProvider.Context context) {
		super(context, new PiedPiperModel(context.bakeLayer(RatsModelLayers.PIPER)), 0.5F);
		this.addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PiedPiper entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
		state.riding = entity.isPassenger();
		state.holdingFlute = entity.isHolding(RatsItemRegistry.RAT_FLUTE.get());
		state.fluteSpeed = 0.01F * (float) (entity.getId() % 10);
		state.tickCount = entity.tickCount;
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return PIPER;
	}

	public static class State extends ArmedEntityRenderState {
		public boolean riding;
		public boolean holdingFlute;
		public float fluteSpeed;
		public int tickCount;
	}
}
