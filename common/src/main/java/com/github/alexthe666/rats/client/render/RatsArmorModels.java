package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.hats.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The armor models of the hats and the Ratlantis armor (the original's IClientItemExtensions, which baked a new model
 * every frame): baked once and reused. NeoForge hands them out through its client item extensions, Fabric draws them
 * with its armor renderers.
 */
public final class RatsArmorModels {
	private static final Map<Item, AbstractHatModel> HATS = new ConcurrentHashMap<>();
	private static final Map<EquipmentSlot, AbstractHatModel> RATLANTIS = new ConcurrentHashMap<>();

	private RatsArmorModels() {
	}

	/** The model of a hat, or null for an item without its own model */
	@Nullable
	public static AbstractHatModel hatModel(Item item) {
		AbstractHatModel model = HATS.get(item);
		if (model == null) {
			model = createHatModel(item, Minecraft.getInstance().getEntityModels());
			if (model != null) {
				HATS.put(item, model);
			}
		}
		return model;
	}

	@Nullable
	private static AbstractHatModel createHatModel(Item item, EntityModelSet models) {
		return switch (BuiltInRegistries.ITEM.getKey(item).getPath()) {
					case "chef_toque" ->
							new ChefToqueModel(models.bakeLayer(RatsModelLayers.CHEF_TOQUE));
					case "piper_hat" ->
							new PiperHatModel(models.bakeLayer(RatsModelLayers.PIPER_HAT));
					case "archeologist_hat" ->
							new ArcheologistHatModel(models.bakeLayer(RatsModelLayers.ARCHEOLOGIST_HAT));
					case "farmer_hat", "fisherman_hat" ->
							new FarmerHatModel(models.bakeLayer(RatsModelLayers.FARMER_HAT));
					case "rat_fez" ->
							new RatFezModel(models.bakeLayer(RatsModelLayers.FEZ));
					case "top_hat" ->
							new TopHatModel(models.bakeLayer(RatsModelLayers.TOP_HAT));
					case "santa_hat" ->
							new SantaHatModel(models.bakeLayer(RatsModelLayers.SANTA_HAT));
					case "halo_hat" ->
							new HaloHatModel(models.bakeLayer(RatsModelLayers.HALO));
					case "pirat_hat" ->
							new PiratHatModel(models.bakeLayer(RatsModelLayers.PIRATE_HAT));
					case "rat_king_crown" ->
							new CrownModel(models.bakeLayer(RatsModelLayers.CROWN));
					case "plague_doctor_mask", "black_death_mask" ->
							new PlagueDoctorMaskModel(models.bakeLayer(RatsModelLayers.PLAGUE_DOCTOR_MASK));
					case "exterminator_hat" ->
							new ExterminatorHatModel(models.bakeLayer(RatsModelLayers.EXTERMINATOR_HAT));
					case "aviator_hat" ->
							new AviatorHatModel(models.bakeLayer(RatsModelLayers.AVIATOR_HAT));
					case "ghost_pirat_hat" ->
							new GhostPiratHatModel(models.bakeLayer(RatsModelLayers.PIRATE_HAT));
					case "military_hat" ->
							new MilitaryHatModel(models.bakeLayer(RatsModelLayers.OFFICER_HAT));
					default -> null;
		};
	}

	/**
	 * The Ratlantis armor model of one slot. One instance per slot, with only that slot's parts visible (26.x draws the
	 * models later in the frame, so a shared model's visibility cannot be switched per piece; vanilla bakes one model
	 * per slot too).
	 */
	public static AbstractHatModel ratlantisArmorModel(EquipmentSlot slot) {
		return RATLANTIS.computeIfAbsent(slot, s -> {
			RatlantisArmorModel model = new RatlantisArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(s == EquipmentSlot.LEGS ? RatsModelLayers.RATLANTIS_ARMOR_INNER : RatsModelLayers.RATLANTIS_ARMOR_OUTER));
			model.head.visible = s == EquipmentSlot.HEAD;
			model.hat.visible = s == EquipmentSlot.HEAD;
			model.body.visible = s == EquipmentSlot.CHEST || s == EquipmentSlot.LEGS;
			model.rightArm.visible = s == EquipmentSlot.CHEST;
			model.leftArm.visible = s == EquipmentSlot.CHEST;
			model.rightLeg.visible = s == EquipmentSlot.LEGS || s == EquipmentSlot.FEET;
			model.leftLeg.visible = s == EquipmentSlot.LEGS || s == EquipmentSlot.FEET;
			return model;
		});
	}

	/** After a resource reload the model layers are baked again */
	public static void clear() {
		HATS.clear();
		RATLANTIS.clear();
	}
}
