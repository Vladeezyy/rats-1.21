package com.github.alexthe666.rats.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;

/** Fluid textures (26.x: vanilla's fluid models, also for the loaders' fluids) */
public final class FluidSprites {

	private FluidSprites() {
	}

	/** The still sprite of the fluid, on the block atlas */
	public static TextureAtlasSprite still(Fluid fluid) {
		return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState()).stillMaterial().sprite();
	}
}
