package com.github.alexthe666.rats.server.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ConfitByaldiMobEffect extends MobEffect {

	public ConfitByaldiMobEffect() {
		super(MobEffectCategory.BENEFICIAL, 0XFFDD59);
		this.addAttributeModifier(Attributes.ATTACK_SPEED, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 1.0D, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	public boolean applyEffectTick(LivingEntity living, int amplifier) {
		if (living.getHealth() < living.getMaxHealth()) {
			living.heal(1.0F);
		}
		if (living instanceof Player player) {
			player.getFoodData().eat(1, 0.1F);
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

	/** The effect ended or was removed (called from the effect events, see PlagueMobEffect.onRemoved) */
	public static void onRemoved(LivingEntity living, int amplifier) {
		living.setAbsorptionAmount(living.getAbsorptionAmount() - (float) (20 * (amplifier + 1)));
		if (living.getHealth() > living.getMaxHealth()) {
			living.setHealth(living.getMaxHealth());
		}
	}

	@Override
	public void onEffectStarted(LivingEntity living, int amplifier) {
		living.setAbsorptionAmount(living.getAbsorptionAmount() + (float) (20 * (amplifier + 1)));
	}
}
