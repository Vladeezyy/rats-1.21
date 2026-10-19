package dev.vladeezyy.rats.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluid;

import java.util.ServiceLoader;

/**
 * What the client code of Rats needs from the mod loader. Only touched on the client (render code), so the
 * implementation may use client-only loader classes. The defaults are what vanilla does.
 */
public interface ClientPlatform {
    ClientPlatform INSTANCE = ServiceLoader.load(ClientPlatform.class, ClientPlatform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Rats client platform for this mod loader"));

    /** The model an armor item is drawn with (other mods' items may bring their own) */
    Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original);

    /** The texture of one layer of an armor item */
    ResourceLocation armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot);

    /** The color of an undyed dyeable armor (vanilla: leather brown) */
    default int armorDefaultDyeColor(ItemStack stack) {
        return stack.is(net.minecraft.tags.ItemTags.DYEABLE) ? -6265536 : -1;
    }

    /** The tint of one armor layer; 0 skips the layer */
    default int armorLayerTint(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIndex, int fallbackColor) {
        return layer.dyeable() ? net.minecraft.util.FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, fallbackColor)) : -1;
    }

    /** The still texture of a fluid on the block atlas */
    ResourceLocation fluidStillTexture(Fluid fluid);

    /** Whether a name tag at this squared distance is drawn (NeoForge: the name tag distance attribute) */
    default boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return distanceSq <= 4096.0D;
    }
}
