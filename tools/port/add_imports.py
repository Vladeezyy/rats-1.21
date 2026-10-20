"""Adds imports for the given simple names where a file uses them without an import (after the package line).

python3 tools/port/add_imports.py Name=fully.qualified.Name ...
"""
import pathlib
import re
import sys

wanted = dict(arg.split('=', 1) for arg in sys.argv[1:])
changed = 0
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        new = text
        package = re.search(r'^package ([\w.]+);', text, re.M)
        code = re.sub(r'^import [^\n]*\n', '', re.sub(r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"', '', text, flags=re.S), flags=re.M)
        for name, fqn in wanted.items():
            if path.stem == name or (package and fqn.rsplit('.', 1)[0] == package.group(1)):
                continue
            if re.search(r'(?<![\w.])' + name + r'\b', code) and not re.search(r'^import [\w.]*\b' + name + ';', text, re.M):
                new = re.sub(r'^(package [\w.]+;\n)', r'\1import ' + fqn + ';\n', new, count=1, flags=re.M)
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
