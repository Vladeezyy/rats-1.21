package com.github.alexthe666.rats.server.items;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.rats.registry.RatlantisEntityRegistry;
import com.github.alexthe666.rats.registry.RatsToolMaterialRegistry;
import com.github.alexthe666.rats.server.entity.misc.RatProtector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;


public class RatlantisSwordItem extends Item {

	public RatlantisSwordItem(Item.Properties properties) {
		super(properties.sword(RatsToolMaterialRegistry.RATLANTIS, 3, -2.4F));
	}

	// NeoForge extension method (vanilla types): NeoForge calls it, Fabric does not
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (entity instanceof LivingEntity living && living.isAlive() && !(living instanceof RatProtector) && player.swingTime == 0) {
			RatProtector protector = new RatProtector(RatlantisEntityRegistry.RAT_PROTECTOR.get(), entity.level());
			protector.snapTo(player.getX(), player.getY() + 1.0D, player.getZ(), player.getYRot(), player.getXRot());
			protector.setTarget(living);
			entity.level().addFreshEntity(protector);
		}
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, display, tooltip, flag);
		tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc0").withStyle(ChatFormatting.YELLOW));
		tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
	}
}
