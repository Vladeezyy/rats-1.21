package dev.vladeezyy.rats.platform.transfer;

import net.minecraft.world.item.ItemStack;

/** An {@link ItemHandler} whose slots can be set directly (NeoForge's {@code IItemHandlerModifiable}) */
public interface ModifiableItemHandler extends ItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}
