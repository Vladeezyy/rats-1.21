package com.github.alexthe666.rats.server.block.entity;

import com.github.alexthe666.rats.registry.RatlantisBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PiratHangingSignBlockEntity extends HangingSignBlockEntity {
	public PiratHangingSignBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public BlockEntityType<?> getType() {
		return RatlantisBlockEntityRegistry.PIRAT_HANGING_SIGN.get();
	}

	// vanilla checks the block against the type passed to the constructor (the vanilla sign's), NeoForge against
	// getType(): the pirat signs are only valid for their own type, so check that one on both loaders
	@Override
	public boolean isValidBlockState(BlockState state) {
		return this.getType().isValid(state);
	}
}
