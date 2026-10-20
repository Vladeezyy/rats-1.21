package com.github.alexthe666.rats.server.block;
import net.minecraft.server.level.ServerLevel;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
public class RatQuarryBlock extends BaseEntityBlock implements BlockTooltip {



	public RatQuarryBlock(BlockBehaviour.Properties properties) {
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
		return new RatQuarryBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, RatsBlockEntityRegistry.RAT_QUARRY.get(), RatQuarryBlockEntity::tick);
	}

	@Override
	protected @NotNull InteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			} else {
				player.openMenu(state.getMenuProvider(level, pos));
				return InteractionResult.CONSUME;
			}

		}
		return InteractionResult.FAIL;
	}

	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
		tooltip.accept(Component.translatable("block.rats.rat_quarry.desc0").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("block.rats.rat_quarry.desc1").withStyle(ChatFormatting.GRAY));
	}
}
