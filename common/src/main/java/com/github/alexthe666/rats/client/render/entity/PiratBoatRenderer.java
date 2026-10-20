package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.entity.PiratBoatModel;
import com.github.alexthe666.rats.client.render.entity.layer.PiratBoatSailLayer;
import com.github.alexthe666.rats.server.entity.misc.PiratBoat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

/** The pirats' boat (a mob): drawn as a boat, with its sail (the pirat banner) and its cannon */
public class PiratBoatRenderer extends MobRenderer<PiratBoat, PiratBoatRenderer.State, PiratBoatModel> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/boat/spruce.png");
	private final Model.Simple waterPatch;

	public PiratBoatRenderer(EntityRendererProvider.Context context) {
		super(context, new PiratBoatModel(context.bakeLayer(RatsModelLayers.PIRAT_BOAT)), 0.0F);
		this.waterPatch = new Model.Simple(context.bakeLayer(RatsModelLayers.PIRAT_BOAT_WATER_PATCH), t -> RenderTypes.waterMask());
		this.addLayer(new PiratBoatSailLayer(this, context));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PiratBoat entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.boatYaw = entity.getYRot(partialTicks);
		state.hurt = (float) entity.hurtTime - partialTicks;
		state.damage = Mth.abs(entity.getHealth() - entity.getMaxHealth()) - partialTicks;
		state.rowingTimeLeft = entity.getRowingTime(0, partialTicks);
		state.rowingTimeRight = entity.getRowingTime(1, partialTicks);
		state.banner = entity.banner;
		state.firing = entity.isFiring();
		state.spectator = entity.isSpectator();
	}

	@Override
	public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		stack.pushPose();
		stack.rotate(Axis.YP.rotationDegrees(180.0F - state.boatYaw));
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.scale(1.2F, 1.2F, 1.2F);
		stack.translate(0.0D, -1.501F, 0.0D);
		stack.rotate(Axis.YP.rotationDegrees(90.0F));
		if (state.hurt > 0.0F) {
			stack.rotate(Axis.YP.rotationDegrees(Mth.sin(state.hurt) * state.hurt * state.damage / 10.0F));
		}
		boolean visible = this.isBodyVisible(state);
		boolean translucent = !visible && !state.isInvisibleToPlayer;
		RenderType rendertype = this.getRenderType(state, visible, translucent, state.appearsGlowing());
		if (rendertype != null) {
			collector.submitModel(this.model, state, stack, rendertype, state.lightCoords, overlay(state), ARGB.colorFromFloat(translucent ? 0.15F : 1.0F, 1.0F, 1.0F, 1.0F), null, state.outlineColor);
		}
		collector.submitModel(this.waterPatch, Unit.INSTANCE, stack, RenderTypes.waterMask(), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0);
		if (!state.spectator) {
			for (RenderLayer<State, PiratBoatModel> layer : this.layers) {
				layer.submit(stack, collector, state.lightCoords, state, 0.0F, 0.0F);
			}
		}
		stack.popPose();
	}

	/** The red death flash of the boat */
	public static int overlay(LivingEntityRenderState state) {
		return OverlayTexture.pack(state.deathTime > 0 ? (int) state.deathTime + 1 : 0, false);
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}

	public static class State extends LivingEntityRenderState {
		public float boatYaw;
		public float hurt;
		public float damage;
		public float rowingTimeLeft;
		public float rowingTimeRight;
		public ItemStack banner = ItemStack.EMPTY;
		public boolean firing;
		public boolean spectator;
	}
}
