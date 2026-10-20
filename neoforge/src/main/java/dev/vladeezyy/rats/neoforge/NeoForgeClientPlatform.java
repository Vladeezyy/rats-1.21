package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.platform.ClientPlatform;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** The client hooks of NeoForge the render code of Rats needs (26.2: name tags have vanilla's fixed distance) */
public final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public boolean isPlagued(EntityRenderState state) {
        return state.getRenderData(RatsNeoForgeClient.PLAGUED) != null;
    }
}
