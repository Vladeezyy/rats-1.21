"""Mechanical entity/item API changes of 1.20.5-1.21.1 (one-off, already applied).

hurtAndBreak with a break callback -> with a slot, finalizeSpawn without the NBT argument, setTame(boolean, boolean),
setMaxUpStep -> STEP_HEIGHT attribute, inline net.minecraftforge names. Usage: python -I tools/port_entities.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')


def slot_of(arg):
    arg = arg.strip()
    if arg.startswith('EquipmentSlot.'):
        return arg
    return f'LivingEntity.getSlotForHand({arg})'


def port(t):
    t = re.sub(r'hurtAndBreak\(([^,()]+(?:\([^()]*\))?), ([^,()]+(?:\([^()]*\))?), \(?\w+\)? -> \{?\s*\w+\.broadcastBreakEvent\(((?:[^()]|\([^()]*\))*)\);?\s*\}?\)',
               lambda m: f'hurtAndBreak({m.group(1)}, {m.group(2)}, {slot_of(m.group(3))})', t)
    t = re.sub(r',\s*@Nullable CompoundTag \w+\) \{', ') {', t) if 'finalizeSpawn' in t else t
    t = re.sub(r'(finalizeSpawn\([^;]*?), (?:dataTag|compound|tag|nbt|p_\w+|null)\)', r'\1)', t)
    t = re.sub(r'\.setTame\(((?:compound|tag|nbt)\.getBoolean\([^)]*\))\)', r'.setTame(\1, false)', t)
    t = re.sub(r'\.setTame\(([^,()]+)\);', r'.setTame(\1, true);', t)
    t = re.sub(r'(?<![\w.])setTame\(([^,()]+)\);', r'setTame(\1, true);', t)
    t = re.sub(r'\bthis\.setMaxUpStep\(([^;]+)\);', r'this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(\1);', t)
    t = re.sub(r'(?<![\w.])setMaxUpStep\(([^;]+)\);', r'this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(\1);', t)
    t = t.replace('net.minecraftforge.event.EventHooks.', 'net.neoforged.neoforge.event.EventHooks.')
    t = t.replace('net.minecraftforge.client.event.', 'net.neoforged.neoforge.client.event.')
    return t


def add_import(t, imp):
    if f'import {imp};' in t or re.search(r'import ' + re.escape(imp.rsplit('.', 1)[0]) + r'\.\*;', t):
        return t
    return re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + f'\nimport {imp};', t, count=1)


for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            old = open(p, encoding='utf-8', newline='').read()
            new = port(old)
            if new != old:
                if 'Attributes.STEP_HEIGHT' in new:
                    new = add_import(new, 'net.minecraft.world.entity.ai.attributes.Attributes')
                if 'LivingEntity.getSlotForHand' in new:
                    new = add_import(new, 'net.minecraft.world.entity.LivingEntity')
                open(p, 'w', encoding='utf-8', newline='').write(new)
                print('ported', os.path.relpath(p, SRC))
            for i, line in enumerate(new.splitlines(), 1):
                if 'broadcastBreakEvent' in line:
                    print(f'  manual {os.path.relpath(p, SRC)}:{i}: {line.strip()}')
