package com.github.alexthe666.rats.client.events;

import net.minecraft.world.entity.Entity;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.gui.*;
import com.github.alexthe666.rats.client.model.CubeModel;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.deco.RatHammockModel;
import com.github.alexthe666.rats.client.model.deco.RatIglooModel;
import com.github.alexthe666.rats.client.model.deco.RatSeedBowlModel;
import com.github.alexthe666.rats.client.model.deco.RatWaterBottleModel;
import com.github.alexthe666.rats.client.model.entity.*;
import com.github.alexthe666.rats.client.model.hats.*;
import com.github.alexthe666.rats.client.particle.*;
import com.github.alexthe666.rats.client.render.NuggetColorRegister;
import com.github.alexthe666.rats.client.render.RatsRenderType;
import com.github.alexthe666.rats.client.render.block.*;
import com.github.alexthe666.rats.client.render.entity.*;
import com.github.alexthe666.rats.client.render.entity.layer.PartyHatLayer;
import com.github.alexthe666.rats.client.render.entity.layer.PlagueLayer;
import com.github.alexthe666.rats.registry.*;
import com.github.alexthe666.rats.server.block.entity.RatTubeBlockEntity;
import com.github.alexthe666.rats.server.entity.misc.PiratWoodBoat;
import com.github.alexthe666.rats.server.items.*;
import com.github.alexthe666.rats.server.items.upgrades.DemonRatUpgradeItem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.vladeezyy.rats.platform.RegistryEntry;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

/**
 * The client registrations of Rats (the original's mod-bus client events): renderers, model layers, particles,
 * screens, colors and item properties. Each loader passes its own registration API in as a sink.
 */
public final class RatsClientSetup {

	private static ShaderInstance rendertypeRatlantisPortalShader;

	private RatsClientSetup() {
	}

	public static boolean shouldRenderNameplates() {
		return Minecraft.getInstance().screen == null || !(Minecraft.getInstance().screen instanceof RatScreen) && !(Minecraft.getInstance().screen instanceof CheeseStaffScreen);
	}

	@Nullable
	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static void openMobFilterScreen(InteractionHand hand) {
		Minecraft.getInstance().setScreen(new MobFilterScreen(hand));
	}

