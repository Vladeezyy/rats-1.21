package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.RatsMod;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
    private record Modifier(Identifier id, JsonObject json, List<Identifier> tables) {}

    private record Parsed(List<LootItemCondition> conditions, Map<Item, Integer> items, boolean replacePool) {}

    private static final List<Modifier> MODIFIERS = new ArrayList<>();
    private static final Map<Identifier, Parsed> PARSED = new LinkedHashMap<>();
    private static HolderLookup.Provider parsedWith;
    /** Loot table ids by table, rebuilt once per data reload (26.x has no reverse lookup; a scan per roll would cost TPS) */
    private static net.minecraft.server.ReloadableServerRegistries.Holder tablesOf;
    private static Map<net.minecraft.world.level.storage.loot.LootTable, Identifier> tableIds = Map.of();

    private static synchronized Identifier tableId(net.minecraft.server.MinecraftServer server, net.minecraft.world.level.storage.loot.LootTable table) {
        var registries = server.reloadableRegistries();
        if (registries != tablesOf) {
            Map<net.minecraft.world.level.storage.loot.LootTable, Identifier> ids = new java.util.IdentityHashMap<>();
            registries.lookup().lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE).listElements()
                .forEach(ref -> ids.put(ref.value(), ref.key().identifier()));
            tableIds = ids;
            tablesOf = registries;
        }
        return tableIds.get(table);
    }

    private FabricLootModifiers() {}

    static void register() {
        var mod = FabricLoader.getInstance().getModContainer(RatsMod.MODID).orElseThrow();
        // 26.x NeoForge loads every file in loot_modifiers/ (no global list any more): the same files here
        List<Path> files;
        try (var list = Files.list(mod.findPath("data/" + RatsMod.MODID + "/loot_modifiers").orElseThrow())) {
            files = list.filter(file -> file.getFileName().toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not list the loot modifiers of Rats", e);
        }
        for (Path file : files) {
            String name = file.getFileName().toString();
            Identifier id = Identifier.fromNamespaceAndPath(RatsMod.MODID, name.substring(0, name.length() - ".json".length()));
            JsonObject json = read(file);
            if (!json.get("type").getAsString().equals(RatsMod.MODID + ":add_item")) {
                throw new IllegalStateException("Unsupported loot modifier " + id + ": " + json.get("type"));
            }
            List<Identifier> tables = new ArrayList<>();
            for (JsonObject condition : terms(json)) {
                if (condition.get("type").getAsString().equals("neoforge:loot_table_id")) {
                    tables.add(Identifier.parse(condition.get("loot_table_id").getAsString()));
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
        Identifier tableId = tableId(context.getLevel().getServer(), table);
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
            for (JsonObject condition : terms(modifier.json())) {
                if (!condition.get("type").getAsString().equals("neoforge:loot_table_id")) {
                    conditions.add(LootItemCondition.DIRECT_CODEC.parse(ops, condition).getOrThrow(error -> new IllegalStateException(id + ": " + error)));
                }
            }
            Map<Item, Integer> items = new LinkedHashMap<>();
            modifier.json().getAsJsonObject("items").entrySet().forEach(e -> items.put(BuiltInRegistries.ITEM.getValue(Identifier.parse(e.getKey())), e.getValue().getAsInt()));
            boolean replacePool = modifier.json().has("replacePool") && modifier.json().get("replacePool").getAsBoolean();
            return new Parsed(conditions, items, replacePool);
        });
    }

    /** 26.3: one "condition" (an all_of of several) instead of a "conditions" list */
    private static List<JsonObject> terms(JsonObject json) {
        if (!json.has("condition")) {
            return List.of();
        }
        JsonObject condition = json.getAsJsonObject("condition");
        if (!condition.get("type").getAsString().equals("minecraft:all_of")) {
            return List.of(condition);
        }
        List<JsonObject> terms = new ArrayList<>();
        condition.getAsJsonArray("terms").forEach(term -> terms.add(term.getAsJsonObject()));
        return terms;
    }

    private static JsonObject read(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }
}
