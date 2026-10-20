package com.github.alexthe666.rats.server.entity.projectile;
import com.github.alexthe666.rats.server.misc.Values;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.server.level.ServerLevel;
import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;


public class RatArrow extends AbstractArrow {

	private final ItemStack stack;

	public RatArrow(EntityType<? extends AbstractArrow> type, Level level) {
		super(type, level);
		this.stack = new ItemStack(RatsItemRegistry.RAT_ARROW.get());
	}

	public RatArrow(EntityType<? extends AbstractArrow> type, Level level, LivingEntity shooter, ItemStack stack) {
		super(type, shooter, level, stack.copyWithCount(1), null);
		this.stack = stack;
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	private void spawnRat(@Nullable Entity entity, BlockPos pos) {
		if (this.pickup == Pickup.ALLOWED) {
			TamedRat rat = new TamedRat(RatsEntityRegistry.TAMED_RAT.get(), this.level());
			CompoundTag ratTag = ItemNbt.hasTag(this.stack) ? ItemNbt.view(this.stack).getCompoundOrEmpty("Rat") : new CompoundTag();
			if (!ratTag.isEmpty()) {
				// the rat's save data, its name included
				Values.load(rat, ratTag);
			}
			if (ratTag.isEmpty()) {
				Platform.INSTANCE.finalizeMobSpawn(rat, (ServerLevelAccessor) this.level(), ((net.minecraft.world.level.ServerLevelAccessor) this.level()).getCurrentDifficultyAt(rat.blockPosition()), EntitySpawnReason.EVENT, null);
				if (this.getOwner() instanceof Player player) {
					rat.tame(player);
				}
			}
			rat.setCommand(RatCommand.WANDER);
			rat.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			if (!this.level().isClientSide()) {
				this.level().addFreshEntity(rat);
			}
			if (entity instanceof LivingEntity living && !rat.isAlliedTo(entity)) {
				rat.setTarget(living);
			}
		}
	}

	// 26.x: vanilla's hit (the original copied it to spawn the rat after the damage); an arrow that hurt its target is
	// removed by it, so the rat jumps off then
	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!this.level().isClientSide() && this.isRemoved()) {
			this.spawnRat(result.getEntity(), result.getEntity().blockPosition());
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		if (this.isInGround()) {
			if (!this.level().isClientSide()) {
				this.spawnRat(null, result.getBlockPos().relative(result.getDirection()));
				this.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), this.getPickupItem(), 0.0F);
			}
			this.discard();
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}
}
