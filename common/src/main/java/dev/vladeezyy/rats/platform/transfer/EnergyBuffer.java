package dev.vladeezyy.rats.platform.transfer;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

/** A simple energy store with transfer limits (NeoForge's {@code EnergyStorage}); saves as an int tag like it */
public class EnergyBuffer implements EnergyHandler {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public EnergyBuffer(int capacity, int maxReceive, int maxExtract, int energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!this.canReceive() || toReceive <= 0) {
            return 0;
        }
        int received = Math.min(this.capacity - this.energy, Math.min(this.maxReceive, toReceive));
        if (!simulate) {
            this.energy += received;
        }
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!this.canExtract() || toExtract <= 0) {
            return 0;
        }
        int extracted = Math.min(this.energy, Math.min(this.maxExtract, toExtract));
        if (!simulate) {
            this.energy -= extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    public Tag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        return this.serializeNBT();
    }

    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, Tag tag) {
        this.deserializeNBT(tag);
    }

    public Tag serializeNBT() {
        return IntTag.valueOf(this.energy);
    }

    public void deserializeNBT(Tag tag) {
        if (tag instanceof IntTag intTag) {
            this.energy = intTag.getAsInt();
        }
    }
}
