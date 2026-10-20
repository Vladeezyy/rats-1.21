package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesOverlayUpgrade;
import com.github.alexthe666.rats.server.misc.RatColorUtil;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;

/** Tamed rats: the angel and god glints, dyes, the upgrades' overlays and the toga */
public class TamedRatOverlayLayer<S extends RampartLivingRenderState> extends EntityLayer<TamedRat, S, AbstractRatModel<TamedRat>> {
	private static final RenderType TEXTURE_DYED_NOT = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/undyed_part.png"));
	private static final RenderType TEXTURE_DYED = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/dyed_part.png"), false);
	private static final RenderType TOGA_TEX = RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/upgrades/toga.png"));

	public TamedRatOverlayLayer(RenderLayerParent<S, AbstractRatModel<TamedRat>> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, TamedRat rat) {
		if (rat.getRespawnCountdown() > 0 && RatConfig.ratAngelGlint) {
			this.submitParent(stack, collector, state, RatsRenderType.getWhiteGlint(), light);
		} else if (rat.hasCustomName() && rat.getCustomName().getString().equalsIgnoreCase("ultrakill")) {
			this.submitParent(stack, collector, state, RatsRenderType.getGreenGlint(), light);
		} else {
			int order = 1;
			int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
			if (RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_GOD.get()) && RatConfig.ratGodGlint) {
				// the original drew the rat again with the foil buffer: the body is drawn, the glint goes on top
				this.submitParent(stack, collector, state, RenderTypes.entityGlint(), light, OverlayTexture.NO_OVERLAY, -1, order++);
			}
			if (rat.isDyed()) {
				if (rat.getDyeColor() == 100) {
					RenderType type = RatsRenderType.GlintType.getRenderTypeBasedOnKeyword(rat.getSpecialDye());
					this.submitParent(stack, collector, state, type != null ? type : RatsRenderType.getRainbowGlint(), light, overlay, -1, order++);
					if (!RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_UNDEAD.get())) {
						this.submitParent(stack, collector, state, TEXTURE_DYED_NOT, light, overlay, -1, order++);
					}
				} else {
					float[] color = RatColorUtil.getDyeRgb(DyeColor.byId(rat.getDyeColor()));
					this.submitParent(stack, collector, state, TEXTURE_DYED, light, overlay, ARGB.colorFromFloat(1.0F, color[0], color[1], color[2]), order++);
				}
			}
			int[] next = {order};
			RatUpgradeUtils.forEachUpgrade(rat, item -> item instanceof ChangesOverlayUpgrade, (upgrade, slot) -> {
				if (rat.isSlotVisible(slot)) {
					RenderType type = ((ChangesOverlayUpgrade) upgrade.getItem()).getOverlayTexture(upgrade, rat, state.partialTick);
					if (type != null) {
						this.submitParent(stack, collector, state, type, light, OverlayTexture.NO_OVERLAY, -1, next[0]++);
					}
				}
			});
			if (rat.hasToga()) {
				this.submitParent(stack, collector, state, TOGA_TEX, light, OverlayTexture.NO_OVERLAY, -1, next[0]);
			}
		}
	}
}
