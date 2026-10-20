package com.github.alexthe666.rats.client.gui;

import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.util.EntityRenderingUtil;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.inventory.RatMenu;
import com.github.alexthe666.rats.server.message.RatCommandPacket;
import com.github.alexthe666.rats.server.message.RatUpgradeVisibilityPacket;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RatUtils;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RatScreen extends AbstractContainerScreen<RatMenu> {
	protected static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/rat_inventory.png");
	private static final Identifier TEXTURE_BACKDROP = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/rat_inventory_backdrop.png");
	private int currentDisplayCommand = 0;
	private final TamedRat rat;

	public RatScreen(RatMenu container, Inventory inv, TamedRat rat) {
		super(container, inv, rat.getDisplayName(), 192, 166);
		this.rat = rat;
	}


	@Override
	protected void init() {
		super.init();
		this.renderables.clear();
		int i = (this.width - 248) / 2;
		int j = (this.height - 166) / 2;
		if (!this.rat.isBaby()) {
			this.addRenderableWidget(new ChangeCommandButton(i + 116, j + 54, false, button -> {
				this.currentDisplayCommand--;
				this.currentDisplayCommand = RatUtils.wrapCommand(this.currentDisplayCommand).ordinal();
			}));
			this.addRenderableWidget(new ChangeCommandButton(i + 199, j + 54, true, button -> {
				this.currentDisplayCommand++;
				this.currentDisplayCommand = RatUtils.wrapCommand(this.currentDisplayCommand).ordinal();
			}));
			this.addRenderableWidget(new CommandPressButton(i + 123, j + 52, button -> {
				this.rat.setCommand(RatCommand.values()[this.currentDisplayCommand]);
				RatsNetworkHandler.toServer(new RatCommandPacket(this.rat.getId(), this.currentDisplayCommand));
			}));
			this.addRenderableWidget(new CommandPressButton(i + 123, j + 52, button -> {
				this.rat.setCommand(RatCommand.values()[this.currentDisplayCommand]);
				RatsNetworkHandler.toServer(new RatCommandPacket(this.rat.getId(), this.currentDisplayCommand));
			}));

			this.addRenderableWidget(new UpgradeVisibilityButton(i + 39, j + 15, this.rat.isSlotVisible(EquipmentSlot.CHEST), button -> {
				((UpgradeVisibilityButton) button).toggleVisibility();
				this.rat.setSlotVisibility(EquipmentSlot.CHEST, ((UpgradeVisibilityButton) button).getUpgradeVisibility());
				RatsNetworkHandler.toServer(new RatUpgradeVisibilityPacket(this.rat.getId(), EquipmentSlot.CHEST, ((UpgradeVisibilityButton) button).getUpgradeVisibility()));
			}));
			this.addRenderableWidget(new UpgradeVisibilityButton(i + 39, j + 33, this.rat.isSlotVisible(EquipmentSlot.LEGS), button -> {
				((UpgradeVisibilityButton) button).toggleVisibility();
				this.rat.setSlotVisibility(EquipmentSlot.LEGS, ((UpgradeVisibilityButton) button).getUpgradeVisibility());
				RatsNetworkHandler.toServer(new RatUpgradeVisibilityPacket(this.rat.getId(), EquipmentSlot.LEGS, ((UpgradeVisibilityButton) button).getUpgradeVisibility()));
			}));
			this.addRenderableWidget(new UpgradeVisibilityButton(i + 39, j + 51, this.rat.isSlotVisible(EquipmentSlot.FEET), button -> {
				((UpgradeVisibilityButton) button).toggleVisibility();
				this.rat.setSlotVisibility(EquipmentSlot.FEET, ((UpgradeVisibilityButton) button).getUpgradeVisibility());
				RatsNetworkHandler.toServer(new RatUpgradeVisibilityPacket(this.rat.getId(), EquipmentSlot.FEET, ((UpgradeVisibilityButton) button).getUpgradeVisibility()));
			}));

		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_BACKDROP, k - 8, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		EntityRenderingUtil.drawEntityOnScreen(graphics, k + 42, l + 64, 70, k + 51 - (float) mouseX, l + 75 - 50 - (float) mouseY, this.rat, false);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k - 8, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + 52, l + 20, this.rat.isMale() ? 0 : 16, 209, 16, 16, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(this.font, this.getTitle(), this.imageWidth / 2 - this.font.width(this.getTitle()) / 2, 6, 0xFF404040, false);

		Component commandDesc = Component.translatable(RatsLangConstants.RAT_CURRENT_COMMAND);
		graphics.text(this.font, commandDesc, this.imageWidth / 2 - this.font.width(commandDesc) / 2 + 38, 19, 0xFF404040, false);

		Component command = Component.translatable(rat.getCommand().getTranslateName());
		graphics.text(this.font, command, this.imageWidth / 2 - this.font.width(command) / 2 + 36, 31, ARGB.opaque(0XFFFFFF), false);

		Component statusDesc = Component.translatable(RatsLangConstants.RAT_COMMAND_SET);
		graphics.text(this.font, statusDesc, this.imageWidth / 2 - this.font.width(statusDesc) / 2 + 36, 44, 0xFF404040, false);
		RatCommand command1 = RatUtils.wrapCommand(currentDisplayCommand);
		Component command2 = Component.translatable(command1.getTranslateName());
		graphics.text(this.font, command2, this.imageWidth / 2 - this.font.width(command2) / 2 + 36, 56, ARGB.opaque(0XFFFFFF), false);
		int i = (this.width - 248) / 2;
		int j = (this.height - 166) / 2;
		if (mouseX > i + 116 && mouseX < i + 198 && mouseY > j + 22 && mouseY < j + 45) {
			MutableComponent commandText = Component.translatable(this.rat.getCommand().getTranslateDescription());
			String[] everySpace = commandText.getString().split(" ");
			int currentStrLength = 0;
			StringBuilder builtString = new StringBuilder();
			ArrayList<String> list = new ArrayList<>();
			for (String s : everySpace) {
				builtString.append(s).append(" ");
				currentStrLength += this.font.width(s + " ");
				if (currentStrLength >= 95) {
					list.add(builtString.toString());
					builtString = new StringBuilder();
					currentStrLength = 0;
				}
			}
			List<MutableComponent> convertedList = new ArrayList<>();
			for (String str : list) {
				convertedList.add(Component.literal(str));
			}
			graphics.setTooltipForNextFrame(this.font, Lists.transform(convertedList, Component::getVisualOrderText), mouseX, mouseY + 10);
		}
		if (mouseX > i + 116 && mouseX < i + 198 && mouseY > j + 53 && mouseY < j + 69) {
			MutableComponent commandText = Component.translatable(command1.getTranslateDescription());
			graphics.setTooltipForNextFrame(this.font, this.font.split(commandText, 110), mouseX, mouseY + 10);
		}
	}
}
