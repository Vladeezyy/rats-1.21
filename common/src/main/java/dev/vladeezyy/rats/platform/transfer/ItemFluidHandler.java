package dev.vladeezyy.rats.platform.transfer;

import net.minecraft.world.item.ItemStack;

/** The fluid in an item; the item may change when filled or drained (a bucket), {@link #getContainer()} is the result */
public interface ItemFluidHandler extends FluidHandler {
    ItemStack getContainer();
}
