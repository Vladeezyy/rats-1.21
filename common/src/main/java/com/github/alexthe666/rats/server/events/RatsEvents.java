package com.github.alexthe666.rats.server.events;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.data.tags.RatsEntityTags;
import com.github.alexthe666.rats.registry.*;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.capability.SelectedRats;
import com.github.alexthe666.rats.server.effect.ConfitByaldiMobEffect;
import com.github.alexthe666.rats.server.effect.PlagueMobEffect;
import com.github.alexthe666.rats.server.entity.misc.PlagueDoctor;
import com.github.alexthe666.rats.server.entity.projectile.PlagueShot;
import com.github.alexthe666.rats.server.entity.rat.DemonRat;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.RatSackItem;
import com.github.alexthe666.rats.server.items.RatStaffItem;
import com.github.alexthe666.rats.server.message.DismountRatPacket;
import com.github.alexthe666.rats.server.message.ManageRatStaffPacket;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.message.SyncArmSwingPacket;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.github.alexthe666.rats.server.misc.RatUtils;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.github.alexthe666.rats.server.world.PlagueDoctorSpawner;
import dev.vladeezyy.rats.platform.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The game event handlers of Rats with plain vanilla arguments (the original's Forge event subscribers). Each loader
 * calls them from its own events: NeoForge {@code NeoForgeGameEvents}, Fabric its callbacks and mixins.
 */
public final class RatsEvents {

	private RatsEvents() {
	}

	/** Right click on an entity (both sides); never cancels */
	public static void onPlayerInteractWithEntity(Player player, Level level, InteractionHand hand, Entity target) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (target instanceof Sheep sheep && sheep.getColor() != DyeColor.WHITE && player.getItemInHand(hand).is(RatsBlockRegistry.DYE_SPONGE.get().asItem())) {
			sheep.setColor(DyeColor.WHITE);
			for (int i = 0; i < 8; i++) {
				double d0 = sheep.getRandom().nextGaussian() * 0.02D;
				double d1 = sheep.getRandom().nextGaussian() * 0.02D;
				double d2 = sheep.getRandom().nextGaussian() * 0.02D;
				sheep.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, RatsBlockRegistry.DYE_SPONGE.get().asItem()), sheep.getX() + (double) (sheep.getRandom().nextFloat() * sheep.getBbWidth() * 2.0F) - (double) sheep.getBbWidth(), sheep.getY() + (double) (sheep.getRandom().nextFloat() * sheep.getBbHeight() * 2.0F) - (double) sheep.getBbHeight(), sheep.getZ() + (double) (sheep.getRandom().nextFloat() * sheep.getBbWidth() * 2.0F) - (double) sheep.getBbWidth(), d0, d1, d2);
			}
			sheep.playSound(RatsSoundRegistry.DYE_SPONGE_USED.get(), 1.0F, 1.0F);

		}
		if (heldItem.is(RatsItemRegistry.PLAGUE_DOCTORATE.get())) {
			if (target instanceof Villager villager && !villager.isBaby() && (villager.getVillagerData().profession().is(VillagerProfession.NITWIT) || villager.getVillagerData().profession().is(VillagerProfession.NONE))) {
				PlagueDoctor doctor = villager.convertTo(RatsEntityRegistry.PLAGUE_DOCTOR.get(), ConversionParams.single(villager, true, false), converted -> {});
				if (doctor != null) {
					doctor.setWillDespawn(false);
					player.swing(hand);
					level.playSound(null, doctor.blockPosition(), RatsSoundRegistry.PLAGUE_DOCTOR_SUMMON.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
					if (!player.isCreative()) {
						heldItem.shrink(1);
					}
				}
			}
		}
		if (target instanceof LivingEntity living && player.hasEffect(RatsEffectRegistry.PLAGUE.holder())) {
			living.addEffect(new MobEffectInstance(RatsEffectRegistry.PLAGUE.holder(), 6000));
		}
	}

	public static int getProtectorCount(LivingEntity entity) {
		int protectors = 0;
		if (entity.getItemBySlot(EquipmentSlot.HEAD).is(RatlantisItemRegistry.RATLANTIS_HELMET.get())) {
			protectors++;
		}
		if (entity.getItemBySlot(EquipmentSlot.CHEST).is(RatlantisItemRegistry.RATLANTIS_CHESTPLATE.get())) {
			protectors++;
		}
		if (entity.getItemBySlot(EquipmentSlot.LEGS).is(RatlantisItemRegistry.RATLANTIS_LEGGINGS.get())) {
			protectors++;
		}
		if (entity.getItemBySlot(EquipmentSlot.FEET).is(RatlantisItemRegistry.RATLANTIS_BOOTS.get())) {
			protectors++;
		}
		return protectors;
	}

	/** True if the mob must not take this target (piglins never attack rats with the idol upgrade) */
	public static boolean cancelTargetChange(LivingEntity entity, @Nullable LivingEntity newTarget) {
		return entity instanceof Piglin && newTarget instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_IDOL.get());
	}

	/** True if the game event must not be emitted (rats with the sculked upgrade make no vibrations) */
	public static boolean cancelGameEvent(@Nullable Entity cause) {
		return cause instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_SCULKED.get());
	}

	/** True if the death is cancelled: rats with the angel upgrade come back */
	public static boolean onLivingDeath(LivingEntity entity) {
		if (entity instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_ANGEL.get())) {
			rat.spawnAngelCopy();
			rat.playSound(RatsSoundRegistry.RAT_DIE.get());
			if (rat.getOwner() instanceof Player player) {
				player.sendSystemMessage(Component.translatable(RatsLangConstants.RAT_ANGEL_RESPAWN, rat.getName().getString()));
			}
			rat.discard();
			return true;
		}
		return false;
	}

	/** An item entity reached the end of its lifespan */
	public static void onItemExpire(ItemEntity item) {
		if (item.getItem().is(RatsItemRegistry.RAT_SACK.get()) && RatSackItem.getRatsInSack(item.getItem()) > 0) {
			RatSackItem.ejectRatsFromSack(item.getItem(), item.level(), item.blockPosition());
		}
	}

	/**
	 * Damage about to be dealt (the original's three LivingIncomingDamageEvent handlers, in their order). Returns true if
	 * the damage is cancelled: the voodoo upgrade spreads a player's damage over their rats.
	 */
	public static boolean onIncomingDamage(LivingEntity entity, DamageSource source, float amount) {
		//complete hack workaround for rats not cooking food dropped by victims if they kill them in 1 shot
		if (!entity.fireImmune()) {
			if (source.getEntity() instanceof TamedRat rat && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_DEMON.get())) {
				entity.igniteForSeconds(1);
			}
		}

		// an attacker with the plague gives it to what it hits (the original gave it to the attacker again)
		if (source.getDirectEntity() instanceof LivingEntity living && living.hasEffect(RatsEffectRegistry.PLAGUE.holder()) && entity != living) {
			entity.addEffect(new MobEffectInstance(RatsEffectRegistry.PLAGUE.holder(), 6000));
		}
		int protectors = getProtectorCount(entity);
		if (protectors > 0) {
			if (source != null && source.getEntity() != null) {
				Entity trueSource = source.getEntity();
				if (trueSource.distanceTo(entity) < 4.0D) {
					trueSource.hurt(entity.damageSources().thorns(entity), 0.75F * protectors);
					Vec3 vec3d = trueSource.getDeltaMovement();
					double strength = 0.15D * protectors;
					Vec3 vec3d1 = (new Vec3(entity.getX() - trueSource.getX(), 0.0D, entity.getZ() - trueSource.getZ())).normalize().scale(strength);
					trueSource.setDeltaMovement(vec3d.x / 2.0D - vec3d1.x, trueSource.onGround() ? Math.min(0.4D, vec3d.y / 2.0D + strength) : vec3d.y, vec3d.z / 2.0D - vec3d1.z);
				}
			}
		}
	

		boolean cancelled = false;
		if (entity instanceof Player) {
			List<TamedRat> list = entity.level().getEntitiesOfClass(TamedRat.class, entity.getBoundingBox().inflate(RatConfig.ratVoodooDistance), rat -> rat.isTame() && rat.isOwnedBy(entity) && !rat.isInvulnerable() && !rat.isInvulnerableTo((net.minecraft.server.level.ServerLevel) rat.level(), source) && RatUpgradeUtils.hasUpgrade(rat, RatsItemRegistry.RAT_UPGRADE_VOODOO.get()));
			if (!list.isEmpty()) {
				float damage = amount / list.size();
				cancelled = true;
				for (TamedRat rat : list) {
					rat.hurt(source, damage);
				}
			}
		}
			return cancelled;
	}

	/** False if the effect must not be applied (the plague when it does not spread or the mob is immune) */
	public static boolean canApplyEffect(LivingEntity entity, MobEffectInstance instance) {
		return !(instance.getEffect().is(RatsEffectRegistry.PLAGUE.holder()) && (!RatConfig.plagueSpread || entity.is(RatsEntityTags.PLAGUE_IMMUNE)));
	}

	public static void onLevelLoad(ServerLevel server) {
		if (server.dimension().equals(Level.OVERWORLD)) {
			List<CustomSpawner> spawners = new ArrayList<>(server.customSpawners);
			spawners.add(new PlagueDoctorSpawner(server));
			server.customSpawners = spawners;
		}
	}

	/** The mob griefing warning, once per player (kept as an entity tag: the same on both loaders) */
	public static void onPlayerLoggedIn(Player player) {
		if (player.level() instanceof ServerLevel serverLevel && !serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
			if (!player.entityTags().contains(GRIEFING_WARNING_TAG)) {
				player.sendSystemMessage(Component.translatable(RatsLangConstants.MOB_GRIEFING_WARNING).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				player.addTag(GRIEFING_WARNING_TAG);
			}
		}
	}

	private static final String GRIEFING_WARNING_TAG = "rats.griefing_warning";

	public static void onEntityJoin(Entity entity) {
		if (entity != null && entity instanceof IronGolem golem && RatConfig.golemsTargetRats) {
			golem.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(golem, Rat.class, true, (target, level) -> RatUtils.UNTAMED_RAT_SELECTOR.test(target)));
		}

		if (entity != null && RatUtils.isPredator(entity) && entity instanceof Animal animal) {
			animal.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(animal, Rat.class, true, (target, level) -> RatUtils.UNTAMED_RAT_SELECTOR.test(target)));
		}

		if (entity != null && entity instanceof Husk husk) {
			if (husk.getRandom().nextFloat() < 0.12F) {
				husk.setItemSlot(EquipmentSlot.HEAD, new ItemStack(RatsItemRegistry.ARCHEOLOGIST_HAT.get()));
				husk.setDropChance(EquipmentSlot.HEAD, 1.0F);
			}
		}
		if (entity != null && entity instanceof AbstractSkeleton skele && entity.level().getBiome(entity.blockPosition()).is(BiomeTags.IS_JUNGLE)) {
			if (skele.getRandom().nextFloat() < 0.25F) {
				skele.setItemSlot(EquipmentSlot.HEAD, new ItemStack(RatsItemRegistry.ARCHEOLOGIST_HAT.get()));
				skele.setDropChance(EquipmentSlot.HEAD, 1.0F);
			}
		}
	}

	/** A mob was spawned naturally or by world generation: strider jockeys with demon rats */
	public static void onFinalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType) {
		//Carry On passes null into their difficulty when firing this event so we have to check this unfortunately. They shouldnt be doing this.
		if (difficulty == null) return;
		if (difficulty.getDifficulty() != Difficulty.PEACEFUL && (spawnType == EntitySpawnReason.CHUNK_GENERATION || spawnType == EntitySpawnReason.NATURAL)) {
			if (mob instanceof Strider strider && mob.getType() == EntityType.STRIDER) {
				if (!strider.isBaby() && !strider.isSaddled() && strider.getPassengers().isEmpty() && strider.getRandom().nextFloat() < 0.1F) {
					DemonRat demonRat = RatsEntityRegistry.DEMON_RAT.get().create(level.getLevel(), EntitySpawnReason.CONVERSION);
					if (demonRat != null) {
						demonRat.snapTo(strider.getX(), strider.getY(), strider.getZ(), strider.getYRot(), 0.0F);
						demonRat.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
						demonRat.startRiding(strider, true, true);
						demonRat.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
						strider.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
					}
				}
			}
		}
	}

	/**
	 * Right click on a block (both sides). Returns FAIL to cancel the click (a rat staff), PASS to let it go on (the
	 * worm turned coarse dirt into dirt, or nothing happened).
	 */
	public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack held = player.getItemInHand(hand);
		if (held.is(RatsItemRegistry.LITTLE_BLACK_WORM.get()) && level.getBlockState(hit.getBlockPos()).is(Blocks.COARSE_DIRT)) {
			held.shrink(1);
			ItemStack centipede = new ItemStack(RatsItemRegistry.CENTIPEDE.get());
			if (!player.addItem(centipede.copy())) {
				player.drop(centipede, false);
			}
			player.swing(hand);
			level.setBlockAndUpdate(hit.getBlockPos(), Blocks.DIRT.defaultBlockState());
			level.playSound(null, hit.getBlockPos(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 2.0F, 1.0F);
		}
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof RatStaffItem staff) {
			TamedRat rat = null;
			if (SelectedRats.of(player).isPresent()) {
				rat = SelectedRats.of(player).get().getSelectedRat();
			}
			if (rat != null) {
				player.swing(hand);
				if (!level.isClientSide()) {
					RatsNetworkHandler.toPlayer((ServerPlayer) player, new ManageRatStaffPacket(rat.getId(), hit.getBlockPos(), hit.getDirection().ordinal(), false, true, staff.getStaff(stack)));
				}
			} else {
				player.sendOverlayMessage(Component.translatable(RatsLangConstants.RAT_STAFF_NO_RAT).withStyle(ChatFormatting.RED));
			}
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	/** Fishing in Ratlantis catches from its own loot table */
	public static void onItemFished(Player player, FishingHook hook, List<ItemStack> drops) {
		Level level = player.level();
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED && level.dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY)) {
			drops.clear();

			LootParams.Builder builder = (new LootParams.Builder((ServerLevel) level))
					.withParameter(LootContextParams.ORIGIN, hook.position())
					.withParameter(LootContextParams.TOOL, checkHandsForRod(player))
					.withParameter(LootContextParams.ATTACKING_ENTITY, player)
					.withParameter(LootContextParams.THIS_ENTITY, hook)
					.withLuck((float) hook.luck + player.getLuck());
			LootTable loottable = level.getServer().reloadableRegistries().getLootTable(RatsLootRegistry.RATLANTIS_FISH);
			List<ItemStack> list = loottable.getRandomItems(builder.create(LootContextParamSets.FISHING));
						drops.addAll(list);
		}
	}

	private static ItemStack checkHandsForRod(Player player) {
		if (Platform.INSTANCE.canCastFishingRod(player.getMainHandItem())) {
			return player.getMainHandItem();
		} else if (Platform.INSTANCE.canCastFishingRod(player.getOffhandItem())) {
			return player.getOffhandItem();
		}
		return ItemStack.EMPTY;
	}

	/** The pet shop owner's trades of one level (1-5) */
	/** Left click into the air on the client: dismount rats from the shoulder, swing the plague scythe */
	public static void onLeftClickEmpty(Player player, ItemStack stack) {
		if (player.level().isClientSide()) {
			if (player.isShiftKeyDown() && !player.getPassengers().isEmpty()) {
				for (Entity passenger : player.getPassengers()) {
					if (passenger instanceof TamedRat) {
						passenger.stopRiding();
						Vec3 dismountPos = passenger.getDismountLocationForPassenger(player);
						passenger.setPos(dismountPos.x(), dismountPos.y(), dismountPos.z());
						RatsNetworkHandler.toServer(new DismountRatPacket(passenger.getId()));
					}
				}
			}
			handleArmSwing(stack, player);
			RatsNetworkHandler.toServer(new SyncArmSwingPacket(stack));
		}
	}

	/** True if healing is cancelled (the plague stops it) */
	public static boolean cancelHeal(LivingEntity entity) {
		return entity.hasEffect(RatsEffectRegistry.PLAGUE.holder());
	}

	/** Before an entity ticks (client particles of the plague) */
	public static void onEntityTick(Entity entity) {
		if (entity.level().isClientSide() && entity instanceof LivingEntity living && living.hasEffect(RatsEffectRegistry.PLAGUE.holder())) {
			RandomSource rand = entity.getRandom();
			if (rand.nextInt(4) == 0) {
				int entitySize = 1;
				if (entity.getBoundingBox().getSize() > 0) {
					entitySize = Math.max(1, (int) entity.getBoundingBox().getSize());
				}
				for (int i = 0; i < entitySize; i++) {
					float motionX = rand.nextFloat() * 0.1F - 0.05F;
					float motionZ = rand.nextFloat() * 0.1F - 0.05F;

					entity.level().addParticle(RatsParticleRegistry.FLEA.get(),
							entity.getX() + (double) (rand.nextFloat() * entity.getBbWidth() * 2F) - (double) entity.getBbWidth(),
							entity.getY() + (double) (rand.nextFloat() * entity.getBbHeight()),
							entity.getZ() + (double) (rand.nextFloat() * entity.getBbWidth() * 2F) - (double) entity.getBbWidth(),
							motionX, 0.0F, motionZ);
				}
			}
		}
	}

	public static void handleArmSwing(ItemStack stack, Player player) {
		if (stack.is(RatsItemRegistry.PLAGUE_SCYTHE.get())) {
			if (player.swingTime == 0 && !player.isSpectator()) {
				double[] totalDmg = {0};
				stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
					if (attribute.is(Attributes.ATTACK_DAMAGE)) {
						totalDmg[0] += modifier.amount();
					}
				});
				player.playSound(RatsSoundRegistry.PLAGUE_CLOUD_SHOOT.get(), 1, 1);
				PlagueShot shot = new PlagueShot(RatsEntityRegistry.PLAGUE_SHOT.get(), player.level(), player, totalDmg[0] * 0.5F);
				Vec3 vec3 = player.getViewVector(1.0F);

				shot.shoot(vec3.x(), vec3.y(), vec3.z(), 1.0F, 0.5F);
				player.level().addFreshEntity(shot);
			}
		}
	}

	/** An effect was removed or ran out (1.21: MobEffect has no removal hook with the entity) */
	public static void onEffectEnded(LivingEntity entity, @Nullable MobEffectInstance instance) {
		if (instance == null) {
			return;
		}
		if (instance.getEffect().is(RatsEffectRegistry.PLAGUE.holder())) {
			PlagueMobEffect.onRemoved(entity);
		} else if (instance.getEffect().is(RatsEffectRegistry.SYNESTHESIA.holder())) {
			ConfitByaldiMobEffect.onRemoved(entity, instance.getAmplifier());
		}
	}
}
