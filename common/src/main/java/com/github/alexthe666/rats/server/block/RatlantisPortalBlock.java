package com.github.alexthe666.rats.server.block;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.entity.InsideBlockEffectApplier;

import net.minecraft.world.level.portal.TeleportTransition;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.data.tags.RatsBlockTags;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.block.entity.RatlantisPortalBlockEntity;
import com.github.alexthe666.rats.server.world.RatlantisTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RatlantisPortalBlock extends BaseEntityBlock implements CustomItemRarity {



	public RatlantisPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public Rarity getRarity() {
		return Rarity.EPIC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			if (!entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false)) {
				if (entity.isOnPortalCooldown()) {
					entity.setPortalCooldown();
				} else {
					MinecraftServer server = entity.level().getServer();
					ResourceKey<Level> destination = entity.level().dimension() == RatlantisDimensionRegistry.DIMENSION_KEY ? Level.OVERWORLD : RatlantisDimensionRegistry.DIMENSION_KEY;
					if (server != null) {
						ServerLevel dest = server.getLevel(destination);
						if (dest != null && !entity.isPassenger()) {
							net.minecraft.util.profiling.Profiler.get().push("ratlantis_portal");
							entity.setPortalCooldown();
							TeleportTransition transition = new RatlantisTeleporter(dest).transition(entity);
							if (transition != null) {
								entity.teleport(transition);
							}
							net.minecraft.util.profiling.Profiler.get().pop();
						}
					}
				}
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
		if (!this.canSurviveAt(level, pos)) {
			level.destroyBlock(pos, true);
		}
	}

	public boolean canSurviveAt(Level level, BlockPos pos) {
		return (level.getBlockState(pos.above()).is(RatlantisBlockRegistry.RATLANTIS_PORTAL.get()) || level.getBlockState(pos.above()).is(RatsBlockTags.MARBLED_CHEESE)) &&
				(level.getBlockState(pos.below()).is(RatlantisBlockRegistry.RATLANTIS_PORTAL.get()) || level.getBlockState(pos.below()).is(RatsBlockTags.MARBLED_CHEESE));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof RatlantisPortalBlockEntity) {
				for (int j = 0; j < 2; ++j) {
					double d0 = (float) pos.getX() + random.nextFloat();
					double d1 = (float) pos.getY() + random.nextFloat();
					double d2 = (float) pos.getZ() + random.nextFloat();
					double d3 = ((double) random.nextFloat() - 0.5D) * 0.5D;
					double d4 = ((double) random.nextFloat() - 0.5D) * 0.5D;
					double d5 = ((double) random.nextFloat() - 0.5D) * 0.5D;
					level.addParticle(ParticleTypes.END_ROD, d0, d1, d2, d3, d4, d5);
				}
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RatlantisPortalBlockEntity(pos, state);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}
}
