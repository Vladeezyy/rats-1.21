package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's ViewportEvent.ComputeFov (fired in the same method for the world and the hand): the synesthesia effect */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "modifyFovBasedOnDeathOrFluid(FF)F", at = @At("RETURN"), cancellable = true)
    private void rats$fov(float partialTick, float fov, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float) RatsClientEvents.adjustFov(cir.getReturnValueF()));
    }
}
