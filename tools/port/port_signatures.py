"""1.21.2 - 26.1 override signatures: the declaration gets the new parameters and the super call inside it passes them.

python3 tools/port/port_signatures.py      (repo root; common/src/main/java)
"""
import pathlib
import re

P = r'(?:@\w+(?:\.\w+)* )?(?:final )?'
# name: (declaration regex with groups for the kept parameter names, new parameter list, super-call rewrite)
RULES = [
    ('doHurtTarget', r'doHurtTarget\(' + P + r'Entity (\w+)\)', lambda m: 'doHurtTarget(ServerLevel level, Entity ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.doHurtTarget\((\w+)\)', r'super.doHurtTarget(level, \1)', b)),
    ('customServerAiStep', r'customServerAiStep\(\)', lambda m: 'customServerAiStep(ServerLevel level)',
     lambda b, m: b.replace('super.customServerAiStep()', 'super.customServerAiStep(level)')),
    ('isInvulnerableTo', r'isInvulnerableTo\(' + P + r'DamageSource (\w+)\)', lambda m: 'isInvulnerableTo(ServerLevel level, DamageSource ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.isInvulnerableTo\((\w+)\)', r'super.isInvulnerableTo(level, \1)', b)),
    ('causeFallDamage', r'causeFallDamage\(' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'DamageSource (\w+)\)', lambda m: 'causeFallDamage(double ' + m.group(1) + ', float ' + m.group(2) + ', DamageSource ' + m.group(3) + ')', None),
    ('calculateFallDamage', r'calculateFallDamage\(' + P + r'float (\w+), ' + P + r'float (\w+)\)', lambda m: 'calculateFallDamage(double ' + m.group(1) + ', float ' + m.group(2) + ')', None),
    ('getBaseExperienceReward', r'getBaseExperienceReward\(\)', lambda m: 'getBaseExperienceReward(ServerLevel level)',
     lambda b, m: b.replace('super.getBaseExperienceReward()', 'super.getBaseExperienceReward(level)')),
    ('interact', r'InteractionResult interact\(' + P + r'Player (\w+), ' + P + r'InteractionHand (\w+)\)', lambda m: 'InteractionResult interact(Player ' + m.group(1) + ', InteractionHand ' + m.group(2) + ', Vec3 location)',
     lambda b, m: re.sub(r'super\.interact\((\w+), (\w+)\)', r'super.interact(\1, \2, location)', b)),
    ('applyEffectTick', r'applyEffectTick\(' + P + r'LivingEntity (\w+), ' + P + r'int (\w+)\)', lambda m: 'applyEffectTick(ServerLevel serverLevel, LivingEntity ' + m.group(1) + ', int ' + m.group(2) + ')',
     lambda b, m: re.sub(r'super\.applyEffectTick\((\w+), (\w+)\)', r'super.applyEffectTick(serverLevel, \1, \2)', b)),
    ('startRiding', r'boolean startRiding\(' + P + r'Entity (\w+), ' + P + r'boolean (\w+)\)', lambda m: 'boolean startRiding(Entity ' + m.group(1) + ', boolean ' + m.group(2) + ', boolean sendEventAndTriggers)',
     lambda b, m: re.sub(r'super\.startRiding\((\w+), (\w+)\)', r'super.startRiding(\1, \2, sendEventAndTriggers)', b)),
    ('destroy', r'void destroy\(' + P + r'DamageSource (\w+)\)', lambda m: 'void destroy(ServerLevel level, DamageSource ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.destroy\((\w+)\)', r'super.destroy(level, \1)', b)),
    ('getAnalogOutputSignal', r'getAnalogOutputSignal\(' + P + r'BlockState (\w+), ' + P + r'Level (\w+), ' + P + r'BlockPos (\w+)\)', lambda m: 'getAnalogOutputSignal(BlockState ' + m.group(1) + ', Level ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Direction direction)', None),
    ('fallOn', r'fallOn\(' + P + r'Level (\w+), ' + P + r'BlockState (\w+), ' + P + r'BlockPos (\w+), ' + P + r'Entity (\w+), ' + P + r'float (\w+)\)', lambda m: 'fallOn(Level ' + m.group(1) + ', BlockState ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Entity ' + m.group(4) + ', double ' + m.group(5) + ')', None),
    ('entityInside', r'entityInside\(' + P + r'BlockState (\w+), ' + P + r'Level (\w+), ' + P + r'BlockPos (\w+), ' + P + r'Entity (\w+)\)', lambda m: 'entityInside(BlockState ' + m.group(1) + ', Level ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Entity ' + m.group(4) + ', InsideBlockEffectApplier effectApplier, boolean isPrecise)',
     lambda b, m: re.sub(r'super\.entityInside\((\w+), (\w+), (\w+), (\w+)\)', r'super.entityInside(\1, \2, \3, \4, effectApplier, isPrecise)', b)),
    ('pickUpItem', r'void pickUpItem\(' + P + r'ItemEntity (\w+)\)', lambda m: 'void pickUpItem(ServerLevel level, ItemEntity ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.pickUpItem\((\w+)\)', r'super.pickUpItem(level, \1)', b)),
    ('canChangeDimensions', r'canChangeDimensions\(', lambda m: 'canTeleport(', lambda b, m: b.replace('super.canChangeDimensions(', 'super.canTeleport(')),
]
IMPORTS = {'ServerLevel': 'net.minecraft.server.level.ServerLevel', 'Vec3': 'net.minecraft.world.phys.Vec3', 'Direction': 'net.minecraft.core.Direction',
           'InsideBlockEffectApplier': 'net.minecraft.world.entity.InsideBlockEffectApplier'}


def body_end(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    new = text
    for name, decl, header, body_fix in RULES:
        out, pos = '', 0
        for m in re.finditer(r'(?<=\s)' + decl + r'(\s*(?:throws [\w, ]+)?\s*)\{', new):
            end = body_end(new, m.end())
            body = new[m.end():end]
            if body_fix:
                body = body_fix(body, m)
            out += new[pos:m.start()] + header(m) + m.group(m.lastindex) + '{' + body
            pos = end
        new = out + new[pos:]
    new = re.sub(r'\.isDay\(\)', '.isBrightOutside()', new)
    new = re.sub(r'\.isNight\(\)', '.isDarkOutside()', new)
    new = re.sub(r'\.canChangeDimensions\(', '.canTeleport(', new)
    if new != text:
        for simple, fqn in IMPORTS.items():
            if re.search(r'\b' + simple + r'\b', new) and 'import ' + fqn + ';' not in new and not re.search(r'^import [\w.]*\.' + simple + ';', new, re.M):
                new = re.sub(r'^(package [\w.]+;\n)', r'\1import ' + fqn + ';\n', new, count=1, flags=re.M)
        path.write_text(new, encoding='utf-8')
        changed += 1
print(changed, 'files changed')
