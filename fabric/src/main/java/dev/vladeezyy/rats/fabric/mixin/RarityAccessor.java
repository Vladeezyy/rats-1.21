package dev.vladeezyy.rats.fabric.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Adds the green Ratlantis rarity to the enum (NeoForge: enum extension) */
@Mixin(Rarity.class)
public interface RarityAccessor {
    @Invoker("<init>")
    static Rarity rats$create(String internalName, int ordinal, int id, String name, ChatFormatting color) {
        throw new AssertionError();
    }

    @Accessor("$VALUES")
    static Rarity[] rats$getValues() {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("$VALUES")
    static void rats$setValues(Rarity[] values) {
        throw new AssertionError();
    }
}
