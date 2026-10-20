package com.github.alexthe666.rats.client.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.vladeezyy.rampart.client.render.RampartState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityRenderingUtil {
	private static final Map<Identifier, Entity> ENTITY_MAP = new HashMap<>();
	private static final Set<Identifier> IGNORED_ENTITIES = new HashSet<>();

	@Nullable
	public static LivingEntity fetchEntity(@Nullable Identifier entityName, @Nullable Level level) {
		if (entityName != null && level != null && !IGNORED_ENTITIES.contains(entityName)) {
			EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(entityName);
			if (type != null) {
				Entity entity;
				if (type == net.minecraft.world.entity.EntityTypes.PLAYER) {
					entity = Minecraft.getInstance().player;
				} else {
					entity = ENTITY_MAP.computeIfAbsent(entityName, t -> BuiltInRegistries.ENTITY_TYPE.getValue(t).create(level, net.minecraft.world.entity.EntitySpawnReason.LOAD));
				}
				if (entity instanceof LivingEntity living) {
					return living;
				} else {
					addEntityToBlacklist(entityName);
				}
			}
		}
		return null;
	}

	public static int getAdjustedMobScale(Identifier entityName) {
		LivingEntity entity = fetchEntity(entityName, Minecraft.getInstance().level);
		if (entity != null) {
			int scale = 35;
			float height = entity.getBbHeight();
			float width = entity.getBbWidth();
			if (height > 2.0F || width > 2.0F) {
				scale = (int) (64 / Math.max(height, width));
			}
			return scale;
		}
		return 0;
	}

	public static void addEntityToBlacklist(Identifier entityName) {
		IGNORED_ENTITIES.add(entityName);
		ENTITY_MAP.remove(entityName);
	}

	/**
	 * Draws a living entity standing at (posX, posY) on the screen, {@code scale} pixels per block, looking towards
	 * the mouse offset (vanilla's inventory player), turning around when {@code rotating}
	 */
	public static void drawEntityOnScreen(GuiGraphicsExtractor graphics, int posX, int posY, int scale, float mouseX, float mouseY, @Nullable LivingEntity entity, boolean rotating) {
		if (entity == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		float f = (float) Math.atan(mouseX / 40.0F);
		float f1 = (float) Math.atan(mouseY / 40.0F);
		Quaternionf rotation = Axis.ZP.rotationDegrees(180.0F);
		Quaternionf xRotation = Axis.XP.rotationDegrees(f1 * 20.0F);
		rotation.mul(xRotation);
		if (rotating) {
			float rotate = (minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true) + (minecraft.player != null ? minecraft.player.tickCount : 0)) * 2F;
			rotation.mul(Axis.YP.rotationDegrees(rotate));
		}
		EntityRenderer<? super LivingEntity, ?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
		EntityRenderState state = renderer.createRenderState(entity, 1.0F);
		state.shadowPieces.clear();
		state.outlineColor = 0;
		if (state instanceof LivingEntityRenderState living) {
			living.bodyRot = 180.0F + f * 20.0F;
			living.yRot = f * 20.0F;
			living.xRot = -f1 * 20.0F;
			living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
			living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
			living.scale = 1.0F;
			if (state instanceof RampartState rampart) {
				// Citadel-style models read the head angles from the pose filled during extraction
				rampart.rampart$pose().netHeadYaw = living.yRot;
				rampart.rampart$pose().headPitch = living.xRot;
			}
		}
		float height = state.boundingBoxHeight;
		int halfWidth = Mth.ceil(scale * Math.max(1.0F, Math.max(state.boundingBoxWidth, height)));
		int halfHeight = Mth.ceil(scale * (height / 2.0F + 0.5F));
		int centerY = Mth.floor(posY - scale * height / 2.0F);
		graphics.entity(state, scale, new Vector3f(0.0F, height / 2.0F, 0.0F), rotation, xRotation, posX - halfWidth, centerY - halfHeight, posX + halfWidth, centerY + halfHeight);
	}
}
