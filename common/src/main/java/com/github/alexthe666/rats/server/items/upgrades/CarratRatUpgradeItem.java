package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.rats.client.render.SubmitHelper;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.HoldsItemUpgrade;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CarratRatUpgradeItem extends BaseRatUpgradeItem implements ChangesTextureUpgrade, HoldsItemUpgrade {
	public CarratRatUpgradeItem(Properties properties) {
		super(properties, 1, 1);
	}

	@Override
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/upgrades/carrat.png");
	}

	@Override
	public boolean makesEyesGlowByDefault() {
		return false;
	}

	@Override
	public boolean isFakeHandRender() {
		return true;
	}

	@Override
	public void renderHeldItem(SubmitHelper context, TamedRat rat, RatModel<?> model, PoseStack stack, SubmitNodeCollector buffer, int light, float ageInTicks) {
		stack.pushPose();
		model.body1.translateRotate(stack);
		stack.pushPose();
		stack.translate(0F, -0.05F, 0.5F);
		stack.mulPose(Axis.XP.rotationDegrees(90.0F));
		stack.scale(2, 2, 2);
		context.item(new ItemStack(Blocks.FERN), ItemDisplayContext.GROUND, rat, stack, buffer, light);
		stack.popPose();
		stack.popPose();
	}
}
