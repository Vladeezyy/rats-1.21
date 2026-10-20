package com.github.alexthe666.rats.server.items.upgrades;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.server.entity.RatMount;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import dev.vladeezyy.rats.platform.RegistryEntry;
import org.jetbrains.annotations.Nullable;


public class MountRatUpgradeItem<T extends Mob & RatMount> extends BaseRatUpgradeItem {

	private final RegistryEntry<EntityType<?>, EntityType<T>> entityType;

	public MountRatUpgradeItem(Item.Properties properties, int rarity, int textLength, RegistryEntry<EntityType<?>, EntityType<T>> entityType) {
		super(properties, rarity, textLength);
		this.entityType = entityType;
	}

	public EntityType<?> getEntityType() {
		return this.entityType.get();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable(RatsLangConstants.MOUNT_RESPAWN_TIMER).withStyle(ChatFormatting.GRAY));
	}
}
