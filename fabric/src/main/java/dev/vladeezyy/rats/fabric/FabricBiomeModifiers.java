package dev.vladeezyy.rats.fabric;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.github.alexthe666.rats.RatsMod;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * The NeoForge biome modifiers (data/rats/neoforge/biome_modifier) applied with the Fabric BiomeModifications: the
 * same files, read from the mod jar. Supports neoforge:add_features and neoforge:add_spawns with biomes neoforge:any or a
 * tag (what Rats uses; from Natura's port).
 */
final class FabricBiomeModifiers {
    private static final String DIR = "data/" + RatsMod.MODID + "/neoforge/biome_modifier";

    private FabricBiomeModifiers() {}

    static void register() {
        Path dir = FabricLoader.getInstance().getModContainer(RatsMod.MODID).orElseThrow().findPath(DIR).orElseThrow();
        List<Path> files;
        try (Stream<Path> list = Files.list(dir)) {
            files = list.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not list " + DIR, e);
        }
        for (Path file : files) {
            JsonObject json;
            try (Reader reader = Files.newBufferedReader(file)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + file, e);
            }
            String name = file.getFileName().toString().replace(".json", "");
            apply(Identifier.fromNamespaceAndPath(RatsMod.MODID, name), json);
        }
    }

    private static void apply(Identifier id, JsonObject json) {
        if (json.has("neoforge:conditions")) {
            throw new IllegalStateException("Unsupported conditions in " + id);
        }
        Predicate<BiomeSelectionContext> biomes = biomes(json.get("biomes"));
        String type = json.get("type").getAsString();
        switch (type) {
            case "neoforge:add_features" -> {
                GenerationStep.Decoration step = GenerationStep.Decoration.valueOf(json.get("step").getAsString().toUpperCase(java.util.Locale.ROOT));
                List<ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature>> features = strings(json.get("features")).stream()
                        .map(f -> ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse(f))).toList();
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    features.forEach(feature -> context.getGenerationSettings().addFeature(step, feature));
                });
            }
            case "neoforge:add_spawns" -> {
                List<JsonObject> spawners = new ArrayList<>();
                JsonElement element = json.get("spawners");
                if (element.isJsonArray()) {
                    element.getAsJsonArray().forEach(e -> spawners.add(e.getAsJsonObject()));
                } else {
                    spawners.add(element.getAsJsonObject());
                }
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    spawners.forEach(spawner -> addSpawn(context, spawner));
                });
            }
            default -> throw new IllegalStateException("Unsupported biome modifier type in " + id + ": " + type);
        }
    }

    private static void addSpawn(BiomeModificationContext context, JsonObject spawner) {
        // 26.3: {"type", "count" (an int provider), "weight"}, NeoForge's codec
        MobSpawnSettings.SpawnerData data = MobSpawnSettings.SpawnerData.CODEC.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, spawner)
                .getOrThrow(error -> new IllegalStateException("Bad spawner " + spawner + ": " + error));
        context.getMobSpawnSettings().addSpawn(data.type().getCategory(), data, spawner.get("weight").getAsInt());
    }

    private static Predicate<BiomeSelectionContext> biomes(JsonElement biomes) {
        if (biomes.isJsonObject() && biomes.getAsJsonObject().get("type").getAsString().equals("neoforge:any")) {
            return BiomeSelectors.all();
        }
        if (biomes.isJsonPrimitive() && biomes.getAsString().startsWith("#")) {
            return BiomeSelectors.tag(TagKey.create(Registries.BIOME, Identifier.parse(biomes.getAsString().substring(1))));
        }
        throw new IllegalStateException("Unsupported biomes: " + biomes);
    }

    private static List<String> strings(JsonElement element) {
        if (element.isJsonArray()) {
            List<String> list = new ArrayList<>();
            for (JsonElement e : (JsonArray) element) {
                list.add(e.getAsString());
            }
            return list;
        }
        return List.of(element.getAsString());
    }
}
