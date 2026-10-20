package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.BiplaneModel;
import com.github.alexthe666.rats.server.entity.monster.boss.RatBaronPlane;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RatBaronPlaneRenderer extends RatsMobRenderer<RatBaronPlane, BiplaneModel<RatBaronPlane>> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat_baron_plane.png");

	public RatBaronPlaneRenderer(EntityRendererProvider.Context context) {
		super(context, new BiplaneModel<>(), 1.65F);
	}

	@Override
	protected Identifier getTexture(RatBaronPlane entity) {
		return TEXTURE;
	}
}
