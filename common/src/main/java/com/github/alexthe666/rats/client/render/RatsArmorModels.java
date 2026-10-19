package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.client.model.RatsModelLayers;
import com.github.alexthe666.rats.client.model.hats.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
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
	private static final Map<Item, HumanoidModel<?>> HATS = new ConcurrentHashMap<>();
	private static final Map<EquipmentSlot, HumanoidModel<?>> RATLANTIS = new ConcurrentHashMap<>();

	private RatsArmorModels() {
	}

	/** The model of a hat, or null for an item without its own model */
	@Nullable
	public static HumanoidModel<?> hatModel(Item item) {
		HumanoidModel<?> model = HATS.get(item);
		if (model == null) {
			model = createHatModel(item, Minecraft.getInstance().getEntityModels());
			if (model != null) {
				HATS.put(item, model);
			}
		}
		return model;
	}

	@Nullable
	private static HumanoidModel<?> createHatModel(Item item, EntityModelSet models) {
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

	public static HumanoidModel<?> ratlantisArmorModel(EquipmentSlot slot) {
		return RATLANTIS.computeIfAbsent(slot == EquipmentSlot.LEGS ? EquipmentSlot.LEGS : EquipmentSlot.CHEST, s -> new RatlantisArmorModel(
			Minecraft.getInstance().getEntityModels().bakeLayer(s == EquipmentSlot.LEGS ? RatsModelLayers.RATLANTIS_ARMOR_INNER : RatsModelLayers.RATLANTIS_ARMOR_OUTER)));
	}

	/** After a resource reload the model layers are baked again */
	public static void clear() {
		HATS.clear();
		RATLANTIS.clear();
	}
}
