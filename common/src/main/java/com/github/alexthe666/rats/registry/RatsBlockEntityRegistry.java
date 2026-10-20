package com.github.alexthe666.rats.registry;
import dev.vladeezyy.rats.platform.Platform;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.block.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatsBlockEntityRegistry {

	public static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES = Registrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RatsMod.MODID);

	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatHoleBlockEntity>> RAT_HOLE = BLOCK_ENTITIES.register("rat_hole", () -> Platform.INSTANCE.blockEntityType(RatHoleBlockEntity::new, RatsBlockRegistry.RAT_HOLE.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatTrapBlockEntity>> RAT_TRAP = BLOCK_ENTITIES.register("rat_trap", () -> Platform.INSTANCE.blockEntityType(RatTrapBlockEntity::new, RatsBlockRegistry.RAT_TRAP.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<MilkCauldronBlockEntity>> MILK_CAULDRON = BLOCK_ENTITIES.register("milk_cauldron", () -> Platform.INSTANCE.blockEntityType(MilkCauldronBlockEntity::new, RatsBlockRegistry.MILK_CAULDRON.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<DecoratedRatCageBlockEntity>> RAT_CAGE_DECORATED = BLOCK_ENTITIES.register("rat_cage_decorated", () -> Platform.INSTANCE.blockEntityType(DecoratedRatCageBlockEntity::new, RatsBlockRegistry.RAT_CAGE_DECORATED.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatCageBreedingLanternBlockEntity>> RAT_CAGE_BREEDING_LANTERN = BLOCK_ENTITIES.register("rat_cage_breeding_lantern", () -> Platform.INSTANCE.blockEntityType(RatCageBreedingLanternBlockEntity::new, RatsBlockRegistry.RAT_CAGE_BREEDING_LANTERN.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatCraftingTableBlockEntity>> RAT_CRAFTING_TABLE = BLOCK_ENTITIES.register("rat_crafting_table", () -> Platform.INSTANCE.blockEntityType(RatCraftingTableBlockEntity::new, RatsBlockRegistry.RAT_CRAFTING_TABLE.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatTubeBlockEntity>> RAT_TUBE = BLOCK_ENTITIES.register("rat_tube", () -> Platform.INSTANCE.blockEntityType(RatTubeBlockEntity::new, RatsBlockRegistry.RAT_TUBE_COLOR.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<UpgradeSeparatorBlockEntity>> UPGRADE_SEPERATOR = BLOCK_ENTITIES.register("upgrade_separator", () -> Platform.INSTANCE.blockEntityType(UpgradeSeparatorBlockEntity::new, RatsBlockRegistry.UPGRADE_SEPARATOR.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<UpgradeCombinerBlockEntity>> UPGRADE_COMBINER = BLOCK_ENTITIES.register("upgrade_combiner", () -> Platform.INSTANCE.blockEntityType(UpgradeCombinerBlockEntity::new, RatsBlockRegistry.UPGRADE_COMBINER.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<AutoCurdlerBlockEntity>> AUTO_CURDLER = BLOCK_ENTITIES.register("auto_curdler", () -> Platform.INSTANCE.blockEntityType(AutoCurdlerBlockEntity::new, RatsBlockRegistry.AUTO_CURDLER.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TrashCanBlockEntity>> TRASH_CAN = BLOCK_ENTITIES.register("trash_can", () -> Platform.INSTANCE.blockEntityType(TrashCanBlockEntity::new, RatsBlockRegistry.TRASH_CAN.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatAttractorBlockEntity>> RAT_ATTRACTOR = BLOCK_ENTITIES.register("rat_attractor", () -> Platform.INSTANCE.blockEntityType(RatAttractorBlockEntity::new, RatsBlockRegistry.RAT_ATTRACTOR.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatQuarryBlockEntity>> RAT_QUARRY = BLOCK_ENTITIES.register("rat_quarry", () -> Platform.INSTANCE.blockEntityType(RatQuarryBlockEntity::new, RatsBlockRegistry.RAT_QUARRY.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatCageWheelBlockEntity>> RAT_CAGE_WHEEL = BLOCK_ENTITIES.register("rat_cage_wheel", () -> Platform.INSTANCE.blockEntityType(RatCageWheelBlockEntity::new, RatsBlockRegistry.RAT_CAGE_WHEEL.get()));
}
