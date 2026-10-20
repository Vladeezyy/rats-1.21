package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.rats.client.render.SubmitHelper;

import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.HoldsItemUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.PostAttackUpgrade;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TNTRatUpgradeItem extends BaseRatUpgradeItem implements PostAttackUpgrade, HoldsItemUpgrade {
	public TNTRatUpgradeItem(Properties properties) {
		super(properties, 1, 2);
	}

	public TNTRatUpgradeItem(Item.Properties properties, int rarity, int textLength) {
		super(properties, rarity, textLength);
	}

	@Override
	public void renderHeldItem(SubmitHelper context, TamedRat rat, RatModel<?> model, PoseStack stack, SubmitNodeCollector buffer, int light, float ageInTicks) {
		model.body1.translateRotate(stack);
		stack.pushPose();
		stack.translate(0F, 0.1F, 0.1F);
		stack.rotate(Axis.XP.rotationDegrees(180.0F));
		stack.scale(2, 2, 2);
		context.item(new ItemStack(Items.TNT), ItemDisplayContext.GROUND, rat, stack, buffer, light);
		stack.popPose();
	}

	@Override
	public boolean isFakeHandRender() {
		return true;
	}

	@Override
	public void afterHit(TamedRat rat, LivingEntity target) {
		rat.level().explode(null, rat.getX(), rat.getY() + (double) (rat.getBbHeight() / 16.0F), rat.getZ(), 4.0F, Level.ExplosionInteraction.MOB);
	}
}
