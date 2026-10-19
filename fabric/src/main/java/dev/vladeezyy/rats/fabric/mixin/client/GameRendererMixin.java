package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's ViewportEvent.ComputeFov: the synesthesia effect */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void rats$fov(Camera camera, float partialTick, boolean usesFovSetting, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(RatsClientEvents.adjustFov(cir.getReturnValueD()));
    }
}
