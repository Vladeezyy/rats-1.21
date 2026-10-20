package com.github.alexthe666.rats.client.render;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.items.PartyHatItem;
import com.github.alexthe666.rats.server.items.RatSackItem;
import com.github.alexthe666.rats.server.items.upgrades.DemonRatUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The item model properties and tints of Rats (1.21.1's item properties and item colors; the item definitions in
 * {@code assets/rats/items/*.json} use them). Each loader registers the codecs under these ids.
 */
public final class RatsItemModels {
	public static final Identifier RAT_COUNT = Identifier.fromNamespaceAndPath(RatsMod.MODID, "rat_count");
	public static final Identifier SPECIAL_DYE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "special");
	public static final Identifier SOUL = Identifier.fromNamespaceAndPath(RatsMod.MODID, "soul");
	public static final Identifier NUGGET_ORE = Identifier.fromNamespaceAndPath(RatsMod.MODID, "nugget_ore");
	public static final Identifier PARTY_HAT = Identifier.fromNamespaceAndPath(RatsMod.MODID, "party_hat");

	private RatsItemModels() {
	}

	/** Rats in a rat sack, up to 3 */
	public record RatCount() implements RangeSelectItemModelProperty {
		public static final MapCodec<RatCount> MAP_CODEC = MapCodec.unit(new RatCount());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return Math.min(3, RatSackItem.getRatsInSack(stack));
		}

		@Override
		public MapCodec<RatCount> type() {
			return MAP_CODEC;
		}
	}

	/** The pride texture of a renamed ratbow essence: the glint type's ordinal + 1, 0 for none */
	public record SpecialDye() implements RangeSelectItemModelProperty {
		public static final MapCodec<SpecialDye> MAP_CODEC = MapCodec.unit(new SpecialDye());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			if (stack.has(DataComponents.CUSTOM_NAME)) {
				RatsRenderType.GlintType type = RatsRenderType.GlintType.getGlintBasedOnKeyword(stack.getHoverName().getString());
				return type != null && type.changesItemTexture() ? type.ordinal() + 1 : 0;
			}
			return 0;
		}

		@Override
		public MapCodec<SpecialDye> type() {
			return MAP_CODEC;
		}
	}

	/** The demon upgrade's soul version */
	public record Soul() implements ConditionalItemModelProperty {
		public static final MapCodec<Soul> MAP_CODEC = MapCodec.unit(new Soul());

		@Override
		public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext display) {
			return DemonRatUpgradeItem.isSoulVersion(stack);
		}

		@Override
		public MapCodec<Soul> type() {
			return MAP_CODEC;
		}
	}

	/** An ore nugget: the average color of the ingot it smelts into */
	public record NuggetOre() implements ItemTintSource {
		public static final MapCodec<NuggetOre> MAP_CODEC = MapCodec.unit(new NuggetOre());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(NuggetColorRegister.getNuggetColor(stack));
		}

		@Override
		public MapCodec<NuggetOre> type() {
			return MAP_CODEC;
		}
	}

	/** The party hat's second layer: the inverse of its dye color (default 0x25C9E7) */
	public record PartyHatInverse() implements ItemTintSource {
		public static final MapCodec<PartyHatInverse> MAP_CODEC = MapCodec.unit(new PartyHatInverse());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			int color = stack.getItem() instanceof PartyHatItem ? PartyHatItem.getColor(stack) : 0x25C9E7;
			return ARGB.opaque(0xFFFFFF - (color & 0xFFFFFF));
		}

		@Override
		public MapCodec<PartyHatInverse> type() {
			return MAP_CODEC;
		}
	}
}
