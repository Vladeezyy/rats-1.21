package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.RatConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

/** #864: rat tubes and igloos from plastic (enabled = true) or from glass (enabled = false), per the config */
public record PlasticRecipesCondition(boolean enabled) implements ICondition {
    public static final MapCodec<PlasticRecipesCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(PlasticRecipesCondition::enabled)
    ).apply(instance, PlasticRecipesCondition::new));

    @Override
    public boolean test(IContext context) {
        return RatConfig.plasticRecipes == this.enabled;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
