package com.github.alexthe666.rats.server.items;
import net.minecraft.world.InteractionResult;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import net.minecraft.world.entity.LivingEntity;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.entity.projectile.RatShot;
import com.github.alexthe666.rats.server.misc.RatVariant;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GildedRatFluteItem extends LoreTagItem {

	public GildedRatFluteItem(Item.Properties properties) {
		super(properties, 1);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.TOOT_HORN;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (ItemNbt.hasTag(stack)) {
			RatShot ratShot = new RatShot(RatsEntityRegistry.RAT_SHOT.get(), level, player);
			ratShot.setColorVariant(RatVariant.getRandomVariant(player.getRandom(), false));
			Vec3 vector3d = player.getViewVector(1.0F);
			ratShot.shoot(vector3d.x(), vector3d.y(), vector3d.z(), 1.0F, 1.5F);
			level.addFreshEntity(ratShot);
			stack.hurtAndBreak(1, player, hand);
			player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
			player.getCooldowns().addCooldown(stack, 10);
			level.playSound(player, player.blockPosition(), RatsSoundRegistry.getFluteSound(), SoundSource.PLAYERS, 0.5F, 0.75F);
		}
		return InteractionResult.SUCCESS;
	}
}
