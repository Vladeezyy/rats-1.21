package com.github.alexthe666.rats.server.items;

import net.minecraft.world.item.component.SwingAnimation;import com.github.alexthe666.rats.server.misc.Values;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.projectile.RatArrow;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RatArrowItem extends ArrowItem {

	public RatArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getPlayer().getItemInHand(context.getHand());
		CompoundTag ratTag = new CompoundTag();
		if (ItemNbt.hasTag(stack)) {
			ratTag = ItemNbt.view(stack).getCompoundOrEmpty("Rat");
		}
		TamedRat rat = new TamedRat(RatsEntityRegistry.TAMED_RAT.get(), context.getLevel());
		BlockPos offset = context.getClickedPos().relative(context.getClickedFace());
		if (!ratTag.isEmpty()) {
			// the rat's save data, its name included (as the rat arrow entity loads it)
			Values.load(rat, ratTag);
		}
		rat.snapTo(offset.getX() + 0.5D, offset.getY(), offset.getZ() + 0.5D, 0, 0);
		if (!context.getLevel().isClientSide()) {
			context.getLevel().addFreshEntity(rat);
		}
		stack.shrink(1);
		context.getPlayer().setItemInHand(context.getHand(), new ItemStack(Items.ARROW));
		context.getPlayer().swing(context.getHand(), SwingAnimation.DEFAULT, false);
		return InteractionResult.SUCCESS;
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, @org.jetbrains.annotations.Nullable ItemStack weapon) {
		return new RatArrow(RatsEntityRegistry.RAT_ARROW.get(), level, shooter, stack);
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity player) {
		return false;
	}
}
