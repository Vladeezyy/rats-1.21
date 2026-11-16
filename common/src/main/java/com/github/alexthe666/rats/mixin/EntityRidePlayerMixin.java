package com.github.alexthe666.rats.mixin;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Since 1.21.11 the server seats nothing on a vehicle whose type is not saved (players), while the clients still seat a
 * tamed rat on its owner (sneak + right click): the rat stayed where it was on the server and came back there when it
 * was put down (GitHub #3). Players eject their passengers when they leave, so the rat is not lost with them.
 */
@Mixin(Entity.class)
public abstract class EntityRidePlayerMixin {
	@WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
	private boolean rats$ratOnPlayer(EntityType<?> type, Operation<Boolean> original, @Local(argsOnly = true) Entity vehicle) {
		return original.call(type) || (Object) this instanceof TamedRat && vehicle instanceof Player;
	}
}
