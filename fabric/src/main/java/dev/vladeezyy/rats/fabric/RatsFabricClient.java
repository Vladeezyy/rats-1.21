package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.ConfigHolder;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.client.model.hats.AbstractHatModel;
import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.client.util.ClientRecipes;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import com.mojang.serialization.MapCodec;
import dev.vladeezyy.rats.platform.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** The client side of Rats on Fabric: the common client registrations and handlers on Fabric API callbacks */
public final class RatsFabricClient implements ClientModInitializer {
    private static final Identifier RATLANTIS_ARMOR = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/equipment/humanoid/ratlantis.png");
    private static final Identifier RATLANTIS_LEGGINGS = Identifier.fromNamespaceAndPath(RatsMod.MODID, "textures/entity/equipment/humanoid_leggings/ratlantis.png");

    @Override
    public void onInitializeClient() {
        FabricConfig.load(ConfigHolder.CLIENT_SPEC, "client");
        RatConfig.bakeClient();
        RegistryAccessHolder.setClientLevel(() -> Minecraft.getInstance().level);
        FabricPlatform.clientSender = ClientPlayNetworking::send;

        RatsNetworkHandler.register(new RatsNetworkHandler.PacketRegistrar() {
            @Override
            public <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, new PacketContext() {
                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public void enqueueWork(Runnable work) {
                        context.client().execute(work);
                    }
                }));
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
            }
        });

        RatsClientSetup.registerItemModels(new RatsClientSetup.ItemModelSink() {
            @Override
            public void rangeProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec) {
                RangeSelectItemModelProperties.ID_MAPPER.put(id, codec);
            }

            @Override
            public void conditionalProperty(Identifier id, MapCodec<? extends ConditionalItemModelProperty> codec) {
                ConditionalItemModelProperties.ID_MAPPER.put(id, codec);
            }

            @Override
            public void tintSource(Identifier id, MapCodec<? extends ItemTintSource> codec) {
                ItemTintSources.ID_MAPPER.put(id, codec);
            }

            @Override
            public void specialRenderer(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec) {
                SpecialModelRenderers.ID_MAPPER.put(id, codec);
            }
        });
        RatsClientSetup.registerScreens(new RatsClientSetup.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void screen(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                MenuScreens.register(type, constructor);
            }
        });
        RatsClientSetup.registerLayerDefinitions((location, definition) -> ModelLayerRegistry.registerModelLayer(location, definition::get));
        RatsClientSetup.registerRenderers(new RatsClientSetup.RendererSink() {
            @Override
            public <E extends Entity> void entity(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                EntityRendererRegistry.register(type, provider);
            }

            @Override
            public <B extends BlockEntity, S extends BlockEntityRenderState> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B, S> provider) {
                BlockEntityRendererRegistry.register(type, provider);
            }
        });
        RatsClientSetup.registerParticles(new RatsClientSetup.ParticleSink() {
            @Override
            public <T extends ParticleOptions> void sprite(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> registration) {
                ParticleProviderRegistry.getInstance().register(type, sprites -> registration.apply(sprites));
            }

            @Override
            public <T extends ParticleOptions> void special(ParticleType<T> type, ParticleProvider<T> provider) {
                ParticleProviderRegistry.getInstance().register(type, provider);
            }
        });
        RatsClientSetup.registerBlockTints(BlockColorRegistry::register);
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> attachRenderLayers(renderer));
        // the milk tank of the auto curdler (NeoForge brings its own milk fluid model)
        Material milk = new Material(Identifier.fromNamespaceAndPath("forge", "block/milk_still"));
        FluidRenderingRegistry.register(FabricMilk.MILK, new FluidModel.Unbaked(milk, milk, null, null));
        registerArmorRenderers();

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(RatsMod.MODID, "armor_models"),
            (ResourceManagerReloadListener) manager -> RatsArmorModels.clear());

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> RatsClientEvents.onTooltip(stack, Minecraft.getInstance().player, lines));
        HudElementRegistry.attachElementAfter(VanillaHudElements.HEALTH_BAR, Identifier.fromNamespaceAndPath(RatsMod.MODID, "plague_hearts"),
            (graphics, delta) -> RatsClientEvents.renderPlagueHearts(graphics));
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outline) -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level == null || !(minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)
                || !RatsClientEvents.cancelBlockHighlight(minecraft.level, hit.getBlockPos());
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            var camera = context.levelState().cameraRenderState;
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            RatsClientEvents.submitWorldOverlays(context.poseStack(), context.submitNodeCollector(), camera.pos, camera.orientation, partialTick);
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && !client.isPaused()) {
                RatsClientEvents.onClientEntityTick(client.player);
            }
        });
        // 26.x clients get recipes only when the server sends them (RatsFabric): the cauldron and JEI read these
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) ->
            ClientRecipes.received(recipes.recipes().stream().map(holder -> holder.value()).toList()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void attachRenderLayers(LivingEntityRenderer<?, ?, ?> renderer) {
        RatsClientSetup.attachRenderLayers(renderer, layer -> ((LivingEntityRenderer) renderer).addLayer(layer));
    }

    /** Ratlantis armor has its own model, posed from the wearer like vanilla armor (hats: the common hat layer) */
    private static void registerArmorRenderers() {
        List<Item> armor = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof RatlantisArmorItem) {
                armor.add(item);
            }
        }
        ArmorRenderer.register((poseStack, collector, stack, state, slot, light, contextModel) -> {
            AbstractHatModel model = RatsArmorModels.ratlantisArmorModel(slot);
            Identifier texture = slot == EquipmentSlot.LEGS ? RATLANTIS_LEGGINGS : RATLANTIS_ARMOR;
            collector.submitModel(model, state, poseStack, model.renderType(texture), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
            if (stack.hasFoil()) {
                collector.order(1).submitModel(model, state, poseStack, RenderTypes.trimmedArmorGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
            }
        }, armor.toArray(Item[]::new));
    }
}
