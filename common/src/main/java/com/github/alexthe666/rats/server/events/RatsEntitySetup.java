package com.github.alexthe666.rats.server.events;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatlantisEntityRegistry;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.misc.PiratBoat;
import com.github.alexthe666.rats.server.entity.misc.PlagueDoctor;
import com.github.alexthe666.rats.server.entity.misc.RatProtector;
import com.github.alexthe666.rats.server.entity.misc.Ratfish;
import com.github.alexthe666.rats.server.entity.monster.*;
import com.github.alexthe666.rats.server.entity.monster.boss.*;
import com.github.alexthe666.rats.server.entity.mount.*;
import com.github.alexthe666.rats.server.entity.rat.DemonRat;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.levelgen.Heightmap;

/** The attributes and spawn rules of the mod's mobs; each loader registers them its way */
public final class RatsEntitySetup {

	private RatsEntitySetup() {
	}

	public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> put) {
		put.accept(RatsEntityRegistry.RAT.get(), Rat.createAttributes().build());
		put.accept(RatsEntityRegistry.TAMED_RAT.get(), TamedRat.createAttributes().build());
		put.accept(RatsEntityRegistry.PIED_PIPER.get(), PiedPiper.createAttributes().build());
		put.accept(RatsEntityRegistry.PLAGUE_DOCTOR.get(), PlagueDoctor.createAttributes().build());
		put.accept(RatsEntityRegistry.BLACK_DEATH.get(), BlackDeath.createAttributes().build());
		put.accept(RatsEntityRegistry.PLAGUE_CLOUD.get(), PlagueCloud.createAttributes().build());
		put.accept(RatsEntityRegistry.PLAGUE_BEAST.get(), PlagueBeast.createAttributes().build());
		put.accept(RatsEntityRegistry.RAT_MOUNT_CHICKEN.get(), RatChickenMount.createAttributes().build());
		put.accept(RatsEntityRegistry.RAT_MOUNT_BEAST.get(), RatBeastMount.createAttributes().build());
		put.accept(RatsEntityRegistry.RAT_KING.get(), RatKing.createAttributes().build());
		put.accept(RatsEntityRegistry.DEMON_RAT.get(), DemonRat.createAttributes().build());
		put.accept(RatsEntityRegistry.RAT_STRIDER_MOUNT.get(), RatStriderMount.createAttributes().build());
		put.accept(RatsEntityRegistry.RAT_MOUNT_GOLEM.get(), RatGolemMount.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RATLANTEAN_SPIRIT.get(), RatlanteanSpirit.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RATLANTEAN_AUTOMATON.get(), RatlanteanAutomaton.createAttributes().build());
		put.accept(RatlantisEntityRegistry.FERAL_RATLANTEAN.get(), FeralRatlantean.createAttributes().build());
		put.accept(RatlantisEntityRegistry.NEO_RATLANTEAN.get(), NeoRatlantean.createAttributes().build());
		put.accept(RatlantisEntityRegistry.PIRAT.get(), Pirat.createAttributes().build());
		put.accept(RatlantisEntityRegistry.PIRAT_BOAT.get(), PiratBoat.createAttributes().build());
		put.accept(RatlantisEntityRegistry.GHOST_PIRAT.get(), GhostPirat.createAttributes().build());
		put.accept(RatlantisEntityRegistry.DUTCHRAT.get(), Dutchrat.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RATFISH.get(), Ratfish.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RATLANTEAN_RATBOT.get(), RatlanteanRatbot.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RAT_MOUNT_AUTOMATON.get(), RatAutomatonMount.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RAT_BARON.get(), RatBaron.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RAT_BARON_PLANE.get(), RatBaronPlane.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RAT_MOUNT_BIPLANE.get(), RatBiplaneMount.createAttributes().build());
		put.accept(RatlantisEntityRegistry.RAT_PROTECTOR.get(), RatProtector.createAttributes().build());
	}

	/** Receives the spawn rules (NeoForge: RegisterSpawnPlacementsEvent with REPLACE, Fabric: SpawnPlacements) */
	public interface SpawnPlacementSink {
		<T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, net.minecraft.world.level.levelgen.Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate);
	}

	public static void registerSpawnPlacements(SpawnPlacementSink sink) {
		sink.register(RatsEntityRegistry.RAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Rat::checkRatSpawnRules);
		sink.register(RatsEntityRegistry.PIED_PIPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiedPiper::checkPiperSpawnRules);
		sink.register(RatsEntityRegistry.DEMON_RAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DemonRat::canDemonRatSpawnOn);
		sink.register(RatlantisEntityRegistry.FERAL_RATLANTEAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FeralRatlantean::checkRatlanteanSpawnRules);
		sink.register(RatlantisEntityRegistry.RATLANTEAN_SPIRIT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RatlanteanSpirit::checkSpiritSpawnRules);
		sink.register(RatlantisEntityRegistry.PIRAT.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pirat::checkPiratSpawnRules);
		sink.register(RatlantisEntityRegistry.GHOST_PIRAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GhostPirat::checkGhostPiratSpawnRules);
		sink.register(RatlantisEntityRegistry.RATFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractFish::checkSurfaceWaterAnimalSpawnRules);
		sink.register(RatlantisEntityRegistry.RATLANTEAN_RATBOT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RatlanteanRatbot::checkBotSpawnRule);
	}
}
