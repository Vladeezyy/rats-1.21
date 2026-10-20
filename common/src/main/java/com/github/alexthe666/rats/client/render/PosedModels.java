package com.github.alexthe666.rats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.model.basic.BasicEntityModel;
import dev.vladeezyy.rampart.client.model.basic.BasicModelPart;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Models posed by hand (a trap's hinge, a trash can's lid, a bell's swing): 26.x draws the frame's models after all
 * renderers ran, so a model shared by several block entities would be drawn with the last pose for all of them. Here
 * the pose is applied when the model is drawn, right before its vertices.
 */
public final class PosedModels {

	private PosedModels() {
	}

	/** A Citadel-style model: {@code poser} sets the pose, then the boxes draw */
	public static void submit(SubmitNodeCollector collector, PoseStack stack, RenderType type, BasicEntityModel<?> model, Runnable poser, int light, int overlay, int color) {
		collector.submitCustomGeometry(stack, type, (pose, consumer) -> {
			poser.run();
			PoseStack drawStack = new PoseStack();
			drawStack.last().set(pose);
			for (BasicModelPart part : model.parts()) {
				part.render(drawStack, consumer, light, overlay, color);
			}
		});
	}

	/** A vanilla part: {@code poser} sets the pose, then the part draws */
	public static void submit(SubmitNodeCollector collector, PoseStack stack, RenderType type, ModelPart part, Runnable poser, int light, int overlay, int color) {
		collector.submitCustomGeometry(stack, type, (pose, consumer) -> {
			poser.run();
			PoseStack drawStack = new PoseStack();
			drawStack.last().set(pose);
			part.render(drawStack, consumer, light, overlay, color);
		});
	}
}
