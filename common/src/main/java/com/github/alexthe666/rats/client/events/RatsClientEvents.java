package com.github.alexthe666.rats.client.events;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.Entity;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.entity.StaticRatModel;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.client.render.entity.RatProtectorRenderer;
import com.github.alexthe666.rats.client.util.RatsIconRenderUtil;
import com.github.alexthe666.rats.registry.*;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import com.github.alexthe666.rats.server.capability.SelectedRatCapability;
import com.github.alexthe666.rats.server.capability.SelectedRats;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.events.RatsEvents;
import com.github.alexthe666.rats.server.items.RatStaffItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import org.joml.Quaternionf;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.rats.client.particle.RunningRatParticle;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * The client game event handlers of Rats with vanilla arguments (the original's Forge client event subscribers). Each
 * loader calls them from its own events: NeoForge {@code NeoForgeClientEvents}, Fabric its callbacks and mixins.
 */
public final class RatsClientEvents {

	public static final Identifier PLAGUE_HEART_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/plague_hearts.png");
	private static final Identifier RADIUS_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/rat_radius.png");
	private static final Identifier QUARRY_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/quarry_radius.png");
	private static final Identifier HOME_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/rat_home.png");
	private static final Identifier RAT_DEPOSIT_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/rat_deposit.png");
	private static final Identifier RAT_PICKUP_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/misc/rat_pickup.png");
	private static final Identifier RAT_PATROL_NODE_TEXTURE = Identifier.parse("rats:textures/misc/rat_patrol.png");
	/** assets/rats/post_effect/synesthesia.json */
	private static final Identifier SYNESTHESIA = Identifier.fromNamespaceAndPath(RatsMod.MODID, "synesthesia");
	private static float synesthesiaProgress = 0;
	private static float prevSynesthesiaProgress = 0;
	private static final float MAX_SYNESTESIA = 40;

	private RatsClientEvents() {
	}

	/** The field of view with the synesthesia effect */
	public static double adjustFov(double fov) {
		if (RatConfig.synesthesiaShader) {
			if (prevSynesthesiaProgress > 0) {
				float prog = (prevSynesthesiaProgress + (synesthesiaProgress - prevSynesthesiaProgress) * Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
				float renderProg;
				if (prevSynesthesiaProgress <= synesthesiaProgress) {
					renderProg = (float) Math.sin(prog / MAX_SYNESTESIA * Math.PI) * 40.0F;
				} else {
					renderProg = -(float) Math.sin(prog / MAX_SYNESTESIA * Math.PI) * 40.0F;
				}
				return fov + (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, renderProg);
			}
		}
			return fov;
	}

	/** The FOV modifier while drawing the Ratlantis bow */
	public static float fovModifier(Player player, float modifier) {
				if (player.isUsingItem()) {
			if (player.getUseItem().is(RatlantisItemRegistry.RATLANTIS_BOW.get())) {
				float f = player.getTicksUsingItem() / 10.0F;
				f = f > 1.0F ? 1.0F : f * f;
				return (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (modifier * (1.0F - f * 0.15F)));
			}
		}
			return modifier;
	}

	/** True if the block outline must not be drawn (the marbled cheese rat head) */
	public static boolean cancelBlockHighlight(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(RatlantisBlockRegistry.MARBLED_CHEESE_RAT_HEAD.get());
	}

	/** Before an entity ticks on the client (the local player's synesthesia shader) */
	public static void onClientEntityTick(Entity entity) {
		if (RatConfig.synesthesiaShader) {
			if (entity == Minecraft.getInstance().player) {
				boolean active = Minecraft.getInstance().player.hasEffect(RatsEffectRegistry.SYNESTHESIA.holder());
				// 26.3: the game renderer applies the player's list of post effects every frame (a missing one is skipped)
				java.util.List<Identifier> effects = Minecraft.getInstance().player.getActivePostEffects();
				if (active && !effects.contains(SYNESTHESIA)) {
					effects.add(SYNESTHESIA);
				}
				if (!active) {
					effects.remove(SYNESTHESIA);
				}

				if (prevSynesthesiaProgress == 2 && active) {
					entity.level().playLocalSound(entity.blockPosition(), RatsSoundRegistry.POTION_EFFECT_BEGIN.get(), SoundSource.NEUTRAL, 16.0F, 1.0F, false);
				}
				if (prevSynesthesiaProgress == 38 && !active) {
					entity.level().playLocalSound(entity.blockPosition(), RatsSoundRegistry.POTION_EFFECT_END.get(), SoundSource.NEUTRAL, 16.0F, 1.0F, false);
				}
				prevSynesthesiaProgress = synesthesiaProgress;
				if (active && synesthesiaProgress < MAX_SYNESTESIA) {
					synesthesiaProgress += 2F;
				} else if (!active && synesthesiaProgress > 0.0F) {
					synesthesiaProgress -= 2F;
				}
			}
		}
	}

	/**
	 * After a living entity's render state is filled (the loaders call it): masks hide the player's hat layer. The
	 * plague overlay's flag ({@link #isPlagued}) is stored by the loader in the state.
	 */
	public static void afterLivingExtract(LivingEntity entity, LivingEntityRenderState state) {
		if (state instanceof AvatarRenderState avatar) {
			ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
			if (stack.is(RatsItemRegistry.BLACK_DEATH_MASK.get()) || stack.is(RatsItemRegistry.PLAGUE_DOCTOR_MASK.get()) || stack.is(RatlantisBlockRegistry.MARBLED_CHEESE_RAT_HEAD.get().asItem())) {
				avatar.showHat = false;
			}
		}
	}

	/** Whether the plague overlay goes on this entity (rats draw their own) */
	public static boolean isPlagued(LivingEntity entity) {
		return !(entity instanceof AbstractRat) && entity.hasEffect(RatsEffectRegistry.PLAGUE.holder());
	}

	/** The tooltip of Ratlantis items while the Ratlantis datapack is off */
	public static void onTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
		//only fire if actually loaded into a world, because otherwise the ratlantis flag won't be accurate
		if (player != null) {
			if (!RatsMod.RATLANTIS_DATAPACK_ENABLED) {
				if (!RatsMod.RATLANTIS_ITEMS.isEmpty() && RatsMod.RATLANTIS_ITEMS.contains(stack.getItem())) {
					tooltip.clear();
					tooltip.add(Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color()));
					tooltip.add(Component.translatable("item.rats.ratlantis_disabled.desc0").withStyle(ChatFormatting.DARK_RED));
					tooltip.add(Component.translatable("item.rats.ratlantis_disabled.desc1").withStyle(ChatFormatting.GRAY));
					tooltip.add(Component.translatable("item.rats.ratlantis_disabled.desc2").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}

	/** Right after the vanilla health bar: the plague hearts */
	public static void renderPlagueHearts(GuiGraphicsExtractor graphics) {
		Player player = Minecraft.getInstance().player;
		if (player == null || player.isCreative() || player.isSpectator()) return;
		if (!player.hasEffect(RatsEffectRegistry.PLAGUE.holder()) || !RatConfig.plagueHearts) {
			return;
		}
				int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		int leftHeight = 39;
		int health = Mth.ceil(player.getHealth());
		Hud gui = Minecraft.getInstance().gui.hud;
		boolean highlight = gui.healthBlinkTime > (long) gui.getGuiTicks() && (gui.healthBlinkTime - (long) gui.getGuiTicks()) / 3L % 2L == 1L;

		long j = Util.getMillis();
		if (health < gui.lastHealth && player.getInvulnerableTime() > 0) {
			gui.lastHealthTime = j;
			gui.healthBlinkTime = gui.getGuiTicks() + 20;
		} else if (health > gui.lastHealth && player.getInvulnerableTime() > 0) {
			gui.lastHealthTime = j;
			gui.healthBlinkTime = gui.getGuiTicks() + 10;
		}

		if (j - gui.lastHealthTime > 1000L) {
			gui.displayHealth = health;
			gui.lastHealthTime = j;
		}

		gui.lastHealth = health;
		int healthLast = gui.displayHealth;
		if (RatConfig.singleRowPlagueHearts) {
			//heart overlays cast to long, so have to do the same in this case
			gui.random.setSeed(gui.getGuiTicks() * 312871L);
		} else {
			gui.random.setSeed(gui.getGuiTicks() * 312871);
		}

		AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
		float healthMax = Math.max((float) attrMaxHealth.getValue(), Math.max(healthLast, health));
		int absorption = Mth.ceil(player.getAbsorptionAmount());

		int healthRows = Mth.ceil((healthMax + absorption) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (healthRows - 2), 3);

		int left = width / 2 - 91;
		int top = height - leftHeight;

		int regen = -1;
		if (player.hasEffect(MobEffects.REGENERATION)) {
			regen = gui.getGuiTicks() % (RatConfig.singleRowPlagueHearts ? 25 : Mth.ceil(healthMax + 5.0F));
		}

		int healthAmount = Mth.ceil((double) healthMax / 2.0D);

		for (int currentHeart = RatConfig.singleRowPlagueHearts ? Math.min(9, healthAmount + absorption - 1) : healthAmount + absorption - 1; currentHeart >= 0; currentHeart--) {
			int heartYPos = RatConfig.singleRowPlagueHearts ? 18 : 0;
			int emptyHeartYPos = RatConfig.singleRowPlagueHearts ? 27 : 9;
			int x = left + currentHeart % 10 * 8;
			int y = RatConfig.singleRowPlagueHearts ? top : top - currentHeart / 10 * rowHeight;
			if (health + absorption <= 4) {
				y += gui.random.nextInt(2);
			}

			if (currentHeart < healthAmount && currentHeart == regen) {
				y -= 2;
			}

			if (!RatConfig.singleRowPlagueHearts) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, PLAGUE_HEART_TEXTURE, x, y, 0, emptyHeartYPos, 9, 9, 256, 256);
			}
			int fullHealth = currentHeart * 2;
			//absorption hearts
			if (!RatConfig.singleRowPlagueHearts && currentHeart >= healthAmount) {
				int k2 = fullHealth - health * 2;
				if (k2 < absorption) {
					boolean halfHeart = k2 + 1 == absorption;
					graphics.blit(RenderPipelines.GUI_TEXTURED, PLAGUE_HEART_TEXTURE, x, y, halfHeart ? 9 : 0, heartYPos, 9, 9, 256, 256);
				}
			}

			//blinking hearts
			if (highlight && fullHealth < healthLast) {
				boolean halfHeart = fullHealth + 1 == healthLast;
				graphics.blit(RenderPipelines.GUI_TEXTURED, PLAGUE_HEART_TEXTURE, x, y, halfHeart ? 9 : 0, heartYPos, 9, 9, 256, 256);
			}

			//normal hearts
			if (fullHealth < health) {
				boolean halfHeart = fullHealth + 1 == health;
				graphics.blit(RenderPipelines.GUI_TEXTURED, PLAGUE_HEART_TEXTURE, x, y, halfHeart ? 9 : 0, heartYPos, 9, 9, 256, 256);
			}
		}
	}

	/** 1.21.1's DimensionType.timeOfDay: the sun's place in the day, 0..1 */
	private static float timeOfDay(long dayTime, float partialTick) {
		double d = Mth.frac((dayTime + partialTick) / 24000.0D - 0.25D);
		double e = 0.5D - Math.cos(d * Math.PI) / 2.0D;
		return (float) (d * 2.0D + e) / 3.0F;
	}

	/** The fog color in Ratlantis, or null to keep the vanilla one */
	@Nullable
	public static float[] ratlantisFogColor(Camera camera, float partialTick) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && level.dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY)) {
			float f12 = Mth.clamp(Mth.cos(timeOfDay(level.getDefaultClockTime(), partialTick) * ((float) Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
			if (camera.getFluidInCamera() != net.minecraft.world.level.material.FogType.WATER) {
				return new float[]{f12, f12, f12 * 0.7F};
			}
		}
			return null;
	}

	/**
	 * With the world's other geometry, after the particles (the loaders call it; positions relative to the camera): the
	 * running rats of the flute and the markers of the selected rat for the staffs
	 */
	public static void submitWorldOverlays(PoseStack stack, SubmitNodeCollector collector, Vec3 viewPosition, Quaternionf cameraOrientation, float partialTick) {
		RunningRatParticle.submitAll(stack, collector, viewPosition, partialTick);
		submitSelectedRatMarkers(stack, collector, viewPosition, cameraOrientation, partialTick);
	}

	private static void submitSelectedRatMarkers(PoseStack stack, SubmitNodeCollector collector, Vec3 viewPosition, Quaternionf cameraOrientation, float partialTick) {
		{
			if (Minecraft.getInstance().player != null && SelectedRats.of(Minecraft.getInstance().player).isPresent()) {
				TamedRat rat = SelectedRats.of(Minecraft.getInstance().player).get().getSelectedRat();
				if (rat == null) return;
				ItemStack heldItem = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
								float bob = 1.5F + 0.3F * (Mth.sin((partialTick + Minecraft.getInstance().player.tickCount) * 0.1F) + 1F);
				double px = viewPosition.x;
				double py = viewPosition.y;
				double pz = viewPosition.z;
				if (heldItem.is(RatsItemRegistry.CHEESE_STICK.get())) {
					float finalBob = bob;
					rat.getDepositPos().ifPresent(pos -> {
						if (Minecraft.getInstance().player.level().isLoaded(pos.pos()) && pos.dimension().equals(rat.level().dimension())) {
							RatsIconRenderUtil.submitPOIIcon(RAT_DEPOSIT_TEXTURE, viewPosition, pos.pos(), finalBob, cameraOrientation, stack, collector);
						}
					});
					rat.getPickupPos().ifPresent(pos -> {
						if (Minecraft.getInstance().player.level().isLoaded(pos.pos()) && pos.dimension().equals(rat.level().dimension())) {
							RatsIconRenderUtil.submitPOIIcon(RAT_PICKUP_TEXTURE, viewPosition, pos.pos(), finalBob, cameraOrientation, stack, collector);
						}
					});
					rat.getHomePoint().ifPresent(pos -> {
						if (Minecraft.getInstance().player.level().isLoaded(pos.pos()) && pos.dimension().equals(rat.level().dimension())) {
							RatsIconRenderUtil.submitPOIIcon(HOME_TEXTURE, viewPosition, pos.pos(), finalBob, cameraOrientation, stack, collector);
						}
					});
					if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
						BlockHitResult over = (BlockHitResult) Minecraft.getInstance().hitResult;
						if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockState(over.getBlockPos()).is(RatsBlockRegistry.RAT_QUARRY.get())) {
							if (Minecraft.getInstance().level.getBlockEntity(over.getBlockPos()) instanceof RatQuarryBlockEntity quarry) {
								BlockPos blockPos = quarry.getBlockPos().offset(-quarry.getRadius(), 0, -quarry.getRadius());
								AABB aabb = new AABB(0, -0.05, 0, 1 + quarry.getRadius() * 2, 0.1, 1 + quarry.getRadius() * 2);
								RatsIconRenderUtil.submitBox(QUARRY_TEXTURE, viewPosition, Vec3.atLowerCornerOf(blockPos), aabb, stack, collector);
							}
						}
					}
				} else if (heldItem.is(RatsItemRegistry.RADIUS_STICK.get())) {
					BlockPos blockPos = rat.getSearchCenter();
					if (!Minecraft.getInstance().player.level().isLoaded(blockPos)) return;
					Vec3 renderCenter = new Vec3(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
					double renderRadius = rat.getRadius();
					AABB aabb = new AABB(-renderRadius, -renderRadius, -renderRadius, renderRadius, renderRadius, renderRadius);
					RatsIconRenderUtil.submitBox(RADIUS_TEXTURE, viewPosition, renderCenter, aabb, stack, collector);
				} else if (heldItem.is(RatsItemRegistry.PATROL_STICK.get())) {
					bob = 1.5F + 0.05F * (Mth.sin((partialTick + (float) Minecraft.getInstance().player.tickCount) * 0.1F) + 1.0F);

					for (int i = 0; i < rat.getPatrolNodes().size(); ++i) {
						GlobalPos node = rat.getPatrolNodes().get(i);
						if (!Minecraft.getInstance().player.level().isLoaded(node.pos())) return;
						float r = 0.6F;
						float g = 0.1F;
						float b = 0.1F;
						GlobalPos prev;
						if (i > 0) {
							prev = rat.getPatrolNodes().get(i - 1);
						} else {
							prev = rat.getPatrolNodes().get(rat.getPatrolNodes().size() - 1);
							r = 0.5F;
							g = 0.3F;
						}

						if (node.dimension().equals(Minecraft.getInstance().player.level().dimension()) && prev.dimension().equals(Minecraft.getInstance().player.level().dimension())) {
							stack.pushPose();
							stack.translate(-px, -py, -pz);
							stack.translate((float) prev.pos().getX() + 0.5F, (float) prev.pos().getY() + bob - 0.25F, (float) prev.pos().getZ() + 0.5F);
							float pdx = (float) (node.pos().getX() - prev.pos().getX());
							float pdy = (float) (node.pos().getY() - prev.pos().getY());
							float pdz = (float) (node.pos().getZ() - prev.pos().getZ());
							RatsIconRenderUtil.submitLine(stack, collector, pdx, pdy, pdz, ARGB.colorFromFloat(1.0F, r, g, b));
							stack.popPose();
						}

						if (node.dimension().equals(Minecraft.getInstance().player.level().dimension())) {
							RatsIconRenderUtil.submitPOIIcon(RAT_PATROL_NODE_TEXTURE, viewPosition, node.pos(), bob, cameraOrientation, stack, collector);
						}
					}
				}
			}
		}
	}

	public static boolean isRatSelectedOnStaff(TamedRat rat) {
		if (Minecraft.getInstance().player != null) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RatStaffItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof RatStaffItem) {
				java.util.Optional<SelectedRatCapability> cap = SelectedRats.of(player);
				return cap.isPresent() && Objects.equals(cap.get().getSelectedRat(), rat);
			}
		}
		return false;
	}
}
