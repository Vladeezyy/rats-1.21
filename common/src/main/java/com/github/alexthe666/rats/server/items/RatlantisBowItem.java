package com.github.alexthe666.rats.server.items;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.server.entity.projectile.RatlantisArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class RatlantisBowItem extends BowItem {

	public RatlantisBowItem(Item.Properties properties) {
		super(properties);
	}

	// 1.21: the bow is vanilla's; plain arrows become Ratlantis arrows (4 base damage). Power, Punch, Flame and
	// Infinity are enchantment effects of the bow now, applied by vanilla to any arrow it fires.
	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
		if (!ammo.is(Items.ARROW)) {
			return super.createProjectile(level, shooter, weapon, ammo, isCrit);
		}
		RatlantisArrow arrow = new RatlantisArrow(level, shooter, ammo.copyWithCount(1), weapon);
		if (isCrit) {
			arrow.setCritArrow(true);
		}
		return Platform.INSTANCE.customArrow(this, arrow, ammo, weapon);
	}

	public static float getPowerForTime(int time) {
		float f = (float) time / 10.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable("item.rats.ratlantis_bow.desc0").withStyle(ChatFormatting.YELLOW));
		tooltip.accept(Component.translatable("item.rats.ratlantis_bow.desc1").withStyle(ChatFormatting.GRAY));
	}

}
