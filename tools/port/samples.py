"""Code lines of the errors whose message (or symbol) contains a text: python3 tools/port/samples.py <text> [n]"""
import re
import sys

log = open('build-compile.log').read().split('* What went wrong')[0]
key = sys.argv[1]
n = int(sys.argv[2]) if len(sys.argv) > 2 else 15
seen = 0
for f, l, m, code, s in re.findall(r'(/[^\n]+?\.java):(\d+): error: ([^\n]+)\n([^\n]*)\n[^\n]*\n(?:\s+symbol:\s+([^\n]+))?', log):
    if key in m or key in (s or ''):
        print(f'{f.split("/rats/")[-1].split("/java/")[-1]}:{l}: {m[:90]}{" | " + s.strip() if s else ""}\n    {code.strip()[:200]}')
        seen += 1
        if seen >= n:
            break
