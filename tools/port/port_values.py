"""1.21.6+: entity and block entity save/load methods from CompoundTag to ValueOutput / ValueInput (same keys, same NBT).

python3 tools/port/port_values.py      (repo root; common/src/main/java)
"""
import pathlib
import re

ARG = r'("[^"]*"|[\w.]+(?:\(\))?)'
GET = {'getInt': ', 0', 'getBoolean': ', false', 'getString': ', ""', 'getFloat': ', 0.0F', 'getDouble': ', 0.0D',
       'getLong': ', 0L', 'getByte': ', (byte) 0', 'getShort': ', (short) 0'}


def body_end(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
        i += 1
    return i


def convert_save(body, var, provider):
    if provider:
        body = re.sub(r'super\.saveAdditional\(' + var + r',\s*' + provider + r'\)', 'super.saveAdditional(' + var + ')', body)
    body = re.sub(r'\b' + var + r'\.putUUID\(', var + '.store(UUIDUtil.CODEC_PLACEHOLDER, ', body)
    body = re.sub(var + r'\.store\(UUIDUtil\.CODEC_PLACEHOLDER, ' + ARG + r',', lambda m: var + '.store(' + m.group(1) + ', UUIDUtil.CODEC,', body)
    body = re.sub(r'\b' + var + r'\.put\(', 'Values.put(' + var + ', ', body)
    return body


def convert_load(body, var, provider):
    if provider:
        body = re.sub(r'super\.loadAdditional\(' + var + r',\s*' + provider + r'\)', 'super.loadAdditional(' + var + ')', body)
        body = re.sub(r'\b' + provider + r'\b', var + '.lookup()', body)
    for name, default in GET.items():
        body = re.sub(r'\b' + var + r'\.' + name + r'\(' + ARG + r'\)', lambda m, n=name, d=default: var + '.' + n + 'Or(' + m.group(1) + d + ')', body)
    body = re.sub(r'\b' + var + r'\.hasUUID\(' + ARG + r'\)', lambda m: var + '.read(' + m.group(1) + ', UUIDUtil.CODEC).isPresent()', body)
    body = re.sub(r'\b' + var + r'\.getUUID\(' + ARG + r'\)', lambda m: var + '.read(' + m.group(1) + ', UUIDUtil.CODEC).orElseThrow()', body)
    body = re.sub(r'\b' + var + r'\.contains\(' + ARG + r'(?:,\s*[\w.]+)?\)', lambda m: 'Values.contains(' + var + ', ' + m.group(1) + ')', body)
    body = re.sub(r'\b' + var + r'\.getCompound\(' + ARG + r'\)', lambda m: 'Values.compound(' + var + ', ' + m.group(1) + ')', body)
    body = re.sub(r'\b' + var + r'\.getList\(' + ARG + r',\s*[\w.]+\)', lambda m: 'Values.list(' + var + ', ' + m.group(1) + ')', body)
    body = re.sub(r'\b' + var + r'\.get\(' + ARG + r'\)', lambda m: 'Values.get(' + var + ', ' + m.group(1) + ')', body)
    return body


HEADER = re.compile(r'void (addAdditionalSaveData|saveAdditional|readAdditionalSaveData|loadAdditional)\((?:@\w+ )?CompoundTag (\w+)(?:,\s*(?:net\.minecraft\.core\.)?HolderLookup\.(?:@\w+ )?Provider (\w+))?\)\s*\{')
changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    out, pos, hit = [], 0, False
    for m in HEADER.finditer(text):
        name, var, provider = m.group(1), m.group(2), m.group(3)
        save = name in ('addAdditionalSaveData', 'saveAdditional')
        end = body_end(text, m.end())
        body = text[m.end():end]
        body = convert_save(body, var, provider) if save else convert_load(body, var, provider)
        out.append(text[pos:m.start()])
        out.append('void ' + name + '(' + ('ValueOutput ' if save else 'ValueInput ') + var + ') {')
        out.append(body)
        pos = end
        hit = True
    if not hit:
        continue
    out.append(text[pos:])
    new = ''.join(out)
    imports = ['net.minecraft.world.level.storage.ValueInput', 'net.minecraft.world.level.storage.ValueOutput']
    if 'UUIDUtil.CODEC' in new:
        imports.append('net.minecraft.core.UUIDUtil')
    if 'Values.' in new and 'server.misc.Values' not in new and path.stem != 'Values':
        imports.append('com.github.alexthe666.rats.server.misc.Values')
    for imp in imports:
        if 'import ' + imp + ';' not in new:
            new = re.sub(r'(\npackage [\w.]+;\n)', r'\1\nimport ' + imp + ';', new, count=1)
    path.write_text(new, encoding='utf-8')
    changed += 1
print(changed, 'files changed')
