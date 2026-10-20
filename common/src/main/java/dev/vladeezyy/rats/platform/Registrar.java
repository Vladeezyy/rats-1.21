package dev.vladeezyy.rats.platform;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

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
        Identifier id = Identifier.fromNamespaceAndPath(this.modId, name);
        RegistryEntry<R, T> entry = new RegistryEntry<>(this.registryKey, id);
        this.entries.add(entry);
        ResourceKey<R> key = ResourceKey.create(this.registryKey, id);
        Platform.INSTANCE.register(this.registryKey, id, () -> {
            // 26.x: blocks and items need their id in their properties (itemProps, blockProps)
            ResourceKey<?> outer = CURRENT.get();
            CURRENT.set(key);
            try {
                return factory.get();
            } finally {
                CURRENT.set(outer);
            }
        });
        return entry;
    }

    private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

    /** New item properties with the id of the item being registered */
    public static Item.Properties itemProps() {
        return new Item.Properties().setId(current(Registries.ITEM));
    }

    /** New block properties with the id of the block being registered */
    public static BlockBehaviour.Properties blockProps() {
        return BlockBehaviour.Properties.of().setId(current(Registries.BLOCK));
    }

    /** The properties of another block, with the id of the block being registered */
    public static BlockBehaviour.Properties blockCopy(BlockBehaviour block) {
        return BlockBehaviour.Properties.ofFullCopy(block).setId(current(Registries.BLOCK));
    }

    /** The properties of another block (1.20 copy rules), with the id of the block being registered */
    public static BlockBehaviour.Properties blockLegacyCopy(BlockBehaviour block) {
        return BlockBehaviour.Properties.ofLegacyCopy(block).setId(current(Registries.BLOCK));
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<T> current(ResourceKey<? extends Registry<T>> registry) {
        ResourceKey<?> key = CURRENT.get();
        if (key == null || !key.isFor(registry)) {
            throw new IllegalStateException("Properties for " + registry.identifier() + " made outside their registration (" + key + ")");
        }
        return (ResourceKey<T>) key;
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Function<Identifier, ? extends T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(this.modId, name);
        return this.register(name, () -> factory.apply(id));
    }

    public Collection<RegistryEntry<R, ? extends R>> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return this.registryKey;
    }
}
