package com.github.alexthe666.rats.client.render.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/** Render state of the rats (the entity itself is in {@link #pose}) */
public class RatRenderState extends RatsMobRenderState {
	/** Where the rat sits on what it rides (a player's head, a rattling gun, a plane), in its model space; or null */
	@Nullable
	public Matrix4f riderTransform;
	/** The fisherman rat waits for a bite: the rod and dots over its head */
	public boolean fishing;
	public boolean showsName;
}
