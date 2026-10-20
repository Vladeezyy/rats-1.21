package com.github.alexthe666.rats.client.render.block;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.deco.RatBreedingLanternModel;
import com.github.alexthe666.rats.client.model.deco.RatHammockModel;
import com.github.alexthe666.rats.client.model.deco.RatIglooModel;
import com.github.alexthe666.rats.client.model.deco.RatSeedBowlModel;
import com.github.alexthe666.rats.client.model.deco.RatWaterBottleModel;
import com.github.alexthe666.rats.client.model.deco.RatWheelModel;
import com.github.alexthe666.rats.client.render.PosedModels;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.block.RatCageDecoratedBlock;
import com.github.alexthe666.rats.server.block.entity.DecoratedRatCageBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCageBreedingLanternBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCageWheelBlockEntity;
import com.github.alexthe666.rats.server.items.RatHammockItem;
import com.github.alexthe666.rats.server.items.RatIglooItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

/** What a decorated cage holds: igloo, hammock, water bottle, seed bowl, wheel or breeding lantern */
public class DecoratedRatCageRenderer extends StateBlockEntityRenderer<DecoratedRatCageBlockEntity> {
	private static final RatBreedingLanternModel<?> MODEL_RAT_BREEDING_LANTERN = new RatBreedingLanternModel<>();
	private static final RatWheelModel<?> MODEL_RAT_WHEEL = new RatWheelModel<>();
	private static final RenderType TEXTURE_RAT_IGLOO = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_igloo.png"));
	private static final RenderType TEXTURE_RAT_HAMMOCK = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_hammock_0.png"));
	private static final RenderType TEXTURE_RAT_WATER_BOTTLE = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_water_bottle.png"));
	private static final RenderType TEXTURE_RAT_SEED_BOWL = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_seed_bowl.png"));
	private static final RenderType TEXTURE_RAT_BREEDING_LANTERN = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_breeding_lantern.png"));
	private static final RenderType TEXTURE_RAT_WHEEL = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/block/rat_wheel.png"));
	private final RatIglooModel igloo;
	private final RatHammockModel hammock;
	private final RatWaterBottleModel waterBottle;
	private final RatSeedBowlModel seedBowl;

	public DecoratedRatCageRenderer(BlockEntityRendererProvider.Context context) {
		this.igloo = new RatIglooModel(context.bakeLayer(RatsModelLayers.IGLOO));
		this.hammock = new RatHammockModel(context.bakeLayer(RatsModelLayers.HAMMOCK));
		this.waterBottle = new RatWaterBottleModel(context.bakeLayer(RatsModelLayers.WATER_BOTTLE));
		this.seedBowl = new RatSeedBowlModel(context.bakeLayer(RatsModelLayers.SEED_BOWL));
	}

	private static void submit(SubmitNodeCollector collector, PoseStack stack, Model<Unit> model, RenderType type, int light, int overlay, int color) {
		collector.submitModel(model, Unit.INSTANCE, stack, type, light, overlay, color, null, 0, null);
	}

	@Override
	protected void submit(DecoratedRatCageBlockEntity entity, float partialTicks, PoseStack stack, SubmitNodeCollector collector, int light, int overlay) {
		stack.pushPose();
		stack.translate(0.5D, 1.5D, 0.5D);
		stack.mulPose(Axis.ZP.rotationDegrees(180));
		float f = entity.getBlockState().getValue(RatCageDecoratedBlock.FACING).getOpposite().toYRot();
		stack.mulPose(Axis.YP.rotationDegrees(f));
		ItemStack containedItem = entity.getLevel() != null ? entity.getContainedItem() : ItemStack.EMPTY;
		if (containedItem.getItem() instanceof RatIglooItem iglooItem) {
			submit(collector, stack, this.igloo, TEXTURE_RAT_IGLOO, light, overlay, iglooItem.color.getTextureDiffuseColor());
		}
		if (containedItem.getItem() instanceof RatHammockItem hammockItem) {
			submit(collector, stack, this.hammock, TEXTURE_RAT_HAMMOCK, light, overlay, hammockItem.color.getTextureDiffuseColor());
		}
		if (containedItem.is(RatsItemRegistry.RAT_WATER_BOTTLE.get())) {
			submit(collector, stack, this.waterBottle, TEXTURE_RAT_WATER_BOTTLE, light, overlay, -1);
		}
		if (containedItem.is(RatsItemRegistry.RAT_SEED_BOWL.get())) {
			submit(collector, stack, this.seedBowl, TEXTURE_RAT_SEED_BOWL, light, overlay, -1);
		}
		if (containedItem.is(RatsItemRegistry.RAT_WHEEL.get())) {
			PosedModels.submit(collector, stack, TEXTURE_RAT_WHEEL, MODEL_RAT_WHEEL, () -> {
				if (entity instanceof RatCageWheelBlockEntity wheel) {
					MODEL_RAT_WHEEL.animate(wheel, partialTicks);
				} else {
					MODEL_RAT_WHEEL.resetToDefaultPose();
				}
			}, light, overlay, -1);
		}
		if (containedItem.is(RatsItemRegistry.RAT_BREEDING_LANTERN.get()) && entity instanceof RatCageBreedingLanternBlockEntity lantern) {
			float brightness = lantern.getBreedingCooldown() > 0 ? 0.5F : 1.0F;
			PosedModels.submit(collector, stack, TEXTURE_RAT_BREEDING_LANTERN, MODEL_RAT_BREEDING_LANTERN, MODEL_RAT_BREEDING_LANTERN::swingChain, light, overlay, ARGB.colorFromFloat(1.0F, brightness, brightness, brightness));
		}
		stack.popPose();
	}
}
