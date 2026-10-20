package com.github.alexthe666.rats.client.render.entity.layer;

import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.model.entity.PinkieModel;
import com.github.alexthe666.rats.client.model.entity.StaticRatModel;
import com.github.alexthe666.rats.client.render.EntityLayer;
import com.github.alexthe666.rats.client.render.PartyHats;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.PartyHatItem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** The party hat on a rat's head (humanoids get it through {@link HatLayer}) */
public class PartyHatLayer<T extends AbstractRat, S extends RampartLivingRenderState, M extends AbstractRatModel<T>> extends EntityLayer<T, S, M> {

	public PartyHatLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	protected void submit(PoseStack stack, SubmitNodeCollector collector, int light, S state, T entity) {
		ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.HEAD);
		if (entity instanceof TamedRat rat && rat.getRespawnCountdown() > 0) return;
		if (itemstack.getItem() instanceof PartyHatItem) {
			stack.pushPose();
			if (this.getParentModel() instanceof PinkieModel<?> pinkie) {
				pinkie.body.translateRotate(stack);
				stack.translate(0.0D, -0.01D, -0.05D);
				stack.scale(0.65F, 0.65F, 0.65F);
			} else if (this.getParentModel() instanceof StaticRatModel<?> rat) {
				rat.body1.translateRotate(stack);
				rat.body2.translateRotate(stack);
				rat.neck.translateRotate(stack);
				rat.head.translateRotate(stack);
			}
			stack.translate(0.0F, -0.35F, 0.0F);
			stack.scale(0.75F, 0.75F, 0.75F);
			PartyHats.submit(itemstack, stack, collector, light, state.outlineColor);
			stack.popPose();
		}
	}
}
