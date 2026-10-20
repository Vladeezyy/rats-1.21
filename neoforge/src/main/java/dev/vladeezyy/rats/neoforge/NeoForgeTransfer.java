package dev.vladeezyy.rats.neoforge;

import dev.vladeezyy.rats.platform.transfer.EnergyBuffer;
import dev.vladeezyy.rats.platform.transfer.EnergyHandler;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.FluidTank;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import dev.vladeezyy.rats.platform.transfer.ModifiableItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * The common item, fluid and energy handlers of Rats as NeoForge 26.x resource handlers and back (wrappers unwrap each
 * other). Rats' own handlers change at once inside a transaction and are put back from a snapshot if it aborts; other
 * mods' handlers are called in a root transaction per operation, committed unless simulating.
 */
public final class NeoForgeTransfer {
    private NeoForgeTransfer() {}

    // --- items ---

    @Nullable
    public static ResourceHandler<ItemResource> toNeo(@Nullable ItemHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof FromNeoItems wrapped ? wrapped.neo : new ToNeoItems(handler);
    }

    @Nullable
    public static ItemHandler fromNeoItems(@Nullable ResourceHandler<ItemResource> handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof ToNeoItems wrapped ? wrapped.handler : new FromNeoItems(handler);
    }

    static final class ToNeoItems extends SnapshotJournal<ItemStack[]> implements ResourceHandler<ItemResource> {
        final ItemHandler handler;

