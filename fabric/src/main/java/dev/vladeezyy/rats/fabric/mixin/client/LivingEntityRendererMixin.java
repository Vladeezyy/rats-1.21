package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import dev.vladeezyy.rats.fabric.FabricClientPlatform;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's render state modifier: the plague flag for the plague layer, masks hide the player's hat */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void rats$extract(T entity, S state, float partialTick, CallbackInfo ci) {
        ((FabricRenderState) state).setData(FabricClientPlatform.PLAGUED, RatsClientEvents.isPlagued(entity) ? Boolean.TRUE : null);
        RatsClientEvents.afterLivingExtract(entity, state);
    }
}
