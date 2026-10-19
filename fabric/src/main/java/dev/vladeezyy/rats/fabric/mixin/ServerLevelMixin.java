package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's VanillaGameEvent: rats with the sculked upgrade make no vibrations */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void rats$cancelGameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context context, CallbackInfo ci) {
        if (RatsEvents.cancelGameEvent(context.sourceEntity())) {
            ci.cancel();
        }
    }
}
