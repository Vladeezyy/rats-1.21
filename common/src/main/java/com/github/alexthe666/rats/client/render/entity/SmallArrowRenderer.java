package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.server.entity.projectile.SmallArrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** The rats' arrows: a vanilla arrow at half size */
public class SmallArrowRenderer extends ArrowShapeRenderer<SmallArrow> {
	public static final Identifier ARROW = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");

	public SmallArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected RenderType renderType(SmallArrow entity) {
		return RenderTypes.entityCutoutCull(ARROW);
	}

	@Override
	protected float scale() {
		return 0.5F;
	}
}
