package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.HatRendering;
import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.HatItem;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.Optional;

/**
 * What a rat wears on its head (helmets, the mod's hats, skulls and blocks) and its banner. Helmets are drawn with
 * vanilla's equipment layers (dye, trims, glint) on a head-only armor model kept at rest; the rat's head moves it.
 */
public class RatHelmetLayer<T extends AbstractRat, S extends RampartLivingRenderState, M extends AbstractRatModel<T>> extends EntityLayer<T, S, M> {
	private final HumanoidModel<HumanoidRenderState> helmetModel;
	private final EquipmentLayerRenderer equipment;
	private final SubmitHelper items;
	private ItemStack halo = ItemStack.EMPTY;

	public RatHelmetLayer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context) {
		super(parent);
		this.helmetModel = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new).head();
		this.equipment = context.getEquipmentRenderer();
		this.items = new SubmitHelper(context);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, T rat) {
		ItemStack itemstack = rat.getItemBySlot(EquipmentSlot.HEAD);
		//Rats: always render a halo on top of dead rats
		if (rat instanceof TamedRat tamed && tamed.getRespawnCountdown() > 0) {
			if (this.halo.isEmpty()) {
				this.halo = new ItemStack(RatsItemRegistry.HALO_HAT.get());
			}
			itemstack = this.halo;
		}
		if (!itemstack.isEmpty()) {
			stack.pushPose();
			Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
			Optional<ResourceKey<EquipmentAsset>> asset = equippable != null && equippable.slot() == EquipmentSlot.HEAD ? equippable.assetId() : Optional.empty();
			if (asset.isPresent() || itemstack.getItem() instanceof HatItem) {
				//Rats: do some extra transforms based on which model is being used and what item is rendering.
				this.getParentModel().translateToHead(stack);
				if (rat.isBaby()) {
					stack.translate(0.0D, 0.025D, -0.05D);
					stack.scale(0.65F, 0.65F, 0.65F);
				}
				stack.translate(0, -0.325F, -0.045F);
				stack.scale(0.55F, 0.55F, 0.55F);
				if (itemstack.getItem() instanceof HatItem hat) {
					hat.transformOnHead(rat, stack);
					HatRendering.submitHat(itemstack, HatRendering.REST, stack, collector, light, state.outlineColor);
				} else {
					// vanilla's armor layers: dyed leather, trims and the glint; the Ratlantis helmet has its own model
					HumanoidModel<HumanoidRenderState> model = itemstack.getItem() instanceof RatlantisArmorItem ? RatsArmorModels.ratlantisArmorModel(EquipmentSlot.HEAD) : this.helmetModel;
					this.equipment.renderLayers(EquipmentClientInfo.LayerType.HUMANOID, asset.get(), model, HatRendering.REST, itemstack, stack, collector, light, state.outlineColor);
				}
			} else if (!itemstack.is(RatsItemRegistry.PARTY_HAT.get())) {
				//Rats: handle some special case hats in the mod
				this.getParentModel().translateToHead(stack);
				stack.translate(0, 0.025F, -0.15F);
				stack.rotate(Axis.XP.rotationDegrees(180));
				stack.rotate(Axis.YP.rotationDegrees(180));
				stack.scale(0.5F, 0.5F, 0.5F);
				if (itemstack.is(RatlantisBlockRegistry.MARBLED_CHEESE_RAT_HEAD.get().asItem())) {
					stack.translate(0, -0.1F, 0.1F);
					stack.rotate(Axis.XP.rotationDegrees(15));
					if (rat.isBaby()) {
						stack.scale(0.4F, 0.4F, 0.4F);
						stack.translate(0.0D, 0.25D, 0.0D);
					}
				} else if (itemstack.is(ItemTags.SKULLS) && BuiltInRegistries.ITEM.getKey(itemstack.getItem()).getNamespace().equals("minecraft")) {
					stack.rotate(Axis.YP.rotationDegrees(180));
					stack.translate(0.0D, 0.55D, -0.0D);
					stack.scale(2.0F, 2.0F, 2.0F);
					if (rat.isBaby()) {
						stack.scale(0.3F, 0.3F, 0.3F);
						stack.translate(0.0D, -0.75D, -0.25D);
					}
				}
				stack.translate(0.0D, -0.1D, 0.0D);
				stack.scale(0.85F, 0.85F, 0.85F);
				this.items.item(itemstack, ItemDisplayContext.HEAD, rat, stack, collector, light);
			}
			stack.popPose();

			//Rats: render a banner if the rat has one in its banner slot
			ItemStack banner = rat.getItemBySlot(EquipmentSlot.OFFHAND);
			if (banner.getItem() instanceof BannerItem) {
				stack.pushPose();
				this.getParentModel().translateToBody(stack);
				stack.translate(0.0D, -0.5D, -0.2D);
				stack.rotate(Axis.ZP.rotationDegrees(180.0F));
				float sitProgress = rat.sitProgress / 20.0F;
				stack.rotate(Axis.XP.rotationDegrees(sitProgress * -40.0F));
				stack.translate(0.0D, 0.0D, -sitProgress * 0.04F);
				stack.scale(1.7F, 1.7F, 1.7F);
				this.items.item(banner, ItemDisplayContext.FIXED, rat, stack, collector, light);
				stack.popPose();
			}
		}
	}
}
