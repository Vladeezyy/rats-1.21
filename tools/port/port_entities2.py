"""More 1.21 entity/effect API changes over the Rats sources (one-off). Usage: python -I tools/port/port_entities2.py ."""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')


def imp(t, i):
    return t if f'import {i};' in t else re.sub(r'(package [^;]+;\n)', lambda m: m.group(1) + f'\nimport {i};', t, count=1)


def port(path, t):
    if not path.endswith('RatsArmorMaterialRegistry.java'):
        t = re.sub(r'\bSoundEvents\.(ARMOR_EQUIP_\w+)\b(?!\.value)', r'SoundEvents.\1.value()', t)
    # death loot: (ServerLevel, DamageSource, boolean); looting is gone (loot tables do it)
    t = re.sub(r'protected void dropCustomDeathLoot\(DamageSource (\w+), int \w+, boolean (\w+)\)',
               r'protected void dropCustomDeathLoot(ServerLevel level, DamageSource \1, boolean \2)', t)
    t = re.sub(r'super\.dropCustomDeathLoot\((\w+), \w+, (\w+)\);', r'super.dropCustomDeathLoot(level, \1, \2);', t)
    t = re.sub(r'public boolean canChangeDimensions\(\)', 'public boolean canChangeDimensions(Level from, Level to)', t)
    t = re.sub(r'super\.canChangeDimensions\(\)', 'super.canChangeDimensions(from, to)', t)
    t = re.sub(r'Component\.Serializer\.toJson\(([^;]+?)\)\)', r'Component.Serializer.toJson(\1, this.registryAccess()))', t) if 'extends' in t and ('Entity' in t or 'Rat' in t) else t
    t = re.sub(r'DeferredSpawnEggItem\.fromEntityType\(', 'SpawnEggItem.byId(', t)
    t = re.sub(r'EventHooks\.onFinalizeSpawn\(([^;]*?), null, null\)', r'EventHooks.finalizeMobSpawn(\1, null)', t)
    # effects
    t = re.sub(r'public void applyEffectTick\(LivingEntity (\w+), int (\w+)\) \{', r'public boolean applyEffectTick(LivingEntity \1, int \2) {', t)
    t = re.sub(r'public boolean isDurationEffectTick\(int (\w+), int (\w+)\)', r'public boolean shouldApplyEffectTickThisTick(int \1, int \2)', t)
    if 'public boolean applyEffectTick(' in t:
        m = re.search(r'public boolean applyEffectTick\([^)]*\) \{', t)
        i, depth = m.end() - 1, 0
        for j in range(i, len(t)):
            if t[j] == '{':
                depth += 1
            elif t[j] == '}':
                depth -= 1
                if depth == 0:
                    break
        body = t[i:j]
        body = re.sub(r'super\.applyEffectTick\(([^;]*)\);', r'return super.applyEffectTick(\1);', body)
        body = re.sub(r'\breturn;', 'return true;', body)
        if 'return super.applyEffectTick' not in body:
            body = body.rstrip() + '\n\t\treturn true;\n\t'
        t = t[:i] + body + t[j:]
    # tame
    t = re.sub(r'public void setTame\(boolean (\w+)\) \{', r'public void setTame(boolean \1, boolean applyTamingSideEffects) {', t)
    t = re.sub(r'super\.setTame\((\w+)\);', r'super.setTame(\1, applyTamingSideEffects);', t)
    # FluidStack NBT
    t = re.sub(r'(\w+(?:\.\w+)*)\.writeToNBT\((\w+)\);', r'\2.merge((CompoundTag) \1.saveOptional(this.registryAccess()));', t) if 'FluidStack' in t else t
    t = re.sub(r'FluidStack\.loadFluidStackFromNBT\((\w+)\)', r'FluidStack.parseOptional(this.registryAccess(), \1)', t)
    for cls, imp_ in (('ServerLevel', 'net.minecraft.server.level.ServerLevel'), ('SpawnEggItem', 'net.minecraft.world.item.SpawnEggItem'), ('Level', 'net.minecraft.world.level.Level')):
        if re.search(r'\b' + cls + r'\b', t):
            t = imp(t, imp_)
    return t


for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            old = open(p, encoding='utf-8').read()
            new = port(p, old)
            if new != old:
                open(p, 'w', encoding='utf-8').write(new)
                print('ported', os.path.relpath(p, SRC))
