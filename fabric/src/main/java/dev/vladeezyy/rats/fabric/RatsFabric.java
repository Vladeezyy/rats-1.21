package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.ConfigHolder;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.RatsCreativeTabRegistry;
import com.github.alexthe666.rats.registry.RatsLootRegistry;
import com.github.alexthe666.rats.registry.RatsVillagerRegistry;
import com.github.alexthe666.rats.server.events.RatlantisPack;
import com.github.alexthe666.rats.server.events.RatsEntitySetup;
import com.github.alexthe666.rats.server.events.RatsEvents;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import dev.vladeezyy.rats.platform.PacketContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.BiConsumer;

/** Rats on Fabric: the common code wired to Fabric API (the NeoForge module does the same with NeoForge's events) */
public final class RatsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricConfig.load(ConfigHolder.SERVER_SPEC, "common");
        RatConfig.bakeServer();
        FabricMilk.register();
        net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions.register(FabricPlasticCondition.TYPE);

        RatsMod.init();
        FabricPlatform.registerAll();
        RatsMod.setup();

        RatsNetworkHandler.register(new RatsNetworkHandler.PacketRegistrar() {
            @Override
            public <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, new PacketContext() {
                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public void enqueueWork(Runnable work) {
                        context.server().execute(work);
                    }
                }));
            }
        });

        RatsEntitySetup.registerAttributes(FabricDefaultAttributeRegistry::register);
        RatsEntitySetup.registerSpawnPlacements(new RatsEntitySetup.SpawnPlacementSink() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacements.register(type, placement, heightmap, predicate);
            }
        });
        FabricBiomeModifiers.register();
        FabricLootModifiers.register();
        // NeoForge: data/neoforge/data_maps/villager_profession/raid_hero_gifts.json
        GiveGiftToHero.GIFTS.put(RatsVillagerRegistry.PET_SHOP_OWNER.get(), RatsLootRegistry.PET_SHOP_HOTV);
        for (int level = 1; level <= 5; level++) {
            int tradeLevel = level;
            TradeOfferHelper.registerVillagerOffers(RatsVillagerRegistry.PET_SHOP_OWNER.get(), level, trades -> RatsEvents.addPetShopTrades(tradeLevel, trades));
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries ->
            RatsCreativeTabRegistry.addToVanillaTabs(CreativeModeTabs.OP_BLOCKS, entries.shouldShowOpRestrictedItems(), entries::accept));
        // the built-in Ratlantis datapack (resourcepacks/ratlantis in the jar, copied there by the build)
        ResourceManagerHelper.registerBuiltinResourcePack(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "ratlantis"),
            FabricLoader.getInstance().getModContainer(RatsMod.MODID).orElseThrow(), Component.literal("Ratlantis"),
            // NeoForge (and 1.20.1) make the pack required when the option is on: always enabled
            RatConfig.ratlantisEnabledByDefault ? ResourcePackActivationType.ALWAYS_ENABLED : ResourcePackActivationType.NORMAL);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            RegistryAccessHolder.setServer(server);
            RatsMod.addPetShops(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RegistryAccessHolder.setServer(null));
        registerGameEvents();
    }

    /** The common handlers ({@link RatsEvents}) on Fabric's callbacks; the rest are mixins (dev.vladeezyy.rats.fabric.mixin) */
    private static void registerGameEvents() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            // NeoForge's EntityInteract is the plain interaction, not the one at a point (hit != null)
            if (hit == null) {
                RatsEvents.onPlayerInteractWithEntity(player, level, hand, entity);
            }
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> RatsEvents.onRightClickBlock(player, level, hand, hit) == InteractionResult.FAIL ? InteractionResult.FAIL : InteractionResult.PASS);
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> !RatsEvents.onLivingDeath(entity));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !RatsEvents.onIncomingDamage(entity, source, amount));
        ServerWorldEvents.LOAD.register((server, level) -> {
            RatsEvents.onLevelLoad(level);
            RatlantisPack.onLevelLoad(level);
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> RatlantisPack.onDatapackSync(player, player.server.getPlayerList()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RatsEvents.onPlayerLoggedIn(handler.player));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> RatsEvents.onEntityJoin(entity));
    }
}
