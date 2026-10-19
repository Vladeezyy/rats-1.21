package dev.vladeezyy.rats.fabric;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Rarity;

/** The enum values the Fabric mixins add (set while MobCategory / Rarity initialize) */
public final class FabricEnumValues {
    public static MobCategory ratsCategory;
    public static Rarity ratlantisRarity;

    private FabricEnumValues() {}

    static MobCategory ratsCategory() {
        MobCategory.values(); // initializes the enum, which adds the value
        return ratsCategory;
    }

    static Rarity ratlantisRarity() {
        Rarity.values();
        return ratlantisRarity;
    }
}
