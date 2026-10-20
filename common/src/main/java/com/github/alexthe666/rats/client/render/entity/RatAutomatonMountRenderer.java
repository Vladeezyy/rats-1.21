package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanAutomatonModel;
import com.github.alexthe666.rats.client.render.entity.layer.GlowingOverlayLayer;
import com.github.alexthe666.rats.server.entity.mount.RatAutomatonMount;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class RatAutomatonMountRenderer extends RatsMobRenderer<RatAutomatonMount, RatlanteanAutomatonModel<RatAutomatonMount>> {
	private static final Identifier MARBLED_CHEESE_GOLEM_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_automaton/ratlantean_automaton.png");
	private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_automaton/ratlantean_automaton_glow.png");
	private static final Vec3 OFFSET = new Vec3(0, 0.35F, 0);

	public RatAutomatonMountRenderer(EntityRendererProvider.Context context) {
		super(context, new RatlanteanAutomatonModel<>(true), 0.95F);
		this.addLayer(new GlowingOverlayLayer<>(this, GLOW_TEXTURE));
	}

	@Override
	public Vec3 getRenderOffset(RatsMobRenderState state) {
		return OFFSET;
	}

	@Override
	protected Identifier getTexture(RatAutomatonMount entity) {
		return MARBLED_CHEESE_GOLEM_TEXTURE;
	}
}
