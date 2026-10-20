"""Compares the vanilla data JSON schemas of two Minecraft versions: for each data folder, the "type" values and the
keys objects of that type carry (python3 tools/port/schema_diff.py 26.2 26.3 [folder substring])."""
import collections
import json
import pathlib
import sys
import zipfile

JARS = pathlib.Path.home() / '.gradle/caches/neoformruntime/artifacts'


def schemas(version, only):
    z = zipfile.ZipFile(JARS / f'minecraft_{version}_client.jar')
    out = collections.defaultdict(lambda: collections.defaultdict(set))

    def walk(folder, node, parent_key):
        if isinstance(node, dict):
            kind = node.get('type') if isinstance(node.get('type'), str) else ('@' + parent_key)
            out[folder][kind].add(tuple(sorted(k for k in node if k != 'type')))
            for k, v in node.items():
                walk(folder, v, k)
        elif isinstance(node, list):
            for v in node:
                walk(folder, v, parent_key)

    for name in z.namelist():
        if not name.startswith('data/minecraft/') or not name.endswith('.json'):
            continue
        parts = name.split('/')
        folder = '/'.join(parts[2:4]) if parts[2] == 'worldgen' else parts[2]
        if only and only not in folder:
            continue
        walk(folder, json.loads(z.read(name)), '')
    return out


old, new = schemas(sys.argv[1], sys.argv[3] if len(sys.argv) > 3 else ''), schemas(sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else '')
for folder in sorted(set(old) | set(new)):
    a, b = old.get(folder, {}), new.get(folder, {})
    lines = []
    for kind in sorted(set(a) | set(b)):
        ka, kb = set().union(*a.get(kind, [set()])), set().union(*b.get(kind, [set()]))
        if ka != kb:
            lines.append(f'  {kind}: -{sorted(ka - kb)} +{sorted(kb - ka)}' + ('' if kind in a and kind in b else ('  (only old)' if kind in a else '  (only new)')))
    if lines:
        print(folder)
        print('\n'.join(lines))
