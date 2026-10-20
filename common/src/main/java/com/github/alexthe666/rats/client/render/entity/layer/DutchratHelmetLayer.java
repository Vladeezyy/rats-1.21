package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.FlyingDutchratModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.HatRendering;
import com.github.alexthe666.rats.client.render.entity.RatsMobRenderState;
import com.github.alexthe666.rats.server.entity.monster.boss.Dutchrat;
import com.github.alexthe666.rats.server.items.HatItem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/** The Dutchrat's helmet (its pirat hat), on a head-only armor model at rest that its head moves */
public class DutchratHelmetLayer extends EntityLayer<Dutchrat, RatsMobRenderState, FlyingDutchratModel<Dutchrat>> {
	private final HumanoidModel<HumanoidRenderState> helmetModel;
	private final EquipmentLayerRenderer equipment;

	public DutchratHelmetLayer(RenderLayerParent<RatsMobRenderState, FlyingDutchratModel<Dutchrat>> parent, EntityRendererProvider.Context context) {
		super(parent);
		this.helmetModel = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new).head();
		this.equipment = context.getEquipmentRenderer();
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, RatsMobRenderState state, Dutchrat rat) {
		if (rat.getBellSummonTicks() <= 0) {
			ItemStack itemstack = rat.getItemBySlot(EquipmentSlot.HEAD);
			Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
			if (equippable == null || equippable.slot() != EquipmentSlot.HEAD) {
				return;
			}
			stack.pushPose();
			this.getParentModel().body1.translateRotate(stack);
			this.getParentModel().neck.translateRotate(stack);
			this.getParentModel().head.translateRotate(stack);
			stack.translate(0, -0.77F, 0);
			if (itemstack.getItem() instanceof HatItem) {
				HatRendering.submitHat(itemstack, HatRendering.REST, stack, collector, light, state.outlineColor);
			} else if (equippable.assetId().isPresent()) {
				this.equipment.renderLayers(EquipmentClientInfo.LayerType.HUMANOID, equippable.assetId().get(), this.helmetModel, HatRendering.REST, itemstack, stack, collector, light, state.outlineColor);
			}
			stack.popPose();
		}
	}
}
