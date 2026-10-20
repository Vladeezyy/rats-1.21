package com.github.alexthe666.rats.client.render.entity;

import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.resources.Identifier;

/** Render state of the mod's mobs: their texture, picked from the entity when the state is filled */
public class RatsMobRenderState extends RampartLivingRenderState {
	public Identifier texture;
}
