package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatGolemMountModel;
import com.github.alexthe666.rats.client.render.entity.layer.RatGolemMountCracksLayer;
import com.github.alexthe666.rats.server.entity.mount.RatGolemMount;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class RatGolemMountRenderer extends RatsMobRenderer<RatGolemMount, RatGolemMountModel<RatGolemMount>> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/mounts/golem_mount.png");

	public RatGolemMountRenderer(EntityRendererProvider.Context context) {
		super(context, new RatGolemMountModel<>(), 0.75F);
		this.addLayer(new RatGolemMountCracksLayer(this));
	}

	@Override
	protected Identifier getTexture(RatGolemMount entity) {
		return TEXTURE;
	}
}
