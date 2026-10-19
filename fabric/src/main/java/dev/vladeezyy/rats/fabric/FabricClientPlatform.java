package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.server.items.HatItem;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import dev.vladeezyy.rats.platform.ClientPlatform;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** The client side on Fabric: the mod's own armor models and textures, fluid sprites from Fabric's fluid rendering */
public final class FabricClientPlatform implements ClientPlatform {
    @Override
    public Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (stack.getItem() instanceof HatItem) {
            HumanoidModel<?> model = RatsArmorModels.hatModel(stack.getItem());
            return model != null ? model : original;
        }
        if (stack.getItem() instanceof RatlantisArmorItem) {
            return RatsArmorModels.ratlantisArmorModel(slot);
        }
        return original;
    }

    @Override
    public ResourceLocation armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot) {
        if (stack.getItem() instanceof HatItem hat) {
            return hat.getArmorTexture(stack, entity, slot, layer, innerModel);
        }
        if (stack.getItem() instanceof RatlantisArmorItem armor) {
            return armor.getArmorTexture(stack, entity, slot, layer, innerModel);
        }
        return layer.texture(innerModel);
    }

    @Override
    public ResourceLocation fluidStillTexture(Fluid fluid) {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(FluidVariant.of(fluid));
        return sprite != null ? sprite.contents().name() : MissingTextureAtlasSprite.getLocation();
    }
}
