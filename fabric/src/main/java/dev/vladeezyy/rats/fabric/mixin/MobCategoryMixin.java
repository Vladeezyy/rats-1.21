package dev.vladeezyy.rats.fabric.mixin;

import dev.vladeezyy.rats.fabric.FabricEnumValues;
import net.minecraft.world.entity.MobCategory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * The rats spawn category, added while the enum initializes (right after its values), so CODEC and everything built
 * from values() knows "rats:rats" (the Ratlantis biome lists its rats under it), like NeoForge's enum extension.
 */
@Mixin(MobCategory.class)
public abstract class MobCategoryMixin {
    @Shadow
    @Final
    @Mutable
    private static MobCategory[] $VALUES;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/MobCategory;$VALUES:[Lnet/minecraft/world/entity/MobCategory;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void rats$addCategory(CallbackInfo ci) {
        MobCategory[] values = $VALUES;
        MobCategory rats = MobCategoryAccessor.rats$create("RATS_RATS", values.length, "rats:rats", 25, true, false, 128);
        MobCategory[] extended = Arrays.copyOf(values, values.length + 1);
        extended[values.length] = rats;
        $VALUES = extended;
        FabricEnumValues.ratsCategory = rats;
    }
}
