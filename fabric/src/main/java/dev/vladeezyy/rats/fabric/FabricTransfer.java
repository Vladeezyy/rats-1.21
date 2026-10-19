package dev.vladeezyy.rats.fabric;

import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import dev.vladeezyy.rats.platform.transfer.ModifiableItemHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Fabric transfer API storages as the common handlers of Rats (fluids in millibuckets: 81 droplets each) */
final class FabricTransfer {
    private FabricTransfer() {}

    static final long DROPLETS_PER_MB = FluidConstants.BUCKET / FluidStack.BUCKET_VOLUME;

    static FluidStack fromFabric(FluidVariant variant, long droplets) {
        return variant.isBlank() || droplets <= 0 ? FluidStack.EMPTY : new FluidStack(variant.getFluid(), (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS_PER_MB));
    }

    /** A slotted item storage (other mods' inventories) as item slots */
    static final class SlottedItems implements ModifiableItemHandler {
        private final SlottedStorage<ItemVariant> storage;

        SlottedItems(SlottedStorage<ItemVariant> storage) {
            this.storage = storage;
        }

        @Override
        public int getSlots() {
            return this.storage.getSlotCount();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            SingleSlotStorage<ItemVariant> view = this.storage.getSlot(slot);
            return view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int) view.getAmount());
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = this.storage.getSlot(slot).insert(ItemVariant.of(stack), stack.getCount(), tx);
                if (!simulate) {
                    tx.commit();
                }
                return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - (int) inserted);
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            SingleSlotStorage<ItemVariant> view = this.storage.getSlot(slot);
            if (amount <= 0 || view.isResourceBlank()) {
                return ItemStack.EMPTY;
            }
            ItemVariant variant = view.getResource();
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = view.extract(variant, amount, tx);
                if (!simulate) {
                    tx.commit();
                }
                return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return (int) Math.min(Integer.MAX_VALUE, this.storage.getSlot(slot).getCapacity());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            try (Transaction tx = Transaction.openOuter()) {
                return this.storage.getSlot(slot).insert(ItemVariant.of(stack), 1, tx) > 0;
            }
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            try (Transaction tx = Transaction.openOuter()) {
                SingleSlotStorage<ItemVariant> view = this.storage.getSlot(slot);
                if (!view.isResourceBlank()) {
                    view.extract(view.getResource(), view.getAmount(), tx);
                }
                if (!stack.isEmpty()) {
                    view.insert(ItemVariant.of(stack), stack.getCount(), tx);
                }
                tx.commit();
            }
        }
    }

    /** A fluid storage as tanks (one tank per view) */
    static class Fluids implements FluidHandler {
        final Storage<FluidVariant> storage;

        Fluids(Storage<FluidVariant> storage) {
            this.storage = storage;
        }

        private List<StorageView<FluidVariant>> views() {
            List<StorageView<FluidVariant>> views = new ArrayList<>();
            for (StorageView<FluidVariant> view : this.storage) {
                views.add(view);
            }
            return views;
        }

        @Override
        public int getTanks() {
            return this.views().size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            List<StorageView<FluidVariant>> views = this.views();
            if (tank < 0 || tank >= views.size()) {
                return FluidStack.EMPTY;
            }
            return fromFabric(views.get(tank).getResource(), views.get(tank).getAmount());
        }

        @Override
        public int getTankCapacity(int tank) {
            List<StorageView<FluidVariant>> views = this.views();
            return tank < 0 || tank >= views.size() ? 0 : (int) Math.min(Integer.MAX_VALUE, views.get(tank).getCapacity() / DROPLETS_PER_MB);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return this.storage.supportsInsertion();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = this.storage.insert(FluidVariant.of(resource.getFluid()), resource.getAmount() * DROPLETS_PER_MB, tx);
                if (action.execute()) {
                    tx.commit();
                }
                return (int) (inserted / DROPLETS_PER_MB);
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            try (Transaction tx = Transaction.openOuter()) {
                FluidVariant variant = FluidVariant.of(resource.getFluid());
                long extracted = this.storage.extract(variant, resource.getAmount() * DROPLETS_PER_MB, tx);
                if (action.execute()) {
                    tx.commit();
                }
                return fromFabric(variant, extracted);
            }
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (StorageView<FluidVariant> view : this.storage) {
                if (!view.isResourceBlank() && view.getAmount() > 0) {
                    return this.drain(fromFabric(view.getResource(), maxDrain * DROPLETS_PER_MB), action);
                }
            }
            return FluidStack.EMPTY;
        }
    }

    /** The fluid of an item; the item in the context changes when filled or drained (a bucket) */
    static final class ItemFluids extends Fluids implements ItemFluidHandler {
        private final ContainerItemContext context;

        ItemFluids(Storage<FluidVariant> storage, ContainerItemContext context) {
            super(storage);
            this.context = context;
        }

        @Override
        public ItemStack getContainer() {
            ItemVariant variant = this.context.getItemVariant();
            return variant.isBlank() ? ItemStack.EMPTY : variant.toStack((int) this.context.getAmount());
        }
    }
}
