package dev.vladeezyy.rats.neoforge;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.client.render.RatsBEWLR;
import com.github.alexthe666.rats.server.items.HatItem;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import com.github.alexthe666.rats.server.items.RatsBlockItem;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/** The client side of Rats on NeoForge: the common client registrations and handlers wired to NeoForge's events */
@Mod(value = RatsMod.MODID, dist = Dist.CLIENT)
public final class RatsNeoForgeClient {
    private static Field addLayersRenderers;

    public RatsNeoForgeClient(IEventBus bus) {
        RegistryAccessHolder.setClientLevel(() -> Minecraft.getInstance().level);
        bus.addListener((RegisterShadersEvent event) -> {
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(), RatsClientSetup.RATLANTIS_PORTAL_SHADER, DefaultVertexFormat.POSITION_COLOR), RatsClientSetup::setRendertypeRatlantisPortalShader);
            } catch (IOException e) {
                throw new RuntimeException("Could not load the Ratlantis portal shader", e);
            }
        });
        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> RatsClientSetup.registerItemProperties(ItemProperties::register)));
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
            public <B extends BlockEntity> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B> provider) {
                event.registerBlockEntityRenderer(type, provider);
            }
        }));
        bus.addListener((RegisterParticleProvidersEvent event) -> RatsClientSetup.registerParticles(new RatsClientSetup.ParticleSink() {
            @Override
            public <T extends ParticleOptions> void sprite(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
                event.registerSpriteSet(type, registration);
            }

            @Override
            public <T extends ParticleOptions> void special(ParticleType<T> type, ParticleProvider<T> provider) {
                event.registerSpecial(type, provider);
            }
        }));
        bus.addListener((RegisterColorHandlersEvent.Block event) -> RatsClientSetup.registerBlockColors(event::register));
        bus.addListener((RegisterColorHandlersEvent.Item event) -> RatsClientSetup.registerItemColors(event::register));
        bus.addListener(RatsNeoForgeClient::attachRenderLayers);
        bus.addListener(RatsNeoForgeClient::registerClientExtensions);
        bus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener) manager -> RatsArmorModels.clear()));

        var game = NeoForge.EVENT_BUS;
        game.addListener((ViewportEvent.ComputeFov event) -> event.setFOV(RatsClientEvents.adjustFov(event.getFOV())));
        game.addListener((ComputeFovModifierEvent event) -> event.setNewFovModifier(RatsClientEvents.fovModifier(event.getPlayer(), event.getNewFovModifier())));
        game.addListener((RenderHighlightEvent.Block event) -> {
            if (RatsClientEvents.cancelBlockHighlight(event.getCamera().getEntity().level(), event.getTarget().getBlockPos())) {
                event.setCanceled(true);
            }
        });
        game.addListener((EntityTickEvent.Pre event) -> {
            if (event.getEntity().level().isClientSide()) {
                RatsClientEvents.onClientEntityTick(event.getEntity());
            }
        });
        game.addListener((RenderLivingEvent.Pre<?, ?> event) -> RatsClientEvents.beforeLivingRender(event.getEntity(), event.getRenderer().getModel()));
        game.addListener((RenderLivingEvent.Post<?, ?> event) -> RatsClientEvents.afterLivingRender(event.getEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick()));
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
        game.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                RatsClientEvents.renderSelectedRatMarkers(event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
            }
        });
    }

    /** Hats and Ratlantis armor bring their models, block items with special rendering their item renderer */
    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        BlockEntityWithoutLevelRenderer bewlr = new RatsBEWLR();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HatItem) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                        HumanoidModel<?> model = RatsArmorModels.hatModel(stack.getItem());
                        return model != null ? model : original;
                    }
                }, item);
            } else if (item instanceof RatlantisArmorItem) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                        return RatsArmorModels.ratlantisArmorModel(slot);
                    }
                }, item);
            } else if (item instanceof RatsBlockItem) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return bewlr;
                    }
                }, item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void attachRenderLayers(EntityRenderersEvent.AddLayers event) {
        if (addLayersRenderers == null) {
            try {
                addLayersRenderers = EntityRenderersEvent.AddLayers.class.getDeclaredField("renderers");
                addLayersRenderers.setAccessible(true);
            } catch (NoSuchFieldException e) {
                RatsMod.LOGGER.error("Could not reach the entity renderers to add the plague layers", e);
                return;
            }
        }
        event.getSkins().forEach(skin -> {
            LivingEntityRenderer<Player, EntityModel<Player>> renderer = event.getSkin(skin);
            RatsClientSetup.attachRenderLayers(Objects.requireNonNull(renderer));
        });
        try {
            ((Map<EntityType<?>, EntityRenderer<?>>) addLayersRenderers.get(event)).values().stream()
                .filter(LivingEntityRenderer.class::isInstance)
                .map(LivingEntityRenderer.class::cast)
                .forEach(RatsClientSetup::attachRenderLayers);
        } catch (IllegalAccessException e) {
            RatsMod.LOGGER.error("Could not reach the entity renderers to add the plague layers", e);
        }
    }
}