        ToNeoItems(ItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public int size() {
            return this.handler.getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(this.handler.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return this.handler.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            int limit = this.handler.getSlotLimit(index);
            return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && this.handler.isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            int inserted = amount - this.handler.insertItem(index, resource.toStack(amount), true).getCount();
            if (inserted > 0) {
                this.updateSnapshots(transaction);
                this.handler.insertItem(index, resource.toStack(inserted), false);
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!resource.matches(this.handler.getStackInSlot(index))) {
                return 0;
            }
            int extracted = this.handler.extractItem(index, amount, true).getCount();
            if (extracted > 0) {
                this.updateSnapshots(transaction);
                this.handler.extractItem(index, extracted, false);
            }
            return extracted;
        }

        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] stacks = new ItemStack[this.handler.getSlots()];
            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = this.handler.getStackInSlot(i).copy();
            }
            return stacks;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            if (this.handler instanceof ModifiableItemHandler modifiable) {
                for (int i = 0; i < snapshot.length; i++) {
                    modifiable.setStackInSlot(i, snapshot[i]);
                }
            }
        }
    }

    static final class FromNeoItems implements ModifiableItemHandler {
        final ResourceHandler<ItemResource> neo;

        FromNeoItems(ResourceHandler<ItemResource> neo) {
            this.neo = neo;
        }

        @Override
        public int getSlots() {
            return this.neo.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemUtil.getStack(this.neo, slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = this.neo.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
                if (!simulate) {
                    tx.commit();
                }
                return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemResource resource = this.neo.getResource(slot);
            if (amount <= 0 || resource.isEmpty()) {
                return ItemStack.EMPTY;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = this.neo.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), tx);
                if (!simulate) {
                    tx.commit();
                }
                return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return this.neo.getCapacityAsInt(slot, ItemResource.EMPTY);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return this.neo.isValid(slot, ItemResource.of(stack));
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            try (Transaction tx = Transaction.openRoot()) {
                ItemResource current = this.neo.getResource(slot);
                if (!current.isEmpty()) {
                    this.neo.extract(slot, current, this.neo.getAmountAsInt(slot), tx);
                }
                if (!stack.isEmpty()) {
                    this.neo.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
                }
                tx.commit();
            }
        }
    }

    // --- fluids (millibuckets on both sides) ---

    static FluidStack fromNeo(FluidResource resource, int amount) {
        return resource.isEmpty() || amount <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), amount);
    }

    /** Rats' fluids carry no components: a resource with any is not theirs */
    static boolean isPlain(FluidResource resource) {
        return resource.equals(FluidResource.of(resource.getFluid()));
    }

    @Nullable
    public static ResourceHandler<FluidResource> toNeo(@Nullable FluidHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof FromNeoFluids wrapped ? wrapped.neo : new ToNeoFluids(handler);
    }

    @Nullable
    public static FluidHandler fromNeoFluids(@Nullable ResourceHandler<FluidResource> handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof ToNeoFluids wrapped ? wrapped.handler : new FromNeoFluids(handler);
    }

    /** The fluid of a stack, as a one-slot inventory: filling or draining swaps the item there (a bucket) */
    @Nullable
    public static ItemFluidHandler fromNeo(@Nullable ResourceHandler<FluidResource> handler, ItemStacksResourceHandler slot) {
        return handler == null ? null : new FromNeoItemFluids(handler, slot);
    }

    static final class ToNeoFluids extends SnapshotJournal<FluidStack[]> implements ResourceHandler<FluidResource> {
        final FluidHandler handler;

        ToNeoFluids(FluidHandler handler) {
            this.handler = handler;
        }

        @Override
        public int size() {
            return this.handler.getTanks();
        }

        @Override
        public FluidResource getResource(int index) {
            FluidStack stack = this.handler.getFluidInTank(index);
            return stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack.getFluid());
        }

        @Override
        public long getAmountAsLong(int index) {
            return this.handler.getFluidInTank(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return this.handler.getTankCapacity(index);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return !resource.isEmpty() && isPlain(resource) && this.handler.isFluidValid(index, new FluidStack(resource.getFluid(), FluidStack.BUCKET_VOLUME));
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (amount == 0 || !isPlain(resource)) {
                return 0;
            }
            FluidStack stack = new FluidStack(resource.getFluid(), amount);
            int filled = this.handler.fill(stack, FluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                this.updateSnapshots(transaction);
                this.handler.fill(stack.copyWithAmount(filled), FluidHandler.FluidAction.EXECUTE);
            }
            return filled;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            FluidStack inTank = this.handler.getFluidInTank(index);
            if (amount == 0 || inTank.isEmpty() || !inTank.is(resource.getFluid()) || !isPlain(resource)) {
                return 0;
            }
            FluidStack stack = new FluidStack(resource.getFluid(), amount);
            int drained = this.handler.drain(stack, FluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained > 0) {
                this.updateSnapshots(transaction);
                this.handler.drain(stack.copyWithAmount(drained), FluidHandler.FluidAction.EXECUTE);
            }
            return drained;
        }

        @Override
        protected FluidStack[] createSnapshot() {
            FluidStack[] tanks = new FluidStack[this.handler.getTanks()];
            for (int i = 0; i < tanks.length; i++) {
                tanks[i] = this.handler.getFluidInTank(i).copy();
            }
            return tanks;
        }

        @Override
        protected void revertToSnapshot(FluidStack[] snapshot) {
            if (this.handler instanceof FluidTank tank && snapshot.length == 1) {
                tank.setFluid(snapshot[0]);
            }
        }
    }

    static class FromNeoFluids implements FluidHandler {
        final ResourceHandler<FluidResource> neo;

        FromNeoFluids(ResourceHandler<FluidResource> neo) {
            this.neo = neo;
        }

        @Override
        public int getTanks() {
            return this.neo.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return fromNeo(this.neo.getResource(tank), this.neo.getAmountAsInt(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return this.neo.getCapacityAsInt(tank, FluidResource.EMPTY);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return !stack.isEmpty() && this.neo.isValid(tank, FluidResource.of(stack.getFluid()));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int filled = this.neo.insert(FluidResource.of(resource.getFluid()), resource.getAmount(), tx);
                if (action.execute()) {
                    tx.commit();
                }
                return filled;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidResource fluid = FluidResource.of(resource.getFluid());
            try (Transaction tx = Transaction.openRoot()) {
                int drained = this.neo.extract(fluid, resource.getAmount(), tx);
                if (action.execute()) {
                    tx.commit();
                }
                return fromNeo(fluid, drained);
            }
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (int i = 0; i < this.neo.size(); i++) {
                FluidResource resource = this.neo.getResource(i);
                if (!resource.isEmpty() && this.neo.getAmountAsLong(i) > 0) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int drained = this.neo.extract(resource, maxDrain, tx);
                        if (action.execute()) {
                            tx.commit();
                        }
                        return fromNeo(resource, drained);
                    }
                }
            }
            return FluidStack.EMPTY;
        }
    }

    static final class FromNeoItemFluids extends FromNeoFluids implements ItemFluidHandler {
        private final ItemStacksResourceHandler slot;

        FromNeoItemFluids(ResourceHandler<FluidResource> neo, ItemStacksResourceHandler slot) {
            super(neo);
            this.slot = slot;
        }

        @Override
        public ItemStack getContainer() {
            return ItemUtil.getStack(this.slot, 0);
        }
    }

    // --- energy ---

    @Nullable
    public static net.neoforged.neoforge.transfer.energy.EnergyHandler toNeo(@Nullable EnergyHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof FromNeoEnergy wrapped ? wrapped.neo() : new ToNeoEnergy(handler);
    }

    @Nullable
    public static EnergyHandler fromNeo(@Nullable net.neoforged.neoforge.transfer.energy.EnergyHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof ToNeoEnergy wrapped ? wrapped.handler : new FromNeoEnergy(handler);
    }

    static final class ToNeoEnergy extends SnapshotJournal<Integer> implements net.neoforged.neoforge.transfer.energy.EnergyHandler {
        final EnergyHandler handler;

        ToNeoEnergy(EnergyHandler handler) {
            this.handler = handler;
        }

        @Override
        public long getAmountAsLong() {
            return this.handler.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return this.handler.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int received = this.handler.receiveEnergy(amount, true);
            if (received > 0) {
                this.updateSnapshots(transaction);
                this.handler.receiveEnergy(received, false);
            }
            return received;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int extracted = this.handler.extractEnergy(amount, true);
            if (extracted > 0) {
                this.updateSnapshots(transaction);
                this.handler.extractEnergy(extracted, false);
            }
            return extracted;
        }

        @Override
        protected Integer createSnapshot() {
            return this.handler.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            if (this.handler instanceof EnergyBuffer buffer) {
                buffer.setEnergyStored(snapshot);
            }
        }
    }

    record FromNeoEnergy(net.neoforged.neoforge.transfer.energy.EnergyHandler neo) implements EnergyHandler {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (toReceive <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int received = this.neo.insert(toReceive, tx);
                if (!simulate) {
                    tx.commit();
                }
                return received;
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            if (toExtract <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = this.neo.extract(toExtract, tx);
                if (!simulate) {
                    tx.commit();
                }
                return extracted;
            }
        }

        @Override public int getEnergyStored() { return this.neo.getAmountAsInt(); }
        @Override public int getMaxEnergyStored() { return this.neo.getCapacityAsInt(); }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }
}
