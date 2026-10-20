package dev.vladeezyy.rats.platform;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * What the common code of Rats needs from the mod loader (NeoForge or Fabric). The NeoForge implementation calls the
 * NeoForge hooks the original code called; the Fabric one does what vanilla does (or the Fabric API equivalent).
 */
public interface Platform {
    Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Rats platform for this mod loader"));

    /** "neoforge" or "fabric" */
    String name();

    Path configDir();

    boolean isModLoaded(String modId);

    /** True in the game client (also while it runs the integrated server), false on a dedicated server */
    boolean isClient();

    // --- registration ---

    /** Registers an entry (NeoForge: deferred register, Fabric: right away); see {@link Registrar} */
    <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, Identifier id, Supplier<? extends T> factory);

    /** A new registry of the mod (synced: the client gets the server's entries) */
    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key, boolean synced);

    void registerDataSerializer(Identifier id, EntityDataSerializer<?> serializer);

    /** A spawn egg for a type registered later (NeoForge: deferred spawn egg) */
    Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties);

    /** A creative tab builder (NeoForge: vanilla builder, Fabric: its own builder) */
    CreativeModeTab.Builder creativeTabBuilder();

    /** A block entity type (the vanilla builder's factory interface is not public outside NeoForge) */
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    /** A menu type whose client side reads extra data written by {@link #openMenu} */
    <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory);

    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory, RegistryFriendlyByteBuf data);
    }

    /** Opens a menu and sends the extra data its client factory reads */
    void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData);

    /** The rats spawn category (NeoForge: enum extension, Fabric: added to the enum by a mixin) */
    MobCategory ratsCategory();

    /** The green rarity of Ratlantis items */
    Rarity ratlantisRarity();

    // --- network ---

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToTracking(Entity entity, CustomPacketPayload payload);

    void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);

    void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload);

    void sendToAll(CustomPacketPayload payload);

    // --- item, fluid and energy storages of blocks, entities and items ---

    Optional<ItemHandler> itemHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side);

    Optional<ItemHandler> itemHandler(Entity entity);

    Optional<FluidHandler> fluidHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side);

    Optional<EnergyHandler> energyHandler(Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Direction side);

    Optional<EnergyHandler> energyHandler(ItemStack stack);

    /** The fluid an item holds (a bucket, a tank item) */
    Optional<ItemFluidHandler> fluidHandler(ItemStack stack);

    default Optional<FluidStack> fluidContained(ItemStack stack) {
        return this.fluidHandler(stack).map(handler -> handler.drain(Integer.MAX_VALUE, FluidHandler.FluidAction.SIMULATE)).filter(fluid -> !fluid.isEmpty());
    }

    /** A bucket filled with the fluid (an empty one for no fluid or a fluid without bucket) */
    ItemStack filledBucket(FluidStack fluid);

    /** The milk fluid (NeoForge's, enabled by the mod; on Fabric the mod's own) */
    Fluid milkFluid();

    // --- hooks of the loader the game code calls (vanilla behaviour on Fabric) ---

    @Nullable
    SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData);

    boolean canEntityGrief(Level level, @Nullable Entity entity);

    boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state);

    boolean canEntityDestroy(Level level, BlockPos pos, LivingEntity entity);

    /** True if the impact was cancelled */
    boolean onProjectileImpact(Projectile projectile, HitResult hit);

    /** True if placing was cancelled */
    boolean onBlockPlace(@Nullable Entity entity, Level level, BlockPos pos, Direction against);

    /** True if the death was cancelled */
    boolean onLivingDeath(LivingEntity entity, DamageSource source);

    void firePlayerCraftingEvent(Player player, ItemStack crafted, Container matrix);

    void setCraftingPlayer(@Nullable Player player);

    /** Fishing rods: what NeoForge's item ability FISHING_ROD_CAST says */
    boolean canCastFishingRod(ItemStack stack);

    /** Swords: what NeoForge's item ability SWORD_SWEEP says */
    boolean canSweep(ItemStack stack);

    /** Lets axes strip this block (26.3: block transformers are data; NeoForge reads the transformables data map) */
    default void registerStripping(net.minecraft.world.level.block.Block block, net.minecraft.world.level.block.Block stripped) {
    }

    /** A tool's block transforms (26.3, the hoe's tilling and the like; NeoForge adds its data map ones) */
    default Iterable<net.minecraft.core.component.BlockTransformer.BlockTransformData> blockTransforms(net.minecraft.core.Holder<net.minecraft.core.component.BlockTransformer> transformer) {
        return transformer.value().transforms();
    }

    /** Whether the entity can be sheared now (NeoForge: IShearable, Fabric: vanilla Shearable) */
    boolean isShearable(Entity entity, ItemStack shears);

    /** Shears the entity; returns the drops to spawn (Fabric: vanilla drops them itself, the list is empty) */
    java.util.List<ItemStack> shear(Entity entity, ItemStack shears);

    /** Whether the block lets the entity break it (NeoForge: Block.canEntityDestroy; vanilla: dragon / wither immunity) */
    boolean blockAllowsDestroy(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, Entity entity);


    /** Whether the item may go into the slot (NeoForge: Item.canEquip; vanilla: the item's own equipment slot) */
    boolean canEquip(ItemStack stack, net.minecraft.world.entity.EquipmentSlot slot, LivingEntity entity);

    /** The arrow a bow shoots (NeoForge: BowItem.customArrow, which other mods' bows change) */
    net.minecraft.world.entity.projectile.arrow.AbstractArrow customArrow(net.minecraft.world.item.ProjectileWeaponItem bow, net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow, ItemStack ammo, ItemStack weapon);

    /** The enchanting power of a block (NeoForge: getEnchantPowerBonus; vanilla: 1 for the enchantment power provider tag) */
    float enchantPowerBonus(BlockState state, Level level, BlockPos pos);

    /** What stays in the slot after using the stack up (NeoForge: stack-aware getCraftingRemainingItem) */
    ItemStack craftingRemainder(ItemStack stack);

    /** A server player for code that acts in the world without one (rats fishing) */
    ServerPlayer fakePlayer(ServerLevel level);

    /** Tells other mods that items were fished; true if they cancelled it */
    boolean fireItemFished(java.util.List<ItemStack> drops, net.minecraft.world.entity.projectile.FishingHook hook);

    /** Tells other mods that a menu was opened by hand (the rat's inventory) */
    void onContainerOpened(ServerPlayer player, AbstractContainerMenu menu);
}
