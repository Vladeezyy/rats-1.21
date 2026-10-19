package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's LivingChangeTargetEvent: piglins never target rats with the idol upgrade */
@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void rats$cancelTarget(LivingEntity target, CallbackInfo ci) {
        if (RatsEvents.cancelTargetChange((Mob) (Object) this, target)) {
            ci.cancel();
        }
    }
}
