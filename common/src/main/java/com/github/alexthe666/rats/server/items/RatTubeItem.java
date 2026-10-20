package com.github.alexthe666.rats.server.items;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.BlockItem;
import com.github.alexthe666.rats.server.misc.ItemNbt;
import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;


public class RatTubeItem extends Item {
	public final DyeColor color;

	public RatTubeItem(Item.Properties properties, DyeColor color) {
		super(properties);
		this.color = color;
	}

	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
		tooltip.accept(Component.translatable("block.rats.rat_tube.desc0").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("block.rats.rat_tube.desc1").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("block.rats.rat_tube.desc2").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		InteractionResult actionresulttype = this.tryPlace(new BlockPlaceContext(context));
		return actionresulttype != InteractionResult.SUCCESS && this.components().has(net.minecraft.core.component.DataComponents.FOOD) ? this.use(context.getLevel(), context.getPlayer(), context.getHand()) : actionresulttype;
	}

	public InteractionResult tryPlace(BlockPlaceContext context) {
		if (!context.canPlace()) {
			return InteractionResult.FAIL;
		} else {

			BlockState placedState = this.getStateForPlacement(context);
			if (placedState == null) {
				return InteractionResult.FAIL;
			} else if (!this.placeBlock(context, placedState)) {
				return InteractionResult.FAIL;
			} else {
				BlockPos pos = context.getClickedPos();
				Level level = context.getLevel();
				Player player = context.getPlayer();
				ItemStack stack = context.getItemInHand();
				BlockState currentState = level.getBlockState(pos);
				Block block = currentState.getBlock();
				if (block == placedState.getBlock()) {
					currentState = this.stateWithTag(pos, level, stack, currentState);
					this.setBlockEntityTag(level, player, pos, stack);
					block.setPlacedBy(level, pos, currentState, player, stack);
					if (player instanceof ServerPlayer) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
					}
				}

				SoundType soundtype = currentState.getSoundType();
				level.playSound(player, pos, currentState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}
	}

	@Nullable
	protected BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = RatsBlockRegistry.RAT_TUBE_COLOR.get().getStateForPlacement(context);
		return blockstate != null && this.canPlace(context, blockstate) ? blockstate : null;
	}

	private BlockState stateWithTag(BlockPos pos, Level level, ItemStack stack, BlockState state) {
		BlockState blockstate = state;
		CompoundTag compoundnbt = ItemNbt.getTag(stack);
		if (compoundnbt != null) {
			CompoundTag compoundnbt1 = compoundnbt.getCompoundOrEmpty("BlockStateTag");
			StateDefinition<Block, BlockState> statecontainer = state.getBlock().getStateDefinition();

			for (String s : compoundnbt1.keySet()) {
				Property<?> iproperty = statecontainer.getProperty(s);
				if (iproperty != null) {
					String s1 = compoundnbt1.getStringOr(s, "");
					blockstate = remapProperties(blockstate, iproperty, s1);
				}
			}
		}

		if (blockstate != state) {
			level.setBlock(pos, blockstate, 2);
		}

		return blockstate;
	}

	private static <T extends Comparable<T>> BlockState remapProperties(BlockState state, Property<T> property, String value) {
		return property.getValue(value).map(t -> state.setValue(property, t)).orElse(state);
	}

	protected boolean canPlace(BlockPlaceContext context, BlockState state) {
		return state.canSurvive(context.getLevel(), context.getClickedPos());
	}

	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		return context.getLevel().setBlock(context.getClickedPos(), state, 11);
	}

	public void setBlockEntityTag(Level level, @Nullable Player player, BlockPos pos, ItemStack stack) {
		// 1.21: the block entity data component, applied like vanilla's BlockItem
		BlockItem.updateCustomBlockEntityTag(level, player, pos, stack);
	}
}
