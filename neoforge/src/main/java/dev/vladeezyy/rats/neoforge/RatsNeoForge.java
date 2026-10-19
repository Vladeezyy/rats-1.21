package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.ConfigHolder;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatsBlockEntityRegistry;
import com.github.alexthe666.rats.registry.RatsCreativeTabRegistry;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.events.RatlantisPack;
import com.github.alexthe666.rats.server.events.RatsEntitySetup;
import com.github.alexthe666.rats.server.loot.GenericAddItemLootModifier;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RatsStorages;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import com.mojang.serialization.MapCodec;
import dev.vladeezyy.rats.platform.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** Rats on NeoForge: the mod-bus wiring of the common code (the original's @Mod class and mod-bus subscribers) */
@Mod(RatsMod.MODID)
public final class RatsNeoForge {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RatsMod.MODID);
    private static ModConfigSpec clientSpec;
    private static ModConfigSpec serverSpec;

    static {
        LOOT_MODIFIERS.register("add_item", () -> GenericAddItemLootModifier.CODEC);
    }

    public RatsNeoForge(IEventBus bus, ModContainer container) {
        clientSpec = NeoForgeConfig.build(ConfigHolder.CLIENT_SPEC);
        serverSpec = NeoForgeConfig.build(ConfigHolder.SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, clientSpec);
        container.registerConfig(ModConfig.Type.COMMON, serverSpec);
        //melk
        NeoForgeMod.enableMilkFluid();

        RatsMod.init();
        NeoForgePlatform.registerAll(bus);
        LOOT_MODIFIERS.register(bus);

        bus.addListener(RatsNeoForge::reloadConfigs);
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(RatsMod::setup));
        bus.addListener(RatsNeoForge::registerPackets);
        bus.addListener(RatsNeoForge::registerCapabilities);
        bus.addListener((EntityAttributeCreationEvent event) -> RatsEntitySetup.registerAttributes(event::put));
        bus.addListener(RatsNeoForge::registerSpawnPlacements);
        bus.addListener(RatsNeoForge::addRatlantisDatapack);
        bus.addListener((BuildCreativeModeTabContentsEvent event) ->
            RatsCreativeTabRegistry.addToVanillaTabs(event.getTabKey(), event.hasPermissions(), stack -> event.accept(stack)));

        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> {
            // registries for item data read without a level (1.20.1 combined upgrades)
            RegistryAccessHolder.setServer(event.getServer());
            RatsMod.addPetShops(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> RegistryAccessHolder.setServer(null));
        NeoForgeGameEvents.register();
    }

    private static void reloadConfigs(ModConfigEvent event) {
        if (event.getConfig().getSpec() == serverSpec) {
            RatConfig.bakeServer();
            RatsMod.LOGGER.debug("Reloading Rats Server Config!");
        }
        if (event.getConfig().getSpec() == clientSpec) {
            RatConfig.bakeClient();
            RatsMod.LOGGER.debug("Reloading Rats Client Config!");
        }
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        RatsNetworkHandler.register(new RatsNetworkHandler.PacketRegistrar() {
            @Override
            public <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                registrar.playToClient(type, codec, (payload, context) -> handler.accept(payload, context(context)));
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                registrar.playToServer(type, codec, (payload, context) -> handler.accept(payload, context(context)));
            }
        });
    }

    private static PacketContext context(IPayloadContext context) {
        return new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public void enqueueWork(Runnable work) {
                context.enqueueWork(work);
            }
        };
    }

    /** Item, fluid and energy handlers of the mod's blocks and of tamed rats (the 1.20.1 original overrode getCapability) */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Supplier<? extends BlockEntityType<?>> type : new Supplier[]{RatsBlockEntityRegistry.UPGRADE_COMBINER, RatsBlockEntityRegistry.RAT_QUARRY, RatsBlockEntityRegistry.AUTO_CURDLER, RatsBlockEntityRegistry.RAT_CRAFTING_TABLE}) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, (BlockEntityType<BlockEntity>) type.get(), (be, side) -> NeoForgeTransfer.toNeo(RatsStorages.items(be, side)));
        }
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, (BlockEntityType<BlockEntity>) (BlockEntityType<?>) RatsBlockEntityRegistry.AUTO_CURDLER.get(), (be, side) -> NeoForgeTransfer.toNeo(RatsStorages.fluids(be, side)));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, (BlockEntityType<BlockEntity>) (BlockEntityType<?>) RatsBlockEntityRegistry.RAT_CAGE_WHEEL.get(), (be, side) -> NeoForgeTransfer.toNeo(RatsStorages.energy(be, side)));
        event.registerEntity(Capabilities.ItemHandler.ENTITY, RatsEntityRegistry.TAMED_RAT.get(), (TamedRat rat, Void context) -> NeoForgeTransfer.toNeo(rat.getItemHandler()));
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        RatsEntitySetup.registerSpawnPlacements(new RatsEntitySetup.SpawnPlacementSink() {
            @Override
            public <T extends net.minecraft.world.entity.Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacementType placement, net.minecraft.world.level.levelgen.Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }

    //despite being a builtin datapack, this is still necessary because without it the Ratlantis pack doesn't show up. Whatever.
    private static void addRatlantisDatapack(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            var resourcePath = ModList.get().getModFileById(RatsMod.MODID).getFile().findResource("data", "minecraft", "datapacks", "ratlantis");
            var info = new PackLocationInfo(RatlantisPack.PACK_ID, Component.literal("Ratlantis"), PackSource.FEATURE, Optional.empty());
            var pack = Pack.readMetaAndCreate(info, new PathPackResources.PathResourcesSupplier(resourcePath), PackType.SERVER_DATA,
                new PackSelectionConfig(RatConfig.ratlantisEnabledByDefault, Pack.Position.TOP, false));
            if (pack != null) {
                event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
            }
        }
    }
}
