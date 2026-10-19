package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderLivingEvent.Pre / Post: masks hide hats, protector rats circle players in Ratlantis armor */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    protected M model;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void rats$before(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        RatsClientEvents.beforeLivingRender(entity, this.model);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void rats$after(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        RatsClientEvents.afterLivingRender(entity, poseStack, buffers, light, partialTick);
    }
}
