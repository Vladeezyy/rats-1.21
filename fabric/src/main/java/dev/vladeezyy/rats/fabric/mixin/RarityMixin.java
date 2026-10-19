package dev.vladeezyy.rats.fabric.mixin;

import dev.vladeezyy.rats.fabric.FabricEnumValues;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/** The green Ratlantis rarity, added while the enum initializes, so BY_ID and the codecs know it (NeoForge: enum extension) */
@Mixin(Rarity.class)
public abstract class RarityMixin {
    @Shadow
    @Final
    @Mutable
    private static Rarity[] $VALUES;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Rarity;$VALUES:[Lnet/minecraft/world/item/Rarity;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void rats$addRarity(CallbackInfo ci) {
        Rarity[] values = $VALUES;
        Rarity ratlantis = RarityAccessor.rats$create("RATS_RATLANTIS_SPECIAL", values.length, values.length, "rats:ratlantis_special", ChatFormatting.GREEN);
        Rarity[] extended = Arrays.copyOf(values, values.length + 1);
        extended[values.length] = ratlantis;
        $VALUES = extended;
        FabricEnumValues.ratlantisRarity = ratlantis;
    }
}
