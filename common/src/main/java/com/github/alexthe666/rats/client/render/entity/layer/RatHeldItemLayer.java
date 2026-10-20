package com.github.alexthe666.rats.client.render.entity.layer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.HoldsItemUpgrade;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RatHeldItemLayer<T extends AbstractRat, S extends RampartLivingRenderState, M extends AbstractRatModel<T>> extends EntityLayer<T, S, M> {

	private static ChestModel christmasChestModel;
	private final SubmitHelper items;
	private ItemStack featheryWing = ItemStack.EMPTY;

	public RatHeldItemLayer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context) {
		super(parent);
		this.items = new SubmitHelper(context);
	}

	/** Vanilla's chest model (the christmas upgrade's gift); baked on first use */
	/** The closed chest a Christmas rat carries (called from the upgrade item: the model types stay out of server classes) */
	public static void submitChristmasChest(PoseStack stack, SubmitNodeCollector collector, int light) {
		collector.submitModel(christmasChestModel(), 0.0F, stack, RenderTypes.entityCutout(Identifier.parse("textures/entity/chest/christmas.png")), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
	}

	public static ChestModel christmasChestModel() {
		if (christmasChestModel == null) {
			christmasChestModel = new ChestModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.CHEST));
		}
		return christmasChestModel;
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, T entity) {
		if (!entity.isBaby() && this.getParentModel() instanceof RatModel<?> model) {
			ItemStack itemstack = entity.getItemInHand(InteractionHand.MAIN_HAND);
			if (!itemstack.isEmpty()) {
				stack.pushPose();
				if (!entity.isHoldingItemInHands()) {
					this.getParentModel().translateToHead(stack);
					stack.mulPose(Axis.ZP.rotationDegrees(180));
					stack.mulPose(Axis.YP.rotationDegrees(180));
					stack.mulPose(Axis.XP.rotationDegrees(90));
					stack.translate(0F, 0.35F, 0.05F);
					stack.scale(0.5F, 0.5F, 0.5F);
					this.items.item(itemstack, ItemDisplayContext.FIXED, entity, stack, collector, light);
				} else {
					boolean platter = entity instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_PLATTER.get());
					ItemDisplayContext display = platter ? ItemDisplayContext.FIXED : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
					this.items.resolve(itemstack, display, entity);
					boolean model3d = this.items.resolvedIs3d();
					if (platter) {
						this.translateToHand(model, true, stack);
						stack.translate(-0.125F, -0.15F, -0.2F);
						if (model3d) {
							stack.mulPose(Axis.XP.rotationDegrees(110));
							stack.mulPose(Axis.YP.rotationDegrees(10));
							stack.mulPose(Axis.ZN.rotationDegrees(3));
							stack.translate(0.0F, 0.2F, 0.025F);
						} else {
							stack.mulPose(Axis.XP.rotationDegrees(20));
							stack.mulPose(Axis.YP.rotationDegrees(3));
							stack.mulPose(Axis.ZP.rotationDegrees(10));
							stack.translate(0.0F, -0.025F, 0.09F);
							stack.scale(0.75F, 0.75F, 0.75F);
						}
						stack.scale(0.5F, 0.5F, 0.5F);
					} else {
						this.translateToHand(model, true, stack);
						stack.mulPose(Axis.ZP.rotationDegrees(180));
						stack.mulPose(Axis.XP.rotationDegrees(20));
						stack.scale(0.65F, 0.65F, 0.65F);
						stack.translate(0.0F, -0.075F, -0.2F);
					}
					this.items.submitResolved(stack, collector, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
					if (platter && !model3d && itemstack.getCount() > 5) {
						//this ensures our stacks dont constantly rotate around either when rendering on the plate or when the stack size changes
						RandomSource random = RandomSource.create(itemstack.getItem().hashCode());
						for (int i = 0; i < itemstack.getCount() / 5; i++) {
							stack.translate(0.0D, 0.025D, 0.0D);
							stack.mulPose(Axis.ZP.rotationDegrees(random.nextIntBetweenInclusive(-90, 90)));
							stack.translate(0.0D, -0.025D, 0.05D);
							this.items.submitResolved(stack, collector, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
						}
					}
				}
				stack.popPose();
			}

			if (entity instanceof TamedRat rat) {
				if (rat.getRespawnCountdown() > 0) {
					if (this.featheryWing.isEmpty()) {
						this.featheryWing = new ItemStack(RatsItemRegistry.FEATHERY_WING.get());
					}
					stack.pushPose();
					float wingAngle = 0;
					float wingFold = -45.0F;
					this.getParentModel().translateToBody(stack);
					stack.pushPose();
					stack.mulPose(Axis.ZN.rotationDegrees(wingAngle));
					stack.mulPose(Axis.YP.rotationDegrees(wingFold));
					stack.mulPose(Axis.XN.rotationDegrees(90));
					stack.translate(0.2F, 0.0F, -0.15F);
					this.items.item(this.featheryWing, ItemDisplayContext.GROUND, rat, stack, collector, light);
					stack.popPose();
					stack.pushPose();
					stack.mulPose(Axis.ZP.rotationDegrees(wingAngle));
					stack.mulPose(Axis.YN.rotationDegrees(wingFold));
					stack.mulPose(Axis.XN.rotationDegrees(90));
					stack.mulPose(Axis.YP.rotationDegrees(180));
					stack.translate(0.2F, -0.0F, 0.15F);
					this.items.item(this.featheryWing, ItemDisplayContext.GROUND, rat, stack, collector, light);
					stack.popPose();
					stack.popPose();
				} else {
					RatUpgradeUtils.forEachUpgrade(rat, item -> item instanceof HoldsItemUpgrade, (stack1, slot) -> {
						if (rat.isSlotVisible(slot)) {
							stack.pushPose();
							((HoldsItemUpgrade) stack1.getItem()).renderHeldItem(this.items, rat, model, stack, collector, light, state.ageInTicks);
							stack.popPose();
						}
					});
				}
			}
		}
	}

	protected void translateToHand(RatModel<?> model, boolean left, PoseStack stack) {
		model.body1.translateRotate(stack);
		model.body2.translateRotate(stack);
		if (left) {
			model.leftArm.translateRotate(stack);
			model.leftHand.translateRotate(stack);
		} else {
			model.rightArm.translateRotate(stack);
			model.rightHand.translateRotate(stack);
		}
	}
}