	/** The shader of the Ratlantis portal ({@code shaders/core/rendertype_ratlantis_portal.json}), set by the loader */
	public static final ResourceLocation RATLANTIS_PORTAL_SHADER = ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "rendertype_ratlantis_portal");

	public static void setRendertypeRatlantisPortalShader(ShaderInstance shader) {
		rendertypeRatlantisPortalShader = shader;
	}

	public static ShaderInstance getRendertypeRatlantisPortalShader() {
		return rendertypeRatlantisPortalShader;
	}

	public interface ItemPropertySink {
		void register(Item item, ResourceLocation id, ClampedItemPropertyFunction function);
	}

	public static void registerItemProperties(ItemPropertySink sink) {
			sink.register(RatsItemRegistry.RAT_SACK.get(), ResourceLocation.parse("rat_count"), (stack, level, entity, i) -> Math.min(3, RatSackItem.getRatsInSack(stack)));

			sink.register(RatlantisItemRegistry.RATLANTIS_BOW.get(), ResourceLocation.parse("pull"), (stack, level, living, i) -> {
				if (living == null) {
					return 0.0F;
				} else {
					return living.getUseItem() != stack ? 0.0F : (float) (stack.getUseDuration(living) - living.getUseItemRemainingTicks()) / 10.0F;
				}
			});

			sink.register(RatlantisItemRegistry.RATLANTIS_BOW.get(), ResourceLocation.parse("pulling"), (stack, level, living, i) -> living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);

			sink.register(RatsItemRegistry.RATBOW_ESSENCE.get(), ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "special"), (stack, level, entity, i) -> {
				if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
					RatsRenderType.GlintType type = RatsRenderType.GlintType.getGlintBasedOnKeyword(stack.getHoverName().getString());
					return type != null && type.changesItemTexture() ? type.ordinal() + 1 : 0;
				}
				return 0;
			});

			sink.register(RatsItemRegistry.RAT_UPGRADE_DEMON.get(), ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "soul"), (stack, level, living, i) -> DemonRatUpgradeItem.isSoulVersion(stack) ? 1 : 0);
			}

	public interface ScreenSink {
		<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void screen(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor);
	}

	public static void registerScreens(ScreenSink sink) {
		sink.screen(RatsMenuRegistry.RAT_CRAFTING_TABLE_CONTAINER.get(), RatCraftingTableScreen::new);
		sink.screen(RatsMenuRegistry.RAT_UPGRADE_CONTAINER.get(), RatUpgradeScreen::new);
		sink.screen(RatsMenuRegistry.RAT_UPGRADE_JR_CONTAINER.get(), JuryRiggedRatUpgradeScreen::new);
		sink.screen(RatsMenuRegistry.UPGRADE_COMBINER_CONTAINER.get(), UpgradeCombinerScreen::new);
		sink.screen(RatsMenuRegistry.AUTO_CURDLER_CONTAINER.get(), AutoCurdlerScreen::new);
	}

	public interface LayerSink {
		void layer(ModelLayerLocation location, Supplier<LayerDefinition> definition);
	}

	public static void registerLayerDefinitions(LayerSink sink) {
		for (PiratWoodBoat.Type boatType : PiratWoodBoat.Type.values()) {
			sink.layer(PiratWoodBoatRenderer.createBoatModelName(boatType), BoatModel::createBodyModel);
			sink.layer(PiratWoodBoatRenderer.createChestBoatModelName(boatType), ChestBoatModel::createBodyModel);
		}

		sink.layer(RatsModelLayers.BLACK_DEATH, BlackDeathModel::create);
		sink.layer(RatsModelLayers.PIPER, PiedPiperModel::create);
		sink.layer(RatsModelLayers.PIRAT_BOAT, PiratBoatModel::create);
		sink.layer(RatsModelLayers.PLAGUE_DOCTOR, PlagueDoctorModel::create);
		sink.layer(RatsModelLayers.RAT_STRIDER_MOUNT, RatStriderMountModel::create);
		sink.layer(RatsModelLayers.THROWN_BLOCK, CubeModel::create);

		sink.layer(RatsModelLayers.HAMMOCK, RatHammockModel::create);
		sink.layer(RatsModelLayers.IGLOO, RatIglooModel::create);
		sink.layer(RatsModelLayers.SEED_BOWL, RatSeedBowlModel::create);
		sink.layer(RatsModelLayers.WATER_BOTTLE, RatWaterBottleModel::create);

		sink.layer(RatsModelLayers.CHEF_TOQUE, ChefToqueModel::create);
		sink.layer(RatsModelLayers.PIPER_HAT, PiperHatModel::create);
		sink.layer(RatsModelLayers.ARCHEOLOGIST_HAT, ArcheologistHatModel::create);
		sink.layer(RatsModelLayers.FARMER_HAT, FarmerHatModel::create);
		sink.layer(RatsModelLayers.FEZ, RatFezModel::create);
		sink.layer(RatsModelLayers.TOP_HAT, TopHatModel::create);
		sink.layer(RatsModelLayers.SANTA_HAT, SantaHatModel::create);
		sink.layer(RatsModelLayers.HALO, HaloHatModel::create);
		sink.layer(RatsModelLayers.PARTY_HAT, PartyHatModel::create);
		sink.layer(RatsModelLayers.PIRATE_HAT, PiratHatModel::create);
		sink.layer(RatsModelLayers.CROWN, CrownModel::create);
		sink.layer(RatsModelLayers.PLAGUE_DOCTOR_MASK, PlagueDoctorMaskModel::create);
		sink.layer(RatsModelLayers.EXTERMINATOR_HAT, ExterminatorHatModel::create);
		sink.layer(RatsModelLayers.AVIATOR_HAT, AviatorHatModel::create);
		sink.layer(RatsModelLayers.OFFICER_HAT, MilitaryHatModel::create);

		sink.layer(RatsModelLayers.RATLANTIS_ARMOR_OUTER, () -> RatlantisArmorModel.create(LayerDefinitions.OUTER_ARMOR_DEFORMATION));
		sink.layer(RatsModelLayers.RATLANTIS_ARMOR_INNER, () -> RatlantisArmorModel.create(LayerDefinitions.INNER_ARMOR_DEFORMATION));
	}

	public interface RendererSink {
		<E extends Entity> void entity(EntityType<? extends E> type, EntityRendererProvider<E> provider);

		<B extends BlockEntity> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B> provider);
	}

	public static void registerRenderers(RendererSink sink) {
		sink.entity(RatsEntityRegistry.RAT.get(), RatRenderer::new);
		sink.entity(RatsEntityRegistry.TAMED_RAT.get(), TamedRatRenderer::new);
		sink.entity(RatsEntityRegistry.PIED_PIPER.get(), PiedPiperRenderer::new);
		sink.entity(RatsEntityRegistry.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
		sink.entity(RatsEntityRegistry.PLAGUE_DOCTOR.get(), PlagueDoctorRenderer::new);
		sink.entity(RatsEntityRegistry.PURIFYING_LIQUID.get(), ThrownItemRenderer::new);
		sink.entity(RatsEntityRegistry.BLACK_DEATH.get(), BlackDeathRenderer::new);
		sink.entity(RatsEntityRegistry.PLAGUE_CLOUD.get(), context -> new RatlateanSpiritRenderer<>(context, true));
		sink.entity(RatsEntityRegistry.PLAGUE_BEAST.get(), PlagueBeastRenderer::new);
		sink.entity(RatsEntityRegistry.PLAGUE_SHOT.get(), PlagueShotRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_CAPTURE_NET.get(), ThrownItemRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_DRAGON_FIRE.get(), NothingRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_ARROW.get(), RatArrowRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_MOUNT_GOLEM.get(), RatGolemMountRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_MOUNT_CHICKEN.get(), RatChickenMountRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_MOUNT_BEAST.get(), RatBeastMountRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_KING.get(), RatKingRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_SHOT.get(), RatShotRenderer::new);
		sink.entity(RatsEntityRegistry.DEMON_RAT.get(), DemonRatRenderer::new);
		sink.entity(RatsEntityRegistry.RAT_STRIDER_MOUNT.get(), RatStriderMountRenderer::new);
		sink.entity(RatsEntityRegistry.SMALL_ARROW.get(), SmallArrowRenderer::new);

		sink.blockEntity(RatsBlockEntityRegistry.RAT_HOLE.get(), RatHoleRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.RAT_TRAP.get(), RatTrapRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.AUTO_CURDLER.get(), AutoCurdlerRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.RAT_CAGE_DECORATED.get(), DecoratedRatCageRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.RAT_CAGE_BREEDING_LANTERN.get(), DecoratedRatCageRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.RAT_CAGE_WHEEL.get(), DecoratedRatCageRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.UPGRADE_COMBINER.get(), UpgradeCombinerRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.UPGRADE_SEPERATOR.get(), UpgradeSeparatorRenderer::new);
		sink.blockEntity(RatsBlockEntityRegistry.TRASH_CAN.get(), TrashCanRenderer::new);


		sink.entity(RatlantisEntityRegistry.DUTCHRAT.get(), DutchratRenderer::new);
		sink.entity(RatlantisEntityRegistry.DUTCHRAT_SWORD.get(), DutchratSwordRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATFISH.get(), RatfishRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATTLING_GUN.get(), RattlingGunRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATTLING_GUN_BULLET.get(), RattlingGunBulletRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATLANTEAN_RATBOT.get(), RatlanteanRatbotRenderer::new);
		sink.entity(RatlantisEntityRegistry.PIRAT.get(), PiratRenderer::new);
		sink.entity(RatlantisEntityRegistry.RAT_MOUNT_AUTOMATON.get(), RatAutomatonMountRenderer::new);
		sink.entity(RatlantisEntityRegistry.GHOST_PIRAT.get(), GhostPiratRenderer::new);
		sink.entity(RatlantisEntityRegistry.RAT_BARON.get(), RatBaronRenderer::new);
		sink.entity(RatlantisEntityRegistry.RAT_BARON_PLANE.get(), RatBaronPlaneRenderer::new);
		sink.entity(RatlantisEntityRegistry.RAT_MOUNT_BIPLANE.get(), RatBiplaneMountRenderer::new);
		sink.entity(RatlantisEntityRegistry.RAT_PROTECTOR.get(), RatProtectorRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATLANTIS_ARROW.get(), RatlantisArrowRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATLANTEAN_SPIRIT.get(), context -> new RatlateanSpiritRenderer<>(context, false));
		sink.entity(RatlantisEntityRegistry.RATLANTEAN_FLAME.get(), ThrownItemRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATLANTEAN_AUTOMATON.get(), RatlanteanAutomatonRenderer::new);
		sink.entity(RatlantisEntityRegistry.RATLANTEAN_AUTOMATON_BEAM.get(), GolemBeamRenderer::new);
		sink.entity(RatlantisEntityRegistry.FERAL_RATLANTEAN.get(), FeralRatlanteanRenderer::new);
		sink.entity(RatlantisEntityRegistry.NEO_RATLANTEAN.get(), NeoRatlanteanRenderer::new);
		sink.entity(RatlantisEntityRegistry.LASER_BEAM.get(), LaserBeamRenderer::new);
		sink.entity(RatlantisEntityRegistry.LASER_PORTAL.get(), LaserPortalRenderer::new);
		sink.entity(RatlantisEntityRegistry.VIAL_OF_SENTIENCE.get(), ThrownItemRenderer::new);
		sink.entity(RatlantisEntityRegistry.PIRAT_BOAT.get(), context -> new PiratBoatRenderer<>(context, new PiratBoatModel<>(context.bakeLayer(RatsModelLayers.PIRAT_BOAT))));
		sink.entity(RatlantisEntityRegistry.CHEESE_CANNONBALL.get(), ThrownItemRenderer::new);
		sink.entity(RatlantisEntityRegistry.BOAT.get(), context -> new PiratWoodBoatRenderer(context, false));
		sink.entity(RatlantisEntityRegistry.CHEST_BOAT.get(), context -> new PiratWoodBoatRenderer(context, true));

		sink.blockEntity(RatlantisBlockEntityRegistry.RATLANTIS_PORTAL.get(), RatlantisPortalRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.DUTCHRAT_BELL.get(), DutchratBellRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.AUTOMATON_HEAD.get(), RatlanteanAutomatonHeadRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.TOKEN.get(), RatlantisTokenRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.PIRAT_SIGN.get(), PiratSignRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.PIRAT_HANGING_SIGN.get(), PiratHangingSignRenderer::new);
	}

	public interface ParticleSink {
		<T extends ParticleOptions> void sprite(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration);

		<T extends ParticleOptions> void special(ParticleType<T> type, ParticleProvider<T> provider);
	}

	public static void registerParticles(ParticleSink sink) {
		sink.sprite(RatsParticleRegistry.BLACK_DEATH.get(), BlackDeathParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.DUTCHRAT_SMOKE.get(), DutchratSmokeParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.FLEA.get(), FleaParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.FLY.get(), FlyParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.LIGHTNING.get(), LightningParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.MILK_BUBBLE.get(), MilkBubbleParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.PIRAT_GHOST.get(), PiratGhostParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.RAT_GHOST.get(), RatGhostParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.RAT_KING_SMOKE.get(), RatKingSmokeParticle.Provider::new);
		sink.special(RatsParticleRegistry.RUNNING_RAT.get(), new RunningRatParticle.Provider());
		sink.sprite(RatsParticleRegistry.SALIVA.get(), SalivaParticle.Provider::new);
		sink.sprite(RatsParticleRegistry.UPGRADE_COMBINER.get(), UpgradeCombinerParticle.Provider::new);
	}

	public interface BlockColorSink {
		void register(BlockColor color, Block... blocks);
	}

	public static void registerBlockColors(BlockColorSink sink) {
		sink.register((state, level, pos, tint) -> {
			int meta = 0;
			if (level != null && pos != null && level.getBlockEntity(pos) instanceof RatTubeBlockEntity tube) {
				meta = tube.getColor();
			}
			DyeColor color = DyeColor.byId(meta);
			return color.getFireworkColor();
		}, RatsBlockRegistry.RAT_TUBE_COLOR.get());


		sink.register((state, level, pos, tint) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.get(0.5D, 1.0D), RatlantisBlockRegistry.MARBLED_CHEESE_GRASS.get());
	}

	public interface ItemColorSink {
		void register(ItemColor color, ItemLike... items);
	}

	public static void registerItemColors(ItemColorSink sink) {
		sink.register((stack, tint) -> FoliageColor.get(0.5D, 1.0D), RatlantisBlockRegistry.MARBLED_CHEESE_GRASS.get().asItem());

		for (RegistryEntry<Item, Item> item : RatsItemRegistry.RAT_TUBES) {
			sink.register((stack, tint) -> ((RatTubeItem) item.get()).color.getFireworkColor(), item.get());
		}
		for (RegistryEntry<Item, Item> item : RatsItemRegistry.RAT_IGLOOS) {
			sink.register((stack, tint) -> ((RatIglooItem) item.get()).color.getFireworkColor(), item.get());
		}
		for (RegistryEntry<Item, Item> item : RatsItemRegistry.RAT_HAMMOCKS) {
			sink.register((stack, tint) -> ((RatHammockItem) item.get()).color.getFireworkColor(), item.get());
		}
		sink.register((stack, tint) -> {
			if (tint == 1) {
				return NuggetColorRegister.getNuggetColor(stack);
			} else {
				return -1;
			}
		}, RatsItemRegistry.RAT_NUGGET_ORE.get());

		sink.register((stack, tintIndex) -> {
					int colorToUse;
					if (tintIndex == 0) {
						colorToUse = stack.getItem() instanceof PartyHatItem hat
								? PartyHatItem.getColor(stack)
								: 0x25C9E7;
					} else {
						colorToUse = stack.getItem() instanceof PartyHatItem hat
								? invertColor(PartyHatItem.getColor(stack))
								: invertColor(0x25C9E7);
					}
					return colorToUse;
				},
				RatsItemRegistry.PARTY_HAT.get());
	}

	private static int invertColor(int color) {
		int a = (color >> 24) & 0xff;
		int r = (color >> 16) & 0xff;
		int g = (color >> 8) & 0xff;
		int b = color & 0xff;

		r = 255 - r;
		g = 255 - g;
		b = 255 - b;
		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}

	/** The plague and party hat layers every living renderer gets (the loaders call it for each renderer) */
	public static <T extends LivingEntity, M extends EntityModel<T>> void attachRenderLayers(LivingEntityRenderer<T, M> renderer) {
		renderer.addLayer(new PlagueLayer<>(renderer));
		if (renderer.getModel() instanceof HumanoidModel<?>) {
			renderer.addLayer(new PartyHatLayer<>(renderer, new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
		}
	}
}
