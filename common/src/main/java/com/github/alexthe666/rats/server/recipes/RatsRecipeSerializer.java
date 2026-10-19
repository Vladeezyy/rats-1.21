package com.github.alexthe666.rats.server.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Serializer of the chef and archeologist recipes: {"group", "ingredient", "result": {"id"}, "count"}, the JSON of the
 * original (its "result" string became an item stack object in 1.21's format).
 */
public class RatsRecipeSerializer<T extends BaseRatRecipe> implements RecipeSerializer<T> {
	private final MapCodec<T> codec;
	private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

	public RatsRecipeSerializer(SingleItemMaker<T> factory) {
		this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
				Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.getIngredients().get(0)),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(BaseRatRecipe::getResult),
				Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.getResult().getCount())
		).apply(instance, (group, ingredient, result, count) -> factory.create(group, ingredient, result.copyWithCount(count))));
		this.streamCodec = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, Recipe::getGroup,
				Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.getIngredients().get(0),
				ItemStack.STREAM_CODEC, BaseRatRecipe::getResult,
				factory::create);
	}

	@Override
	public MapCodec<T> codec() {
		return this.codec;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return this.streamCodec;
	}

	public interface SingleItemMaker<T extends BaseRatRecipe> {
		T create(String group, Ingredient input, ItemStack output);
	}
}
