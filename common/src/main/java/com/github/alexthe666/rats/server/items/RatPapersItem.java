package com.github.alexthe666.rats.server.items;
import net.minecraft.core.UUIDUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RatPapersItem extends Item {

	public RatPapersItem(Item.Properties properties) {
		super(properties);
	}

	public boolean isFoil(ItemStack stack) {
		return isEntityBound(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (!isEntityBound(stack)) {
			tooltip.accept(Component.translatable("item.rats.rat_papers.desc0").withStyle(ChatFormatting.GRAY));
			tooltip.accept(Component.translatable("item.rats.rat_papers.desc1").withStyle(ChatFormatting.GRAY));
		}
		tooltip.accept(Component.translatable("item.rats.rat_papers.desc2").withStyle(ChatFormatting.GRAY));
		if (ItemNbt.hasTag(stack) && !ItemNbt.view(stack).isEmpty()) {
			String ratName = I18n.get("entity.rats.tamed_rat");
			String entity = ItemNbt.view(stack).getStringOr("RatName", "");
			Component rat = Component.empty();
			if (ItemNbt.view(stack).read("RatUUID", UUIDUtil.CODEC).isPresent()) {
				if (entity.isEmpty()) {
					rat = Component.literal(ratName + " (" + ItemNbt.view(stack).read("RatUUID", UUIDUtil.CODEC).orElseThrow() + ")").withStyle(ChatFormatting.GRAY);
				} else {
					rat = Component.literal(entity).withStyle(ChatFormatting.GRAY);
				}
			}
			tooltip.accept(Component.translatable(RatsLangConstants.RAT_PAPERS_BOUND_RAT, rat.getString()).withStyle(ChatFormatting.GRAY));
		}
	}

	public static boolean isEntityBound(ItemStack stack) {
		return ItemNbt.view(stack).read("RatUUID", UUIDUtil.CODEC).isPresent();
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		CompoundTag nbt = ItemNbt.getOrEmpty(stack);
		if (target instanceof Player transferTo) {
			try {
				if (nbt.read("RatUUID", UUIDUtil.CODEC).isPresent()) {
					UUID ratUUID = nbt.read("RatUUID", UUIDUtil.CODEC).orElseThrow();
					if (!target.level().isClientSide()) {
						Entity entity = target.level().getServer().getLevel(target.level().dimension()).getEntity(ratUUID);
						if (entity instanceof TamedRat rat) {
							if (rat.isTame() && rat.isOwnedBy(player)) {
								rat.tame(transferTo);
								stack.shrink(1);
								player.level().playSound(player, player.blockPosition(), RatsSoundRegistry.RAT_TRANSFER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
							}
						}
					}
					return InteractionResult.SUCCESS;
				}
			} catch (Exception e) {
				player.sendOverlayMessage(Component.literal("Couldnt transfer ownership! Check the log and report this!").withStyle(ChatFormatting.RED));
				RatsMod.LOGGER.error("Couldnt transfer rat ownership to {}!", transferTo.getStringUUID(), e);
			}
			return InteractionResult.PASS;
		}

		if (target instanceof TamedRat rat && rat.isOwnedBy(player)) {
			if (rat.hasCustomName()) {
				nbt.putString("RatName", rat.getCustomName().getString());
			}
			nbt.store("RatUUID", UUIDUtil.CODEC, rat.getUUID());
			ItemNbt.setTag(stack, nbt);

			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
