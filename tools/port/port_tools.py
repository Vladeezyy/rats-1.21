"""Tools and weapons of the original -> 1.21.1 (one-off, already applied).

Tier + damage/speed constructors -> Item.Properties().attributes(createAttributes(...)), the dragonsteel attribute
override -> ItemAttributeModifiers built from the config. Usage: python -I tools/port_tools.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
ITEM = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats', 'item')

SWORDS = ['ItemModSword', 'ItemAlchemySword', 'ItemAmphithereMacuahuitl', 'ItemGhostSword', 'ItemHippocampusSlapper',
          'ItemHippogryphSword', 'ItemStymphalianDagger', 'ItemTrollWeapon']
DIGGERS = ['ItemModAxe', 'ItemModHoe', 'ItemModPickaxe', 'ItemModShovel']


def body_span(text, start):
    i = text.index('{', start)
    depth = 0
    for j in range(i, len(text)):
        if text[j] == '{':
            depth += 1
        elif text[j] == '}':
            depth -= 1
            if depth == 0:
                return i, j + 1


def add_import(t, imp):
    if f'import {imp};' in t:
        return t
    return re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + f'\r\nimport {imp};', t, count=1)


for cls in SWORDS + DIGGERS:
    p = os.path.join(ITEM, cls + '.java')
    t = open(p, encoding='utf-8', newline='').read()
    factory = 'SwordItem.createAttributes' if cls in SWORDS else 'DiggerItem.createAttributes'
    t = re.sub(r'super\(([\w.]+), ([-\d.F]+), ([-\d.F]+), \(?new Item\.Properties\(\)\)?',
               lambda m: f'super({m.group(1)}, new Item.Properties().attributes({factory}({m.group(1)}, {m.group(2)}, {m.group(3)}))', t)
    # attribute override by slot -> by stack
    t = re.sub(r'public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers\(@NotNull EquipmentSlot equipmentSlot\) \{\s*'
               r'return equipmentSlot == EquipmentSlot\.MAINHAND && isDragonsteel\(getTier\(\)\) \? this\.bakeDragonsteel\(\) : super\.getDefaultAttributeModifiers\(equipmentSlot\);',
               'public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {\r\n'
               '        return isDragonsteel(getTier()) ? this.bakeDragonsteel() : super.getDefaultAttributeModifiers(stack);', t)
    m = re.search(r'public Multimap<Attribute, AttributeModifier> bakeDragonsteel\(\)', t)
    if m:
        b0, b1 = body_span(t, m.end())
        body = t[b0:b1]
        dmg = re.search(r'Attributes\.ATTACK_DAMAGE, new AttributeModifier\(\w+, "[^"]*", ([^,]+), AttributeModifier', body).group(1)
        spd = re.search(r'Attributes\.ATTACK_SPEED, new AttributeModifier\(\w+, "[^"]*", ([^,]+), AttributeModifier', body).group(1)
        new_body = ('{\r\n'
                    '        if (dragonsteelModifiers == null || dragonsteelDamage != IafConfig.dragonsteelBaseDamage) {\r\n'
                    '            dragonsteelDamage = IafConfig.dragonsteelBaseDamage;\r\n'
                    '            dragonsteelModifiers = ItemAttributeModifiers.builder()\r\n'
                    f'                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, {dmg}, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)\r\n'
                    f'                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, {spd}, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)\r\n'
                    '                    .build();\r\n'
                    '        }\r\n'
                    '        return dragonsteelModifiers;\r\n'
                    '    }')
        t = t[:m.start()] + 'public ItemAttributeModifiers bakeDragonsteel() ' + new_body + t[b1:]
        t = t.replace('private Multimap<Attribute, AttributeModifier> dragonsteelModifiers;',
                      'private ItemAttributeModifiers dragonsteelModifiers;\r\n    private double dragonsteelDamage;')
        t = add_import(t, 'net.minecraft.world.item.component.ItemAttributeModifiers')
        t = add_import(t, 'net.minecraft.world.entity.EquipmentSlotGroup')
    for imp in ('com.google.common.collect.ImmutableMultimap', 'com.google.common.collect.Multimap',
                'net.minecraft.world.entity.ai.attributes.Attribute'):
        if imp.split('.')[-1] + '<' not in t.split('public class')[1] and imp.split('.')[-1] + '.' not in t.split('public class')[1]:
            t = t.replace(f'import {imp};\r\n', '').replace(f'import {imp};\n', '')
    open(p, 'w', encoding='utf-8', newline='').write(t)
    print('ported', cls)
