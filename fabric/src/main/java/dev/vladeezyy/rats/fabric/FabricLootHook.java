package dev.vladeezyy.rats.fabric;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/** Public entry of the loot modifiers for the mixin (mixin classes can only call public code) */
public final class FabricLootHook {
    private FabricLootHook() {}

    public static void apply(LootTable table, LootContext context, List<ItemStack> drops) {
        FabricLootModifiers.apply(table, context, drops);
    }
}
