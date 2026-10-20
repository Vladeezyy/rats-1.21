package com.github.alexthe666.rats.client.events;

import com.github.alexthe666.rats.client.render.RatsItemModels;
import com.github.alexthe666.rats.client.render.RatsItemSpecialRenderer;
import com.github.alexthe666.rats.client.render.entity.layer.HatLayer;
import com.github.alexthe666.rats.client.render.entity.layer.RatProtectorLayer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
import com.github.alexthe666.rats.server.items.*;
import com.github.alexthe666.rats.server.items.upgrades.DemonRatUpgradeItem;
import dev.vladeezyy.rats.platform.RegistryEntry;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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


	private RatsClientSetup() {
	}

	public static boolean shouldRenderNameplates() {
		return Minecraft.getInstance().gui.screen() == null || !(Minecraft.getInstance().gui.screen() instanceof RatScreen) && !(Minecraft.getInstance().gui.screen() instanceof CheeseStaffScreen);
	}

	@Nullable
	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static void openMobFilterScreen(InteractionHand hand) {
		Minecraft.getInstance().gui.setScreen(new MobFilterScreen(hand));
	}

	/** What the item definitions (assets/rats/items/*.json) use beyond vanilla: properties, tints, special models */
	public interface ItemModelSink {
		void rangeProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec);

		void conditionalProperty(Identifier id, MapCodec<? extends ConditionalItemModelProperty> codec);

		void tintSource(Identifier id, MapCodec<? extends ItemTintSource> codec);

		void specialRenderer(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec);
	}

	public static void registerItemModels(ItemModelSink sink) {
		sink.rangeProperty(RatsItemModels.RAT_COUNT, RatsItemModels.RatCount.MAP_CODEC);
		sink.rangeProperty(RatsItemModels.SPECIAL_DYE, RatsItemModels.SpecialDye.MAP_CODEC);
		sink.conditionalProperty(RatsItemModels.SOUL, RatsItemModels.Soul.MAP_CODEC);
		sink.tintSource(RatsItemModels.NUGGET_ORE, RatsItemModels.NuggetOre.MAP_CODEC);
		sink.tintSource(RatsItemModels.PARTY_HAT, RatsItemModels.PartyHatInverse.MAP_CODEC);
		sink.specialRenderer(RatsItemSpecialRenderer.ID, RatsItemSpecialRenderer.Unbaked.MAP_CODEC);
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
		sink.layer(RatsModelLayers.PIRAT_WOOD_BOAT, BoatModel::createBoatModel);
		sink.layer(RatsModelLayers.PIRAT_WOOD_CHEST_BOAT, BoatModel::createChestBoatModel);

		sink.layer(RatsModelLayers.BLACK_DEATH, BlackDeathModel::create);
		sink.layer(RatsModelLayers.PIPER, PiedPiperModel::create);
		sink.layer(RatsModelLayers.PIRAT_BOAT, PiratBoatModel::create);
		sink.layer(RatsModelLayers.PIRAT_BOAT_WATER_PATCH, PiratBoatModel::createWaterPatch);
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

		<B extends BlockEntity, S extends BlockEntityRenderState> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B, S> provider);
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
		sink.entity(RatlantisEntityRegistry.PIRAT_BOAT.get(), PiratBoatRenderer::new);
		sink.entity(RatlantisEntityRegistry.CHEESE_CANNONBALL.get(), ThrownItemRenderer::new);
		sink.entity(RatlantisEntityRegistry.BOAT.get(), context -> new BoatRenderer(context, RatsModelLayers.PIRAT_WOOD_BOAT));
		sink.entity(RatlantisEntityRegistry.CHEST_BOAT.get(), context -> new BoatRenderer(context, RatsModelLayers.PIRAT_WOOD_CHEST_BOAT));

		sink.blockEntity(RatlantisBlockEntityRegistry.RATLANTIS_PORTAL.get(), RatlantisPortalRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.DUTCHRAT_BELL.get(), DutchratBellRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.AUTOMATON_HEAD.get(), RatlanteanAutomatonHeadRenderer::new);
		sink.blockEntity(RatlantisBlockEntityRegistry.TOKEN.get(), RatlantisTokenRenderer::new);
		// vanilla's sign renderers (the pirat wood type's textures in the sign atlases)
		sink.blockEntity(RatlantisBlockEntityRegistry.PIRAT_SIGN.get(), signRenderer(StandingSignRenderer::new));
		sink.blockEntity(RatlantisBlockEntityRegistry.PIRAT_HANGING_SIGN.get(), signRenderer(HangingSignRenderer::new));
	}

	public interface ParticleSink {
		<T extends ParticleOptions> void sprite(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> registration);

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

	public interface BlockTintSink {
		void register(List<BlockTintSource> tints, Block... blocks);
	}

	public static void registerBlockTints(BlockTintSink sink) {
		// a tube's dye color (kept by its block entity)
		sink.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return ARGB.opaque(DyeColor.WHITE.getFireworkColor());
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				int meta = level.getBlockEntity(pos) instanceof RatTubeBlockEntity tube ? tube.getColor() : 0;
				return ARGB.opaque(DyeColor.byId(meta).getFireworkColor());
			}
		}), RatsBlockRegistry.RAT_TUBE_COLOR.get());
		sink.register(List.of(BlockTintSources.foliage()), RatlantisBlockRegistry.MARBLED_CHEESE_GRASS.get());
	}

	/**
	 * The layers vanilla renderers get (the loaders call it for each living renderer): the plague overlay; on humanoids
	 * the mod's hats, the party hat and the Ratlantis armor's protector rats
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void attachRenderLayers(LivingEntityRenderer<?, ?, ?> renderer, Consumer<RenderLayer<?, ?>> addLayer) {
		LivingEntityRenderer raw = renderer;
		addLayer.accept(new PlagueLayer<>(raw));
		if (renderer.getModel() instanceof HumanoidModel<?>) {
			addLayer.accept(new HatLayer<>(raw));
			addLayer.accept(new RatProtectorLayer<>(raw));
		}
	}

	/** Vanilla's sign renderers for the pirat signs' own block entity types */
	@SuppressWarnings("unchecked")
	private static <B extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRendererProvider<B, S> signRenderer(Function<BlockEntityRendererProvider.Context, ? extends BlockEntityRenderer<SignBlockEntity, ?>> factory) {
		return context -> (BlockEntityRenderer<B, S>) (BlockEntityRenderer<?, ?>) factory.apply(context);
	}
}
