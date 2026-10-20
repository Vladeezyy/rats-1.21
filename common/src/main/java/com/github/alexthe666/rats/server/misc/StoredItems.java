package com.github.alexthe666.rats.server.misc;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Items stored inside an item (combined upgrades, the black/white list upgrades), in {@code minecraft:container}.
 *
 * <p>The 1.20.1 original kept them as an "Items" list in the item's NBT and parsed that list every time a rat checked
 * its upgrades (several times per tick per rat). The container component is parsed once when the stack is loaded and
 * read here without copying. Stacks from 1.20.1 worlds still carry the old list in {@code custom_data}: it is moved
 * into the component the first time the stack is read on the server.
 */
public final class StoredItems {
	/** Data version of 1.20.1, the original's version */
	private static final int LEGACY_DATA_VERSION = 3465;

	private StoredItems() {
	}

	/** The stored items (no copies: never modify them), padded with empty stacks to {@code size} */
	public static NonNullList<ItemStack> view(ItemStack holder, int size) {
		NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
		ItemContainerContents contents = holder.get(DataComponents.CONTAINER);
		if (contents == null) {
			migrate(holder);
			contents = holder.get(DataComponents.CONTAINER);
		}
		if (contents != null) {
			contents.copyInto(items);
		}
		return items;
	}

	public static boolean has(ItemStack holder) {
		ItemContainerContents contents = holder.get(DataComponents.CONTAINER);
		return contents != null && contents.nonEmptyItems().iterator().hasNext() || legacyList(holder) != null;
	}

	public static void set(ItemStack holder, NonNullList<ItemStack> items) {
		holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
	}

	/** 1.20.1 stacks: "Items" ([{Slot:b, id:..., Count:b, tag:{}}]) in custom data -> the container component */
	private static void migrate(ItemStack holder) {
		ListTag list = legacyList(holder);
		if (list == null) {
			return;
		}
		var registries = RegistryAccessHolder.get();
		if (registries == null) {
			return;
		}
		NonNullList<ItemStack> items = NonNullList.withSize(Math.max(27, list.size()), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			int slot = entry.getByte("Slot") & 255;
			if (slot < items.size()) {
				// the entry is a 1.20.1 stack ("Count", "tag"): vanilla's item fixers bring it to this version first
				Dynamic<Tag> fixed = DataFixers.getDataFixer().update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, entry), LEGACY_DATA_VERSION, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
				items.set(slot, ItemStack.parseOptional(registries, (CompoundTag) fixed.getValue()));
			}
		}
		set(holder, items);
		CustomData.update(DataComponents.CUSTOM_DATA, holder, tag -> tag.remove("Items"));
	}

	private static ListTag legacyList(ItemStack holder) {
		CompoundTag tag = ItemNbt.view(holder);
		return tag.contains("Items", Tag.TAG_LIST) ? tag.getList("Items", Tag.TAG_COMPOUND) : null;
	}
}
