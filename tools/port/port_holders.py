"""RegistryObject<X> (already renamed to DeferredHolder<X>) -> DeferredHolder<R, X>, R = the registry's element type.

Items and blocks are found by class name (ends with Item/Block, or a class of this mod that extends one).
Usage: python -I tools/port/port_holders.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java')

GENERIC_BASES = {'EntityType', 'BlockEntityType', 'MenuType', 'RecipeSerializer', 'RecipeType', 'Feature', 'StructureType',
                 'StructureProcessorType', 'PlacementModifierType', 'ParticleType', 'LootItemFunctionType', 'WorldCarver',
                 'TrunkPlacerType', 'FoliagePlacerType', 'EntityDataSerializer'}
PLAIN_BASES = {'CreativeModeTab', 'VillagerProfession', 'PoiType', 'MobEffect', 'Potion', 'SoundEvent', 'BannerPattern',
               'RatVariant', 'LootItemConditionType', 'Enchantment'}
SPECIAL = {'SimpleParticleType': 'ParticleType<?>'}

# classes of this mod and what they extend
EXTENDS = {}
for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            text = open(os.path.join(d, f), encoding='utf-8').read()
            for m in re.finditer(r'class\s+(\w+)(?:<[^{]*?>)?\s+extends\s+(\w+)', text):
                EXTENDS.setdefault(m.group(1), m.group(2))


def base_kind(name):
    seen = set()
    while name and name not in seen:
        seen.add(name)
        if name == 'Item' or name.endswith('Item'):
            return 'Item'
        if name == 'Block' or name.endswith('Block'):
            return 'Block'
        name = EXTENDS.get(name)
    return None


def split_top(s):
    depth = 0
    for c in s:
        if c == '<':
            depth += 1
        elif c == '>':
            depth -= 1
        elif c == ',' and depth == 0:
            return True
    return False


def convert(arg):
    head = arg.split('<', 1)[0].strip()
    if head in SPECIAL:
        return f'DeferredHolder<{SPECIAL[head]}, {arg}>'
    if head in GENERIC_BASES:
        return f'DeferredHolder<{head}<?>, {arg}>'
    if head in PLAIN_BASES:
        return f'DeferredHolder<{head}, {arg}>'
    kind = base_kind(head)
    if kind:
        return f'DeferredHolder<{kind}, {arg}>'
    print('unknown holder type', arg)
    return None


def port(text):
    out, i = [], 0
    token = 'DeferredHolder<'
    while True:
        j = text.find(token, i)
        if j < 0:
            out.append(text[i:])
            break
        out.append(text[i:j])
        k, depth = j + len(token), 1
        while depth:
            if text[k] == '<':
                depth += 1
            elif text[k] == '>':
                depth -= 1
            k += 1
        arg = text[j + len(token):k - 1]
        new = None if split_top(arg) else convert(arg)
        out.append(new if new else text[j:k])
        i = k
    return ''.join(out)


changed = 0
for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            with open(p, encoding='utf-8', newline='') as fh:
                old = fh.read()
            new = port(old)
            if new != old:
                with open(p, 'w', encoding='utf-8', newline='') as fh:
                    fh.write(new)
                changed += 1
print('changed', changed)
