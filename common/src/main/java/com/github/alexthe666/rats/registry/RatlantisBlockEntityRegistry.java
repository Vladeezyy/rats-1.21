package com.github.alexthe666.rats.registry;
import dev.vladeezyy.rats.platform.Platform;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.block.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.vladeezyy.rats.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.rats.platform.RegistryEntry;

public class RatlantisBlockEntityRegistry {

	public static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES = Registrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RatsMod.MODID);

	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<PiratSignBlockEntity>> PIRAT_SIGN = BLOCK_ENTITIES.register("pirat_sign", () -> Platform.INSTANCE.blockEntityType(PiratSignBlockEntity::new, RatlantisBlockRegistry.PIRAT_SIGN.get(), RatlantisBlockRegistry.PIRAT_WALL_SIGN.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<PiratHangingSignBlockEntity>> PIRAT_HANGING_SIGN = BLOCK_ENTITIES.register("pirat_hanging_sign", () -> Platform.INSTANCE.blockEntityType(PiratHangingSignBlockEntity::new, RatlantisBlockRegistry.PIRAT_HANGING_SIGN.get(), RatlantisBlockRegistry.PIRAT_WALL_HANGING_SIGN.get()));

	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatlantisPortalBlockEntity>> RATLANTIS_PORTAL = BLOCK_ENTITIES.register("ratlantis_portal", () -> Platform.INSTANCE.blockEntityType(RatlantisPortalBlockEntity::new, RatlantisBlockRegistry.RATLANTIS_PORTAL.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<DutchratBellBlockEntity>> DUTCHRAT_BELL = BLOCK_ENTITIES.register("dutchrat_bell", () -> Platform.INSTANCE.blockEntityType(DutchratBellBlockEntity::new, RatlantisBlockRegistry.DUTCHRAT_BELL.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatlanteanAutomatonHeadBlockEntity>> AUTOMATON_HEAD = BLOCK_ENTITIES.register("ratlantean_automaton_head", () -> Platform.INSTANCE.blockEntityType(RatlanteanAutomatonHeadBlockEntity::new, RatlantisBlockRegistry.MARBLED_CHEESE_RAT_HEAD.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatlantisTokenBlockEntity>> TOKEN = BLOCK_ENTITIES.register("chunky_cheese_token", () -> Platform.INSTANCE.blockEntityType(RatlantisTokenBlockEntity::new, RatlantisBlockRegistry.CHUNKY_CHEESE_TOKEN.get()));
	public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<RatlantisReactorBlockEntity>> RATLANTIS_REACTOR = BLOCK_ENTITIES.register("ratlantis_reactor", () -> Platform.INSTANCE.blockEntityType(RatlantisReactorBlockEntity::new, RatlantisBlockRegistry.RATLANTIS_REACTOR.get()));
}
