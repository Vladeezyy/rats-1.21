package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.projectile.GolemBeam;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class GolemBeamRenderer extends ArrowShapeRenderer<GolemBeam> {
	private static final RenderType TYPE = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_automaton/automaton_beam.png"));

	public GolemBeamRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected RenderType renderType(GolemBeam entity) {
		return TYPE;
	}

	@Override
	protected int light(State state) {
		return 240;
	}
}
