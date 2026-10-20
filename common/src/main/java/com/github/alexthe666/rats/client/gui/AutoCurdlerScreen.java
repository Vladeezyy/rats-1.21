package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.util.FluidSprites;
import com.github.alexthe666.rats.server.inventory.AutoCurdlerMenu;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import dev.vladeezyy.rats.platform.Platform;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class AutoCurdlerScreen extends AbstractContainerScreen<AutoCurdlerMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/auto_curdler.png");
	private final AutoCurdlerMenu curdler;

	public AutoCurdlerScreen(AutoCurdlerMenu container, Inventory inv, Component name) {
		super(container, inv, name);
		this.curdler = container;
	}

	/** The fluid's still sprite, tiled upwards from (x, y) over the given size (the original's tank filling) */
	public static void extractFluidStack(GuiGraphicsExtractor graphics, int xPosition, int yPosition, int desiredWidth, int desiredHeight, Fluid fluid) {
		TextureAtlasSprite sprite = FluidSprites.still(fluid);
		int xTileCount = desiredWidth / 16;
		int xRemainder = desiredWidth - (xTileCount * 16);
		int yTileCount = desiredHeight / 16;
		int yRemainder = desiredHeight - (yTileCount * 16);
		float uMin = sprite.getU0();
		float uMax = sprite.getU1();
		float vMin = sprite.getV0();
		float vMax = sprite.getV1();
		float uDif = uMax - uMin;
		float vDif = vMax - vMin;
		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			int width = (xTile == xTileCount) ? xRemainder : 16;
			if (width == 0) {
				break;
			}
			int x = xPosition + (xTile * 16);
			int maskRight = 16 - width;
			int shiftedX = x + 16 - maskRight;
			float uLocalDif = uDif * maskRight / 16;
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int height = (yTile == yTileCount) ? yRemainder : 16;
				if (height == 0) {
					break;
				}
				int y = yPosition - ((yTile + 1) * 16);
				int maskTop = 16 - height;
				float vLocalDif = vDif * maskTop / 16;
				graphics.blit(TextureAtlas.LOCATION_BLOCKS, x, y + maskTop, shiftedX, y + 16, uMin + uLocalDif, uMax, vMin + vLocalDif, vMax);
			}
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
		super.extractBackground(graphics, x, y, partialTicks);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		int l = this.curdler.getCookProgressionScaled();
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 63, j + 35, 176, 0, l + 1, 16, 256, 256);
		int tankWidth = 24;
		int tankHeight = 63;
		int amount = Math.round((this.curdler.getFluidAmount() / (float) this.curdler.getTankCapacity()) * (tankHeight - 4));
		extractFluidStack(graphics, i + 29, j + 73, tankWidth, amount, Platform.INSTANCE.milkFluid());
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 29, j + 12, 0, 166, tankWidth, tankHeight, 256, 256);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		if (this.isHovering(29, 15, 24, 58, mouseX, mouseY)) {
			String fluidName = new FluidStack(Platform.INSTANCE.milkFluid(), this.curdler.getTankCapacity()).getHoverName().getString();
			String fluidSize = this.curdler.getFluidAmount() + " " + Component.translatable(RatsLangConstants.CURDLER_MB).getString();
			List<Component> list = List.of(Component.literal(fluidName).withStyle(ChatFormatting.BLUE), Component.literal(fluidSize).withStyle(ChatFormatting.GRAY));
			graphics.setComponentTooltipForNextFrame(this.font, list, mouseX, mouseY);
		}
	}
}
