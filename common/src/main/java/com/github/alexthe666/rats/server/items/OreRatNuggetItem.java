package com.github.alexthe666.rats.server.items;

import net.minecraft.util.Prediction;import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class OreRatNuggetItem extends Item {

	public OreRatNuggetItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), RatsSoundRegistry.RAT_NUGGET_ORE.get(), SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		ItemStack poopStack = getStoredItem(itemstack, new ItemStack(Items.IRON_INGOT));
		if (!player.getInventory().add(poopStack)) {
			player.drop(poopStack, false, Prediction.SERVER_ONLY);
		}
		if (!player.isCreative()) {
			itemstack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}

	public static ItemStack getStoredItem(ItemStack poopItem, ItemStack fallback) {
		if (ItemNbt.view(poopItem).contains("OreItem")) {
			// the ore by its id ("id" in 1.20.1's saved stack too); ores carry no other data
			CompoundTag poopTag = ItemNbt.view(poopItem).getCompoundOrEmpty("OreItem");
			Identifier id = Identifier.tryParse(poopTag.getStringOr("id", ""));
			ItemStack oreItem = id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.getValue(id));
			if (oreItem.isEmpty()) {
				return fallback;
			} else {
				return oreItem;
			}
		}
		return fallback;
	}

	public static ItemStack getIngot(Level level, ItemStack stack) {
		if (level instanceof net.minecraft.server.level.ServerLevel server) {
			SingleRecipeInput input = new SingleRecipeInput(stack);
			return server.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack saveResourceToNugget(ItemStack resource) {
		ItemStack stack = new ItemStack(RatsItemRegistry.RAT_NUGGET_ORE.get());
		CompoundTag nuggetTag = new CompoundTag();
		CompoundTag oreTag = new CompoundTag();
		oreTag.putString("id", BuiltInRegistries.ITEM.getKey(resource.getItem()).toString());
		nuggetTag.put("OreItem", oreTag);
		ItemNbt.setTag(stack, nuggetTag);
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (flag.isCreative()) {
			ItemStack ingot = getStoredItem(stack, new ItemStack(Items.AIR));
			tooltip.accept(Component.translatable(RatsLangConstants.ORE_NUGGET_CONTAINS, ingot.getDisplayName().getString()).withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.accept(Component.translatable("item.rats.rat_nugget_ore.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
	}
}
