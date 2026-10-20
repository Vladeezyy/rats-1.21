package com.github.alexthe666.rats.server.items;
import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class RatFluteItem extends Item {

	public RatFluteItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.TOOT_HORN;
	}

	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		int commandInt = ItemNbt.view(stack).getIntOr("Command", 0);
		RatCommand ratCommand = RatCommand.values()[Mth.clamp(commandInt, 0, RatCommand.values().length - 1)];
		if (player.isShiftKeyDown()) {
			commandInt++;
			if (commandInt > RatCommand.values().length - 1) {
				commandInt = 0;
			}
			int newCommand = commandInt;
			ItemNbt.update(stack, tag -> tag.putInt("Command", newCommand));
			ratCommand = RatCommand.values()[Mth.clamp(commandInt, 0, RatCommand.values().length - 1)];
			level.playSound(player, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.NEUTRAL, 1.0F, 1.25F);
			player.sendOverlayMessage(Component.translatable(RatsLangConstants.RAT_FLUTE_COMMAND, Component.translatable(ratCommand.getTranslateName())));
		} else {
			player.getCooldowns().addCooldown(stack, 60);
			float chunksize = 16 * RatConfig.ratFluteDistance;
			List<Entity> list = level.getEntities(player, (new AABB(player.getX(), player.getY(), player.getZ(), player.getX() + 1.0D, player.getY() + 1.0D, player.getZ() + 1.0D)).inflate(chunksize, level.getHeight(), chunksize));
			Iterator<Entity> itr = list.iterator();
			int ratCount = 0;
			while (itr.hasNext()) {
				Entity entity = itr.next();
				if (entity instanceof TamedRat rat) {
					if (rat.onHearFlute(player, ratCommand)) {
						ratCount++;
					}
				}
			}
			player.swing(hand);
			player.sendOverlayMessage(Component.translatable(RatsLangConstants.RAT_FLUTE_COUNT, ratCount).withStyle(ChatFormatting.GRAY));
			level.playSound(player, player.blockPosition(), RatsSoundRegistry.getFluteSound(), SoundSource.NEUTRAL, 1, 1.25F);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.rats.rat_flute.desc0").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.rats.rat_flute.desc1").withStyle(ChatFormatting.GRAY));
		if (ItemNbt.hasTag(stack)) {
			RatCommand ratCommand = RatCommand.values()[Mth.clamp(ItemNbt.view(stack).getIntOr("Command", 0), 0, RatCommand.values().length - 1)];
			tooltip.accept(Component.translatable(RatsLangConstants.RAT_CURRENT_COMMAND, Component.translatable(ratCommand.getTranslateName())).withStyle(ChatFormatting.GRAY));
		}
	}
}
