"""Ports the armor items of the original to the 1.21.1 armor API (one-off, already applied).

- Citadel CustomArmorMaterial -> our IafArmorMaterial, items get material.holder() and durability from the material;
- getArmorTexture(String) -> getArmorTexture(ArmorMaterial.Layer, boolean) returning a ResourceLocation.
Usage: python -I tools/port_armor.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
BASE = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')


def method_body_span(text, start):
    i = text.index('{', start)
    depth = 0
    for j in range(i, len(text)):
        if text[j] == '{':
            depth += 1
        elif text[j] == '}':
            depth -= 1
            if depth == 0:
                return i, j + 1
    raise ValueError


def fix_texture(text):
    sig = re.compile(r'public (?:@NotNull )?String getArmorTexture\(ItemStack (\w+), Entity (\w+), EquipmentSlot (\w+), String (\w+)\)')
    while True:
        m = sig.search(text)
        if not m:
            return text
        new_sig = (f'public ResourceLocation getArmorTexture(ItemStack {m.group(1)}, Entity {m.group(2)}, '
                   f'EquipmentSlot {m.group(3)}, ArmorMaterial.Layer layer, boolean innerModel)')
        b0, b1 = method_body_span(text, m.end())
        body = text[b0:b1]
        body = re.sub(r'return (?!null)([^;]+);', r'return ResourceLocation.parse(\1);', body)
        text = text[:m.start()] + new_sig + ' ' + body + text[b1:]


def add_import(text, imp):
    if f'import {imp};' in text:
        return text
    nl = '\r\n' if '\r\n' in text else '\n'
    return re.sub(r'(package [^;]+;\r?\n\r?\n)', lambda m: m.group(1) + f'import {imp};{nl}', text, count=1)


for d in ('item', 'enums'):
    for f in os.listdir(os.path.join(BASE, d)):
        p = os.path.join(BASE, d, f)
        if not f.endswith('.java') or f in ('IafArmorMaterial.java', 'DragonsteelArmorMaterial.java'):
            continue
        with open(p, encoding='utf-8', newline='') as fh:
            old = fh.read()
        t = old
        t = re.sub(r'import com\.github\.alexthe666\.citadel\.server\.item\.CustomArmorMaterial;\r?\n', '', t)
        t = t.replace('CustomArmorMaterial', 'IafArmorMaterial')
        if 'extends ArmorItem' in t:
            t = re.sub(r'\((ArmorMaterial|IafArmorMaterial) material, (int renderIndex, )?ArmorItem\.Type slot\)',
                       r'(IafArmorMaterial material, \2ArmorItem.Type slot)', t)
            t = re.sub(r'(\w+ \w+, )(ArmorMaterial|IafArmorMaterial) material, ArmorItem\.Type slot\)',
                       r'\1IafArmorMaterial material, ArmorItem.Type slot)', t)
            t = t.replace('super(material, slot, new Item.Properties()',
                          'super(material.holder(), slot, new Item.Properties().durability(material.getDurabilityForType(slot))')
            t = fix_texture(t)
            if 'ResourceLocation' in t:
                t = add_import(t, 'net.minecraft.resources.ResourceLocation')
            t = add_import(t, 'net.minecraft.world.item.ArmorMaterial') if 'ArmorMaterial.Layer' in t else t
        if d == 'enums' and 'IafArmorMaterial' in t:
            t = add_import(t, 'com.github.alexthe666.iceandfire.item.IafArmorMaterial')
        if t != old:
            with open(p, 'w', encoding='utf-8', newline='') as fh:
                fh.write(t)
            print('ported', f)
