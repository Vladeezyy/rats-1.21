package com.github.alexthe666.rats.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import com.github.alexthe666.rats.RatsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/** Tool tiers (1.21: the harvest level is the tag of blocks the tier cannot mine; the original sorted its tiers by level) */
public class RatsToolMaterialRegistry {

	public static final Tier CUTLASS = new RatsTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 5.0F, 4.5F, 20, () -> Ingredient.of(Items.IRON_INGOT));

	public static final Tier GHOST_CUTLASS = new RatsTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 5.0F, 4.5F, 20, () -> Ingredient.of(RatlantisItemRegistry.GHOST_PIRAT_ECTOPLASM.get()));

	public static final Tier BAGHNAKHS = new RatsTier(BlockTags.INCORRECT_FOR_STONE_TOOL, 500, 2.0F, 3.5F, 15, () -> Ingredient.of(RatlantisItemRegistry.FERAL_RAT_CLAW.get()));

	public static final Tier PLAGUE_SCYTHE = new RatsTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 1500, 5.0F, 6.0F, 20, () -> Ingredient.of(RatsItemRegistry.PLAGUE_ESSENCE.get()));

	public static final Tier RATLANTIS = new RatsTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3500, 9.0F, 7.0F, 20, () -> Ingredient.of(RatlantisItemRegistry.ORATCHALCUM_INGOT.get()));

	/** A tool tier (NeoForge's SimpleTier) */
	public record RatsTier(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus, int enchantmentValue, java.util.function.Supplier<Ingredient> repair) implements Tier {
		@Override
		public int getUses() {
			return this.uses;
		}

		@Override
		public float getSpeed() {
			return this.speed;
		}

		@Override
		public float getAttackDamageBonus() {
			return this.attackDamageBonus;
		}

		@Override
		public TagKey<Block> getIncorrectBlocksForDrops() {
			return this.incorrectBlocksForDrops;
		}

		@Override
		public int getEnchantmentValue() {
			return this.enchantmentValue;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return this.repair.get();
		}
	}
}
