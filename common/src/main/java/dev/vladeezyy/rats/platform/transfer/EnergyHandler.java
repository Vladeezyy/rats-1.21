package dev.vladeezyy.rats.platform.transfer;

/** Stored energy (the methods of NeoForge 1.21.1's {@code IEnergyStorage}); Fabric has no common energy API */
public interface EnergyHandler {
    int receiveEnergy(int toReceive, boolean simulate);

    int extractEnergy(int toExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
