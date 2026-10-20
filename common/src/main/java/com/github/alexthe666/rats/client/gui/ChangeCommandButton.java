package com.github.alexthe666.rats.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ChangeCommandButton extends Button {
	private final boolean right;

	public ChangeCommandButton(int x, int y, boolean right, Button.OnPress onPress) {
		super(x, y, 7, 11, Component.literal(""), onPress, message -> Component.empty());
		this.right = right;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			boolean flag = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
			int i = 0;
			int j = 166;
			if (flag) {
				i += 14;
			}
			if (this.right) {
				i += 7;
			}
			graphics.blit(RenderPipelines.GUI_TEXTURED, RatScreen.TEXTURE, this.getX(), this.getY(), i, j, this.width, this.height, 256, 256);
		}
	}
}
