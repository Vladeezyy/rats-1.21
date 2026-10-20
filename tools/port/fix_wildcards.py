"""Wildcard imports of Minecraft packages whose classes moved in 26.x: an explicit import of the new package for every
moved class the file uses. python3 tools/port/fix_wildcards.py <1.21.1 sources> <26.1.2 sources>"""
import pathlib
import re
import sys

old_root, new_root = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])


def index(root):
    by_pkg, by_name = {}, {}
    for p in root.rglob('*.java'):
        rel = p.relative_to(root).with_suffix('').parts
        by_pkg.setdefault('.'.join(rel[:-1]), set()).add(rel[-1])
        by_name.setdefault(rel[-1], []).append('.'.join(rel))
    return by_pkg, by_name


old_pkg, _ = index(old_root)
new_pkg, new_name = index(new_root)
changed = 0
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        wildcards = re.findall(r'^import ((?:net\.minecraft|com\.mojang)[\w.]*)\.\*;', text, re.M)
        if not wildcards:
            continue
        code = re.sub(r'^import [^\n]*\n', '', text, flags=re.M)
        adds = []
        for pkg in wildcards:
            for name in sorted(old_pkg.get(pkg, ())):
                if name in new_pkg.get(pkg, ()) or not re.search(r'(?<![\w.])' + name + r'\b', code):
                    continue
                cands = [c for c in new_name.get(name, []) if c.startswith(('net.minecraft', 'com.mojang'))]
                if len(cands) == 1 and 'import ' + cands[0] + ';' not in text:
                    adds.append('import ' + cands[0] + ';')
        if adds:
            text = re.sub(r'^(package [\w.]+;\n)', lambda m: m.group(1) + '\n'.join(sorted(set(adds))) + '\n', text, count=1, flags=re.M)
            path.write_text(text, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
