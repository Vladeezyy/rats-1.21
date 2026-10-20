package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.server.entity.projectile.RatlantisArrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;

/** The Ratlantis arrow: drawn with the yellow glint only */
public class RatlantisArrowRenderer extends ArrowShapeRenderer<RatlantisArrow> {

	public RatlantisArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected RenderType renderType(RatlantisArrow entity) {
		return RatsRenderType.getYellowGlint();
	}

	@Override
	protected int light(State state) {
		return 1;
	}
}
