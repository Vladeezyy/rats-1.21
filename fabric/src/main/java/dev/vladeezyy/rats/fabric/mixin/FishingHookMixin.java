package dev.vladeezyy.rats.fabric.mixin;

import com.github.alexthe666.rats.server.events.RatsEvents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/** NeoForge's ItemFishedEvent: fishing in Ratlantis catches from its own table */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow
    public abstract Player getPlayerOwner();

    @ModifyExpressionValue(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> rats$fished(ObjectArrayList<ItemStack> drops) {
        Player player = this.getPlayerOwner();
        if (player != null) {
            RatsEvents.onItemFished(player, (FishingHook) (Object) this, drops);
        }
        return drops;
    }
}
