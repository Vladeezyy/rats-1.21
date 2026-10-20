package com.github.alexthe666.rats.server.items;
import com.github.alexthe666.rats.server.misc.Values;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ComponentSerialization;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.core.RegistryAccess;
import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RatSackItem extends Item {

	public RatSackItem(Item.Properties properties) {
		super(properties);
	}

	public static void packRatIntoSack(ItemStack sack, TamedRat rat, int ratCount) {
		// the rat's save data, its name included
		CompoundTag ratTag = Values.save(rat);
		// custom data is immutable in 1.21: the port first wrote into a copy, so packed rats were lost
		ItemNbt.update(sack, tag -> tag.put("Rat_" + ratCount, ratTag));
	}

	/**
	 * The name of a rat in a sack: 26.x saves it as a component; sacks filled in 1.21.1 hold it as a JSON string
	 */
	@Nullable
	public static Component ratName(CompoundTag ratTag, HolderLookup.Provider registries) {
		Tag name = ratTag.get("CustomName");
		if (name == null) {
			return null;
		}
		if (name instanceof StringTag string && (string.value().startsWith("{") || string.value().startsWith("\""))) {
			try {
				return ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(string.value())).result().orElse(null);
			} catch (RuntimeException e) {
				return null;
			}
		}
		return ComponentSerialization.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), name).result().orElse(null);
	}

	public static int getRatsInSack(ItemStack sack) {
		int ratCount = 0;
		if (ItemNbt.hasTag(sack)) {
			for (String tagInfo : ItemNbt.view(sack).keySet()) {
				if (tagInfo.contains("Rat")) {
					ratCount++;
				}
			}
		}
		return ratCount;
	}

	public static int ejectRatsFromSack(ItemStack stack, Level level, BlockPos pos) {
		int ratCount = 0;
		if (ItemNbt.hasTag(stack)) {
			for (String tagInfo : ItemNbt.view(stack).keySet()) {
				if (tagInfo.contains("Rat")) {
					ratCount++;
					CompoundTag ratTag = ItemNbt.view(stack).getCompoundOrEmpty(tagInfo);
					TamedRat rat = new TamedRat(RatsEntityRegistry.TAMED_RAT.get(), level);
					CompoundTag data = ratTag.copy();
					// a new entity (a sack copied in creative would give two rats one id)
					data.remove("UUID");
					Values.load(rat, data);
					Component name = ratName(ratTag, level.registryAccess());
					if (name != null) {
						rat.setCustomName(name);
					}
					rat.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
					if (!level.isClientSide()) {
						level.addFreshEntity(rat);
					}
				}
			}
		}
		return ratCount;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		int ratCount = 0;
		List<String> ratNames = new ArrayList<>();
		if (ItemNbt.hasTag(stack)) {
			for (String tagInfo : ItemNbt.view(stack).keySet()) {
				if (tagInfo.contains("Rat")) {
					CompoundTag ratTag = ItemNbt.view(stack).getCompoundOrEmpty(tagInfo);
					ratCount++;
					String ratName = I18n.get("entity.rats.rat");
					Component ratNameTag = ratName(ratTag, level.registries() != null ? level.registries() : RegistryAccess.EMPTY);
					if (ratNameTag != null) {
						ratName = ratNameTag.getString();
					}
					ratNames.add(ratName);
				}
			}
		}
		tooltip.accept(Component.translatable(RatsLangConstants.RAT_SACK_CONTAINED_RATS, ratCount, RatConfig.ratSackCapacity).withStyle(ChatFormatting.GRAY));
		if (!ratNames.isEmpty()) {
			for (int i = 0; i < ratNames.size(); i++) {
				if (i < 3) {
					tooltip.accept(Component.literal(ratNames.get(i)).withStyle(ChatFormatting.GRAY));
				} else {
					break;
				}
			}
			if (ratNames.size() > 3) {
				tooltip.accept(Component.translatable(RatsLangConstants.AND_MORE, ratNames.size() - 3).withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getPlayer().getItemInHand(context.getHand());
		if (stack.is(RatsItemRegistry.RAT_SACK.get()) && getRatsInSack(stack) > 0) {
			int ratCount = ejectRatsFromSack(stack, context.getLevel(), context.getClickedPos().relative(context.getClickedFace()));

			if (ratCount > 0) {
				context.getPlayer().sendOverlayMessage(Component.translatable(RatsLangConstants.RAT_SACK_RELEASED_RATS, ratCount));
				ItemNbt.setTag(stack, new CompoundTag());
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		ItemStack stack = entity.getItem();
		if (getRatsInSack(stack) > 0) {
			ejectRatsFromSack(stack, entity.level(), entity.blockPosition());
		}
	}
}
