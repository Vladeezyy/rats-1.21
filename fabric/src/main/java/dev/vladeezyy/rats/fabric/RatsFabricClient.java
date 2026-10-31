package dev.vladeezyy.rats.fabric;

import com.github.alexthe666.rats.ConfigHolder;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.events.RatsClientEvents;
import com.github.alexthe666.rats.client.events.RatsClientSetup;
import com.github.alexthe666.rats.client.render.RatsArmorModels;
import com.github.alexthe666.rats.client.render.RatsBEWLR;
import com.github.alexthe666.rats.server.items.HatItem;
import com.github.alexthe666.rats.server.items.RatlantisArmorItem;
import com.github.alexthe666.rats.server.items.RatsBlockItem;
import com.github.alexthe666.rats.server.message.RatsNetworkHandler;
import com.github.alexthe666.rats.server.misc.RegistryAccessHolder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rats.platform.ClientPlatform;
import dev.vladeezyy.rats.platform.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiConsumer;

/** The client side of Rats on Fabric: the common client registrations and handlers on Fabric API */
public final class RatsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        renderLayers();
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
                        // Fabric runs the handler on the client thread already: execute() would queue the work after the
                        // packets received next (a rat's screen opened after its contents arrived and showed it empty)
                        if (context.client().isSameThread()) {
                            work.run();
                        } else {
                            context.client().execute(work);
                        }
                    }
                }));
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
            }
        });

        CoreShaderRegistrationCallback.EVENT.register(context ->
            context.register(RatsClientSetup.RATLANTIS_PORTAL_SHADER, DefaultVertexFormat.POSITION_COLOR, RatsClientSetup::setRendertypeRatlantisPortalShader));
        RatsClientSetup.registerItemProperties(ItemProperties::register);
        RatsClientSetup.registerScreens(new RatsClientSetup.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void screen(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                MenuScreens.register(type, constructor);
            }
        });
        RatsClientSetup.registerLayerDefinitions((location, definition) -> EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        RatsClientSetup.registerRenderers(new RatsClientSetup.RendererSink() {
            @Override
            public <E extends Entity> void entity(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                EntityRendererRegistry.register(type, provider);
            }

            @Override
            public <B extends BlockEntity> void blockEntity(BlockEntityType<? extends B> type, BlockEntityRendererProvider<B> provider) {
                BlockEntityRenderers.register(type, provider);
            }
        });
        RatsClientSetup.registerParticles(new RatsClientSetup.ParticleSink() {
            @Override
            public <T extends ParticleOptions> void sprite(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
                ParticleFactoryRegistry.getInstance().register(type, registration::create);
            }

            @Override
            public <T extends ParticleOptions> void special(ParticleType<T> type, ParticleProvider<T> provider) {
                ParticleFactoryRegistry.getInstance().register(type, provider);
            }
        });
        RatsClientSetup.registerBlockColors(ColorProviderRegistry.BLOCK::register);
        RatsClientSetup.registerItemColors(ColorProviderRegistry.ITEM::register);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> RatsClientSetup.attachRenderLayers((LivingEntityRenderer) renderer));
        FluidRenderHandlerRegistry.INSTANCE.register(FabricMilk.MILK, new SimpleFluidRenderHandler(
            ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), ResourceLocation.fromNamespaceAndPath("forge", "block/milk_flowing")));
        registerItemRenderers();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "armor_models");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                RatsArmorModels.clear();
            }
        });

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> RatsClientEvents.onTooltip(stack, Minecraft.getInstance().player, lines));
        HudRenderCallback.EVENT.register((graphics, delta) -> RatsClientEvents.renderPlagueHearts(graphics));
        WorldRenderEvents.BLOCK_OUTLINE.register((world, outline) -> !RatsClientEvents.cancelBlockHighlight(world.world(), outline.blockPos()));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(context.positionMatrix());
            RatsClientEvents.renderSelectedRatMarkers(stack, context.tickCounter().getGameTimeDeltaPartialTick(false));
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && !client.isPaused()) {
                RatsClientEvents.onClientEntityTick(client.player);
            }
        });
    }

    /** Hats and Ratlantis armor drawn with their own models, block items with special rendering through the BEWLR */
    private static void registerItemRenderers() {
        RatsBEWLR bewlr = new RatsBEWLR();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HatItem || item instanceof RatlantisArmorItem) {
                ArmorRenderer.register((poseStack, buffers, stack, entity, slot, light, contextModel) -> {
                    Model model = ClientPlatform.INSTANCE.armorModel(entity, stack, slot, contextModel);
                    if (model instanceof HumanoidModel<?> humanoid) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        HumanoidModel<net.minecraft.world.entity.LivingEntity> target = (HumanoidModel) humanoid;
                        contextModel.copyPropertiesTo(target);
                        target.setAllVisible(false);
                        switch (slot) {
                            case HEAD -> {
                                target.head.visible = true;
                                target.hat.visible = true;
                            }
                            case CHEST -> {
                                target.body.visible = true;
                                target.rightArm.visible = true;
                                target.leftArm.visible = true;
                            }
                            case LEGS -> {
                                target.body.visible = true;
                                target.rightLeg.visible = true;
                                target.leftLeg.visible = true;
                            }
                            case FEET -> {
                                target.rightLeg.visible = true;
                                target.leftLeg.visible = true;
                            }
                            default -> {
                            }
                        }
                        if (stack.getItem() instanceof ArmorItem armor) {
                            var layer = armor.getMaterial().value().layers().getFirst();
                            ArmorRenderer.renderPart(poseStack, buffers, light, stack, target, ClientPlatform.INSTANCE.armorTexture(entity, stack, layer, slot == EquipmentSlot.LEGS, slot));
                        }
                    }
                }, item);
            } else if (item instanceof RatsBlockItem) {
                BuiltinItemRendererRegistry.INSTANCE.register(item, bewlr::renderByItem);
            }
        }
    }

    /**
     * The "render_type" of the mod's block models: NeoForge reads it, Fabric does not (Fabric drew every cutout and
     * translucent block solid: the rat attractor's chains with white holes, the tubes opaque). Every block whose
     * blockstate models (or their parents) ask for a layer is put in that layer.
     */
    private static void renderLayers() {
        var mod = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(RatsMod.MODID).orElseThrow();
        for (net.minecraft.world.level.block.Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals(RatsMod.MODID)) {
                continue;
            }
            var states = readJson(mod.findPath("assets/" + RatsMod.MODID + "/blockstates/" + id.getPath() + ".json"));
            if (states.isEmpty()) {
                continue;
            }
            java.util.List<String> models = new java.util.ArrayList<>();
            collectModels(states.get(), models);
            for (String model : models) {
                String type = renderType(mod, net.minecraft.resources.ResourceLocation.parse(model), 0);
                net.minecraft.client.renderer.RenderType layer = type == null ? null : switch (type) {
                    case "minecraft:cutout", "cutout" -> net.minecraft.client.renderer.RenderType.cutout();
                    case "minecraft:cutout_mipped", "cutout_mipped" -> net.minecraft.client.renderer.RenderType.cutoutMipped();
                    case "minecraft:translucent", "translucent" -> net.minecraft.client.renderer.RenderType.translucent();
                    case "minecraft:tripwire", "tripwire" -> net.minecraft.client.renderer.RenderType.tripwire();
                    default -> null;
                };
                if (layer != null) {
                    net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(block, layer);
                    break;
                }
            }
        }
    }

    private static void collectModels(com.google.gson.JsonElement element, java.util.List<String> models) {
        if (element.isJsonObject()) {
            com.google.gson.JsonObject object = element.getAsJsonObject();
            if (object.has("model") && object.get("model").isJsonPrimitive()) {
                models.add(object.get("model").getAsString());
            }
            object.entrySet().forEach(e -> collectModels(e.getValue(), models));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> collectModels(e, models));
        }
    }

    /** The model's render_type, or its parent's (the mod's models only) */
    private static String renderType(net.fabricmc.loader.api.ModContainer mod, net.minecraft.resources.ResourceLocation model, int depth) {
        if (!model.getNamespace().equals(RatsMod.MODID) || depth > 8) {
            return null;
        }
        var json = readJson(mod.findPath("assets/" + RatsMod.MODID + "/models/" + model.getPath() + ".json"));
        if (json.isEmpty()) {
            return null;
        }
        if (json.get().has("render_type")) {
            return json.get().get("render_type").getAsString();
        }
        return json.get().has("parent") ? renderType(mod, net.minecraft.resources.ResourceLocation.parse(json.get().get("parent").getAsString()), depth + 1) : null;
    }

    private static java.util.Optional<com.google.gson.JsonObject> readJson(java.util.Optional<java.nio.file.Path> path) {
        if (path.isEmpty() || !java.nio.file.Files.exists(path.get())) {
            return java.util.Optional.empty();
        }
        try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(path.get())) {
            return java.util.Optional.of(com.google.gson.JsonParser.parseReader(reader).getAsJsonObject());
        } catch (java.io.IOException | RuntimeException e) {
            RatsMod.LOGGER.warn("Could not read {}", path.get(), e);
            return java.util.Optional.empty();
        }
    }
}
