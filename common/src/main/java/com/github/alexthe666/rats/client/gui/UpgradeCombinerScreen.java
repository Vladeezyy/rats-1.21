package com.github.alexthe666.rats.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.block.entity.UpgradeCombinerBlockEntity;
import com.github.alexthe666.rats.server.inventory.UpgradeCombinerMenu;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class UpgradeCombinerScreen extends AbstractContainerScreen<UpgradeCombinerMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/upgrade_combiner.png");
	private final Inventory playerInventory;
	private final UpgradeCombinerMenu combiner;

	public UpgradeCombinerScreen(UpgradeCombinerMenu container, Inventory inv, Component name) {
		super(container, inv, name);
		this.playerInventory = inv;
		this.combiner = container;
	}


	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		int l = this.combiner.getCookProgressScaled();
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 92, j + 35, 176, 14, l + 1, 16, 256, 256);
		if (!UpgradeCombinerBlockEntity.canCombine(this.combiner.container.getItem(0), this.combiner.container.getItem(2)) && !this.combiner.container.getItem(0).isEmpty() && !this.combiner.container.getItem(2).isEmpty()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 42, j + 34, 198, 31, 21, 21, 256, 256);
		}
		if (!RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 44, j + 57, 0, 166, 16, 16, 256, 256);
		}
		if (this.combiner.getBurnLeftScaled() > 0) {
			int k = this.combiner.getBurnLeftScaled();
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 70, j + 58 + 12 - k, 176, 12 - k, 14, k + 1, 256, 256);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		String s = this.getTitle().getString();
		graphics.text(this.font, s, this.imageWidth / 2 - this.font.width(s) / 2, 5, 0xFF404040, false);
		graphics.text(this.font, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 94 + 2, 0xFF404040, false);
		int screenW = (this.width - this.imageWidth) / 2;
		int screenH = (this.height - this.imageHeight) / 2;
		if (UpgradeCombinerBlockEntity.canCombine(this.combiner.container.getItem(0), this.combiner.container.getItem(2)) && !this.combiner.container.getItem(0).isEmpty() && !this.combiner.container.getItem(2).isEmpty()) {
			if (mouseX > screenW + 42 && mouseX < screenW + 63 && mouseY > screenH + 34 && mouseY < screenH + 55) {
				Component ratDesc = Component.translatable(RatsLangConstants.COMBINER_CANNOT_COMBINE);
				List<Component> list = List.of(ratDesc);
				graphics.setTooltipForNextFrame(this.font, Lists.transform(list, Component::getVisualOrderText), mouseX, mouseY + 10);
			}
		}
	}
}