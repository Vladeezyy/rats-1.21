package dev.vladeezyy.rats.fabric;

import dev.vladeezyy.rats.fabric.mixin.MobCategoryAccessor;
import dev.vladeezyy.rats.fabric.mixin.RarityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Rarity;

import java.util.Arrays;

/** The enum values Rats adds, the same as NeoForge's META-INF/enumextensions.json */
final class FabricEnums {
    static final MobCategory RATS_CATEGORY;
    static final Rarity RATLANTIS_SPECIAL;

    static {
        MobCategory[] categories = MobCategoryAccessor.rats$getValues();
        RATS_CATEGORY = MobCategoryAccessor.rats$create("RATS_RATS", categories.length, "rats:rats", 25, true, false, 128);
        MobCategory[] extended = Arrays.copyOf(categories, categories.length + 1);
        extended[categories.length] = RATS_CATEGORY;
        MobCategoryAccessor.rats$setValues(extended);

        Rarity[] rarities = RarityAccessor.rats$getValues();
        RATLANTIS_SPECIAL = RarityAccessor.rats$create("RATS_RATLANTIS_SPECIAL", rarities.length, rarities.length, "rats:ratlantis_special", ChatFormatting.GREEN);
        Rarity[] extendedRarities = Arrays.copyOf(rarities, rarities.length + 1);
        extendedRarities[rarities.length] = RATLANTIS_SPECIAL;
        RarityAccessor.rats$setValues(extendedRarities);
    }

    private FabricEnums() {}
}
