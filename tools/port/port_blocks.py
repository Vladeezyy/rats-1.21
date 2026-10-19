"""Block API changes of 1.20.2-1.21.1 (one-off, already applied).

use(...) -> useItemOn(ItemStack, ...) with ItemInteractionResult (it is also called with an empty hand, so the
behavior stays the same), codec() for blocks, isPathfindable(state, type), playerWillDestroy returns the state.
Usage: python -I tools/port_blocks.py . <Class> ...  (classes that need codec())
"""
import os
import re
import sys

ROOT = sys.argv[1]
CODEC_CLASSES = set(sys.argv[2:])
BLOCK = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats', 'server', 'block')

RESULTS = {'SUCCESS': 'SUCCESS', 'CONSUME': 'CONSUME', 'CONSUME_PARTIAL': 'CONSUME_PARTIAL', 'FAIL': 'FAIL',
           'PASS': 'PASS_TO_DEFAULT_BLOCK_INTERACTION'}


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
    if f'import {imp};' in t or re.search(r'import ' + re.escape(imp.rsplit('.', 1)[0]) + r'\.\*;', t):
        return t
    return re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + f'\nimport {imp};', t, count=1)


def port_use(t):
    sig = re.compile(r'public (?:@NotNull )?InteractionResult use\(((?:@\w+ )*)BlockState (\w+), ((?:@\w+ )*)Level (\w+), ((?:@\w+ )*)BlockPos (\w+), ((?:@\w+ )*)Player (\w+), ((?:@\w+ )*)InteractionHand (\w+), ((?:@\w+ )*)BlockHitResult (\w+)\)')
    m = sig.search(t)
    if not m:
        return t
    b0, b1 = body_span(t, m.end())
    body = t[b0:b1]
    body = re.sub(r'InteractionResult\.(SUCCESS|CONSUME_PARTIAL|CONSUME|FAIL|PASS)\b', lambda r: 'ItemInteractionResult.' + RESULTS[r.group(1)], body)
    body = body.replace('InteractionResult.sidedSuccess(', 'ItemInteractionResult.sidedSuccess(')
    body = re.sub(r'super\.use\(', 'super.useItemOn(heldStack, ', body)
    g = m.groups()
    new_sig = (f'protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldStack, {g[0]}BlockState {g[1]}, {g[2]}Level {g[3]}, '
               f'{g[4]}BlockPos {g[5]}, {g[6]}Player {g[7]}, {g[8]}InteractionHand {g[9]}, {g[10]}BlockHitResult {g[11]})')
    t = t[:m.start()] + new_sig + ' ' + body + t[b1:]
    t = add_import(t, 'net.minecraft.world.ItemInteractionResult')
    t = add_import(t, 'net.minecraft.world.item.ItemStack')
    return t


def port_codec(t, cls):
    if cls not in CODEC_CLASSES or 'codec()' in t:
        return t
    ctors = re.findall(r'public ' + cls + r'\(([^)]*)\)', t)
    if any(re.fullmatch(r'\s*(?:BlockBehaviour\.)?Properties \w+\s*', c) for c in ctors):
        factory = f'{cls}::new'
    elif any(c.strip() == '' for c in ctors):
        factory = f'properties -> new {cls}()'
    else:
        print('manual codec', cls, ctors)
        return t
    m = re.search(r'public (?:abstract )?class ' + cls + r'\b[^{]*\{', t)
    nl = '\r\n' if '\r\n' in t else '\n'
    insert = (f'{nl}    public static final MapCodec<{cls}> CODEC = simpleCodec({factory});{nl}{nl}'
              f'    @Override{nl}    protected @NotNull MapCodec<? extends {cls}> codec() {{{nl}        return CODEC;{nl}    }}{nl}')
    t = t[:m.end()] + insert + t[m.end():]
    t = add_import(t, 'com.mojang.serialization.MapCodec')
    t = add_import(t, 'org.jetbrains.annotations.NotNull')
    return t


def port_misc(t):
    t = re.sub(r'public boolean isPathfindable\(((?:@\w+ )*)BlockState (\w+), ((?:@\w+ )*)BlockGetter \w+, ((?:@\w+ )*)BlockPos \w+, ((?:@\w+ )*)PathComputationType (\w+)\)',
               r'protected boolean isPathfindable(\1BlockState \2, \5PathComputationType \6)', t)
    m = re.search(r'public void playerWillDestroy\(([^)]*)\)', t)
    if m:
        b0, b1 = body_span(t, m.end())
        body = t[b0:b1]
        body = re.sub(r'super\.playerWillDestroy\(([^;]*)\);', r'return super.playerWillDestroy(\1);', body)
        if 'return super.playerWillDestroy' not in body:
            names = [a.split()[-1] for a in m.group(1).split(',')]
            body = body[:body.rindex('}')] + f'    return super.playerWillDestroy({", ".join(names)});\n    }}'
        t = t[:m.start()] + 'public @NotNull BlockState playerWillDestroy(' + m.group(1) + ') ' + body + t[b1:]
    t = t.replace('BlockBehaviour.Properties.copy(', 'BlockBehaviour.Properties.ofFullCopy(')
    return t


for f in os.listdir(BLOCK):
    if not f.endswith('.java'):
        continue
    p = os.path.join(BLOCK, f)
    old = open(p, encoding='utf-8', newline='').read()
    t = port_use(old)
    t = port_codec(t, f[:-5])
    t = port_misc(t)
    if t != old:
        open(p, 'w', encoding='utf-8', newline='').write(t)
        print('ported', f)
