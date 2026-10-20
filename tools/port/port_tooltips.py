"""1.21.5+: Item.appendHoverText(stack, context, TooltipDisplay, Consumer<Component>, flag) instead of a List<Component>."""
import pathlib
import re

HEADER = re.compile(r'appendHoverText\(((?:@\w+ )?(?:final )?ItemStack \w+), ((?:@\w+ )?(?:Item\.)?TooltipContext \w+), (?:@\w+ )?List<Component> (\w+), ((?:@\w+ )?TooltipFlag \w+)\)\s*\{')


def body_end(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    out, pos = [], 0
    for m in HEADER.finditer(text):
        var = m.group(3)
        end = body_end(text, m.end())
        body = text[m.end():end]
        body = re.sub(r'\b' + var + r'\.add\(', var + '.accept(', body)
        body = re.sub(r'\b' + var + r'\.addAll\(([^;]+)\);', var + r'::accept is not a list: \1;', body) if False else body
        body = re.sub(r'super\.appendHoverText\((\w+), (\w+), ' + var + r', (\w+)\)', r'super.appendHoverText(\1, \2, display, ' + var + r', \3)', body)
        out.append(text[pos:m.start()])
        out.append('appendHoverText(' + m.group(1) + ', ' + m.group(2) + ', TooltipDisplay display, Consumer<Component> ' + var + ', ' + m.group(4) + ') {')
        out.append(body)
        pos = end
    if not out:
        continue
    out.append(text[pos:])
    new = ''.join(out)
    for imp in ['net.minecraft.world.item.component.TooltipDisplay', 'java.util.function.Consumer']:
        if 'import ' + imp + ';' not in new:
            new = re.sub(r'(\npackage [\w.]+;\n)', r'\1\nimport ' + imp + ';', new, count=1)
    path.write_text(new, encoding='utf-8')
    changed += 1
print(changed, 'files changed')
