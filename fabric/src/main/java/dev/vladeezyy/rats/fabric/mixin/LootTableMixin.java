package dev.vladeezyy.rats.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.vladeezyy.rats.fabric.FabricLootHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * NeoForge applies global loot modifiers to what a loot table rolls; here the same for the tables rolled directly
 * (all of vanilla's ways to roll a table end in getRandomItemsRaw; a table rolled inside another one is left alone).
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
    private static final ThreadLocal<int[]> RATS$DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    @WrapMethod(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V")
    private void rats$lootModifiers(LootContext context, Consumer<ItemStack> output, Operation<Void> original) {
        int[] depth = RATS$DEPTH.get();
        if (depth[0] > 0 || context.getLevel().getServer() == null) {
            depth[0]++;
            try {
                original.call(context, output);
            } finally {
                depth[0]--;
            }
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        depth[0]++;
        try {
            original.call(context, (Consumer<ItemStack>) drops::add);
        } finally {
            depth[0]--;
        }
        FabricLootHook.apply((LootTable) (Object) this, context, drops);
        drops.forEach(output);
    }
}
