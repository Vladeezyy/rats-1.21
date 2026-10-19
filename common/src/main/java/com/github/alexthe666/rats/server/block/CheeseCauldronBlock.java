package com.github.alexthe666.rats.server.block;

import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
import com.github.alexthe666.rats.registry.RatsSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.vladeezyy.rats.platform.RegistryEntry;

import java.util.Map;

public class CheeseCauldronBlock extends AbstractCauldronBlock {
	// 1.21 wants a codec per block class; these blocks are only made by the mod's registry, so it gives the same block
	private final MapCodec<CheeseCauldronBlock> codec = simpleCodec(properties -> this);

	@Override
	protected MapCodec<? extends CheeseCauldronBlock> codec() {
		return this.codec;
	}


	private final RegistryEntry<Block, Block> drop;

	public CheeseCauldronBlock(BlockBehaviour.Properties properties, RegistryEntry<Block, Block> dropBlock, CauldronInteraction.InteractionMap interaction) {
		super(properties, interaction);
		this.drop = dropBlock;
	}

	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (super.useItemOn(heldStack, state, level, pos, player, hand, hit) == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
			level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
			level.playSound(null, pos, RatsSoundRegistry.CHEESE_CAULDRON_EMPTY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!player.getInventory().add(new ItemStack(this.drop.get()))) {
				player.drop(new ItemStack(this.drop.get()), false);
			}
		}
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter getter, BlockPos pos) {
		return this.getShape(state, getter, pos, CollisionContext.empty());
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		return Shapes.join(Shapes.block(), box(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);
	}

	@Override
	protected double getContentHeight(BlockState state) {
		return 0.9375D;
	}

	@Override
	public boolean isFull(BlockState state) {
		return true;
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return new ItemStack(Items.CAULDRON);
	}
}
