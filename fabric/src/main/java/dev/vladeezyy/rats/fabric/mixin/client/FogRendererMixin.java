package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeFogColor: the fog of Ratlantis */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void rats$fogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, CallbackInfo ci) {
        float[] color = RatsClientEvents.ratlantisFogColor(camera, partialTick);
        if (color != null) {
            fogRed = color[0];
            fogGreen = color[1];
            fogBlue = color[2];
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
    }
}
