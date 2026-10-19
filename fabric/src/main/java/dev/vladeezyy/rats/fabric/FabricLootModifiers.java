package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.RatsMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NeoForge's global loot modifiers of Rats (data/neoforge/loot_modifiers/global_loot_modifiers.json and the
 * rats:add_item modifiers it lists) applied to the drops of loot tables (LootTableMixin): the same files, from the jar.
 * Conditions: neoforge:loot_table_id is matched against the table, the others are vanilla or Rats loot conditions.
 */
final class FabricLootModifiers {
    private record Modifier(ResourceLocation id, JsonObject json, List<ResourceLocation> tables) {}

    private record Parsed(List<LootItemCondition> conditions, Map<Item, Integer> items, boolean replacePool) {}

    private static final List<Modifier> MODIFIERS = new ArrayList<>();
    private static final Map<ResourceLocation, Parsed> PARSED = new LinkedHashMap<>();
    private static HolderLookup.Provider parsedWith;

    private FabricLootModifiers() {}

    static void register() {
        var mod = FabricLoader.getInstance().getModContainer(RatsMod.MODID).orElseThrow();
        JsonObject global = read(mod.findPath("data/neoforge/loot_modifiers/global_loot_modifiers.json").orElseThrow());
        for (JsonElement entry : global.getAsJsonArray("entries")) {
            ResourceLocation id = ResourceLocation.parse(entry.getAsString());
            JsonObject json = read(mod.findPath("data/" + id.getNamespace() + "/loot_modifiers/" + id.getPath() + ".json").orElseThrow());
            if (!json.get("type").getAsString().equals(RatsMod.MODID + ":add_item")) {
                throw new IllegalStateException("Unsupported loot modifier " + id + ": " + json.get("type"));
            }
            List<ResourceLocation> tables = new ArrayList<>();
            for (JsonElement condition : json.getAsJsonArray("conditions")) {
                JsonObject object = condition.getAsJsonObject();
                if (object.get("condition").getAsString().equals("neoforge:loot_table_id")) {
                    tables.add(ResourceLocation.parse(object.get("loot_table_id").getAsString()));
                }
            }
            MODIFIERS.add(new Modifier(id, json, tables));
        }
    }

    /** The drops of a loot table that was rolled (not one rolled inside another), from {@code LootTableMixin} */
    static void apply(net.minecraft.world.level.storage.loot.LootTable table, LootContext context, List<ItemStack> drops) {
        if (MODIFIERS.isEmpty()) {
            return;
        }
        ResourceLocation tableId = context.getLevel().getServer().reloadableRegistries().get()
            .registryOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE).getKey(table);
        for (Modifier modifier : MODIFIERS) {
            if (!modifier.tables().isEmpty() && !modifier.tables().contains(tableId)) {
                continue;
            }
            Parsed parsed = parse(modifier, context);
            if (parsed.conditions().stream().allMatch(condition -> condition.test(context))) {
                apply(parsed, context, drops);
            }
        }
    }

    /** What GenericAddItemLootModifier does on NeoForge */
    private static void apply(Parsed parsed, LootContext context, List<ItemStack> drops) {
        if (parsed.replacePool()) {
            drops.clear();
            if (parsed.items().size() > 1) {
                Item chosen = new ArrayList<>(parsed.items().keySet()).get(context.getRandom().nextInt(parsed.items().size()));
                drops.add(new ItemStack(chosen, parsed.items().get(chosen)));
                return;
            }
        }
        parsed.items().forEach((item, count) -> drops.add(new ItemStack(item, count)));
    }

    private static synchronized Parsed parse(Modifier modifier, LootContext context) {
        HolderLookup.Provider registries = context.getLevel().registryAccess();
        if (parsedWith != registries) {
            PARSED.clear();
            parsedWith = registries;
        }
        return PARSED.computeIfAbsent(modifier.id(), id -> {
            var ops = registries.createSerializationContext(JsonOps.INSTANCE);
            List<LootItemCondition> conditions = new ArrayList<>();
            for (JsonElement condition : modifier.json().getAsJsonArray("conditions")) {
                if (!condition.getAsJsonObject().get("condition").getAsString().equals("neoforge:loot_table_id")) {
                    conditions.add(LootItemCondition.DIRECT_CODEC.parse(ops, condition).getOrThrow(error -> new IllegalStateException(id + ": " + error)));
                }
            }
            Map<Item, Integer> items = new LinkedHashMap<>();
            modifier.json().getAsJsonObject("items").entrySet().forEach(e -> items.put(BuiltInRegistries.ITEM.get(ResourceLocation.parse(e.getKey())), e.getValue().getAsInt()));
            boolean replacePool = modifier.json().has("replacePool") && modifier.json().get("replacePool").getAsBoolean();
            return new Parsed(conditions, items, replacePool);
        });
    }

    private static JsonObject read(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }
}
