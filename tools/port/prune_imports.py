"""Removes single-type imports whose simple name the file no longer uses. python3 tools/port/prune_imports.py <files or dirs>"""
import pathlib
import re
import sys

changed = 0
for arg in sys.argv[1:]:
    paths = [pathlib.Path(arg)] if arg.endswith('.java') else pathlib.Path(arg).rglob('*.java')
    for path in paths:
        text = path.read_text(encoding='utf-8')
        code = re.sub(r'^import [^\n]*\n', '', text, flags=re.M)
        code = re.sub(r'//[^\n]*|/\*.*?\*/', '', code, flags=re.S)
        def keep(m):
            name = m.group(2).rsplit('.', 1)[-1]
            return m.group(0) if m.group(1) or name == '*' or re.search(r'\b' + re.escape(name) + r'\b', code) else ''
        new = re.sub(r'^import (static )?([\w.]+(?:\.\*)?);\n', keep, text, flags=re.M)
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
