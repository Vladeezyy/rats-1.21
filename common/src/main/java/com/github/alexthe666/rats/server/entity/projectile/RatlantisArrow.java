package com.github.alexthe666.rats.server.entity.projectile;

import com.github.alexthe666.rats.registry.RatlantisEntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class RatlantisArrow extends AbstractArrow {

	public RatlantisArrow(EntityType<? extends AbstractArrow> arrow, Level level) {
		super(arrow, level);
		this.setBaseDamage(4.0D);
	}

	public RatlantisArrow(Level level, LivingEntity shooter) {
		this(level, shooter, new ItemStack(Items.ARROW), null);
	}

	public RatlantisArrow(Level level, LivingEntity shooter, ItemStack pickup, @org.jetbrains.annotations.Nullable ItemStack weapon) {
		super(RatlantisEntityRegistry.RATLANTIS_ARROW.get(), shooter, level, pickup, weapon);
		this.setBaseDamage(4.0D);
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}
}
