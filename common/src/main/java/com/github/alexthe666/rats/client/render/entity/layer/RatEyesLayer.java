package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;

/** Eyes that glow in the dark (or the plague eyes) */
public class RatEyesLayer<T extends AbstractRat, S extends RampartLivingRenderState, M extends AbstractRatModel<T>> extends EntityLayer<T, S, M> {
	protected static final RenderType EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/eyes/glow.png"));
	protected static final RenderType PLAGUE_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/eyes/plague.png"));

	public RatEyesLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, T rat) {
		long roundedTime = rat.level().getDefaultClockTime() % 24000;
		boolean night = roundedTime >= 13000 && roundedTime <= 23000;
		BlockPos ratPos = rat.getLightPosition();
		int i = rat.level().getBrightness(LightLayer.SKY, ratPos);
		int j = rat.level().getBrightness(LightLayer.BLOCK, ratPos);
		int brightness = night ? j : Math.max(i, j);
		if (rat instanceof Rat plagueable && plagueable.hasPlague()) {
			this.submitParent(stack, collector, state, PLAGUE_EYES, light);
		} else if (brightness < 7) {
			this.submitParent(stack, collector, state, EYES, light);
		}
	}
}
