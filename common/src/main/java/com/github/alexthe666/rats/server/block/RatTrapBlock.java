package com.github.alexthe666.rats.server.block;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.Containers;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.InteractionResult;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import com.github.alexthe666.rats.server.block.entity.RatTrapBlockEntity;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RatTrapBlock extends BaseEntityBlock {
    public static final MapCodec<RatTrapBlock> CODEC = simpleCodec(RatTrapBlock::new);

    @Override
    protected @NotNull MapCodec<? extends RatTrapBlock> codec() {
        return CODEC;
    }

	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL);
	public static final BooleanProperty SHUT = BooleanProperty.create("shut");
	private static final VoxelShape NS_AABB = Block.box(4, 0, 1, 12, 2, 15);
	private static final VoxelShape EW_AABB = Block.box(1, 0, 4, 15, 2, 12);

	public RatTrapBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(SHUT, false));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NS_AABB : EW_AABB;
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader accessor, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos newPos, BlockState newState, RandomSource random) {
		return !state.canSurvive(accessor, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, accessor, ticks, pos, direction, newPos, newState, random);
	}

	// 26.x: the block entity drops what it holds (preRemoveSideEffects); the block tells comparators
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
		if (level.hasNeighborSignal(pos)) {
			if (state.getValue(SHUT)) {
				level.setBlockAndUpdate(pos, state.setValue(SHUT, false));
				level.sendBlockUpdated(pos, state, state, 3);
				level.playSound(null, pos, RatsSoundRegistry.RAT_TRAP_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
		return reader.getBlockState(pos.below()).isFaceSturdy(reader, pos.below(), Direction.UP);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, SHUT);
	}

	@Override
	protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		ItemStack itemstack = player.getItemInHand(hand);
		BlockEntity be = level.getBlockEntity(pos);
		if (state.getValue(SHUT)) {
			level.setBlockAndUpdate(pos, state.setValue(SHUT, false));
			level.playSound(null, pos, RatsSoundRegistry.RAT_TRAP_OPEN.get(), SoundSource.BLOCKS, 1F, 1F);
			return InteractionResult.SUCCESS;
		}
		if (be instanceof RatTrapBlockEntity ratTrap) {
			if (ratTrap.getBait().isEmpty() && RatUtils.isRatFood(itemstack)) {
				ratTrap.setBaitStack(itemstack.copy());
				level.sendBlockUpdated(pos, state, state, 3);
				itemstack.setCount(0);
				level.playSound(null, pos, RatsSoundRegistry.RAT_TRAP_ADD_BAIT.get(), SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
				return InteractionResult.SUCCESS;
			}
			if (!ratTrap.getBait().isEmpty() && !state.getValue(SHUT) && player.isShiftKeyDown()) {
				if (!level.isClientSide()) {
					level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, ratTrap.getBait()));
				}
				ratTrap.setBaitStack(ItemStack.EMPTY);
				level.sendBlockUpdated(pos, state, state, 3);
				level.playSound(null, pos, RatsSoundRegistry.RAT_TRAP_REMOVE_BAIT.get(), SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
				return InteractionResult.SUCCESS;

			}
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return state.getValue(SHUT) ? 15 : 0;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RatTrapBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, RatsBlockEntityRegistry.RAT_TRAP.get(), RatTrapBlockEntity::tick);
	}
}
