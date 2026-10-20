package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.RatsEnums;
import com.github.alexthe666.rats.RatsMod;
import dev.vladeezyy.rats.platform.Platform;
import dev.vladeezyy.rats.platform.transfer.EnergyHandler;
import dev.vladeezyy.rats.platform.transfer.FluidHandler;
import dev.vladeezyy.rats.platform.transfer.FluidStack;
import dev.vladeezyy.rats.platform.transfer.ItemFluidHandler;
import dev.vladeezyy.rats.platform.transfer.ItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Rats on NeoForge: the NeoForge APIs and hooks the original code called */
public final class NeoForgePlatform implements Platform {
    /** Deferred registers per registry, filled by the common code before {@link #registerAll} */
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();
    private static final List<Registry<?>> NEW_REGISTRIES = new ArrayList<>();

    static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
        modBus.addListener((NewRegistryEvent event) -> NEW_REGISTRIES.forEach(event::register));
    }

    @Override
    public String name() {
        return "neoforge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    // --- registration ---

    @SuppressWarnings("unchecked")
    private static <R> DeferredRegister<R> registerFor(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry, key -> DeferredRegister.create((ResourceKey<? extends Registry<R>>) key, RatsMod.MODID));
    }

    @Override
    public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, Identifier id, Supplier<? extends T> factory) {
        registerFor(registry).register(id.getPath(), factory);
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced) {
        Registry<T> registry = new RegistryBuilder<>(key).sync(synced).create();
        NEW_REGISTRIES.add(registry);
        return registry;
    }

    @Override
    public void registerDataSerializer(Identifier id, EntityDataSerializer<?> serializer) {
        registerFor(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS).register(id.getPath(), () -> serializer);
    }

    @Override
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        // 26.x: eggs have their own textures, entity types are registered before items
        return new SpawnEggItem(properties.spawnEgg(type.get()));
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::apply, blocks);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return IMenuTypeExtension.create(factory::create);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        player.openMenu(provider, extraData);
    }

    @Override
    public MobCategory ratsCategory() {
        return RatsEnums.RATS_CATEGORY.getValue();
    }

    @Override
    public Rarity ratlantisRarity() {
        return RatsEnums.RATLANTIS_SPECIAL.getValue();
    }

    // --- network ---

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, ChunkPos.containing(pos), payload);
    }

    @Override
    public void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    // --- storages ---

    @Override
    public Optional<ItemHandler> itemHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        BlockState state = blockEntity != null ? blockEntity.getBlockState() : level.getBlockState(pos);
        return Optional.ofNullable(NeoForgeTransfer.fromNeoItems(level.getCapability(Capabilities.Item.BLOCK, pos, state, blockEntity, side)));
    }

    @Override
    public Optional<ItemHandler> itemHandler(Entity entity) {
        return Optional.ofNullable(NeoForgeTransfer.fromNeoItems(entity.getCapability(Capabilities.Item.ENTITY, null)));
    }

    @Override
    public Optional<FluidHandler> fluidHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        BlockState state = blockEntity != null ? blockEntity.getBlockState() : level.getBlockState(pos);
        return Optional.ofNullable(NeoForgeTransfer.fromNeoFluids(level.getCapability(Capabilities.Fluid.BLOCK, pos, state, blockEntity, side)));
    }

    @Override
    public Optional<EnergyHandler> energyHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        BlockState state = blockEntity != null ? blockEntity.getBlockState() : level.getBlockState(pos);
        return Optional.ofNullable(NeoForgeTransfer.fromNeo(level.getCapability(Capabilities.Energy.BLOCK, pos, state, blockEntity, side)));
    }

    @Override
    public Optional<EnergyHandler> energyHandler(ItemStack stack) {
        return Optional.ofNullable(NeoForgeTransfer.fromNeo(ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM)));
    }

    @Override
    public Optional<ItemFluidHandler> fluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        // a one-slot inventory: filling or draining swaps the item there, read back with getContainer()
        ItemStacksResourceHandler slot = new ItemStacksResourceHandler(1);
        slot.set(0, ItemResource.of(stack), stack.getCount());
        return Optional.ofNullable(NeoForgeTransfer.fromNeo(ItemAccess.forHandlerIndex(slot, 0).getCapability(Capabilities.Fluid.ITEM), slot));
    }

    @Override
    public ItemStack filledBucket(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (fluid.is(Fluids.WATER)) {
            return new ItemStack(Items.WATER_BUCKET);
        } else if (fluid.is(Fluids.LAVA)) {
            return new ItemStack(Items.LAVA_BUCKET);
        }
        return fluid.getFluid().getFluidType().getBucket(new net.neoforged.neoforge.fluids.FluidStack(fluid.getFluid(), fluid.getAmount()));
    }

    @Override
    public Fluid milkFluid() {
        return NeoForgeMod.MILK.get();
    }

    // --- hooks ---

    @Override
    @Nullable
    public SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {
        return EventHooks.finalizeMobSpawn(mob, level, difficulty, spawnType, spawnData);
    }

    @Override
    public boolean canEntityGrief(Level level, @Nullable Entity entity) {
        return level instanceof ServerLevel server && EventHooks.canEntityGrief(server, entity);
    }

    @Override
    public boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state) {
        return EventHooks.onEntityDestroyBlock(entity, pos, state);
    }

    @Override
    public boolean canEntityDestroy(Level level, BlockPos pos, LivingEntity entity) {
        return level instanceof ServerLevel server && CommonHooks.canEntityDestroy(server, pos, entity);
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hit) {
        return EventHooks.onProjectileImpact(projectile, hit);
    }

    @Override
    public boolean onBlockPlace(@Nullable Entity entity, Level level, BlockPos pos, Direction against) {
        return EventHooks.onBlockPlace(entity, BlockSnapshot.create(level.dimension(), level, pos), against);
    }

    @Override
    public boolean onLivingDeath(LivingEntity entity, DamageSource source) {
        return CommonHooks.onLivingDeath(entity, source);
    }

    @Override
    public void firePlayerCraftingEvent(Player player, ItemStack crafted, Container matrix) {
        EventHooks.firePlayerCraftingEvent(player, crafted, matrix);
    }

    @Override
    public void setCraftingPlayer(@Nullable Player player) {
        CommonHooks.setCraftingPlayer(player);
    }

    @Override
    public boolean canCastFishingRod(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST);
    }

    @Override
    public boolean canSweep(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SWORD_SWEEP);
    }

    @Override
    public boolean canTill(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.HOE_TILL);
    }

    @Override
    @Nullable
    public BlockState tilledState(BlockState state, UseOnContext context) {
        return state.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
    }

    @Override
    public boolean isShearable(Entity entity, ItemStack shears) {
        return entity instanceof IShearable shearable && shearable.isShearable(null, shears, entity.level(), entity.blockPosition());
    }

    @Override
    public List<ItemStack> shear(Entity entity, ItemStack shears) {
        return entity instanceof IShearable shearable ? shearable.onSheared(null, shears, entity.level(), entity.blockPosition()) : List.of();
    }

    @Override
    public boolean blockAllowsDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return state.canEntityDestroy(level, pos, entity);
    }

    @Override
    public boolean canSetDirtUnderTree(LevelSimulatedReader reader, BiConsumer<BlockPos, BlockState> placer, RandomSource random, BlockPos pos, TreeConfiguration config) {
        return !(reader instanceof WorldGenLevel level) || !level.getBlockState(pos).onTreeGrow(level, placer, random, pos, config);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return stack.canEquip(slot, entity);
    }

    @Override
    public AbstractArrow customArrow(ProjectileWeaponItem bow, AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return bow.customArrow(arrow, ammo, weapon);
    }

    @Override
    public float enchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.getEnchantPowerBonus(level, pos);
    }

    @Override
    public ItemStack craftingRemainder(ItemStack stack) {
        ItemStackTemplate remainder = stack.getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level) {
        return FakePlayerFactory.getMinecraft(level);
    }

    @Override
    public boolean fireItemFished(List<ItemStack> drops, FishingHook hook) {
        return NeoForge.EVENT_BUS.post(new ItemFishedEvent(drops, 1, hook)).isCanceled();
    }

    @Override
    public void onContainerOpened(ServerPlayer player, AbstractContainerMenu menu) {
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));
    }
}
