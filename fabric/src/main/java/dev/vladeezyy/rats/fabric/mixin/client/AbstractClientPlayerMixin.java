package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's ComputeFovModifierEvent: zooming while drawing the Ratlantis bow */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void rats$fovModifier(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(RatsClientEvents.fovModifier((AbstractClientPlayer) (Object) this, cir.getReturnValueF()));
    }
}
