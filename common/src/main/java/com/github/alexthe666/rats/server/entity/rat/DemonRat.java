package com.github.alexthe666.rats.server.entity.rat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class DemonRat extends AbstractRat implements Enemy {

	public static final EntityDataAccessor<Boolean> SOUL_VARIANT = SynchedEntityData.defineId(DemonRat.class, EntityDataSerializers.BOOLEAN);

	public DemonRat(EntityType<? extends AbstractRat> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0F);
		this.setPathfindingMalus(PathType.FIRE, -1.0F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SOUL_VARIANT, false);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.35D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	public static boolean canDemonRatSpawnOn(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		BlockPos blockpos = pos.below();
		return (level.getDifficulty() != Difficulty.PEACEFUL && !level.getBlockState(blockpos).is(Blocks.NETHER_WART_BLOCK) && level.getBlockState(blockpos).isValidSpawn(level, pos, type)) || reason == EntitySpawnReason.SPAWNER;
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor accessor, EntitySpawnReason type) {
		if (type == EntitySpawnReason.EVENT || type == EntitySpawnReason.SPAWNER) return super.checkSpawnRules(accessor, type);
		return accessor.getRandom().nextInt(5) == 0;
	}

	@Override
	public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
		if (source.is(DamageTypeTags.IS_FIRE)) {
			return true;
		}
		return super.isInvulnerableTo(level, source);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity entity) {
		if (this.getRandom().nextInt(3) == 0) {
			entity.igniteForSeconds(5);
		}
		return super.doHurtTarget(level, entity);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && this.getRandom().nextFloat() < 0.15F) {
			double d2 = this.getRandom().nextGaussian() * 0.0075D;
			double d0 = this.getRandom().nextGaussian() * 0.0075D;
			double d1 = this.getRandom().nextGaussian() * 0.0075D;
			this.level().addParticle(this.isSoulVariant() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), d0, d1, d2);
		}
	}

	@Override
	public void addAdditionalSaveData(ValueOutput tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("SoulVariant", this.isSoulVariant());
	}

	@Override
	public void readAdditionalSaveData(ValueInput tag) {
		super.readAdditionalSaveData(tag);
		this.setSoulVariant(tag.getBooleanOr("SoulVariant", false));
	}

	// 26.x: despawning in peaceful is a flag of the entity type; checkDespawn asks this
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

	// 1.21: riding offsets are attachment points (the original's getMyRidingOffset was 0.25D)
	@Override
	public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
		return new Vec3(0.0D, -0.25D, 0.0D);
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor, DifficultyInstance difficulty, EntitySpawnReason type, @Nullable SpawnGroupData data) {
		data = super.finalizeSpawn(accessor, difficulty, type, data);
		this.setSoulVariant(accessor.getBiome(this.blockPosition()).is(Biomes.SOUL_SAND_VALLEY) || accessor.getRandom().nextInt(100) == 0);
		return data;
	}

	public boolean isSoulVariant() {
		return this.getEntityData().get(SOUL_VARIANT);
	}

	public void setSoulVariant(boolean soul) {
		this.getEntityData().set(SOUL_VARIANT, soul);
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Override
	public boolean isTame() {
		return false;
	}

	@Override
	public boolean isHoldingItemInHands() {
		return this.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WARPED_FUNGUS_ON_A_STICK);
	}

	@Override
	public void checkDespawn() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
			this.discard();
		} else {
			super.checkDespawn();
		}
	}
}
