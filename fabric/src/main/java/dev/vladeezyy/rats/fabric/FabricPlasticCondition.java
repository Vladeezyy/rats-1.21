package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** #864: rat tubes and igloos from plastic or from glass (the NeoForge module has the same condition) */
public record FabricPlasticCondition(boolean enabled) implements ResourceCondition {
    static final MapCodec<FabricPlasticCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(FabricPlasticCondition::enabled)
    ).apply(instance, FabricPlasticCondition::new));
    static final ResourceConditionType<FabricPlasticCondition> TYPE = ResourceConditionType.create(Identifier.fromNamespaceAndPath(RatsMod.MODID, "plastic_recipes"), CODEC);

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable net.minecraft.resources.RegistryOps.RegistryInfoLookup registries) {
        return RatConfig.plasticRecipes == this.enabled;
    }
}
