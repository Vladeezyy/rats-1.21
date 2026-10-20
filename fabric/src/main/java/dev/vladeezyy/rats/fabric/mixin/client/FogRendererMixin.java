package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeFogColor (at the end of the fog color computation): the fog of Ratlantis */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void rats$fogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f dest, CallbackInfo ci) {
        float[] color = RatsClientEvents.ratlantisFogColor(camera, partialTick);
        if (color != null) {
            dest.set(color[0], color[1], color[2], dest.w);
        }
    }
}
