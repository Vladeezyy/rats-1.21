package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.EmptyModel;
import com.github.alexthe666.rats.client.render.entity.layer.RatKingLayer;
import com.github.alexthe666.rats.server.entity.monster.boss.RatKing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** The rat king: no body of its own, its rats are drawn by {@link RatKingLayer} */
public class RatKingRenderer extends RatsMobRenderer<RatKing, EmptyModel<RatKing>> {
	private static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/black.png");

	public RatKingRenderer(EntityRendererProvider.Context context) {
		super(context, new EmptyModel<>(), 1.0F);
		this.addLayer(new RatKingLayer(this));
	}

	@Override
	public Vec3 getRenderOffset(RatsMobRenderState state) {
		return Vec3.ZERO;
	}

	@Override
	protected void setupRotations(RatsMobRenderState state, PoseStack stack, float bodyRot, float scale) {
		// the rats are placed around the king, which does not turn (only Dinnerbone and Grumm flip it)
		if (state.isUpsideDown) {
			stack.translate(0.0D, state.boundingBoxHeight + 0.1F, 0.0D);
			stack.rotate(Axis.ZP.rotationDegrees(180.0F));
		}
	}

	@Override
	protected Identifier getTexture(RatKing entity) {
		return TEXTURE_1;
	}
}
