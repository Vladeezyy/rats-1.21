package com.github.alexthe666.rats.server.items.upgrades;

import dev.vladeezyy.rats.platform.Platform;
import net.minecraft.core.component.DataComponents;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.TickRatUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.EnchantingTableBlock;

public class EnchanterRatUpgradeItem extends BaseRatUpgradeItem implements TickRatUpgrade {
	public EnchanterRatUpgradeItem(Properties properties, int rarity, int textLength) {
		super(properties, rarity, textLength);
	}

	@Override
	public void tick(TamedRat rat) {
		if (!rat.getMainHandItem().isEmpty()) {
			this.tryEnchanting(rat, this == RatsItemRegistry.RAT_UPGRADE_DISENCHANTER.get());
			this.createFinishedParticles(rat, ParticleTypes.ENCHANT, 2, ParticleTypes.SMOKE, 0.0F);
		}
	}

	private void tryEnchanting(TamedRat rat, boolean disenchant) {
		ItemStack heldItem = rat.getMainHandItem();
		ItemStack burntItem = ItemStack.EMPTY;
		if (heldItem.getItem() == Items.BOOK && !disenchant) {
			burntItem = heldItem.copy();
		}
		if (heldItem.getItem() == Items.ENCHANTED_BOOK && disenchant) {
			burntItem = new ItemStack(Items.BOOK, heldItem.getCount());
		}
		if (heldItem.isEnchantable() && !disenchant && !heldItem.isEnchanted()) {
			burntItem = heldItem.copy();
		}
		if (disenchant && heldItem.isEnchanted()) {
			burntItem = heldItem.copy();
			burntItem.remove(DataComponents.ENCHANTMENTS);
		}
		if (burntItem.isEmpty()) {
			rat.cookingProgress = 0;
		} else {
			rat.cookingProgress++;
			if (rat.cookingProgress == 1000) {
				// the result only on the server: the client used to run this too and showed its own result in the
				// rat's paws while the server still had the input (its progress only drives the particles)
				if (!rat.level().isClientSide()) {
					heldItem.shrink(1);
					if (!disenchant) {
						float power = 0;
						for (BlockPos blockpos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
							if (EnchantingTableBlock.isValidBookShelf(rat.level(), rat.blockPosition(), blockpos)) {
								power += Platform.INSTANCE.enchantPowerBonus(rat.level().getBlockState(rat.blockPosition().offset(blockpos)), rat.level(), rat.blockPosition().offset(blockpos));
							}
						}
						burntItem = EnchantmentHelper.enchantItem(rat.getRandom(), burntItem, (int) (2.0F + rat.getRandom().nextInt(2) + power), rat.level().registryAccess(), rat.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE));
					}
					if (heldItem.isEmpty()) {
						rat.setItemInHand(InteractionHand.MAIN_HAND, burntItem);
					} else {
						if (!rat.tryDepositItemInContainers(burntItem)) {
							if (!rat.level().isClientSide()) {
								rat.spawnAtLocation((net.minecraft.server.level.ServerLevel) rat.level(), burntItem, 0.25F);
							}
						}
					}
				}
				rat.cookingProgress = 0;
			}
		}
	}
}
