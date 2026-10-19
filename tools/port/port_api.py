"""Mechanical 1.20.1 -> 1.21.1 Minecraft API changes over src/main/java (one-off, already applied).

Each rule only rewrites code with a direct counterpart; the compiler finds the rest.
Usage: python -I tools/port_api.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')


def body_span(text, start):
    """Span of the {...} block that starts at or after start."""
    i = text.index('{', start)
    depth = 0
    for j in range(i, len(text)):
        if text[j] == '{':
            depth += 1
        elif text[j] == '}':
            depth -= 1
            if depth == 0:
                return i, j + 1
    raise ValueError


def add_import(text, imp):
    if re.search(r'import ' + re.escape(imp) + r';', text):
        return text
    nl = '\r\n' if '\r\n' in text else '\n'
    return re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + f'{nl}import {imp};', text, count=1)


def tooltips(t):
    pat = re.compile(r'appendHoverText\(((?:@\w+ )*)ItemStack (\w+), ((?:@\w+ )*)(?:Level|BlockGetter) (\w+),')
    if pat.search(t):
        t = pat.sub(r'appendHoverText(\1ItemStack \2, \3Item.TooltipContext \4,', t)
        t = add_import(t, 'net.minecraft.world.item.Item')
    return t


def synched_data(t):
    sig = re.compile(r'protected void defineSynchedData\(\)\s*\{')
    pos = 0
    changed = False
    while True:
        m = sig.search(t, pos)
        if not m:
            break
        b0, b1 = body_span(t, m.start())
        body = t[b0:b1]
        body = body.replace('super.defineSynchedData();', 'super.defineSynchedData(builder);')
        body = re.sub(r'\b(?:this\.)?(?:entityData|getEntityData\(\))\.define\(', 'builder.define(', body)
        t = t[:m.start()] + 'protected void defineSynchedData(SynchedEntityData.Builder builder) ' + body + t[b1:]
        pos = m.start() + 10
        changed = True
    if changed:
        t = add_import(t, 'net.minecraft.network.syncher.SynchedEntityData')
    return t


def remove_method(t, pattern):
    """Removes methods whose signature matches pattern, with their annotations."""
    pat = re.compile(r'(?:[ \t]*@\w+(?:\([^)]*\))?\s*)*[ \t]*' + pattern)
    while True:
        m = pat.search(t)
        if not m:
            return t
        b0, b1 = body_span(t, m.end() - 1)
        end = b1
        while end < len(t) and t[end] in '\r\n':
            end += 1
            if t[end - 1] == '\n':
                break
        t = t[:m.start()] + t[end:]


def spawn_packets(t):
    t = remove_method(t, r'public (?:@NotNull )?Packet<(?:ClientGamePacketListener|\?)> getAddEntityPacket\(\)\s*\{')
    t = re.sub(r'import net\.neoforged\.neoforge\.network\.NetworkHooks;\r?\n', '', t)
    # client spawn constructors of the original (PlayMessages.SpawnEntity)
    pat = re.compile(r'[ \t]*public \w+\(PlayMessages\.SpawnEntity \w+, Level \w+\)\s*\{')
    while True:
        m = pat.search(t)
        if not m:
            break
        b0, b1 = body_span(t, m.start())
        end = b1
        while end < len(t) and t[end] in '\r\n':
            end += 1
        t = t[:m.start()] + t[end:]
    t = re.sub(r'import net\.neoforged\.neoforge\.network\.PlayMessages;\r?\n', '', t)
    t = re.sub(r'\.setCustomClientFactory\([\w:.]+\)', '', t)
    return t


def open_screen(t):
    t = re.sub(r'NetworkHooks\.openScreen\(\((ServerPlayer)\) (\w+), ', r'\2.openMenu(', t)
    t = re.sub(r'NetworkHooks\.openScreen\((\w+), ', r'\1.openMenu(', t)
    return t


def block_entities(t):
    if 'BlockEntity' not in t:
        return t
    reps = [
        (r'public void load\(((?:@\w+ )*)CompoundTag (\w+)\)', r'protected void loadAdditional(\1CompoundTag \2, HolderLookup.@NotNull Provider registries)'),
        (r'super\.load\((\w+)\);', r'super.loadAdditional(\1, registries);'),
        (r'(protected|public) void saveAdditional\(((?:@\w+ )*)CompoundTag (\w+)\)', r'\1 void saveAdditional(\2CompoundTag \3, HolderLookup.@NotNull Provider registries)'),
        (r'super\.saveAdditional\((\w+)\);', r'super.saveAdditional(\1, registries);'),
        (r'public (@NotNull )?CompoundTag getUpdateTag\(\)', r'public \1CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries)'),
        (r'public void onDataPacket\(Connection (\w+), ClientboundBlockEntityDataPacket (\w+)\)', r'public void onDataPacket(Connection \1, ClientboundBlockEntityDataPacket \2, HolderLookup.@NotNull Provider registries)'),
        (r'public void handleUpdateTag\(CompoundTag (\w+)\)', r'public void handleUpdateTag(CompoundTag \1, HolderLookup.@NotNull Provider registries)'),
        (r'\bsaveWithFullMetadata\(\)', 'saveWithFullMetadata(registries)'),
        (r'\bsaveWithoutMetadata\(\)', 'saveWithoutMetadata(registries)'),
        (r'ContainerHelper\.loadAllItems\((\w+), ([\w.]+)\)', r'ContainerHelper.loadAllItems(\1, \2, registries)'),
        (r'ContainerHelper\.saveAllItems\((\w+), ([\w.]+)\)', r'ContainerHelper.saveAllItems(\1, \2, registries)'),
        (r'ContainerHelper\.saveAllItems\((\w+), ([\w.]+), (true|false)\)', r'ContainerHelper.saveAllItems(\1, \2, \3, registries)'),
        (r'\bthis\.load\(([\w.()]+)\);', r'this.loadAdditional(\1, registries);'),
        (r'\bload\((\w+)\.getTag\(\)\);', r'loadAdditional(\1.getTag(), registries);'),
    ]
    old = t
    for a, b in reps:
        t = re.sub(a, b, t)
    if t != old:
        t = add_import(t, 'net.minecraft.core.HolderLookup')
        if '@NotNull' in t:
            t = add_import(t, 'org.jetbrains.annotations.NotNull')
    return t


VERTEX = [(r'\.vertex\(', '.addVertex('), (r'\.color\(', '.setColor('), (r'\.uv\(', '.setUv('),
          (r'\.uv2\(', '.setLight('), (r'\.overlayCoords\(', '.setOverlay('), (r'\.normal\(', '.setNormal('),
          (r'\.endVertex\(\)', '')]


def vertex_chains(t):
    def fix(m):
        s = m.group(0)
        for a, b in VERTEX:
            s = re.sub(a, b, s)
        return s
    # a vertex statement: from a .vertex( call to the ; after .endVertex()
    return re.sub(r'\.vertex\([^;]*?\.endVertex\(\)', fix, t, flags=re.S)


def simple(t):
    reps = [
        (r'\bBlockPathTypes\b', 'PathType'),
        (r'import net\.minecraft\.world\.level\.pathfinder\.PathType;', 'import net.minecraft.world.level.pathfinder.PathType;'),
        (r'AttributeModifier\.Operation\.ADDITION\b', 'AttributeModifier.Operation.ADD_VALUE'),
        (r'AttributeModifier\.Operation\.MULTIPLY_BASE\b', 'AttributeModifier.Operation.ADD_MULTIPLIED_BASE'),
        (r'AttributeModifier\.Operation\.MULTIPLY_TOTAL\b', 'AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL'),
        (r'\.setSecondsOnFire\(', '.igniteForSeconds('),
        (r'(?<![\w.])setSecondsOnFire\(', 'igniteForSeconds('),
        (r'public int getExperienceReward\(\)', 'protected int getBaseExperienceReward()'),
        (r'Tags\.Items\.STONE\b', 'Tags.Items.STONES'), (r'Tags\.Items\.GRAVEL\b', 'Tags.Items.GRAVELS'),
        (r'Tags\.Items\.COBBLESTONE\b', 'Tags.Items.COBBLESTONES'), (r'Tags\.Items\.STRING\b', 'Tags.Items.STRINGS'),
        (r'Tags\.Items\.SAND\b', 'Tags.Items.SANDS'), (r'Tags\.Items\.GLASS\b', 'Tags.Items.GLASS_BLOCKS'),
        (r'Tags\.Items\.LEATHER\b', 'Tags.Items.LEATHERS'), (r'Tags\.Items\.SLIMEBALLS\b', 'Tags.Items.SLIME_BALLS'),
        (r'Tags\.Items\.OBSIDIAN\b', 'Tags.Items.OBSIDIANS'), (r'Tags\.Items\.GUNPOWDER\b', 'Tags.Items.GUNPOWDERS'),
        (r'Tags\.Items\.BONES\b', 'Tags.Items.BONES'),
        (r'Tags\.Blocks\.STONE\b', 'Tags.Blocks.STONES'), (r'Tags\.Blocks\.GRAVEL\b', 'Tags.Blocks.GRAVELS'),
        (r'Tags\.Blocks\.COBBLESTONE\b', 'Tags.Blocks.COBBLESTONES'), (r'Tags\.Blocks\.SAND\b', 'Tags.Blocks.SANDS'),
        (r'Tags\.Blocks\.GLASS\b', 'Tags.Blocks.GLASS_BLOCKS'), (r'Tags\.Blocks\.OBSIDIAN\b', 'Tags.Blocks.OBSIDIANS'),
        (r'Minecraft\.getInstance\(\)\.getFrameTime\(\)', 'Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)'),
        (r'\b(mc|minecraft|Minecraft\.getInstance\(\))\.getFrameTime\(\)', r'\1.getTimer().getGameTimeDeltaPartialTick(false)'),
    ]
    for a, b in reps:
        t = re.sub(a, b, t)
    if 'PathType' in t and 'net.minecraft.world.level.pathfinder.PathType;' not in t:
        t = re.sub(r'import net\.minecraft\.world\.level\.pathfinder\.BlockPathTypes;', 'import net.minecraft.world.level.pathfinder.PathType;', t)
    return t


def model_colors(t):
    """renderToBuffer/render(..., float r, g, b, a) -> (..., int color)."""
    def fix(m):
        r, g, b, a = (x.strip() for x in m.group(2, 3, 4, 5))
        if all(re.fullmatch(r'1(\.0+)?[Ff]?', x) for x in (r, g, b, a)):
            col = '-1'
        else:
            col = f'FastColor.ARGB32.colorFromFloat({a}, {r}, {g}, {b})'
        return f'{m.group(1)}, {col})'
    num = r'([^,()]+(?:\([^()]*\))?[^,()]*)'
    pat = re.compile(r'(\.(?:renderToBuffer|render)\([^;]*?(?:OverlayTexture\.NO_OVERLAY|packedOverlay|\w*[Oo]verlay\w*|\w+)), ' + num + ', ' + num + ', ' + num + ', ' + num + r'\)(?=;)')
    new = pat.sub(fix, t)
    if new != t and 'FastColor' in new:
        new = add_import(new, 'net.minecraft.util.FastColor')
    return new


def model_color_signatures(t):
    t = re.sub(r'public void renderToBuffer\(((?:@\w+ )*)PoseStack (\w+), ((?:@\w+ )*)VertexConsumer (\w+), int (\w+), int (\w+), float (\w+), float (\w+), float (\w+), float (\w+)\)',
               r'public void renderToBuffer(\1PoseStack \2, \3VertexConsumer \4, int \5, int \6, int color)', t)
    return t


def port(path, t):
    t = tooltips(t)
    t = synched_data(t)
    t = spawn_packets(t)
    t = open_screen(t)
    if os.sep + 'tile' + os.sep in path or (os.sep + 'block' + os.sep + 'entity' + os.sep) in path or 'BlockEntity' in os.path.basename(path) or 'TileEntity' in os.path.basename(path):
        t = block_entities(t)
    if os.sep + 'client' + os.sep in path or os.sep + 'compat' + os.sep in path:
        t = vertex_chains(t)
        t = model_color_signatures(t)
        t = model_colors(t)
    t = simple(t)
    return t


for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            with open(p, encoding='utf-8', newline='') as fh:
                old = fh.read()
            new = port(p, old)
            if new != old:
                with open(p, 'w', encoding='utf-8', newline='') as fh:
                    fh.write(new)
                print('ported', os.path.relpath(p, SRC))
