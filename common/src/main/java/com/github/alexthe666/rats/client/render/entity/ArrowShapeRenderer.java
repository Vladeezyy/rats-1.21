package com.github.alexthe666.rats.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.github.alexthe666.rats.server.entity.projectile.ArrowlikeProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

/** A bolt or beam drawn with {@link ArrowGeometry} */
public abstract class ArrowShapeRenderer<T extends Projectile> extends EntityRenderer<T, ArrowShapeRenderer.State> {

	protected ArrowShapeRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		int shakeTime = entity instanceof AbstractArrow arrow ? arrow.shakeTime : entity instanceof ArrowlikeProjectile bolt ? bolt.shakeTime : 0;
		state.shake = shakeTime - partialTicks;
		state.type = this.renderType(entity);
		state.color = this.color(entity);
	}

	protected abstract RenderType renderType(T entity);

	protected int color(T entity) {
		return -1;
	}

	/** The light of the vertices (full bright for the beams) */
	protected int light(State state) {
		return state.lightCoords;
	}

	protected float scale() {
		return 1.0F;
	}

	protected boolean shaft() {
		return true;
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		ArrowGeometry.submit(stack, collector, state.type, state.yRot, state.xRot, state.shake, this.scale(), this.light(state), state.color, this.shaft());
		super.submit(state, stack, collector, camera);
	}

	public static class State extends ArrowRenderState {
		public RenderType type;
		public int color = -1;
	}
}
