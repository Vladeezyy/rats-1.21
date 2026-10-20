package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.client.util.ClientRecipes;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.mojang.serialization.MapCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.function.Function;

/** The client side of Rats on NeoForge: the common client registrations and handlers wired to NeoForge's events */
@Mod(value = RatsMod.MODID, dist = Dist.CLIENT)
public final class RatsNeoForgeClient {
    /** Whether the entity of a living render state has the plague (read by the plague layer through the client platform) */
    static final ContextKey<Boolean> PLAGUED = new ContextKey<>(Identifier.fromNamespaceAndPath(RatsMod.MODID, "plagued"));

    public RatsNeoForgeClient(IEventBus bus) {
        RegistryAccessHolder.setClientLevel(() -> Minecraft.getInstance().level);
        bus.addListener((RegisterMenuScreensEvent event) -> RatsClientSetup.registerScreens(new RatsClientSetup.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void screen(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                event.register(type, constructor);
            }
        }));
        bus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> RatsClientSetup.registerLayerDefinitions(event::registerLayerDefinition));
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> RatsClientSetup.registerRenderers(new RatsClientSetup.RendererSink() {
            @Override
            public <E extends Entity> void entity(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                event.registerEntityRenderer(type, provider);
            }

            @Override
            public <B extends BlockEntity, S extends BlockEntityRenderState> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B, S> provider) {
                event.registerBlockEntityRenderer(type, provider);
            }
        }));
        bus.addListener((RegisterParticleProvidersEvent event) -> RatsClientSetup.registerParticles(new RatsClientSetup.ParticleSink() {
            @Override
            public <T extends ParticleOptions> void sprite(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> registration) {
                event.registerSpriteSet(type, registration::apply);
            }

            @Override
            public <T extends ParticleOptions> void special(ParticleType<T> type, ParticleProvider<T> provider) {
                event.registerSpecial(type, provider);
            }
        }));
        bus.addListener((RegisterColorHandlersEvent.BlockTintSources event) -> RatsClientSetup.registerBlockTints(event::register));
        // item model properties, tints and special renderers: each kind has its own NeoForge registration event
        bus.addListener((RegisterRangeSelectItemModelPropertyEvent event) -> RatsClientSetup.registerItemModels(itemModels(event::register, null, null, null)));
        bus.addListener((RegisterConditionalItemModelPropertyEvent event) -> RatsClientSetup.registerItemModels(itemModels(null, event::register, null, null)));
        bus.addListener((RegisterColorHandlersEvent.ItemTintSources event) -> RatsClientSetup.registerItemModels(itemModels(null, null, event::register, null)));
        bus.addListener((RegisterSpecialModelRendererEvent event) -> RatsClientSetup.registerItemModels(itemModels(null, null, null, event::register)));
        bus.addListener(RatsNeoForgeClient::attachRenderLayers);
        bus.addListener(RatsNeoForgeClient::registerRenderStateModifiers);
        bus.addListener(RatsNeoForgeClient::registerClientExtensions);
        bus.addListener((AddClientReloadListenersEvent event) ->
            event.addListener(Identifier.fromNamespaceAndPath(RatsMod.MODID, "armor_models"), (ResourceManagerReloadListener) manager -> RatsArmorModels.clear()));

        var game = NeoForge.EVENT_BUS;
        game.addListener((ViewportEvent.ComputeFov event) -> event.setFOV((float) RatsClientEvents.adjustFov(event.getFOV())));
        game.addListener((ComputeFovModifierEvent event) -> event.setNewFovModifier(RatsClientEvents.fovModifier(event.getPlayer(), event.getNewFovModifier())));
        game.addListener((ExtractBlockOutlineRenderStateEvent event) -> {
            if (RatsClientEvents.cancelBlockHighlight(event.getLevel(), event.getBlockPos())) {
                event.setCanceled(true);
            }
        });
        game.addListener((EntityTickEvent.Pre event) -> {
            if (event.getEntity().level().isClientSide()) {
                RatsClientEvents.onClientEntityTick(event.getEntity());
            }
        });
        game.addListener((ItemTooltipEvent event) -> RatsClientEvents.onTooltip(event.getItemStack(), event.getEntity(), event.getToolTip()));
        game.addListener(EventPriority.LOWEST, (RenderGuiLayerEvent.Post event) -> {
            if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
                RatsClientEvents.renderPlagueHearts(event.getGuiGraphics());
            }
        });
        game.addListener((ViewportEvent.ComputeFogColor event) -> {
            float[] color = RatsClientEvents.ratlantisFogColor(event.getCamera(), (float) event.getPartialTick());
            if (color != null) {
                event.setRed(color[0]);
                event.setGreen(color[1]);
                event.setBlue(color[2]);
            }
        });
        game.addListener((SubmitCustomGeometryEvent event) -> {
            var camera = event.getLevelRenderState().cameraRenderState;
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            RatsClientEvents.submitWorldOverlays(event.getPoseStack(), event.getSubmitNodeCollector(), camera.pos, camera.orientation, partialTick);
        });
        // 26.x clients get recipes only when the server sends them (NeoForgeGameEvents): the cauldron and JEI read these
        game.addListener((RecipesReceivedEvent event) -> ClientRecipes.received(event.getRecipeMap().values().stream().map(holder -> holder.value()).toList()));
    }

    private static RatsClientSetup.ItemModelSink itemModels(
        RangeSink range, ConditionalSink conditional, TintSink tint, SpecialSink special) {
        return new RatsClientSetup.ItemModelSink() {
            @Override
            public void rangeProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec) {
                if (range != null) range.register(id, codec);
            }

            @Override
            public void conditionalProperty(Identifier id, MapCodec<? extends ConditionalItemModelProperty> codec) {
                if (conditional != null) conditional.register(id, codec);
            }

            @Override
            public void tintSource(Identifier id, MapCodec<? extends ItemTintSource> codec) {
                if (tint != null) tint.register(id, codec);
            }

            @Override
            public void specialRenderer(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec) {
                if (special != null) special.register(id, codec);
            }
        };
    }

    private interface RangeSink { void register(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec); }
    private interface ConditionalSink { void register(Identifier id, MapCodec<? extends ConditionalItemModelProperty> codec); }
    private interface TintSink { void register(Identifier id, MapCodec<? extends ItemTintSource> codec); }
    private interface SpecialSink { void register(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec); }

    /** The plague flag and the hidden player hat (mask) go into every living render state after extraction */
    private static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {}, (entity, state) -> {
            state.setRenderData(PLAGUED, RatsClientEvents.isPlagued(entity) ? Boolean.TRUE : null);
            RatsClientEvents.afterLivingExtract(entity, state);
        });
    }

    /** Ratlantis armor brings its own model (hats are drawn by the common hat layer, they have no equipment asset) */
    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof RatlantisArmorItem) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                        return RatsArmorModels.ratlantisArmorModel(equippable != null ? equippable.slot() : EquipmentSlot.CHEST);
                    }
                }, item);
            }
        }
    }

    private static void attachRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            LivingEntityRenderer<?, ?, ?> player = event.getPlayerRenderer(skin);
            if (player != null) {
                RatsClientSetup.attachRenderLayers(player, layer -> addLayer(player, layer));
            }
            LivingEntityRenderer<?, ?, ?> mannequin = event.getMannequinRenderer(skin);
            if (mannequin != null) {
                RatsClientSetup.attachRenderLayers(mannequin, layer -> addLayer(mannequin, layer));
            }
        }
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?, ?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> living) {
                RatsClientSetup.attachRenderLayers(living, layer -> addLayer(living, layer));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayer(LivingEntityRenderer renderer, net.minecraft.client.renderer.entity.layers.RenderLayer layer) {
        renderer.addLayer(layer);
    }
}
