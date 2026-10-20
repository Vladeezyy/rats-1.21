package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.platform.ClientPlatform;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.ClientHooks;

/** The client hooks of NeoForge the render code of Rats needs */
public final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public boolean isPlagued(EntityRenderState state) {
        return state.getRenderData(RatsNeoForgeClient.PLAGUED) != null;
    }

    @Override
    public boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return entity instanceof LivingEntity living ? ClientHooks.isNameplateInRenderDistance(living, distanceSq) : distanceSq <= 4096.0D;
    }
}
