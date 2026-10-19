package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.server.misc.RatsStorages;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import dev.vladeezyy.rats.platform.Platform;
import dev.vladeezyy.rats.platform.transfer.EnergyHandler;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.InvWrapper;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import dev.vladeezyy.rats.platform.transfer.SidedInvWrapper;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Rats on Fabric: Fabric API where it has an equivalent of the NeoForge API, vanilla behaviour for NeoForge's hooks.
 * Entries are queued and registered by {@link #registerAll()} in NeoForge's registry order, so the common code sees
 * the same order on both loaders (and no class-init cycles between the registry classes).
 */
public final class FabricPlatform implements Platform {
    private record Pending(ResourceLocation id, Supplier<?> factory) {}

    private static final Map<ResourceKey<?>, List<Pending>> PENDING = new LinkedHashMap<>();
    /** Sends a packet to the server; set by the client entry point (the client networking class is client-only) */
    static Consumer<CustomPacketPayload> clientSender = payload -> {
        throw new IllegalStateException("Packets to the server can only be sent from the client");
    };

    /** Registers the queued entries: attributes, data components, armor materials, then vanilla order, then modded registries */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void registerAll() {
        Set<ResourceLocation> order = new LinkedHashSet<>();
        order.add(Registries.ATTRIBUTE.location());
        order.add(Registries.DATA_COMPONENT_TYPE.location());
        order.add(Registries.ARMOR_MATERIAL.location());
        List<ResourceLocation> modded = new ArrayList<>();
        // the registry of registries in registration order (its keySet() has no order)
        for (Registry<?> each : BuiltInRegistries.REGISTRY) {
            ResourceLocation id = each.key().location();
            if (id.getNamespace().equals("minecraft")) {
                order.add(id);
            } else {
                modded.add(id);
            }
        }
        modded.sort(java.util.Comparator.comparing(ResourceLocation::getNamespace).thenComparing(ResourceLocation::getPath));
        order.addAll(modded);
        for (ResourceLocation registryId : order) {
            List<Pending> entries = PENDING.remove(ResourceKey.createRegistryKey(registryId));
            if (entries == null) {
                continue;
            }
            Registry registry = BuiltInRegistries.REGISTRY.get(registryId);
            for (Pending entry : entries) {
                Registry.register(registry, entry.id(), entry.factory().get());
            }
        }
        if (!PENDING.isEmpty()) {
            throw new IllegalStateException("Rats entries for registries that do not exist: " + PENDING.keySet());
        }
    }

    @Override
    public String name() {
        return "fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    // --- registration ---

    @Override
    public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, Supplier<? extends T> factory) {
        PENDING.computeIfAbsent(registry, key -> new ArrayList<>()).add(new Pending(id, factory));
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced) {
        FabricRegistryBuilder<T, ?> builder = FabricRegistryBuilder.createSimple(key);
        if (synced) {
            builder.attribute(RegistryAttribute.SYNCED);
        }
        return builder.buildAndRegister();
    }

    @Override
    public void registerDataSerializer(ResourceLocation id, EntityDataSerializer<?> serializer) {
        EntityDataSerializers.registerSerializer(serializer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        return new SpawnEggItem((EntityType<? extends Mob>) type.get(), background, highlight, properties);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        // the extra data travels as bytes and is read back with the client's registries
        return new ExtendedScreenHandlerType<>((int id, Inventory inventory, byte[] data) ->
            factory.create(id, inventory, new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess())), ByteBufCodecs.BYTE_ARRAY);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        extraData.accept(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        player.openMenu(new ExtendedScreenHandlerFactory<byte[]>() {
            @Override
            public byte[] getScreenOpeningData(ServerPlayer opener) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player opener) {
                return provider.createMenu(id, inventory, opener);
            }
        });
    }

    @Override
    public MobCategory ratsCategory() {
        return FabricEnumValues.ratsCategory();
    }

    @Override
    public Rarity ratlantisRarity() {
        return FabricEnumValues.ratlantisRarity();
    }

    // --- network ---

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        clientSender.accept(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        this.sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer self) {
            ServerPlayNetworking.send(self, payload);
        }
    }

    @Override
    public void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToAll(CustomPacketPayload payload) {
        if (RegistryAccessHolder.server() != null) {
            for (ServerPlayer player : PlayerLookup.all(RegistryAccessHolder.server())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    // --- storages ---

    @Override
    public Optional<ItemHandler> itemHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) {
            blockEntity = level.getBlockEntity(pos);
        }
        if (blockEntity != null) {
            ItemHandler own = RatsStorages.items(blockEntity, side);
            if (own != null) {
                return Optional.of(own);
            }
            // vanilla containers (chests, barrels, hoppers): their slots, as NeoForge's wrappers
            if (blockEntity instanceof WorldlyContainer worldly && side != null) {
                return Optional.of(new SidedInvWrapper(worldly, side));
            }
            if (blockEntity instanceof Container container) {
                return Optional.of(new InvWrapper(container));
            }
        }
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, null, blockEntity, side == null ? Direction.UP : side);
        if (storage instanceof SlottedStorage<ItemVariant> slotted) {
            return Optional.of(new FabricTransfer.SlottedItems(slotted));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemHandler> itemHandler(Entity entity) {
        return entity instanceof com.github.alexthe666.rats.server.entity.rat.InventoryRat rat ? Optional.ofNullable(rat.getItemHandler()) : Optional.empty();
    }

    @Override
    public Optional<FluidHandler> fluidHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) {
            blockEntity = level.getBlockEntity(pos);
        }
        if (blockEntity != null) {
            FluidHandler own = RatsStorages.fluids(blockEntity, side);
            if (own != null) {
                return Optional.of(own);
            }
        }
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, null, blockEntity, side == null ? Direction.UP : side);
        return storage == null ? Optional.empty() : Optional.of(new FabricTransfer.Fluids(storage));
    }

    @Override
    public Optional<EnergyHandler> energyHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) {
            blockEntity = level.getBlockEntity(pos);
        }
        // Fabric has no common energy API: only the mod's own blocks
        return blockEntity == null ? Optional.empty() : Optional.ofNullable(RatsStorages.energy(blockEntity, side));
    }

    @Override
    public Optional<EnergyHandler> energyHandler(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemFluidHandler> fluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        // a one-slot storage holding the stack: draining a bucket turns it into an empty bucket there
        net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage<ItemVariant> slot = new net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage<>() {
            @Override
            protected ItemVariant getBlankVariant() {
                return ItemVariant.blank();
            }

            @Override
            protected long getCapacity(ItemVariant variant) {
                return variant.getItem().getDefaultMaxStackSize();
            }
        };
        slot.variant = ItemVariant.of(stack);
        slot.amount = stack.getCount();
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        return storage == null ? Optional.empty() : Optional.of(new FabricTransfer.ItemFluids(storage, context));
    }

    @Override
    public ItemStack filledBucket(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return new ItemStack(Items.BUCKET);
        }
        Item bucket = fluid.getFluid().getBucket();
        return bucket == Items.AIR ? new ItemStack(Items.BUCKET) : new ItemStack(bucket);
    }

    @Override
    public Fluid milkFluid() {
        return FabricMilk.MILK;
    }

    // --- hooks: what vanilla does ---

    @Override
    @Nullable
    public SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        return mob.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    @Override
    public boolean canEntityGrief(Level level, @Nullable Entity entity) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    @Override
    public boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state) {
        return this.blockAllowsDestroy(state, entity.level(), pos, entity) && this.canEntityGrief(entity.level(), entity);
    }

    @Override
    public boolean canEntityDestroy(Level level, BlockPos pos, LivingEntity entity) {
        return level.isLoaded(pos) && this.blockAllowsDestroy(level.getBlockState(pos), level, pos, entity) && this.canEntityGrief(level, entity);
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hit) {
        return false;
    }

    @Override
    public boolean onBlockPlace(@Nullable Entity entity, Level level, BlockPos pos, Direction against) {
        return false;
    }

    @Override
    public boolean onLivingDeath(LivingEntity entity, DamageSource source) {
        return false;
    }

    @Override
    public void firePlayerCraftingEvent(Player player, ItemStack crafted, Container matrix) {
    }

    @Override
    public void setCraftingPlayer(@Nullable Player player) {
    }

    @Override
    public boolean canCastFishingRod(ItemStack stack) {
        return stack.getItem() instanceof FishingRodItem;
    }

    @Override
    public boolean canSweep(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canTill(ItemStack stack) {
        return stack.getItem() instanceof HoeItem;
    }

    @Override
    @Nullable
    public BlockState tilledState(BlockState state, UseOnContext context) {
        if (!HoeItem.TILLABLES.containsKey(state.getBlock())) {
            return null;
        }
        return state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState();
    }

    @Override
    public boolean isShearable(Entity entity, ItemStack shears) {
        return entity instanceof Shearable shearable && shearable.readyForShearing();
    }

    @Override
    public List<ItemStack> shear(Entity entity, ItemStack shears) {
        if (entity instanceof Shearable shearable) {
            shearable.shear(SoundSource.PLAYERS);
        }
        return List.of();
    }

    @Override
    public boolean blockAllowsDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        if (entity instanceof EnderDragon) {
            return !state.is(BlockTags.DRAGON_IMMUNE);
        }
        if (entity instanceof WitherBoss || entity instanceof WitherSkull) {
            return !state.is(BlockTags.WITHER_IMMUNE);
        }
        return true;
    }

    @Override
    public boolean canSetDirtUnderTree(LevelSimulatedReader reader, BiConsumer<BlockPos, BlockState> placer, RandomSource random, BlockPos pos, TreeConfiguration config) {
        return true;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == slot;
    }

    @Override
    public AbstractArrow customArrow(ProjectileWeaponItem bow, AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return arrow;
    }

    @Override
    public float enchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1.0F : 0.0F;
    }

    @Override
    public ItemStack craftingRemainder(ItemStack stack) {
        return stack.getItem().hasCraftingRemainingItem() ? new ItemStack(stack.getItem().getCraftingRemainingItem()) : ItemStack.EMPTY;
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level) {
        return FakePlayer.get(level);
    }

    @Override
    public boolean fireItemFished(List<ItemStack> drops, FishingHook hook) {
        return false;
    }

    @Override
    public void onContainerOpened(ServerPlayer player, AbstractContainerMenu menu) {
    }
}
