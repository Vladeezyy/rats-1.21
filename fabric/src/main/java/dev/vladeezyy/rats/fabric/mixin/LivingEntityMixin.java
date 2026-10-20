package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's MobEffectEvent.Applicable / Remove / Expired, LivingHealEvent and EntityTickEvent.Pre for living entities */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "canBeAffected", at = @At("RETURN"), cancellable = true)
    private void rats$canBeAffected(MobEffectInstance instance, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !RatsEvents.canApplyEffect((LivingEntity) (Object) this, instance)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void rats$heal(float amount, CallbackInfo ci) {
        if (RatsEvents.cancelHeal((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "onEffectsRemoved", at = @At("HEAD"))
    private void rats$effectEnded(java.util.Collection<MobEffectInstance> instances, CallbackInfo ci) {
        for (MobEffectInstance instance : instances) {
            RatsEvents.onEffectEnded((LivingEntity) (Object) this, instance);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void rats$tick(CallbackInfo ci) {
        RatsEvents.onEntityTick((LivingEntity) (Object) this);
    }
}
