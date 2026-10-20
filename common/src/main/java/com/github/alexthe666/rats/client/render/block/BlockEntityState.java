package com.github.alexthe666.rats.client.render.block;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Render state of the mod's block entities: the block entity and the frame's partial tick (read when submitting) */
public class BlockEntityState<T extends BlockEntity> extends BlockEntityRenderState {
	public T blockEntity;
	public float partialTick;
}
