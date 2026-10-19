package dev.vladeezyy.rats.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registers the entries of one registry for a mod (the common replacement of NeoForge's {@code DeferredRegister}, with
 * the same calls). The loader decides when the factories run: NeoForge on its register event, Fabric right away.
 */
public final class Registrar<R> {
    private final ResourceKey<? extends Registry<R>> registryKey;
    private final String modId;
    private final List<RegistryEntry<R, ? extends R>> entries = new ArrayList<>();

    private Registrar(ResourceKey<? extends Registry<R>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public static <R> Registrar<R> create(ResourceKey<? extends Registry<R>> registryKey, String modId) {
        return new Registrar<>(registryKey, modId);
    }

    public static <R> Registrar<R> create(Registry<R> registry, String modId) {
        return new Registrar<>(registry.key(), modId);
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
        RegistryEntry<R, T> entry = new RegistryEntry<>(this.registryKey, id);
        this.entries.add(entry);
        Platform.INSTANCE.register(this.registryKey, id, factory);
        return entry;
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Function<ResourceLocation, ? extends T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
        return this.register(name, () -> factory.apply(id));
    }

    public Collection<RegistryEntry<R, ? extends R>> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return this.registryKey;
    }
}
