package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.block.*;
import com.github.alexthe666.rats.server.items.RatsBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

import java.util.function.Supplier;

public class RatsBlockRegistry {

	public static final Registrar<Block> BLOCKS = Registrar.create(BuiltInRegistries.BLOCK, RatsMod.MODID);

	public static final RegistryEntry<Block, Block> BLOCK_OF_CHEESE = register("block_of_cheese", () -> new Block(Registrar.blockProps().mapColor(MapColor.COLOR_YELLOW).sound(SoundType.SLIME_BLOCK).strength(0.6F, 0.0F)));
	public static final RegistryEntry<Block, Block> MILK_CAULDRON = register("cauldron_milk", () -> new MilkCauldronBlock(Registrar.blockCopy(Blocks.CAULDRON)));
	public static final RegistryEntry<Block, Block> CHEESE_CAULDRON = register("cauldron_cheese", () -> new CheeseCauldronBlock(Registrar.blockCopy(Blocks.CAULDRON), RatsBlockRegistry.BLOCK_OF_CHEESE, RatsCauldronRegistry.CHEESE));
	public static final RegistryEntry<Block, Block> BLUE_CHEESE_CAULDRON = register("cauldron_blue_cheese", () -> new CheeseCauldronBlock(Registrar.blockCopy(Blocks.CAULDRON), RatsBlockRegistry.BLOCK_OF_BLUE_CHEESE, RatsCauldronRegistry.BLUE_CHEESE));
	public static final RegistryEntry<Block, Block> NETHER_CHEESE_CAULDRON = register("cauldron_nether_cheese", () -> new CheeseCauldronBlock(Registrar.blockCopy(Blocks.CAULDRON), RatsBlockRegistry.BLOCK_OF_NETHER_CHEESE, RatsCauldronRegistry.NETHER_CHEESE));
	public static final RegistryEntry<Block, Block> RAT_HOLE = BLOCKS.register("rat_hole", () -> new RatHoleBlock(Registrar.blockProps().sound(SoundType.WOOD).noOcclusion().dynamicShape().strength(1.3F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_CAGE = register("rat_cage", () -> new RatCageBlock(Registrar.blockProps().sound(SoundType.METAL).noOcclusion().strength(2.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_CAGE_DECORATED = BLOCKS.register("rat_cage_decorated", () -> new RatCageDecoratedBlock(Registrar.blockCopy(RAT_CAGE.get())));
	public static final RegistryEntry<Block, Block> RAT_CAGE_BREEDING_LANTERN = BLOCKS.register("rat_cage_breeding_lantern", () -> new RatCageBreedingLanternBlock(Registrar.blockCopy(RAT_CAGE.get())));
	public static final RegistryEntry<Block, Block> RAT_CAGE_WHEEL = BLOCKS.register("rat_cage_wheel", () -> new RatCageWheelBlock(Registrar.blockCopy(RAT_CAGE.get())));
	public static final RegistryEntry<Block, Block> FISH_BARREL = register("fish_barrel", () -> new Block(Registrar.blockProps().strength(2.0F, 10.0F).sound(SoundType.WOOD)));
	public static final RegistryEntry<Block, Block> RAT_CRAFTING_TABLE = register("rat_crafting_table", () -> new RatCraftingTableBlock(Registrar.blockProps().sound(SoundType.SLIME_BLOCK).strength(2.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> AUTO_CURDLER = register("auto_curdler", () -> new AutoCurdlerBlock(Registrar.blockProps().sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().strength(2.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_TRAP = register("rat_trap", () -> new RatTrapBlock(Registrar.blockProps().sound(SoundType.WOOD).noCollision().strength(1.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_TUBE_COLOR = BLOCKS.register("rat_tube", () -> new RatTubeBlock(Registrar.blockProps().sound(SoundType.WOOD).strength(0.9F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_UPGRADE_BLOCK = register("rat_upgrade_block", () -> new RatUpgradeBlock(Registrar.blockProps().sound(SoundType.SLIME_BLOCK).strength(0.6F, 0.0F)));
	public static final RegistryEntry<Block, Block> DYE_SPONGE = register("dye_sponge", () -> new DyeSpongeBlock(Registrar.blockProps().strength(0.6F, 0.0F).sound(SoundType.CROP)));
	public static final RegistryEntry<Block, Block> GARBAGE_PILE = register("garbage_pile", () -> new GarbageBlock(Registrar.blockProps().sound(SoundType.GRAVEL).strength(0.7F, 1.0F).randomTicks(), 1.0F));
	public static final RegistryEntry<Block, Block> CURSED_GARBAGE = register("cursed_garbage", () -> new CursedGarbageBlock(Registrar.blockCopy(GARBAGE_PILE.get())));
	public static final RegistryEntry<Block, Block> COMPRESSED_GARBAGE = register("compressed_garbage", () -> new GarbageBlock(Registrar.blockCopy(GARBAGE_PILE.get()), 2.0F));
	public static final RegistryEntry<Block, Block> PURIFIED_GARBAGE = register("purified_garbage", () -> new PurifiedGarbageBlock(Registrar.blockCopy(GARBAGE_PILE.get())));
	public static final RegistryEntry<Block, Block> PIED_GARBAGE = register("pied_garbage", () -> new PiedGarbageBlock(Registrar.blockCopy(GARBAGE_PILE.get())));
	public static final RegistryEntry<Block, Block> MARBLED_CHEESE_RAW = register("marbled_cheese_raw", () -> new Block(Registrar.blockProps().requiresCorrectToolForDrops().strength(2.0F, 10.0F).sound(SoundType.STONE)));
	public static final RegistryEntry<Block, Block> JACK_O_RATERN = register("jack_o_ratern", () -> new SetupHorizontalBlock(Registrar.blockProps().sound(SoundType.WOOD).strength(1.0F, 0).lightLevel(value -> 15)));
	public static final RegistryEntry<Block, Block> BLOCK_OF_BLUE_CHEESE = register("block_of_blue_cheese", () -> new Block(Registrar.blockProps().strength(0.6F, 0.0F).sound(SoundType.SLIME_BLOCK)));
	public static final RegistryEntry<Block, Block> BLOCK_OF_NETHER_CHEESE = register("block_of_nether_cheese", () -> new Block(Registrar.blockProps().strength(0.6F, 0.0F).sound(SoundType.SLIME_BLOCK)));
	public static final RegistryEntry<Block, Block> PIED_WOOL = register("pied_wool", () -> new Block(Registrar.blockProps().strength(1.0F, 0.0F).sound(SoundType.WOOL)));
	public static final RegistryEntry<Block, Block> UPGRADE_COMBINER = register("upgrade_combiner", () -> new UpgradeCombinerBlock(Registrar.blockProps().sound(SoundType.WOOD).requiresCorrectToolForDrops().noOcclusion().strength(5.0F, 0.0F).lightLevel(value -> 4)));
	public static final RegistryEntry<Block, Block> UPGRADE_SEPARATOR = register("upgrade_separator", () -> new UpgradeSeparatorBlock(Registrar.blockProps().sound(SoundType.WOOD).requiresCorrectToolForDrops().noOcclusion().strength(5.0F, 0.0F).lightLevel(value -> 4)));
	public static final RegistryEntry<Block, Block> MANHOLE = register("manhole", () -> new TrapDoorBlock(BlockSetType.IRON, Registrar.blockProps().mapColor(MapColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops().strength(10.0F).sound(SoundType.ANVIL).noOcclusion()));
	public static final RegistryEntry<Block, Block> TRASH_CAN = register("trash_can", () -> new TrashCanBlock(Registrar.blockProps().sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().strength(2.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_ATTRACTOR = register("rat_attractor", () -> new RatAttractorBlock(Registrar.blockProps().sound(SoundType.LANTERN).requiresCorrectToolForDrops().noOcclusion().randomTicks().strength(1.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_QUARRY = register("rat_quarry", () -> new RatQuarryBlock(Registrar.blockProps().sound(SoundType.SLIME_BLOCK).strength(2.0F, 0.0F)));
	public static final RegistryEntry<Block, Block> RAT_QUARRY_PLATFORM = register("rat_quarry_platform", () -> new RatQuarryPlatformBlock(Registrar.blockProps().sound(SoundType.SLIME_BLOCK).noOcclusion().strength(1.0F, 0.0F)));

	public static RegistryEntry<Block, Block> register(String name, Supplier<Block> blockSupplier) {
		RegistryEntry<Block, Block> ret = BLOCKS.register(name, blockSupplier);
		RatsItemRegistry.ITEMS.register(name, () -> new RatsBlockItem(ret.get(), Registrar.itemProps().useBlockDescriptionPrefix()));
		return ret;
	}
}
