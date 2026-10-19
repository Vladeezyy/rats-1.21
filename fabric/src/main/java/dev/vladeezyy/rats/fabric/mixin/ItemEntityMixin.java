package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.server.events.RatsEvents;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ItemExpireEvent: a rat sack that despawns lets its rats out (the second discard() in tick is the expiry) */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
    private void rats$onExpire(CallbackInfo ci) {
        RatsEvents.onItemExpire((ItemEntity) (Object) this);
    }
}
