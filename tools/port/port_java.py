"""Mechanical Forge 1.20.1 -> NeoForge 1.21.1 replacements over src/main/java (idempotent).

Only rewrites what has a one-to-one replacement: imports, renamed classes, ResourceLocation constructors, registry
names. Everything else is ported by hand. Usage: python -I tools/port_java.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java')

IMPORTS = {
    'net.minecraftforge.registries.ForgeRegistries': 'net.minecraft.core.registries.BuiltInRegistries',
    'net.minecraftforge.fml.LogicalSide': 'net.neoforged.fml.LogicalSide',
    'net.minecraftforge.registries.RegistryObject': 'net.neoforged.neoforge.registries.DeferredHolder',
    'net.minecraftforge.common.MinecraftForge': 'net.neoforged.neoforge.common.NeoForge',
    'net.minecraftforge.fml.common.Mod': 'net.neoforged.fml.common.Mod',
    'net.minecraftforge.registries.DeferredRegister': 'net.neoforged.neoforge.registries.DeferredRegister',
    'net.minecraftforge.eventbus.api.SubscribeEvent': 'net.neoforged.bus.api.SubscribeEvent',
    'net.minecraftforge.eventbus.api.EventPriority': 'net.neoforged.bus.api.EventPriority',
    'net.minecraftforge.eventbus.api.Event': 'net.neoforged.bus.api.Event',
    'net.minecraftforge.eventbus.api.IEventBus': 'net.neoforged.bus.api.IEventBus',
    'net.minecraftforge.eventbus.api.Cancelable': 'net.neoforged.bus.api.ICancellableEvent',
    'net.minecraftforge.client.extensions.common.IClientItemExtensions': 'net.neoforged.neoforge.client.extensions.common.IClientItemExtensions',
    'net.minecraftforge.client.extensions.common.IClientBlockExtensions': 'net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions',
    'net.minecraftforge.common.Tags': 'net.neoforged.neoforge.common.Tags',
    'net.minecraftforge.common.capabilities.ForgeCapabilities': 'net.neoforged.neoforge.capabilities.Capabilities',
    'net.minecraftforge.api.distmarker.Dist': 'net.neoforged.api.distmarker.Dist',
    'net.minecraftforge.api.distmarker.OnlyIn': 'net.neoforged.api.distmarker.OnlyIn',
    'net.minecraftforge.common.util.NonNullLazy': 'net.neoforged.neoforge.common.util.Lazy',
    'net.minecraftforge.common.world.ModifiableBiomeInfo': 'net.neoforged.neoforge.common.world.ModifiableBiomeInfo',
    'net.minecraftforge.common.world.BiomeModifier': 'net.neoforged.neoforge.common.world.BiomeModifier',
    'net.minecraftforge.items.IItemHandler': 'net.neoforged.neoforge.items.IItemHandler',
    'net.minecraftforge.items.IItemHandlerModifiable': 'net.neoforged.neoforge.items.IItemHandlerModifiable',
    'net.minecraftforge.items.wrapper.SidedInvWrapper': 'net.neoforged.neoforge.items.wrapper.SidedInvWrapper',
    'net.minecraftforge.items.wrapper.InvWrapper': 'net.neoforged.neoforge.items.wrapper.InvWrapper',
    'net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent': 'net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent',
    'net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent': 'net.neoforged.fml.event.lifecycle.FMLClientSetupEvent',
    'net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent': 'net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent',
    'net.minecraftforge.fml.config.ModConfig': 'net.neoforged.fml.config.ModConfig',
    'net.minecraftforge.fml.event.config.ModConfigEvent': 'net.neoforged.fml.event.config.ModConfigEvent',
    'net.minecraftforge.event.ForgeEventFactory': 'net.neoforged.neoforge.event.EventHooks',
    'net.minecraftforge.common.ToolActions': 'net.neoforged.neoforge.common.ItemAbilities',
    'net.minecraftforge.common.ToolAction': 'net.neoforged.neoforge.common.ItemAbility',
    'net.minecraftforge.common.ForgeConfigSpec': 'net.neoforged.neoforge.common.ModConfigSpec',
    'net.minecraftforge.registries.NewRegistryEvent': 'net.neoforged.neoforge.registries.NewRegistryEvent',
    'net.minecraftforge.network.PacketDistributor': 'net.neoforged.neoforge.network.PacketDistributor',
    'net.minecraftforge.server.ServerLifecycleHooks': 'net.neoforged.neoforge.server.ServerLifecycleHooks',
    'net.minecraftforge.fml.loading.FMLEnvironment': 'net.neoforged.fml.loading.FMLEnvironment',
    'net.minecraftforge.fml.ModLoadingContext': 'net.neoforged.fml.ModLoadingContext',
    'net.minecraftforge.fml.ModList': 'net.neoforged.fml.ModList',
    'net.minecraftforge.fml.ModContainer': 'net.neoforged.fml.ModContainer',
    'net.minecraftforge.fluids.FluidType': 'net.neoforged.neoforge.fluids.FluidType',
    'net.minecraftforge.entity.IEntityAdditionalSpawnData': 'net.neoforged.neoforge.entity.IEntityWithComplexSpawn',
    'net.minecraftforge.common.ForgeTier': 'net.neoforged.neoforge.common.SimpleTier',
    'net.minecraftforge.common.ForgeSpawnEggItem': 'net.neoforged.neoforge.common.DeferredSpawnEggItem',
    'net.minecraftforge.client.ForgeHooksClient': 'net.neoforged.neoforge.client.ClientHooks',
    'net.minecraftforge.common.ForgeHooks': 'net.neoforged.neoforge.common.CommonHooks',
}
# Fallback package moves, longest first
PACKAGES = [
    ('net.minecraftforge.eventbus.api.', 'net.neoforged.bus.api.'),
    ('net.minecraftforge.api.distmarker.', 'net.neoforged.api.distmarker.'),
    ('net.minecraftforge.fml.', 'net.neoforged.fml.'),
    ('net.minecraftforge.', 'net.neoforged.neoforge.'),
]
# Imports with no counterpart: drop them, the code using them is ported by hand
DROP = {
    'net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext',
    'net.minecraftforge.network.NetworkDirection',
    'net.minecraftforge.network.NetworkRegistry',
    'net.minecraftforge.network.simple.SimpleChannel',
    'net.minecraftforge.registries.tags.ITagManager',
    'net.minecraftforge.fml.DistExecutor',
}
REGISTRIES = {
    'ITEMS': 'ITEM', 'BLOCKS': 'BLOCK', 'ENTITY_TYPES': 'ENTITY_TYPE', 'BLOCK_ENTITY_TYPES': 'BLOCK_ENTITY_TYPE',
    'SOUND_EVENTS': 'SOUND_EVENT', 'MOB_EFFECTS': 'MOB_EFFECT', 'POTIONS': 'POTION', 'PARTICLE_TYPES': 'PARTICLE_TYPE',
    'MENU_TYPES': 'MENU', 'RECIPE_SERIALIZERS': 'RECIPE_SERIALIZER', 'RECIPE_TYPES': 'RECIPE_TYPE',
    'FEATURES': 'FEATURE', 'ATTRIBUTES': 'ATTRIBUTE', 'VILLAGER_PROFESSIONS': 'VILLAGER_PROFESSION',
    'POI_TYPES': 'POINT_OF_INTEREST_TYPE', 'FLUIDS': 'FLUID', 'PAINTING_VARIANTS': 'PAINTING_VARIANT',
    'STAT_TYPES': 'STAT_TYPE', 'CHUNK_STATUS': 'CHUNK_STATUS', 'BIOME_SOURCES': 'BIOME_SOURCE',
}


def replace_resource_locations(text):
    out = []
    i = 0
    token = 'new ResourceLocation('
    while True:
        j = text.find(token, i)
        if j < 0:
            out.append(text[i:])
            break
        out.append(text[i:j])
        k = j + len(token)
        depth, commas, start = 1, [], k
        in_str = None
        while depth:
            c = text[k]
            if in_str:
                if c == '\\':
                    k += 1
                elif c == in_str:
                    in_str = None
            elif c in '"\'':
                in_str = c
            elif c in '([{':
                depth += 1
            elif c in ')]}':
                depth -= 1
            elif c == ',' and depth == 1:
                commas.append(k)
            k += 1
        args = text[start:k - 1]
        if commas:
            out.append('ResourceLocation.fromNamespaceAndPath(' + replace_resource_locations(args) + ')')
        else:
            out.append('ResourceLocation.parse(' + replace_resource_locations(args) + ')')
        i = k
    return ''.join(out)


def port(text):
    def fix_import(m):
        static, name, end = m.group(1) or "", m.group(2), m.group(3)
        if name in DROP:
            return ''
        if name in IMPORTS:
            return 'import ' + static + IMPORTS[name] + end
        for old, new in PACKAGES:
            if name.startswith(old):
                return 'import ' + static + new + name[len(old):] + end
        return m.group(0)

    def fix_import_line(m):
        line = fix_import(m)
        return line + m.group(4) if line else ''

    text = re.sub(r'import (static )?(net\.minecraftforge\.[A-Za-z0-9_.]*?)(\.\*;|;)(\r?\n)', fix_import_line, text)
    text = re.sub(r'\bForgeRegistries\.Keys\.', 'Registries.', text)
    for old, new in REGISTRIES.items():
        text = re.sub(r'\bForgeRegistries\.' + old + r'\b', 'BuiltInRegistries.' + new, text)
    text = re.sub(r'(BuiltInRegistries\.\w+)\.getValue\(', r'\1.get(', text)
    text = re.sub(r'(BuiltInRegistries\.\w+)\.getValues\(\)', r'\1.stream().toList()', text)
    text = re.sub(r'\bMinecraftForge\.EVENT_BUS\b', 'NeoForge.EVENT_BUS', text)
    text = re.sub(r'@Mod\.EventBusSubscriber\b', '@EventBusSubscriber', text)
    text = re.sub(r'\bMod\.EventBusSubscriber\.Bus\b', 'EventBusSubscriber.Bus', text)
    text = re.sub(r'\bForgeConfigSpec\b', 'ModConfigSpec', text)
    text = re.sub(r'\bForgeEventFactory\b', 'EventHooks', text)
    text = re.sub(r'\bToolActions\b', 'ItemAbilities', text)
    text = re.sub(r'\bToolAction\b', 'ItemAbility', text)
    text = re.sub(r'\bForgeCapabilities\b', 'Capabilities', text)
    text = re.sub(r'\bForgeHooksClient\b', 'ClientHooks', text)
    text = re.sub(r'\bForgeHooks\b', 'CommonHooks', text)
    text = re.sub(r'\bForgeTier\b', 'SimpleTier', text)
    text = re.sub(r'\bForgeSpawnEggItem\b', 'DeferredSpawnEggItem', text)
    text = re.sub(r'\bNonNullLazy\b', 'Lazy', text)
    text = re.sub(r'\bIEntityAdditionalSpawnData\b', 'IEntityWithComplexSpawn', text)
    text = re.sub(r'\bRegistryObject\b', 'DeferredHolder', text)
    text = replace_resource_locations(text)
    # Citadel's model and animation classes live in Rampart (our library, NeoForge + Fabric, every version)
    text = text.replace('com.github.alexthe666.citadel.server.message.PacketBufferUtils', 'net.minecraft.network.FriendlyByteBuf')
    text = text.replace('com.github.alexthe666.citadel.', 'dev.vladeezyy.rampart.')
    if '@EventBusSubscriber' in text and 'import net.neoforged.fml.common.EventBusSubscriber;' not in text:
        text = re.sub(r'(\npackage [^;]+;\r?\n)', r'\1\nimport net.neoforged.fml.common.EventBusSubscriber;', text, 1)
    if 'BuiltInRegistries.' in text and 'import net.minecraft.core.registries.BuiltInRegistries;' not in text:
        text = re.sub(r'(\npackage [^;]+;\r?\n)', r'\1\nimport net.minecraft.core.registries.BuiltInRegistries;', text, 1)
    if 'Registries.' in re.sub(r'BuiltInRegistries\.|NeoForgeRegistries\.', '', text) \
            and 'import net.minecraft.core.registries.Registries;' not in text:
        text = re.sub(r'(\npackage [^;]+;\r?\n)', r'\1\nimport net.minecraft.core.registries.Registries;', text, 1)
    return text


def main():
    changed = 0
    for d, _, fs in os.walk(SRC):
        for f in fs:
            if not f.endswith('.java'):
                continue
            p = os.path.join(d, f)
            with open(p, encoding='utf-8', newline='') as fh:
                old = fh.read()
            new = port('\n' + old)[1:]
            if new != old:
                with open(p, 'w', encoding='utf-8', newline='') as fh:
                    fh.write(new)
                changed += 1
    print('changed', changed)


if __name__ == '__main__':
    main()
