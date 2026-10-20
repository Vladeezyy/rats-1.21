package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.entity.BlackDeathModel;
import com.github.alexthe666.rats.client.render.entity.layer.GlowingOverlayLayer;
import com.github.alexthe666.rats.server.entity.monster.boss.BlackDeath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class BlackDeathRenderer extends MobRenderer<BlackDeath, BlackDeathRenderer.State, BlackDeathModel> {
	private static final Identifier BLACK_DEATH_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/black_death/black_death.png");
	private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/black_death/black_death_overlay.png");

	public BlackDeathRenderer(EntityRendererProvider.Context context) {
		super(context, new BlackDeathModel(context.bakeLayer(RatsModelLayers.BLACK_DEATH)), 0.5F);
		this.addLayer(new GlowingOverlayLayer<>(this, GLOW_TEXTURE));
		this.addLayer(new ItemInHandLayer<>(this) {
			@Override
			public void submit(PoseStack stack, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
				if (state.summoning || state.meleeAttacking) {
					super.submit(stack, collector, light, state, yRot, xRot);
				}
			}
		});
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BlackDeath entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
		state.summoning = entity.isSummoning();
		state.meleeAttacking = entity.isMeleeAttacking();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return BLACK_DEATH_TEXTURE;
	}

	public static class State extends HumanoidRenderState {
		public boolean summoning;
		public boolean meleeAttacking;
	}
}
