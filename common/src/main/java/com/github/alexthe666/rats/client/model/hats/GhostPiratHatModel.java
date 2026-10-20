package com.github.alexthe666.rats.client.model.hats;

import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

/** The ghost pirat hat: glowing and translucent (drawn full bright by the hat layers) */
public class GhostPiratHatModel extends PiratHatModel {
	public GhostPiratHatModel(ModelPart root) {
		super(root, RatsRenderType::getGlowingTranslucent);
	}

}
