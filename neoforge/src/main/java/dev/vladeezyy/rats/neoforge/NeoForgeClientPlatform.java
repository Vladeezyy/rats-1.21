package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.platform.ClientPlatform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** The client hooks of NeoForge the original render code called */
public final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        return ClientHooks.getArmorModel(entity, stack, slot, original);
    }

    @Override
    public ResourceLocation armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot) {
        return ClientHooks.getArmorTexture(entity, stack, layer, innerModel, slot);
    }

    @Override
    public int armorDefaultDyeColor(ItemStack stack) {
        return IClientItemExtensions.of(stack).getDefaultDyeColor(stack);
    }

    @Override
    public int armorLayerTint(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIndex, int fallbackColor) {
        return IClientItemExtensions.of(stack).getArmorLayerTintColor(stack, entity, layer, layerIndex, fallbackColor);
    }

    @Override
    public ResourceLocation fluidStillTexture(Fluid fluid) {
        return IClientFluidTypeExtensions.of(fluid).getStillTexture();
    }

    @Override
    public boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return ClientHooks.isNameplateInRenderDistance(entity, distanceSq);
    }
}
