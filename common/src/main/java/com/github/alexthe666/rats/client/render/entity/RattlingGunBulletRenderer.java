package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.projectile.RattlingGunBullet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class RattlingGunBulletRenderer extends ArrowShapeRenderer<RattlingGunBullet> {
	private static final RenderType TYPE = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rattling_gun/rattling_gun_bullet.png"));

	public RattlingGunBulletRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected RenderType renderType(RattlingGunBullet entity) {
		return TYPE;
	}
}
