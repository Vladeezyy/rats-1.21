package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatlanteanSpiritModel;
import com.github.alexthe666.rats.client.render.entity.layer.GlowingOverlayLayer;
import com.github.alexthe666.rats.server.entity.monster.PlagueCloud;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

public class RatlateanSpiritRenderer<T extends Mob> extends RatsMobRenderer<T, RatlanteanSpiritModel<T>> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/ratlantean_spirit.png");
	private static final Identifier TEXTURE_CLOUD = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/plague_cloud.png");
	private final boolean cloud;

	public RatlateanSpiritRenderer(EntityRendererProvider.Context context, boolean cloud) {
		super(context, new RatlanteanSpiritModel<>(), 0.5F);
		this.cloud = cloud;
		this.addLayer(new GlowingOverlayLayer<>(this, cloud ? TEXTURE_CLOUD : TEXTURE));
	}

	@Override
	protected Identifier getTexture(T entity) {
		return entity instanceof PlagueCloud ? TEXTURE_CLOUD : TEXTURE;
	}

	@Override
	protected void scale(RatsMobRenderState state, PoseStack stack) {
		float scale = this.cloud ? 2 : 1.5F;
		stack.translate(0.0F, -0.5F, 0.0F);
		stack.scale(scale, scale, scale);
	}
}
