package dev.vladeezyy.rats.fabric.mixin.client;

import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's PlayerInteractEvent.LeftClickEmpty: dismounting rats, the plague scythe's shot */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    protected int missTime;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void rats$leftClickEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (this.missTime <= 0 && this.player != null && !this.player.isHandsBusy() && this.hitResult != null && this.hitResult.getType() == HitResult.Type.MISS) {
            RatsEvents.onLeftClickEmpty(this.player, this.player.getMainHandItem());
        }
    }
}
