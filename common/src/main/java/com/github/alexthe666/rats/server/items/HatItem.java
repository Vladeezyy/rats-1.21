package com.github.alexthe666.rats.server.items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.registry.RatsArmorMaterialRegistry;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.registry.RatsArmorMaterialRegistry;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.hats.*;
import com.github.alexthe666.rats.registry.RatlantisItemRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.monster.GhostPirat;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HatItem extends Item {

	private final int loreLines;

	public HatItem(Item.Properties properties, ArmorMaterial material, int loreLines) {
		super(RatsArmorMaterialRegistry.hat(properties, material));
		this.loreLines = loreLines;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (stack.is(RatsItemRegistry.BLACK_DEATH_MASK.get())) {
			tooltip.accept(Component.translatable("item.rats.plague_doctor_mask.desc").withStyle(ChatFormatting.GRAY));
		}
		if (this.loreLines > 0) {
			for (int i = 0; i < this.loreLines; i++) {
				tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc" + (this.loreLines == 1 ? "" : i)).withStyle(ChatFormatting.GRAY));
			}
		}
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		return stack.is(RatsItemRegistry.RAT_KING_CROWN.get());
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean isEnderMask(ItemStack stack, Player player, EnderMan enderMan) {
		return stack.is(RatsItemRegistry.BLACK_DEATH_MASK.get()) || stack.is(RatsItemRegistry.PLAGUE_DOCTOR_MASK.get());
	}

	/**
	 * Override this to add a custom transformation to the hat when on a rat's head.
	 *
	 * @param rat   the rat to render the hat on
	 * @param stack a PoseStack to allow translation, rotation, and scaling
	 */
	public void transformOnHead(AbstractRat rat, PoseStack stack) {
		if (this == RatsItemRegistry.CHEF_TOQUE.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(25.0F));
			stack.translate(0.0F, 0.1F, 0.3F);
		}
		if (this == RatsItemRegistry.PIPER_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(10.0F));
			stack.translate(0.0F, 0.0F, 0.1F);
		}
		if (this == RatsItemRegistry.PIRAT_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.125F, 0.0F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatlantisItemRegistry.GHOST_PIRAT_HAT.get()) {
			float piratScale = rat instanceof GhostPirat ? 1.1F : 1.425F;
			float piratTranslate = rat instanceof GhostPirat ? 0.05F : -0.125F;
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, piratTranslate, 0.0F);
			stack.scale(piratScale, piratScale, piratScale);
		}
		if (this == RatsItemRegistry.ARCHEOLOGIST_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.1F, 0.0F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatsItemRegistry.FARMER_HAT.get() || this == RatsItemRegistry.FISHERMAN_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.1F, 0.0F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatsItemRegistry.PLAGUE_DOCTOR_MASK.get() || this == RatsItemRegistry.BLACK_DEATH_MASK.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(15.0F));
			stack.translate(0.0F, -0.1F, 0.0F);
			stack.scale(1.5F, 1.2F, 1.5F);
		}
		if (this == RatsItemRegistry.RAT_FEZ.get()) {
			stack.translate(-0.05F, -0.15F, -0.1F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatsItemRegistry.TOP_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.125F, 0.0F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatsItemRegistry.SANTA_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, 0.0F, 0.1F);
			stack.scale(1.25F, 1.25F, 1.25F);
		}
		if (this == RatlantisItemRegistry.MILITARY_HAT.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.1F, 0.0F);
			stack.scale(1.425F, 1.425F, 1.425F);
		}
		if (this == RatsItemRegistry.RAT_KING_CROWN.get()) {
			stack.mulPose(Axis.XN.rotationDegrees(5.0F));
			stack.translate(0.0F, -0.05F, 0.0F);
			stack.scale(1.25F, 1.25F, 1.25F);
		}
		if (this == RatlantisItemRegistry.AVIATOR_HAT.get()) {
			stack.scale(1.25F, 1.25F, 1.25F);
			stack.translate(0, -0.035F, 0.01F);
		}
	}

	public float getRatOffsetOnHead() {
		if (this == RatsItemRegistry.TOP_HAT.get()) {
			return -0.85F;
		} else if (this == RatlantisItemRegistry.MILITARY_HAT.get() || this == RatlantisItemRegistry.GHOST_PIRAT_HAT.get() || this == RatsItemRegistry.PIRAT_HAT.get()) {
			return -0.45F;
		}
		return 0.0F;
	}


	/** The hat's texture (the hats are drawn by Rats on both loaders) */
	public Identifier getHatTexture() {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/model/hat/" + BuiltInRegistries.ITEM.getKey(this).getPath() + ".png");
	}

}
