package dev.vladeezyy.rats.fabric.mixin;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Adds the rats spawn category to the enum (NeoForge: enum extension) */
@Mixin(MobCategory.class)
public interface MobCategoryAccessor {
    @Invoker("<init>")
    static MobCategory rats$create(String internalName, int ordinal, String name, int max, boolean isFriendly, boolean isPersistent, int despawnDistance) {
        throw new AssertionError();
    }

    @Accessor("$VALUES")
    static MobCategory[] rats$getValues() {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("$VALUES")
    static void rats$setValues(MobCategory[] values) {
        throw new AssertionError();
    }
}
