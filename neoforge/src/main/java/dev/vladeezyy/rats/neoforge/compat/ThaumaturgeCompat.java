package dev.vladeezyy.rats.neoforge.compat;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Optional;

/**
 * Thaumaturge (NeoForge only): the Caseus (cheese) aspect and the aspects of Rats items and mobs, as a built-in data
 * pack (data/rats/datapacks/thaumaturge, written by tools/gen_thaumaturge.py). Added only when Thaumaturge is loaded:
 * its data map files without Thaumaturge would make NeoForge warn about an unknown data map type. Required, so it is
 * on in new and existing worlds.
 */
public final class ThaumaturgeCompat {
  public static final String MOD_ID = "thaumaturge";
  public static final String PACK_ID = RatsMod.MODID + ":thaumaturge";

  private ThaumaturgeCompat() {}

  public static void addPack(AddPackFindersEvent event) {
    if (event.getPackType() != PackType.SERVER_DATA || !ModList.get().isLoaded(MOD_ID)) {
      return;
    }
    var path = ModList.get().getModFileById(RatsMod.MODID).getFile().findResource("data", RatsMod.MODID, "datapacks", "thaumaturge");
    var info = new PackLocationInfo(PACK_ID, Component.translatable("pack.rats.thaumaturge.title"), PackSource.BUILT_IN, Optional.empty());
    var pack = Pack.readMetaAndCreate(info, new PathPackResources.PathResourcesSupplier(path), PackType.SERVER_DATA,
        new PackSelectionConfig(true, Pack.Position.TOP, false));
    if (pack != null) {
      event.addRepositorySource(consumer -> consumer.accept(pack));
    }
  }
}
