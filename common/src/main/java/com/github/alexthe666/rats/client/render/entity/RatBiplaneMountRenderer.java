package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.BiplaneModel;
import com.github.alexthe666.rats.server.entity.mount.RatBiplaneMount;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RatBiplaneMountRenderer extends RatsMobRenderer<RatBiplaneMount, BiplaneModel<RatBiplaneMount>> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/mounts/biplane_mount.png");

	public RatBiplaneMountRenderer(EntityRendererProvider.Context context) {
		super(context, new BiplaneModel<>(), 1.65F);
	}

	@Override
	protected Identifier getTexture(RatBiplaneMount entity) {
		return TEXTURE;
	}
}
