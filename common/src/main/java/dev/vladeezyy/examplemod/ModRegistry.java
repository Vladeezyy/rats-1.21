package dev.vladeezyy.examplemod;

import dev.vladeezyy.examplemod.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Everything the mod registers. The example block shows the pattern: replace it with the mod's content. */
public final class ModRegistry {
  /** Items in the creative tab, in order */
  public static final List<Supplier<? extends Item>> TAB_ITEMS = new ArrayList<>();

  public static Supplier<Block> exampleBlock;
  public static Supplier<Item> exampleBlockItem;

  private ModRegistry() {}

  static void register() {
    Platform platform = Platform.INSTANCE;
    exampleBlock = platform.register(Registries.BLOCK, "example_block", () -> new Block(BlockBehaviour.Properties.of()
      .mapColor(MapColor.COLOR_PURPLE).strength(1.5f, 6f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    exampleBlockItem = item("example_block", () -> new BlockItem(exampleBlock.get(), new Item.Properties()));
    platform.register(Registries.CREATIVE_MODE_TAB, ExampleMod.MOD_ID, () -> platform.creativeTabBuilder()
      .title(Component.translatable("itemGroup." + ExampleMod.MOD_ID))
      .icon(() -> new ItemStack(exampleBlockItem.get()))
      .displayItems((params, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())))
      .build());
  }

  private static <T extends Item> Supplier<T> item(String name, Supplier<T> factory) {
    Supplier<T> item = Platform.INSTANCE.register(Registries.ITEM, name, factory);
    TAB_ITEMS.add(item);
    return item;
  }
}
