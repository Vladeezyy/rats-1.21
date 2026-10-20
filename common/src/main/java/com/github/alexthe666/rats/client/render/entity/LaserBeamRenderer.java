package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.projectile.LaserBeam;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** The Neo-Ratlantean's laser: the arrow's back cross only, tinted with the beam's color */
public class LaserBeamRenderer extends ArrowShapeRenderer<LaserBeam> {
	private static final RenderType RENDER_TYPE_RED = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/neo_ratlantean/laser_beam.png"));
	private static final RenderType RENDER_TYPE_BLUE = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/neo_ratlantean/laser_beam_blue.png"));

	public LaserBeamRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected RenderType renderType(LaserBeam entity) {
		return (int) (entity.getRGB()[0] * 255F) > 200 ? RENDER_TYPE_RED : RENDER_TYPE_BLUE;
	}

	@Override
	protected int color(LaserBeam entity) {
		float[] rgb = entity.getRGB();
		return ARGB.color(255, (int) (rgb[0] * 255F), (int) (rgb[1] * 255F), (int) (rgb[2] * 255F));
	}

	@Override
	protected int light(State state) {
		return 240;
	}

	@Override
	protected boolean shaft() {
		return false;
	}
}
