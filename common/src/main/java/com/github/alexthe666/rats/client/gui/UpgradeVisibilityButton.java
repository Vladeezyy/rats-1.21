package com.github.alexthe666.rats.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class UpgradeVisibilityButton extends Button {

	private boolean upgradeVisibility;

	public UpgradeVisibilityButton(int x, int y, boolean visible, OnPress press) {
		super(x, y, 8, 6, Component.empty(), press, message -> Component.empty());
		this.upgradeVisibility = visible;
	}

	public boolean getUpgradeVisibility() {
		return this.upgradeVisibility;
	}

	public void toggleVisibility() {
		this.upgradeVisibility = !this.upgradeVisibility;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			int i = 30;
			int j = 166;
			if (!this.upgradeVisibility) {
				i += 8;
			}
			graphics.blit(RenderPipelines.GUI_TEXTURED, RatScreen.TEXTURE, this.getX(), this.getY(), i, j, this.width, this.height, 256, 256);
		}
	}
}
