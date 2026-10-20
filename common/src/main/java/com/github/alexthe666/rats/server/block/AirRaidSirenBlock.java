package com.github.alexthe666.rats.server.block;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.redstone.Orientation;

import dev.vladeezyy.rats.platform.Platform;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.InteractionResult;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.registry.RatlantisEntityRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import com.github.alexthe666.rats.server.entity.monster.boss.RatBaron;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class AirRaidSirenBlock extends Block implements CustomItemRarity {

	private static final VoxelShape AABB = Block.box(6, 0, 6, 10, 16, 10);

	public AirRaidSirenBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public Rarity getRarity() {
		return Rarity.RARE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		return this.spawnTheBaron(level, pos) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
		boolean flag = level.hasNeighborSignal(pos);
		if (flag) {
			this.spawnTheBaron(level, pos);
		}
	}

	private boolean spawnTheBaron(Level level, BlockPos pos) {
		if (level.isClientSide()) return true;
		if (level.getDifficulty() != Difficulty.PEACEFUL) {
			level.playSound(null, pos, RatsSoundRegistry.AIR_RAID_SIREN.get(), SoundSource.BLOCKS, 1, 1);

			if (RatConfig.summonBaronOnlyInRatlantis && !level.dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY)) {
				for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(16.0D))) {
					player.sendOverlayMessage(Component.translatable(RatsLangConstants.BARON_RATLANTIS_ONLY));
				}
				return true;
			}
			LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.MOB_SUMMONED);
			assert bolt != null;
			bolt.setPos(Vec3.atCenterOf(pos));
			bolt.setVisualOnly(true);
			level.addFreshEntity(bolt);
			level.setBlockAndUpdate(pos, Blocks.OAK_FENCE.defaultBlockState());
			RatBaron baron = new RatBaron(RatlantisEntityRegistry.RAT_BARON.get(), level);
			baron.setPos(pos.getX() + 0.5D, pos.getY() + 5D, pos.getZ() + 0.5D);
			Platform.INSTANCE.finalizeMobSpawn(baron, (ServerLevelAccessor) level, ((net.minecraft.world.level.ServerLevelAccessor) level).getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
			baron.setHomeTo(pos, 16);

			if (level instanceof net.minecraft.server.level.ServerLevel server && server.getGameRules().get(GameRules.BLOCK_DROPS)) {
				for (int i = 0; i < 2; i++) {
					RandomSource rand = level.getRandom();
					level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D + (rand.nextFloat() - 0.5D) * 3, pos.getY() - 1, pos.getZ() + 0.5D + (rand.nextFloat() - 0.5D) * 3, new ItemStack(Items.IRON_INGOT)));
				}
			}
			return level.addFreshEntity(baron);
		}
		return false;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}
}
