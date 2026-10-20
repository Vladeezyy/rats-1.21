"""1.21.2+: InteractionResultHolder<ItemStack> -> InteractionResult (a changed held item: heldItemTransformedTo)."""
import pathlib
import re

CONST = {'success': 'SUCCESS', 'sidedSuccess': 'SUCCESS', 'consume': 'CONSUME', 'pass': 'PASS', 'fail': 'FAIL'}


def args_end(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        depth += {'(': 1, ')': -1}.get(text[i], 0)
        i += 1
    return i


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    if 'InteractionResultHolder' not in text:
        continue
    new = re.sub(r'InteractionResultHolder<ItemStack>', 'InteractionResult', text)
    held = set(re.findall(r'ItemStack (\w+) = \w+\.getItemInHand\(\w+\);', new))
    out, pos = '', 0
    for m in re.finditer(r'InteractionResultHolder\.(success|sidedSuccess|consume|pass|fail)\(', new):
        end = args_end(new, m.end())
        inner = new[m.end():end - 1]
        stack = inner.split(',')[0].strip() if m.group(1) == 'sidedSuccess' else inner.strip()
        result = 'InteractionResult.' + CONST[m.group(1)]
        if stack not in held and not re.fullmatch(r'\w+\.getItemInHand\(\w+\)', stack) and m.group(1) in ('success', 'sidedSuccess', 'consume'):
            result += '.heldItemTransformedTo(' + stack + ')'
        out += new[pos:m.start()] + result
        pos = end
    new = out + new[pos:]
    new = new.replace('import net.minecraft.world.InteractionResultHolder;\n', '')
    if 'import net.minecraft.world.InteractionResult;' not in new:
        new = re.sub(r'^(package [\w.]+;\n)', r'\1import net.minecraft.world.InteractionResult;\n', new, count=1, flags=re.M)
    path.write_text(new, encoding='utf-8')
    changed += 1
print(changed, 'files changed')
