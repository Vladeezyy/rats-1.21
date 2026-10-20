package com.github.alexthe666.rats.server.items;
import net.minecraft.world.item.component.TooltipDisplay;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.world.item.Item;
import com.github.alexthe666.rats.registry.RatsToolMaterialRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RatlantisToolItem {

	public static class Pickaxe extends Item {

		public Pickaxe(Item.Properties properties) {
			super(properties.pickaxe(RatsToolMaterialRegistry.RATLANTIS, 1, -2.8F));
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc0").withStyle(ChatFormatting.YELLOW));
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
		}

		@Override
		public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
			if (state.is(BlockTags.BASE_STONE_OVERWORLD)) {
				if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
					stack.hurtAndBreak(0, living, EquipmentSlot.MAINHAND);
				}
				return true;
			} else {
				return super.mineBlock(stack, level, state, pos, living);
			}
		}


	}

	public static class Axe extends Item {
		public Axe(Item.Properties properties) {
			// 26.3: tools are plain items with tool properties (stripping comes with the axe's block transformer)
			super(properties.axe(RatsToolMaterialRegistry.RATLANTIS, 5.0F, -3.0F));
		}

		@Override
		public float getDestroySpeed(ItemStack stack, BlockState state) {
			if (state.is(BlockTags.LEAVES)) {
				return RatsToolMaterialRegistry.RATLANTIS.speed() * 1.5F;
			}
			return super.getDestroySpeed(stack, state);
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc0").withStyle(ChatFormatting.YELLOW));
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
		}

		@Override
		public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
			if (state.is(BlockTags.LEAVES)) {
				if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
					stack.hurtAndBreak(0, living, EquipmentSlot.MAINHAND);
				}
				return true;
			} else {
				return super.mineBlock(stack, level, state, pos, living);
			}
		}
	}

	public static class Shovel extends Item {
		public Shovel(Item.Properties properties) {
			super(properties.shovel(RatsToolMaterialRegistry.RATLANTIS, 1.5F, -3.0F));
		}

		@Override
		public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity living) {
			if (state.is(BlockTags.SAND)) {
				if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
					stack.hurtAndBreak(0, living, EquipmentSlot.MAINHAND);
				}
				return true;
			} else {
				return super.mineBlock(stack, level, state, pos, living);
			}
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc0").withStyle(ChatFormatting.YELLOW));
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class Hoe extends Item {
		public Hoe(Item.Properties properties) {
			super(properties.hoe(RatsToolMaterialRegistry.RATLANTIS, -7.0F, 0.0F));
		}

		/** Tills 3x3 (26.3: with the hoe's block transformer, the farmland / dirt rules are data), one damage for all */
		@Override
		public InteractionResult useOn(UseOnContext context) {
			Holder<BlockTransformer> transformer = context.getItemInHand().get(DataComponents.BLOCK_TRANSFORMER);
			if (transformer == null) {
				return InteractionResult.PASS;
			}
			boolean tilledAny = false;
			Level level = context.getLevel();
			Player player = context.getPlayer();
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos pos = context.getClickedPos().offset(x, 0, z);
					for (BlockTransformer.BlockTransformData transform : Platform.INSTANCE.blockTransforms(transformer)) {
						if (transform.disallowedFaces().contains(context.getClickedFace())) {
							continue;
						}
						BlockState tilled = transform.blockStateProvider().value().getOptionalState(level, level.getRandom(), pos);
						if (tilled == null) {
							continue;
						}
						level.playSound(player, pos, transform.sound().value(), SoundSource.BLOCKS, 1.0F, 1.0F);
						if (level instanceof ServerLevel serverLevel) {
							BlockState old = level.getBlockState(pos);
							transform.loot().ifPresent(table -> Block.dropFromBlockInteractLootTable(serverLevel, table, pos, old, level.getBlockEntity(pos), context.getItemInHand(), player,
									(sl, stack) -> Block.popResourceFromFace(sl, pos, context.getClickedFace(), stack)));
							BlockState shaped = transform.updateFromNeighbors() ? Block.updateFromNeighbourShapes(tilled, level, pos) : tilled;
							level.setBlock(pos, shaped, 11);
							level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, shaped));
							tilledAny = true;
						}
						break;
					}
				}
			}

			if (tilledAny && player != null) {
				context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
			super.appendHoverText(stack, level, display, tooltip, flag);
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc0").withStyle(ChatFormatting.YELLOW));
			tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
		}
	}
}
