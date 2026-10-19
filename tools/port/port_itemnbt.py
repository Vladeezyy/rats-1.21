"""ItemStack NBT of 1.20.1 -> ItemNbt (minecraft:custom_data with the old keys). One-off, already applied.

Reads go through ItemNbt.view (no copy), writes through ItemNbt.setTag/update. Places that change a tag they read
are left for manual porting: the script prints them. Usage: python -I tools/port_itemnbt.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')

X = r'((?:this\.)?[a-zA-Z_][\w]*(?:\.[a-zA-Z_]\w*\([^()]*\))*)'
SKIP = re.compile(r'^(pkt|packet|tag|compound|nbt|compoundNBT|tagCompound)$')
READS = r'(get\w*|contains|hasUUID|isEmpty|getAllKeys|size)'


def port(t):
    def guard(fn):
        def inner(m):
            if SKIP.match(m.group(1).split('.')[0]) and '(' not in m.group(1):
                return m.group(0)
            return fn(m)
        return inner

    t = re.sub(X + r'\.getTag\(\) != null', guard(lambda m: f'ItemNbt.hasTag({m.group(1)})'), t)
    t = re.sub(X + r'\.getTag\(\) == null', guard(lambda m: f'!ItemNbt.hasTag({m.group(1)})'), t)
    t = re.sub(X + r'\.hasTag\(\)', guard(lambda m: f'ItemNbt.hasTag({m.group(1)})'), t)
    t = re.sub(X + r'\.(?:getTag|getOrCreateTag)\(\)\.' + READS + r'\(', guard(lambda m: f'ItemNbt.view({m.group(1)}).{m.group(2)}('), t)
    t = re.sub(X + r'\.setTag\(', guard(lambda m: f'ItemNbt.setTag({m.group(1)}, '), t)
    t = re.sub(r'(?m)^(\s*)' + X + r'\.getOrCreateTag\(\)\.(put\w*|remove)\(([^;]*)\);',
               lambda m: f'{m.group(1)}ItemNbt.update({m.group(2)}, tag -> tag.{m.group(3)}({m.group(4)}));', t)
    t = re.sub(X + r'\.getTag\(\)(?!\.)', guard(lambda m: f'ItemNbt.getTag({m.group(1)})'), t)
    t = re.sub(X + r'\.getOrCreateTag\(\)(?!\.)', guard(lambda m: f'ItemNbt.getOrEmpty({m.group(1)})'), t)
    return t


for d, _, fs in os.walk(SRC):
    if os.sep + 'datagen' in d:
        continue
    for f in fs:
        if not f.endswith('.java') or f == 'ItemNbt.java':
            continue
        p = os.path.join(d, f)
        old = open(p, encoding='utf-8', newline='').read()
        new = port(old)
        if new != old:
            if 'import com.github.alexthe666.rats.server.misc.ItemNbt;' not in new:
                new = re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + '\nimport com.github.alexthe666.rats.server.misc.ItemNbt;', new, count=1)
            open(p, 'w', encoding='utf-8', newline='') .write(new)
            for i, line in enumerate(new.splitlines(), 1):
                if re.search(r'ItemNbt\.(getTag|getOrEmpty)\(', line) or re.search(r'\.getTag\(\)|getOrCreateTag', line):
                    print(f'{os.path.relpath(p, SRC)}:{i}: {line.strip()}')
