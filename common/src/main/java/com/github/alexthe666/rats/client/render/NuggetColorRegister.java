package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.items.OreRatNuggetItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NuggetColorRegister {

	public static final ItemStack FALLBACK_STACK = new ItemStack(Items.IRON_INGOT);
	public static final Map<String, Integer> TEXTURES_TO_COLOR = new HashMap<>();

	public static int getNuggetColor(ItemStack stack) {
		ItemStack poopStack = OreRatNuggetItem.getStoredItem(stack, FALLBACK_STACK);
		String poopName = poopStack.getDisplayName().getString();
		if (TEXTURES_TO_COLOR.get(poopName) != null) {
			return TEXTURES_TO_COLOR.get(poopName);
		} else {
			int color = 0XFFFFFF;
			try {
				Color texColour = getAverageColour(getTextureAtlas(poopStack));
				color = texColour.getARGB();
			} catch (NullPointerException e) {
				RatsMod.LOGGER.warn("Could not fetch average nugget color for resource {}, defaulting to white.", poopStack.getHoverName().getString());
			}
			TEXTURES_TO_COLOR.put(poopName, color);
			return color;
		}
	}

	private static Color getAverageColour(TextureAtlasSprite image) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float count = 0;
		int uMax = image.contents().width();
		int vMax = image.contents().height();
		for (float i = 0; i < uMax; i++)
			for (float j = 0; j < vMax; j++) {
				// ARGB (1.21.1 read ABGR with getPixelRGBA)
				int pixel = image.contents().byMipLevel[0].getPixel((int) i, (int) j);
				if ((pixel >> 24 & 0xFF) != 255) {
					continue;
				}
				red += pixel >> 16 & 0xFF;
				green += pixel >> 8 & 0xFF;
				blue += pixel & 0xFF;
				count++;
			}
		//Average color
		return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
	}

	private static TextureAtlasSprite getTextureAtlas(ItemStack oreStack) {
		// the particle sprite of the item's model (26.x: resolved into a render state)
		ItemStackRenderState state = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, oreStack, ItemDisplayContext.GROUND, null, null, 0);
		return Objects.requireNonNull(state.pickParticleMaterial(RandomSource.create(0L))).sprite();
	}

	//java.awt bad
	//awt crashes macs, so here's a color holder
	public static class Color {
		private final int value;

		public Color(int r, int g, int b) {
			this(r, g, b, 255);
		}

		public Color(int r, int g, int b, int a) {
			this.value = ((a & 0xFF) << 24) |
					((r & 0xFF) << 16) |
					((g & 0xFF) << 8) |
					((b & 0xFF));
		}

		public int getARGB() {
			return this.value;
		}
	}
}
