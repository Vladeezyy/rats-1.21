package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.FlyingDutchratModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.client.render.entity.layer.DutchratHelmetLayer;
import com.github.alexthe666.rats.server.entity.monster.boss.Dutchrat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DutchratRenderer extends RatsMobRenderer<Dutchrat, FlyingDutchratModel<Dutchrat>> {
	private static final Identifier DUTCHRAT_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/dutchrat/dutchrat.png");

	public DutchratRenderer(EntityRendererProvider.Context context) {
		super(context, new FlyingDutchratModel<>(), 0.5F);
		this.addLayer(new DutchratGlowLayer(this));
		this.addLayer(new DutchratHelmetLayer(this, context));
		this.addLayer(new SwordLayer(this, context));
	}

	@Override
	protected Identifier getTexture(Dutchrat entity) {
		return DUTCHRAT_TEXTURE;
	}

	public static class DutchratGlowLayer extends EntityLayer<Dutchrat, RatsMobRenderState, FlyingDutchratModel<Dutchrat>> {
		private static final Identifier GLOW_1 = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/dutchrat/dutchrat_glow_1.png");
		private static final Identifier GLOW_2 = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/dutchrat/dutchrat_glow_2.png");

		public DutchratGlowLayer(RenderLayerParent<RatsMobRenderState, FlyingDutchratModel<Dutchrat>> parent) {
			super(parent);
		}

		@Override
		protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatsMobRenderState state, Dutchrat entity) {
			this.submitParent(stack, collector, state, RatsRenderType.getGlowingTranslucent(GLOW_1), light, OverlayTexture.NO_OVERLAY, -1, 1);
			this.submitParent(stack, collector, state, RatsRenderType.getGlowingTranslucent(GLOW_2), light, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(0.5F, 1.0F, 1.0F, 1.0F), 2);
		}
	}

	/** The held sword (1.21.1's item in hand layer, while the sword is not thrown and no bell summons) */
	private static class SwordLayer extends EntityLayer<Dutchrat, RatsMobRenderState, FlyingDutchratModel<Dutchrat>> {
		private final SubmitHelper items;

		SwordLayer(RenderLayerParent<RatsMobRenderState, FlyingDutchratModel<Dutchrat>> parent, EntityRendererProvider.Context context) {
			super(parent);
			this.items = new SubmitHelper(context);
		}

		@Override
		protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatsMobRenderState state, Dutchrat entity) {
			if (!entity.hasThrownSword() && entity.getBellSummonTicks() <= 0) {
				boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
				this.submitArm(entity, rightHanded ? entity.getMainHandItem() : entity.getOffhandItem(), HumanoidArm.RIGHT, stack, collector, light);
				this.submitArm(entity, rightHanded ? entity.getOffhandItem() : entity.getMainHandItem(), HumanoidArm.LEFT, stack, collector, light);
			}
		}

		private void submitArm(Dutchrat entity, ItemStack item, HumanoidArm arm, PoseStack stack, SubmitNodeCollector collector, int light) {
			if (!item.isEmpty()) {
				stack.pushPose();
				this.getParentModel().translateToHand(arm, stack);
				stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
				stack.mulPose(Axis.YP.rotationDegrees(180.0F));
				boolean left = arm == HumanoidArm.LEFT;
				stack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
				this.items.item(item, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity, stack, collector, light);
				stack.popPose();
			}
		}
	}
}
