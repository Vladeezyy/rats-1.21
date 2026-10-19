package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Item NBT of the 1.20.1 original, kept in {@code minecraft:custom_data} with the same keys. Items of 1.20.1 worlds
 * are upgraded into exactly this component, so old rat sacks, upgrades and other items keep their data.
 */
public final class ItemNbt {

    private ItemNbt() {
    }

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    /** A copy of the tag, or null when the stack has none. Changes to it are not saved, use {@link #update}. */
    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    /** A copy of the tag, or an empty one. */
    public static CompoundTag getOrEmpty(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** Read-only view of the tag for hot paths (rendering, tooltips). Never modify it. */
    @SuppressWarnings("deprecation")
    public static CompoundTag view(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
    }

    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
