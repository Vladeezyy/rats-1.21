package com.github.alexthe666.rats.server.block;
import net.minecraft.server.level.ServerLevel;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import dev.vladeezyy.rats.platform.Platform;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.message.UpdateCurdlerFluidPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("deprecation")
public class AutoCurdlerBlock extends BaseEntityBlock implements BlockTooltip {
    public static final MapCodec<AutoCurdlerBlock> CODEC = simpleCodec(AutoCurdlerBlock::new);

    @Override
    protected @NotNull MapCodec<? extends AutoCurdlerBlock> codec() {
        return CODEC;
    }


	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL);
	private static final VoxelShape AABB_BASE = Block.box(1, 0, 1, 15, 8, 15);
	private static final VoxelShape AABB_TANK = Block.box(3, 8, 3, 13, 18, 13);
	private static final VoxelShape AABB = Shapes.or(AABB_BASE, AABB_TANK);

	public AutoCurdlerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("block.rats.auto_curdler.desc0").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("block.rats.auto_curdler.desc1").withStyle(ChatFormatting.GRAY));
	}

	// 26.x: the block entity drops what it holds (preRemoveSideEffects); the block tells comparators
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isShiftKeyDown()) {
			ItemStack stack = player.getItemInHand(hand);
			if (AutoCurdlerBlockEntity.isMilk(stack) && level.getBlockEntity(pos) instanceof AutoCurdlerBlockEntity te) {
				if (!level.isClientSide() && Platform.INSTANCE.fluidHandler(stack).isPresent()) {
					FluidStack fluidStack = Platform.INSTANCE.fluidContained(stack).orElse(FluidStack.EMPTY);
					ItemFluidHandler fluidHandler = Platform.INSTANCE.fluidHandler(stack).get();
					FluidStack drain = fluidHandler.drain(Integer.MAX_VALUE, FluidHandler.FluidAction.SIMULATE);
					if (drain.getAmount() > 0 || stack.is(Items.MILK_BUCKET)) {
						if (te.getTank().fill(fluidStack.copy(), FluidHandler.FluidAction.SIMULATE) != 0) {
							int amount = te.getTank().fill(fluidStack.copy(), FluidHandler.FluidAction.EXECUTE);
							level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
							if (!player.isCreative()) {
								fluidHandler.drain(amount, FluidHandler.FluidAction.EXECUTE);
								ItemStack container = fluidHandler.getContainer();
								//support container changing tanks
								if (stack != container) {
									stack.shrink(1);
									player.getInventory().add(container);
								} else if (stack.is(Items.MILK_BUCKET)) {
									stack.shrink(1);
									player.getInventory().add(new ItemStack(Items.BUCKET));
								}
							}
							RatsNetworkHandler.toTrackingBlock(level, pos, new UpdateCurdlerFluidPacket(pos.asLong(), te.getTank().getFluid()));
						}
					}
				}
				return InteractionResult.SUCCESS;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			} else {
				player.openMenu(this.getMenuProvider(state, level, pos));
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AutoCurdlerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, RatsBlockEntityRegistry.AUTO_CURDLER.get(), AutoCurdlerBlockEntity::tick);
	}
}
