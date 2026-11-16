package com.github.alexthe666.rats.mixin.client;

import com.github.alexthe666.rats.client.render.HatRendering;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.x fills a humanoid's (players, armor stands, mobs) head equipment only with items that have a vanilla equipment
 * asset. The mod's hats have none, so {@link com.github.alexthe666.rats.client.render.entity.layer.HatLayer} got an
 * empty stack and drew nothing, and vanilla's head layer drew the hat as a small item above the head (GitHub #4).
 */
@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin {
	@Inject(method = "getEquipmentIfRenderable", at = @At("RETURN"), cancellable = true)
	private static void rats$hatEquipment(LivingEntity entity, EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
		if (slot == EquipmentSlot.HEAD && cir.getReturnValue().isEmpty()) {
			ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
			if (HatRendering.drawsHat(head)) {
				cir.setReturnValue(head.copy());
			}
		}
	}
}
