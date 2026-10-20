package dev.vladeezyy.rats.fabric;

import dev.vladeezyy.rats.platform.ClientPlatform;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** The client side on Fabric: the plague flag that the living renderer mixin puts into render states */
public final class FabricClientPlatform implements ClientPlatform {
    /** Whether the entity of a living render state has the plague (set by LivingEntityRendererMixin) */
    public static final RenderStateDataKey<Boolean> PLAGUED = RenderStateDataKey.create(() -> "rats:plagued");

    @Override
    public boolean isPlagued(EntityRenderState state) {
        return ((FabricRenderState) state).getData(PLAGUED) != null;
    }
}
