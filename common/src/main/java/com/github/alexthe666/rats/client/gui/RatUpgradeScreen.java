package com.github.alexthe666.rats.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.alexthe666.rats.server.inventory.RatUpgradeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public class RatUpgradeScreen extends AbstractContainerScreen<RatUpgradeMenu> {

	private static final Identifier CHEST_GUI_TEXTURE = Identifier.parse("textures/gui/container/generic_54.png");
	private final int inventoryRows;

	public RatUpgradeScreen(RatUpgradeMenu container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name, 176, 114 + container.inventory.getContainerSize() / 9 * 18);
		this.inventoryRows = container.inventory.getContainerSize() / 9;
	}


	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE, i, j, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, CHEST_GUI_TEXTURE, i, j + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
	}
}
