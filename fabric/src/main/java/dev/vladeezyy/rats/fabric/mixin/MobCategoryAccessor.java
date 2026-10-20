package dev.vladeezyy.rats.fabric.mixin;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Adds the rats spawn category to the enum (NeoForge: enum extension) */
@Mixin(MobCategory.class)
public interface MobCategoryAccessor {
    @Invoker("<init>")
    static MobCategory rats$create(String internalName, int ordinal, String name, String debugAbbreviation, int max, boolean isFriendly, boolean isPersistent, int despawnDistance) {
        throw new AssertionError();
    }
}
