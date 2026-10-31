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
        HumanoidModel<?> model = null;
        if (stack.getItem() instanceof HatItem) {
            model = RatsArmorModels.hatModel(stack.getItem());
        } else if (stack.getItem() instanceof RatlantisArmorItem) {
            model = RatsArmorModels.ratlantisArmorModel(slot);
        }
        if (model == null || model == original) {
            return original;
        }
        copyModelProperties(original, model);
        return model;
    }

    /**
     * NeoForge's ClientHooks.copyModelProperties: the replacement model takes the pose and the part visibility of the
     * model it stands for (without it the hats on rats were drawn at their own model's origin, the aviator hat under
     * the rat, and every part of the hat model showed)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyModelProperties(HumanoidModel original, HumanoidModel replacement) {
        original.copyPropertiesTo(replacement);
        replacement.head.visible = original.head.visible;
        replacement.hat.visible = original.hat.visible;
        replacement.body.visible = original.body.visible;
        replacement.rightArm.visible = original.rightArm.visible;
        replacement.leftArm.visible = original.leftArm.visible;
        replacement.rightLeg.visible = original.rightLeg.visible;
        replacement.leftLeg.visible = original.leftLeg.visible;
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
