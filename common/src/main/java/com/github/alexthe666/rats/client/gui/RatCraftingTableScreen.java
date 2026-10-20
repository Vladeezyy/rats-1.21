package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.inventory.RatCraftingTableMenu;
import com.github.alexthe666.rats.server.message.CycleRatRecipePacket;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** The rat crafting table: the grid the rat copies, its buffer, the recipe cycle buttons and vanilla's recipe book */
public class RatCraftingTableScreen extends AbstractRecipeBookScreen<RatCraftingTableMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/rat_crafting_table.png");
	private final Inventory playerInventory;
	private final RatCraftingTableMenu table;

	public RatCraftingTableScreen(RatCraftingTableMenu container, Inventory inv, Component name) {
		super(container, new RatCraftingRecipeBookComponent(container), inv, name);
		this.playerInventory = inv;
		this.table = container;
		this.imageHeight = 211;
	}

	@Override
	protected void init() {
		super.init();
		this.addRenderableWidget(new CycleResultButton(this.leftPos + 100, this.topPos + 58, false, button -> {
			this.table.incrementRecipeIndex(false);
			RatsNetworkHandler.toServer(new CycleRatRecipePacket(this.table.getCraftingTable().getBlockPos().asLong(), false));
		}));
		this.addRenderableWidget(new CycleResultButton(this.leftPos + 100, this.topPos + 28, true, button -> {
			this.table.incrementRecipeIndex(true);
			RatsNetworkHandler.toServer(new CycleRatRecipePacket(this.table.getCraftingTable().getBlockPos().asLong(), true));
		}));
	}

	@Override
	protected ScreenPosition getRecipeBookButtonPosition() {
		return new ScreenPosition(this.leftPos + 128, this.topPos + 65);
	}

	@Override
	protected void onRecipeBookButtonClick() {
		// the cycle buttons move with the screen
		this.rebuildWidgets();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		// the grid only shows what the rat copies: greyed over the items
		graphics.nextStratum();
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				int xPos = this.leftPos + 36 + i * 18;
				int yPos = this.topPos + 22 + j * 18;
				graphics.fill(xPos, yPos, xPos + 16, yPos + 16, 0x9f8b8b8b);
			}
		}
		ItemStack guide = this.table.guideResult();
		if (!guide.isEmpty() && !this.table.getSlot(0).hasItem()) {
			graphics.item(guide, this.leftPos + 130, this.topPos + 40);
			graphics.nextStratum();
			graphics.fill(this.leftPos + 130, this.topPos + 40, this.leftPos + 146, this.topPos + 56, 0x9f8b8b8b);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		int l = this.table.getCookProgressionScaled();
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 96, this.topPos + 39, 0, 211, l, 16, 256, 256);
		if (this.table.hasRat()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 8, this.topPos + 20, 176, 0, 21, 21, 256, 256);
		} else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 7, this.topPos + 40, 198, 0, 21, 21, 256, 256);
		}
		if (!this.table.hasRecipe()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 95, this.topPos + 38, 220, 0, 21, 21, 256, 256);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		String s = this.getTitle().getString();
		graphics.text(this.font, s, this.imageWidth / 2 - this.font.width(s) / 2, 5, 0xFF404040, false);
		graphics.text(this.font, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 93, 0xFF404040, false);
		graphics.text(this.font, Component.translatable(RatsLangConstants.CRAFTING_INPUT), 8, this.imageHeight - 125, 0xFF404040, false);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		if (!this.table.hasRat() && this.isHovering(6, 34, 25, 29, mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(this.font, this.font.split(Component.translatable(RatsLangConstants.CRAFTING_NEEDS_RAT), 200), mouseX, mouseY + 10);
		}
	}

	public boolean shouldRenderButtons() {
		return this.table.recipeCount() > 1;
	}

	private class CycleResultButton extends Button {
		private final boolean up;

		public CycleResultButton(int x, int y, boolean up, OnPress onClick) {
			super(x, y, 14, 9, Component.empty(), onClick, message -> Component.empty());
			this.up = up;
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			if (!RatCraftingTableScreen.this.shouldRenderButtons()) {
				return;
			}
			int textureX = 22;
			int textureY = 211;
			if (this.isHovered()) {
				textureX += this.width;
			}
			if (!this.up) {
				textureY += this.height;
			}
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), textureX, textureY, this.width, this.height, 256, 256);
		}
	}
}
