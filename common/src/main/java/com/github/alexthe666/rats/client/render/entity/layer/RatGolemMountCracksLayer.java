package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.RatGolemMountModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.entity.RatsMobRenderState;
import com.github.alexthe666.rats.server.entity.mount.RatGolemMount;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

import java.util.Map;

/** The iron golem's cracks on the golem mount */
public class RatGolemMountCracksLayer extends EntityLayer<RatGolemMount, RatsMobRenderState, RatGolemMountModel<RatGolemMount>> {
	private static final Map<RatGolemMount.Cracks, Identifier> CRACK_MAP = ImmutableMap.of(RatGolemMount.Cracks.LOW, Identifier.parse("textures/entity/iron_golem/iron_golem_crackiness_low.png"), RatGolemMount.Cracks.MEDIUM, Identifier.parse("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), RatGolemMount.Cracks.HIGH, Identifier.parse("textures/entity/iron_golem/iron_golem_crackiness_high.png"));

	public RatGolemMountCracksLayer(RenderLayerParent<RatsMobRenderState, RatGolemMountModel<RatGolemMount>> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatsMobRenderState state, RatGolemMount entity) {
		RatGolemMount.Cracks cracks = entity.getCracks();
		if (cracks != RatGolemMount.Cracks.NONE) {
			coloredCutoutModelCopyLayerRender(this.getParentModel(), CRACK_MAP.get(cracks), stack, collector, light, state, -1, 1);
		}
	}
}
