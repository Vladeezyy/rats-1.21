package com.github.alexthe666.rats.server.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.List;

/**
 * The chef's and the archeologist's recipes: one ingredient, one result, made by rats (not in the recipe book).
 * JSON as the original's: {"group", "ingredient", "result", "count"} (26.x: the ingredient is a string).
 */
public abstract class BaseRatRecipe extends SingleItemRecipe {
	private final String group;

	public BaseRatRecipe(Recipe.CommonInfo commonInfo, String group, Ingredient input, ItemStackTemplate result) {
		super(commonInfo, input, result);
		this.group = group;
	}

	public final ItemStack getResult() {
		return this.result().create();
	}

	public final ItemStackTemplate resultTemplate() {
		return this.result();
	}

	@Override
	public String group() {
		return this.group;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of();
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	public static <T extends BaseRatRecipe> RecipeSerializer<T> serializer(Factory<T> factory) {
		MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
				Codec.STRING.optionalFieldOf("group", "").forGetter(BaseRatRecipe::group),
				Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
				ItemStackTemplate.CODEC.fieldOf("result").forGetter(BaseRatRecipe::resultTemplate),
				Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.resultTemplate().count())
		).apply(instance, (info, group, input, result, count) -> factory.create(info, group, input, result.withCount(count))));
		StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
				Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
				ByteBufCodecs.STRING_UTF8, BaseRatRecipe::group,
				Ingredient.CONTENTS_STREAM_CODEC, SingleItemRecipe::input,
				ItemStackTemplate.STREAM_CODEC, BaseRatRecipe::resultTemplate,
				factory::create);
		return new RecipeSerializer<>(codec, streamCodec);
	}

	@FunctionalInterface
	public interface Factory<T extends BaseRatRecipe> {
		T create(Recipe.CommonInfo commonInfo, String group, Ingredient input, ItemStackTemplate result);
	}
}
