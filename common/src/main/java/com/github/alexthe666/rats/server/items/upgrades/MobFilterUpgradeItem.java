package com.github.alexthe666.rats.server.items.upgrades;
import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MobFilterUpgradeItem extends BaseRatUpgradeItem {
	public MobFilterUpgradeItem(Properties properties) {
		super(properties, 1, 2);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide() && player.getItemInHand(hand).is(this)) {
			RatsClientSetup.openMobFilterScreen(hand);
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

	public static boolean isWhitelist(ItemStack stack) {
		return ItemNbt.hasTag(stack) && ItemNbt.view(stack).getBooleanOr("Whitelist", false);
	}

	public static void setWhitelist(ItemStack stack, boolean whitelist) {
		ItemNbt.update(stack, tag -> tag.putBoolean("Whitelist", whitelist));
	}

	public static List<String> getSelectedMobs(ItemStack stack) {
		if (!ItemNbt.hasTag(stack)) return new ArrayList<>();
		List<String> mobs = new ArrayList<>();
		ListTag tag = ItemNbt.view(stack).getListOrEmpty("Mobs");
		for (int i = 0; i < tag.size(); ++i) {
			String s = tag.getStringOr(i, "");
			mobs.add(s);
		}
		return mobs;
	}

	public static void setMobs(ItemStack stack, List<String> mobs) {
		ListTag tag = new ListTag();
		for (String mob : mobs) {
			if (BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(mob))) {
				tag.add(StringTag.valueOf(mob));
			}
		}
		ItemNbt.update(stack, data -> data.put("Mobs", tag));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable(RatsLangConstants.MOB_FILTER_MODE, Component.translatable(isWhitelist(stack) ? RatsLangConstants.MOB_FILTER_WHITELIST : RatsLangConstants.MOB_FILTER_BLACKLIST)).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable(RatsLangConstants.MOB_FILTER_SELECTED_MOBS).withStyle(ChatFormatting.GRAY));
		if (!getSelectedMobs(stack).isEmpty()) {
			List<String> mobs = getSelectedMobs(stack);
			for (int i = 0; i < mobs.size(); i++) {
				if (i < 3) {
					tooltip.accept(CommonComponents.space().append(Component.literal(mobs.get(i)).withStyle(ChatFormatting.GRAY)));
				} else {
					break;
				}
			}
			if (mobs.size() > 3) {
				tooltip.accept(CommonComponents.space().append(Component.translatable(RatsLangConstants.AND_MORE, mobs.size() - 3).withStyle(ChatFormatting.GRAY)));
			}
		}
	}
}
