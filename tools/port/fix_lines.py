"""Rewrites the lines javac reported (build-compile.log), by error message: the 26.x fixes that depend on the type of
the receiver, so they cannot be global renames. Run after a compile; prints what it changed per rule.
"""
import collections
import re


def args_end(line, start):
    depth, i = 1, start
    while depth and i < len(line):
        depth += {'(': 1, ')': -1}.get(line[i], 0)
        i += 1
    return i


def split_args(text):
    out, depth, cur = [], 0, ''
    for ch in text:
        if ch == ',' and depth == 0:
            out.append(cur.strip()); cur = ''; continue
        depth += {'(': 1, ')': -1, '<': 0, '>': 0}.get(ch, 0)
        cur += ch
    if cur.strip():
        out.append(cur.strip())
    return out


def rewrite_call(line, name, fn):
    """fn(receiver_text_before_dot, args) -> replacement for 'name(args)' (receiver kept), applied to each call"""
    out, pos = '', 0
    for m in re.finditer(r'\.' + name + r'\(', line):
        if m.start() < pos:
            continue
        end = args_end(line, m.end())
        args = split_args(line[m.end():end - 1])
        rep = fn(args)
        if rep is None:
            continue
        out += line[pos:m.start()] + '.' + rep
        pos = end
    return out + line[pos:]


def difficulty(line):
    return re.sub(r'([\w.]+(?:\(\))?)\.getCurrentDifficultyAt\(', r'((net.minecraft.world.level.ServerLevelAccessor) \1).getCurrentDifficultyAt(', line, count=1)


def location(line):
    return line.replace('.location()', '.identifier()')


def spawn_at(line):
    m = re.search(r'([\w.]+(?:\(\))?|this)\.spawnAtLocation\(', line)
    if not m or line[m.end():].startswith('(net.minecraft.server.level.ServerLevel)'):
        return line
    receiver = m.group(1)
    level = receiver + '.level()'
    return line[:m.end()] + '(net.minecraft.server.level.ServerLevel) ' + level + ', ' + line[m.end():]


def overlay(line):
    return rewrite_call(line, 'displayClientMessage', lambda a: ('sendOverlayMessage(' + a[0] + ')' if a[1] == 'true' else 'sendSystemMessage(' + a[0] + ')') if len(a) == 2 else None)


def hurt_value(line):
    return re.sub(r'\.hurt\(', '.hurtOrSimulate(', line)


def optional_get(line, kind, default):
    return re.sub(r'\.get' + kind + r'\(([^()]*)\)', lambda m: '.get' + kind + 'Or(' + m.group(1) + ', ' + default + ')', line)


def server_level_arg(line, name):
    # x.name(args) -> x.name((ServerLevel) x.level(), args); a bare name(args) -> name((ServerLevel) this.level(), args)
    def rep(m):
        receiver = m.group(1)
        level = (receiver[:-1] if receiver else 'this') + '.level()'
        return (receiver or '') + name + '((net.minecraft.server.level.ServerLevel) ' + level + ', '
    return re.sub(r'((?:[\w.]+(?:\(\))?|this)\.)?' + name + r'\((?!\(net\.minecraft\.server\.level\.ServerLevel\))', rep, line, count=1)


RULES = [
    ('cannot be converted to SoundEvent', lambda l: re.sub(r'(SoundEvents\.\w+)(?!\.value\(\))', r'\1.value()', l)),
    ('Optional<Reference<', lambda l: re.sub(r'(BuiltInRegistries\.\w+)\.get\(', r'\1.getValue(', l)),
    ('no suitable method found for is(Optional<Reference', lambda l: re.sub(r'(BuiltInRegistries\.\w+)\.get\(', r'\1.getValue(', l)),
    ('no suitable method found for create(Level)', lambda l: re.sub(r'\.create\(([^()]*(?:\([^()]*\))?)\)', r'.create(\1, EntitySpawnReason.MOB_SUMMONED)', l, count=1)),
    ('no suitable method found for startRiding', lambda l: re.sub(r'startRiding\(([^()]*(?:\([^()]*\))?), (true|false)\)', r'startRiding(\1, \2, true)', l)),
    ('method isInvulnerableTo in class', lambda l: server_level_arg(l, 'isInvulnerableTo')),
    ('method doHurtTarget in class', lambda l: server_level_arg(l, 'doHurtTarget')),
    ('method isInWaterOrBubble()', lambda l: l.replace('isInWaterOrBubble()', 'isInWater()')),
    ('method moveTo(double,double,double,float,float)', lambda l: l.replace('.moveTo(', '.snapTo(')),
    ('method moveTo(Vec3)', lambda l: l.replace('.moveTo(', '.snapTo(')),
    ('method getCurrentDifficultyAt(BlockPos)', difficulty),
    ('method location()', location),
    ('no suitable method found for spawnAtLocation', spawn_at),
    ('method displayClientMessage', overlay),
    ('void cannot be converted to boolean', hurt_value),
    ('Optional<Integer> cannot be converted to int', lambda l: optional_get(l, 'Int', '0')),
    ('Optional<Boolean> cannot be converted to boolean', lambda l: optional_get(l, 'Boolean', 'false')),
    ('Optional<String> cannot be converted to String', lambda l: optional_get(l, 'String', '""')),
    ('Optional<Float> cannot be converted to float', lambda l: optional_get(l, 'Float', '0.0F')),
    ('Optional<Double> cannot be converted to', lambda l: optional_get(l, 'Double', '0.0D')),
    ('Optional<Long> cannot be converted to long', lambda l: optional_get(l, 'Long', '0L')),
    ('Optional<Byte> cannot be converted to byte', lambda l: optional_get(l, 'Byte', '(byte) 0')),
    ('Optional<CompoundTag> cannot be converted to CompoundTag', lambda l: re.sub(r'\.getCompound\(', '.getCompoundOrEmpty(', l)),
    ('Optional<ListTag> cannot be converted to ListTag', lambda l: re.sub(r'\.getList\(([^(),]*)(?:,\s*[\w.]+)?\)', r'.getListOrEmpty(\1)', l)),
]

log = open('build-compile.log').read().split('* What went wrong')[0]
errs = re.findall(r'(/[^\n]+?\.java):(\d+): error: ([^\n]+)\n([^\n]*)\n[^\n]*\n(?:\s+symbol:\s+([^\n]+))?', log)
todo = collections.defaultdict(set)
for f, l, m, code, s in errs:
    full = m + (' | ' + s if s else '')
    for i, (key, _) in enumerate(RULES):
        if key in full or key in (s or ''):
            todo[(f, int(l), code.strip())].add(i)
count = collections.Counter()
files = collections.defaultdict(list)
for (f, l, code), rules in todo.items():
    files[f].append((l, code, rules))
for f, entries in files.items():
    text = open(f).read().split('\n')
    for l, code, rules in entries:
        # the line javac showed: where it was, or nearby if the file changed since the compile
        at = next((j for j in [l - 1] + [l - 1 + d for k in range(1, 11) for d in (k, -k)] if 0 <= j < len(text) and text[j].strip() == code), None)
        if at is None:
            count['(line not found)'] += 1
            continue
        for i in sorted(rules):
            new = RULES[i][1](text[at])
            if new != text[at]:
                text[at] = new
                count[RULES[i][0]] += 1
    open(f, 'w').write('\n'.join(text))
for k, v in count.most_common():
    print(v, k)
