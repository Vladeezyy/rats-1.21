package com.github.alexthe666.rats.server.entity.projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.github.alexthe666.rats.server.misc.Values;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class ArrowlikeProjectile extends Projectile {
	@Nullable
	private BlockState lastState;
	protected boolean inGround;
	protected int inGroundTime;
	public int shakeTime;
	private int life;
	private double baseDamage = 2.0D;

	protected ArrowlikeProjectile(EntityType<? extends Projectile> type, Level level) {
		super(type, level);
	}

	protected ArrowlikeProjectile(EntityType<? extends Projectile> type, double x, double y, double z, Level level) {
		this(type, level);
		this.setPos(x, y, z);
	}

	protected ArrowlikeProjectile(EntityType<? extends Projectile> type, LivingEntity owner, Level level) {
		this(type, owner.getX(), owner.getEyeY() - (double) 0.1F, owner.getZ(), level);
		this.setOwner(owner);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double dist) {
		double d0 = this.getBoundingBox().getSize() * 10.0D;
		if (Double.isNaN(d0)) {
			d0 = 1.0D;
		}

		d0 *= 64.0D * getViewScale();
		return dist < d0 * d0;
	}

	@Override
	public void shoot(double x, double y, double z, float yRot, float xRot) {
		super.shoot(x, y, z, yRot, xRot);
		this.life = 0;
	}

	@Override
	public void lerpMotion(Vec3 movement) {
		super.lerpMotion(movement);
		this.life = 0;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {

	}

	@Override
	public void tick() {
		super.tick();
		boolean flag = this.noPhysics;
		Vec3 vec3 = this.getDeltaMovement();
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double d0 = vec3.horizontalDistance();
			this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
			this.setXRot((float) (Mth.atan2(vec3.y, d0) * (double) (180F / (float) Math.PI)));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

		BlockPos blockpos = this.blockPosition();
		BlockState blockstate = this.level().getBlockState(blockpos);
		if (!blockstate.isAir() && !flag) {
			VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
			if (!voxelshape.isEmpty()) {
				Vec3 vec31 = this.position();

				for (AABB aabb : voxelshape.toAabbs()) {
					if (aabb.move(blockpos).contains(vec31)) {
						this.inGround = true;
						break;
					}
				}
			}
		}

		if (this.shakeTime > 0) {
			--this.shakeTime;
		}

		if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInWater()) {
			this.clearFire();
		}

		if (this.inGround && !flag) {
			if (this.lastState != blockstate && this.shouldFall()) {
				this.startFalling();
			} else if (!this.level().isClientSide()) {
				this.tickDespawn();
			}

			++this.inGroundTime;
		} else {
			this.inGroundTime = 0;
			HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (hitresult.getType() != HitResult.Type.MISS && !dev.vladeezyy.rats.platform.Platform.INSTANCE.onProjectileImpact(this, hitresult)) {
				this.onHit(hitresult);
			}

			vec3 = this.getDeltaMovement();
			double d5 = vec3.x;
			double d6 = vec3.y;
			double d1 = vec3.z;

			double d7 = this.getX() + d5;
			double d2 = this.getY() + d6;
			double d3 = this.getZ() + d1;
			double d4 = vec3.horizontalDistance();
			if (flag) {
				this.setYRot((float) (Mth.atan2(-d5, -d1) * (double) (180F / (float) Math.PI)));
			} else {
				this.setYRot((float) (Mth.atan2(d5, d1) * (double) (180F / (float) Math.PI)));
			}

			this.setXRot((float) (Mth.atan2(d6, d4) * (double) (180F / (float) Math.PI)));
			this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
			this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
			float f = 0.99F;

			this.setDeltaMovement(vec3.scale(f));
			if (!this.isNoGravity() && !flag) {
				Vec3 vec34 = this.getDeltaMovement();
				this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
			}

			this.setPos(d7, d2, d3);
			this.applyEffectsFromBlocks();
		}
	}

	private boolean shouldFall() {
		return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
	}

	private void startFalling() {
		this.inGround = false;
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
		this.life = 0;
	}

	@Override
	public void move(MoverType type, Vec3 vec) {
		super.move(type, vec);
		if (type != MoverType.SELF && this.shouldFall()) {
			this.startFalling();
		}
	}

	protected void tickDespawn() {
		++this.life;
		if (this.life >= 1200) {
			this.discard();
		}

	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity entity = result.getEntity();
		float f = (float) this.getDeltaMovement().length();
		int i = Mth.ceil(Mth.clamp((double) f * this.getBaseDamage(), 0.0D, 2.147483647E9D));

		Entity entity1 = this.getOwner();
		DamageSource damagesource;
		if (entity1 == null) {
			damagesource = this.damageSources().source(DamageTypes.ARROW, this, this);
		} else {
			damagesource = this.damageSources().source(DamageTypes.ARROW, this, entity1);
			if (entity1 instanceof LivingEntity living) {
				living.setLastHurtMob(entity);
			}
		}

		boolean flag = entity.getType() == net.minecraft.world.entity.EntityTypes.ENDERMAN;
		int k = entity.getRemainingFireTicks();
		if (this.isOnFire() && !flag) {
			entity.igniteForSeconds(5);
		}

		if (entity.hurtOrSimulate(damagesource, (float) i)) {
			if (flag) {
				return;
			}

			if (entity instanceof LivingEntity livingentity) {
				if (this.level() instanceof ServerLevel serverLevel) {
					EnchantmentHelper.doPostAttackEffects(serverLevel, livingentity, damagesource);
				}
			}

		} else {
			entity.setRemainingFireTicks(k);
			this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
			this.setYRot(this.getYRot() + 180.0F);
			this.yRotO += 180.0F;
		}
		this.discard();
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && (this.getOwner() == null || !this.isAlliedToOwner(entity));
	}

	protected boolean isAlliedToOwner(Entity entity) {
		if (this.getOwner() instanceof OwnableEntity ownable && ownable.getOwner() == entity) return true;
		return entity instanceof OwnableEntity ownable && ownable.getOwner() == entity;
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		this.discard();
		if (this.explodesOnHit()) {
			this.level().explode(this.getOwner(), this.getX(), this.getY(), this.getZ(), 0.0F, Level.ExplosionInteraction.MOB);
		}
	}

	@Override
	public void addAdditionalSaveData(ValueOutput tag) {
		super.addAdditionalSaveData(tag);
		tag.putShort("life", (short) this.life);
		if (this.lastState != null) {
			Values.put(tag, "inBlockState", NbtUtils.writeBlockState(this.lastState));
		}

		tag.putByte("shake", (byte) this.shakeTime);
		tag.putBoolean("inGround", this.inGround);
		tag.putDouble("damage", this.baseDamage);
	}

	@Override
	public void readAdditionalSaveData(ValueInput tag) {
		super.readAdditionalSaveData(tag);
		this.life = tag.getShortOr("life", (short) 0);
		if (Values.contains(tag, "inBlockState")) {
			this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), Values.compound(tag, "inBlockState"));
		}

		this.shakeTime = tag.getByteOr("shake", (byte) 0) & 255;
		this.inGround = tag.getBooleanOr("inGround", false);
		if (Values.contains(tag, "damage")) {
			this.baseDamage = tag.getDoubleOr("damage", 0.0D);
		}
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	public void setBaseDamage(double damage) {
		this.baseDamage = damage;
	}

	public double getBaseDamage() {
		return this.baseDamage;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).withEyeHeight(0.13F);
	}

	public abstract boolean explodesOnHit();

}
