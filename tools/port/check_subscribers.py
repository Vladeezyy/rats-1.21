"""Checks event subscribers before a run (NeoForge 21.1 fails mod loading on any of these):

- @EventBusSubscriber class without @SubscribeEvent methods;
- non-static @SubscribeEvent method in an @EventBusSubscriber class;
- mod bus event (lifecycle, registration, ...) in a game bus subscriber and the other way round.
Usage: python -I tools/check_subscribers.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java')

MOD_BUS_EVENTS = re.compile(r'^(FML\w+Event|RegisterEvent|NewRegistryEvent|ModConfigEvent\.\w+|EntityAttributeCreationEvent|'
                            r'EntityAttributeModificationEvent|RegisterPayloadHandlersEvent|RegisterCapabilitiesEvent|'
                            r'RegisterSpawnPlacementsEvent|RegisterTicketControllersEvent|GatherDataEvent|BuildCreativeModeTabContentsEvent|'
                            r'EntityRenderersEvent\.\w+|RegisterKeyMappingsEvent|RegisterColorHandlersEvent\.\w+|RegisterParticleProvidersEvent|'
                            r'RegisterMenuScreensEvent|RegisterClientExtensionsEvent|RegisterShadersEvent|ModelEvent\.\w+|'
                            r'RegisterGuiLayersEvent|RegisterItemDecorationsEvent|TextureAtlasStitchedEvent|RegisterClientReloadListenersEvent|'
                            r'RegisterDimensionSpecialEffectsEvent|RegisterRenderBuffersEvent)$')

problems = 0
for d, _, fs in os.walk(SRC):
    for f in fs:
        if not f.endswith('.java'):
            continue
        p = os.path.join(d, f)
        t = open(p, encoding='utf-8').read()
        for m in re.finditer(r'@EventBusSubscriber\(([^)]*)\)\s*(?:@\w+(?:\([^)]*\))?\s*)*public (?:static )?(?:final )?class (\w+)', t):
            args, cls = m.group(1), m.group(2)
            start = m.end()
            # the class body ends where the next top level/nested class starts or at the end of file: good enough
            body = t[start:]
            nested = re.search(r'\n    public static class \w+|\n    @EventBusSubscriber', body)
            if nested and cls == os.path.basename(p)[:-5]:
                body = body[:nested.start()]
            subs = re.findall(r'@SubscribeEvent(?:\([^)]*\))?\s*public (static )?\w+ \w+\((?:final )?([\w.]+)(?:<[^>]*>)? \w+\)', body)
            rel = os.path.relpath(p, SRC)
            if not subs:
                print(f'{rel}: {cls} has @EventBusSubscriber but no @SubscribeEvent methods')
                problems += 1
            mod_bus = 'Bus.MOD' in args
            for static, event in subs:
                if not static:
                    print(f'{rel}: {cls} non-static subscriber for {event}')
                    problems += 1
                is_mod_event = bool(MOD_BUS_EVENTS.match(event))
                if is_mod_event != mod_bus:
                    print(f'{rel}: {cls} {event} on the {"mod" if mod_bus else "game"} bus')
                    problems += 1
print(problems, 'problems')
