package com.github.alexthe666.rats.client.render.entity;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.client.model.entity.AbstractRatModel;
import com.github.alexthe666.rats.client.model.entity.PinkieModel;
import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.client.render.SubmitHelper;
import com.github.alexthe666.rats.client.render.entity.layer.TamedRatEyesLayer;
import com.github.alexthe666.rats.client.render.entity.layer.TamedRatOverlayLayer;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesTextureUpgrade;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TamedRatRenderer extends AbstractRatRenderer<TamedRat, AbstractRatModel<TamedRat>> {

	private static final RatModel<TamedRat> RAT_MODEL = new RatModel<>();
	private static final PinkieModel<TamedRat> PINKIE_MODEL = new PinkieModel<>();
	private static final Identifier PINKIE_TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/baby.png");
	private static final String[] FISHING_PROGRESS = new String[] {".", "..", "..."};

	private static final ImmutableMap<String, Identifier> SPECIAL_SKINS = ImmutableMap.<String, Identifier>builder()
		.put("brick", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/brick.png"))
		.put("bugraak", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/bugraak.png"))
		.put("dino", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/dino.png"))
		.put("friar", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/friar.png"))
		.put("gizmo", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/gizmo.png"))
		.put("julian", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/julian.png"))
		.put("lil_cheese", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/lil_cheese.png"))
		.put("ratatla", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/ratatla.png"))
		.put("riddler", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/riddler.png"))
		.put("sharva", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/sharva.png"))
		.put("shizuka", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/shizuka.png"))
		.put("skrat", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/skrat.png"))
		.put("splinter", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/splinter.png"))
		.put("ultrakill", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/ultrakill.png"))
		.put("zura", Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/rat/patreon_skins/zura.png"))
		.build();

	private final SubmitHelper items;
	private ItemStack fishingRod = ItemStack.EMPTY;

	public TamedRatRenderer(EntityRendererProvider.Context context) {
		super(context, RAT_MODEL);
		this.items = new SubmitHelper(context);
		this.addLayer(new TamedRatOverlayLayer<>(this));
		this.addLayer(new TamedRatEyesLayer<>(this));
	}

	@Override
	protected boolean shouldShowName(TamedRat entity, double distanceToCameraSq) {
		return RatsClientSetup.shouldRenderNameplates() && super.shouldShowName(entity, distanceToCameraSq);
	}

	@Override
	public void extractRenderState(TamedRat entity, RatRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.fishing = RatsClientSetup.shouldRenderNameplates() && entity.crafting && RatUpgradeUtils.hasUpgrade(entity, RatsItemRegistry.RAT_UPGRADE_FISHERMAN.get());
		state.showsName = state.nameTag != null;
	}

	@Override
	public void submit(RatRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
		// pinkies have their own model (the layers read the renderer's model while they submit)
		this.model = state.isBaby ? PINKIE_MODEL : RAT_MODEL;
		super.submit(state, stack, collector, camera);
		if (state.fishing) {
			boolean hasName = state.showsName;
			if (this.fishingRod.isEmpty()) {
				this.fishingRod = new ItemStack(Items.FISHING_ROD);
			}
			stack.pushPose();
			stack.translate(0.0F, hasName ? 1.3F : 1.05F, 0.0F);
			stack.rotate(camera.orientation);
			stack.scale(0.25F, 0.25F, 0.25F);
			stack.rotate(Axis.YP.rotationDegrees(180));
			stack.rotate(Axis.ZP.rotationDegrees(Mth.sin(state.ageInTicks / 5) * 20));
			this.items.item(this.fishingRod, ItemDisplayContext.GUI, state.pose.entity, stack, collector, state.lightCoords);
			stack.popPose();

			stack.pushPose();
			stack.translate(0.0F, state.boundingBoxHeight + 0.5F, 0.0F);
			stack.rotate(camera.orientation);
			stack.scale(0.025F, -0.025F, 0.025F);
			String dots = FISHING_PROGRESS[(int) (Util.getMillis() / 300L % (long) FISHING_PROGRESS.length)];
			collector.submitText(stack, -this.getFont().width(dots) / 2.0F, hasName ? -10 : 0, Component.literal(dots).getVisualOrderText(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF, 0, 0);
			stack.popPose();
		}
	}

	@Override
	protected Identifier getTexture(TamedRat entity) {
		if (entity.isBaby()) {
			return PINKIE_TEXTURE;
		} else {
			AtomicReference<Identifier> upgradeTex = new AtomicReference<>(null);

			RatUpgradeUtils.forEachUpgrade(entity, item -> item instanceof ChangesTextureUpgrade, (stack, slot) -> {
				if (entity.isSlotVisible(slot)) {
					upgradeTex.set(((ChangesTextureUpgrade) stack.getItem()).getTexture());
				}
			});

			if (upgradeTex.get() != null) {
				return upgradeTex.get();
			}

			if (entity.hasCustomName()) {
				String name = entity.getCustomName().getString().toLowerCase(Locale.ROOT);
				if (SPECIAL_SKINS.containsKey(name)) {
					return Objects.requireNonNull(SPECIAL_SKINS.get(name));
				}
			}

			return super.getTexture(entity);
		}
	}
}
