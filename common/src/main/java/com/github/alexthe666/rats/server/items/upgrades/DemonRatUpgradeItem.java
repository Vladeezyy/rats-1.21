package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesOverlayUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.DamageImmunityUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.GlowingEyesUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.PostAttackUpgrade;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DemonRatUpgradeItem extends StatBoostingRatUpgradeItem implements GlowingEyesUpgrade, DamageImmunityUpgrade, PostAttackUpgrade, ChangesOverlayUpgrade {
	public DemonRatUpgradeItem(Properties properties) {
		super(properties, 1, 2, () -> Map.of(Attributes.MAX_HEALTH, RatConfig.demonHealthUpgrade, Attributes.ATTACK_DAMAGE, RatConfig.demonDamageUpgrade), false);
	}

	public static boolean isSoulVersion(ItemStack stack) {
		return ItemNbt.hasTag(stack) && ItemNbt.view(stack).getBooleanOr("Soul", false);
	}

	public static ItemStack getDemonUpgrade(boolean soul) {
		ItemStack stack = new ItemStack(RatsItemRegistry.RAT_UPGRADE_DEMON.get());
		ItemNbt.update(stack, tag -> tag.putBoolean("Soul", soul));
		return stack;
	}

	@Override
	public RenderType getEyeTexture(ItemStack stack) {
		if (isSoulVersion(stack)) {
			return RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/soul_demon_rat_eye.png"));
		}
		return RenderTypes.eyes(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/demon_rat_eye.png"));
	}

	@Override
	public boolean isImmuneToDamageSource(TamedRat rat, DamageSource source) {
		return source.is(DamageTypeTags.IS_FIRE);
	}

	@Override
	public void afterHit(TamedRat rat, LivingEntity target) {
		if (!target.fireImmune()) target.igniteForSeconds(10);
	}

	@Override
	public @Nullable RenderType getOverlayTexture(ItemStack stack, TamedRat rat, float partialTicks) {
		if (isSoulVersion(stack)) {
			return RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/soul_demon_rat.png"));
		}
		return RenderTypes.entityCutoutCull(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/demon_rat/demon_rat.png"));
	}
}
