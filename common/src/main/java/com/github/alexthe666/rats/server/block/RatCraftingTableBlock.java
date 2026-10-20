package com.github.alexthe666.rats.server.block;
import net.minecraft.server.level.ServerLevel;

import dev.vladeezyy.rats.platform.Platform;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.InteractionResult;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RatCraftingTableBlock extends BaseEntityBlock {
    public static final MapCodec<RatCraftingTableBlock> CODEC = simpleCodec(RatCraftingTableBlock::new);

    @Override
    protected @NotNull MapCodec<? extends RatCraftingTableBlock> codec() {
        return CODEC;
    }


	public RatCraftingTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// 26.x: the block entity drops what it holds (preRemoveSideEffects); the block tells comparators
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RatCraftingTableBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, RatsBlockEntityRegistry.RAT_CRAFTING_TABLE.get(), RatCraftingTableBlockEntity::tick);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		BlockEntity be = level.getBlockEntity(pos);
		if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) && be instanceof RatCraftingTableBlockEntity table) {
			table.setCustomName(stack.getDisplayName());
		}
	}

	@Override
	protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			} else {
				BlockEntity be = level.getBlockEntity(pos);
				if (be instanceof RatCraftingTableBlockEntity table) {
					Platform.INSTANCE.openMenu((net.minecraft.server.level.ServerPlayer) player, table, buf -> buf.writeBlockPos(pos));
					player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
					return InteractionResult.CONSUME;
				}
			}
		}
		return InteractionResult.FAIL;
	}
}
