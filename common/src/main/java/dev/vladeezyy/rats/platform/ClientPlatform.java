package dev.vladeezyy.rats.platform;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import java.util.ServiceLoader;

/**
 * What the client code of Rats needs from the mod loader. Only touched on the client (render code), so the
 * implementation may use client-only loader classes. The defaults are what vanilla does.
 */
public interface ClientPlatform {
    ClientPlatform INSTANCE = ServiceLoader.load(ClientPlatform.class, ClientPlatform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Rats client platform for this mod loader"));

    /**
     * Whether the entity drawn with this state has the plague (26.x layers only see the state: NeoForge fills it with a
     * render state modifier, Fabric with a mixin on the living renderer's extraction)
     */
    boolean isPlagued(EntityRenderState state);

    /** Whether a name tag at this squared distance is drawn (NeoForge: the name tag distance attribute) */
    default boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return distanceSq <= 4096.0D;
    }
}
