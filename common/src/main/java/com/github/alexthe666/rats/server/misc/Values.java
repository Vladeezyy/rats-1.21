package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * 26.x saves through {@link ValueOutput} / {@link ValueInput} instead of a {@code CompoundTag}. The values the original
 * kept as NBT (lists, block states, item handlers, encoded positions) go through these as the same NBT, under the same
 * keys, so worlds saved by the 1.21.1 port load unchanged.
 */
public final class Values {
	private Values() {}

	public static void put(ValueOutput output, String key, Tag tag) {
		output.store(key, ExtraCodecs.NBT, tag);
	}

	@Nullable
	public static Tag get(ValueInput input, String key) {
		return input.read(key, ExtraCodecs.NBT).orElse(null);
	}

	public static boolean contains(ValueInput input, String key) {
		return input.read(key, ExtraCodecs.NBT).isPresent();
	}

	/** The compound under the key, or an empty one */
	public static CompoundTag compound(ValueInput input, String key) {
		return input.read(key, CompoundTag.CODEC).orElseGet(CompoundTag::new);
	}

	/** The list under the key, or an empty one */
	public static ListTag list(ValueInput input, String key) {
		return get(input, key) instanceof ListTag list ? list : new ListTag();
	}

	/** An entity's save data as NBT (the rat sack, the rat copied when tamed) */
	public static CompoundTag save(Entity entity) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
		entity.saveWithoutId(output);
		return output.buildResult();
	}

	/** Loads NBT saved by {@link #save} into an entity */
	public static void load(Entity entity, CompoundTag tag) {
		entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
	}

	/** NBT as a value input, for code that reads a compound the way it reads a save */
	public static ValueInput input(HolderLookup.Provider registries, CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING, registries, tag);
	}
}
