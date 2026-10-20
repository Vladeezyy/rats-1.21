package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.RatsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/** The plague doctor's spawn timer of a level (data/rats/world_data.dat; keys of the original's rats_world_data.dat) */
public class PlagueDoctorWorldData extends SavedData {
	private static final Codec<PlagueDoctorWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("PlagueDoctorSpawnDelay", 0).forGetter(data -> data.doctorSpawnDelay),
		Codec.INT.optionalFieldOf("PlagueDoctorSpawnChance", 0).forGetter(data -> data.doctorSpawnChance),
		UUIDUtil.STRING_CODEC.optionalFieldOf("PlagueDoctorId").forGetter(data -> Optional.ofNullable(data.doctorID))
	).apply(instance, PlagueDoctorWorldData::new));
	// the wandering trader's fix type: the closest vanilla data, and no fixer touches these keys
	private static final SavedDataType<PlagueDoctorWorldData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "world_data"), PlagueDoctorWorldData::new, CODEC, DataFixTypes.SAVED_DATA_WANDERING_TRADER);
	private int doctorSpawnDelay;
	private int doctorSpawnChance;
	private UUID doctorID;

	public PlagueDoctorWorldData() {
		this.setDirty();
	}

	private PlagueDoctorWorldData(int delay, int chance, Optional<UUID> id) {
		this.doctorSpawnDelay = delay;
		this.doctorSpawnChance = chance;
		this.doctorID = id.orElse(null);
	}

	@Nullable
	public static PlagueDoctorWorldData get(Level level) {
		return level instanceof ServerLevel server ? server.getDataStorage().computeIfAbsent(TYPE) : null;
	}

	public int getDoctorSpawnDelay() {
		return this.doctorSpawnDelay;
	}

	public void setDoctorSpawnDelay(int delay) {
		this.doctorSpawnDelay = delay;
		this.setDirty();
	}

	public int getDoctorSpawnChance() {
		return this.doctorSpawnChance;
	}

	public void setDoctorSpawnChance(int chance) {
		this.doctorSpawnChance = chance;
		this.setDirty();
	}

	public void setPlagueDoctorID(UUID id) {
		this.doctorID = id;
		this.setDirty();
	}
}
