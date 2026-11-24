package dev.vladeezyy.rats.neoforge.selftest;

import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.neoforge.compat.ThaumaturgeCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thaumaturge compat (dev runs only). Without Thaumaturge: the compat pack is not there. With it
 * ({@code -Pwith_thaumaturge}): the pack is on, the Caseus aspect is registered, cheese and the rat have their data map
 * entries and Thaumaturge's aspect index gives cheese Caseus. Lists the Rats items that end up with no aspects at all
 * (no data map entry and no recipe Thaumaturge derives them from). Thaumaturge is read by reflection: no compile
 * dependency on it.
 */
final class ThaumaturgeCheck {
  private static final String PREFIX = "[Rats selftest] ";

  private ThaumaturgeCheck() {}

  static boolean run(MinecraftServer server) {
    boolean available = server.getPackRepository().getAvailableIds().contains(ThaumaturgeCompat.PACK_ID);
    if (!ModList.get().isLoaded(ThaumaturgeCompat.MOD_ID)) {
      RatsMod.LOGGER.info(PREFIX + "Thaumaturge not loaded, compat pack available: {}", available);
      return !available;
    }
    boolean selected = server.getPackRepository().getSelectedIds().contains(ThaumaturgeCompat.PACK_ID);
    ResourceKey<net.minecraft.core.Registry<Object>> aspectRegistry = ResourceKey.createRegistryKey(rl("thaumaturge", "aspect"));
    boolean caseus = server.registryAccess().lookup(aspectRegistry).map(r -> r.containsKey(rl(RatsMod.MODID, "caseus"))).orElse(false);
    DataMapType<Item, ?> baseAspects = RegistryManager.getDataMap(Registries.ITEM, rl("thaumaturge", "base_aspects"));
    DataMapType<EntityType<?>, ?> entityAspects = RegistryManager.getDataMap(Registries.ENTITY_TYPE, rl("thaumaturge", "entity_aspects"));
    Item cheese = BuiltInRegistries.ITEM.getValue(rl(RatsMod.MODID, "cheese"));
    Object cheeseEntry = baseAspects == null ? null : BuiltInRegistries.ITEM.wrapAsHolder(cheese).getData(baseAspects);
    Object ratEntry = entityAspects == null ? null : BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(BuiltInRegistries.ENTITY_TYPE.getValue(rl(RatsMod.MODID, "rat"))).getData(entityAspects);
    RatsMod.LOGGER.info(PREFIX + "Thaumaturge: pack on {}, rats:caseus registered {}, cheese entry {}, rat entry {}", selected, caseus, cheeseEntry, ratEntry);
    Set<String> cheeseIndex;
    List<String> without = new ArrayList<>();
    int total = 0;
    try {
      Class<?> access = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess");
      var of = access.getMethod("of", ItemStack.class);
      cheeseIndex = aspectIds(of.invoke(null, new ItemStack(cheese)));
      for (Item item : BuiltInRegistries.ITEM) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (!id.getNamespace().equals(RatsMod.MODID)) {
          continue;
        }
        total++;
        if (aspectIds(of.invoke(null, new ItemStack(item))).isEmpty()) {
          without.add(id.getPath());
        }
      }
    } catch (ReflectiveOperationException e) {
      RatsMod.LOGGER.error(PREFIX + "Thaumaturge API not found", e);
      return false;
    }
    RatsMod.LOGGER.info(PREFIX + "Thaumaturge index: cheese {}; {}/{} Rats items have aspects; without: {}", cheeseIndex,
        total - without.size(), total, without);
    return selected && caseus && cheeseEntry != null && ratEntry != null && cheeseIndex.contains(RatsMod.MODID + ":caseus");
  }

  @SuppressWarnings("unchecked")
  private static Set<String> aspectIds(Object aspectList) throws ReflectiveOperationException {
    Set<Holder<?>> aspects = (Set<Holder<?>>) aspectList.getClass().getMethod("aspects").invoke(aspectList);
    return aspects.stream().map(h -> h.unwrapKey().map(k -> k.identifier().toString()).orElse("?")).collect(Collectors.toCollection(java.util.TreeSet::new));
  }

  private static Identifier rl(String namespace, String path) {
    return Identifier.fromNamespaceAndPath(namespace, path);
  }
}
