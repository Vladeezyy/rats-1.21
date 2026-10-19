"""SimpleChannel messages of Rats -> NeoForge payloads (one-off).

Each record keeps encode/decode/Handler.handle; it gains TYPE, CODEC and type(); handlers take an IPayloadContext.
Sends go through RatsNetworkHandler helpers that pick the right receivers (the original sent per-entity state to
every player of the server). Usage: python -I tools/port/port_network.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
BASE = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')
MSG = os.path.join(BASE, 'server', 'message')


def snake(name):
    name = name[:-len('Packet')] if name.endswith('Packet') else name
    return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()


def add_import(t, imp):
    if f'import {imp};' in t:
        return t
    return re.sub(r'(package [^;]+;\n)', lambda m: m.group(1) + f'\nimport {imp};', t, count=1)


for f in sorted(os.listdir(MSG)):
    if not f.endswith('Packet.java'):
        continue
    p = os.path.join(MSG, f)
    t = open(p, encoding='utf-8').read()
    cls = f[:-5]
    m = re.search(r'public record ' + cls + r'\(([^)]*)\)\s*\{', t)
    assert m, cls
    head = (f'public record {cls}({m.group(1)}) implements CustomPacketPayload {{\n\n'
            f'\tpublic static final Type<{cls}> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "{snake(cls)}"));\n'
            f'\tpublic static final StreamCodec<RegistryFriendlyByteBuf, {cls}> CODEC = StreamCodec.of((buf, packet) -> encode(packet, buf), {cls}::decode);\n\n'
            f'\t@Override\n\tpublic Type<{cls}> type() {{\n\t\treturn TYPE;\n\t}}\n')
    t = t[:m.start()] + head + t[m.end():]
    t = re.sub(r'public static (\w+) decode\(FriendlyByteBuf buf\)', r'public static \1 decode(RegistryFriendlyByteBuf buf)', t)
    t = re.sub(r'public static void encode\((\w+) packet, FriendlyByteBuf buf\)', r'public static void encode(\1 packet, RegistryFriendlyByteBuf buf)', t)
    t = re.sub(r'Supplier<NetworkEvent\.Context> \w+', 'IPayloadContext context', t)
    t = re.sub(r'\bcontext\.get\(\)', 'context', t)
    t = t.replace('context.getSender()', '((ServerPlayer) context.player())')
    t = re.sub(r'[ \t]*context\.setPacketHandled\(true\);\n', '', t)
    t = re.sub(r'import net\.neoforged\.neoforge\.network\.NetworkEvent;\n', '', t)
    if 'Supplier<' not in t:
        t = re.sub(r'import java\.util\.function\.Supplier;\n', '', t)
    for imp in ('net.minecraft.network.RegistryFriendlyByteBuf', 'net.minecraft.network.codec.StreamCodec',
                'net.minecraft.network.protocol.common.custom.CustomPacketPayload', 'net.minecraft.resources.ResourceLocation',
                'net.neoforged.neoforge.network.handling.IPayloadContext', 'com.github.alexthe666.rats.RatsMod',
                'net.minecraft.server.level.ServerPlayer'):
        t = add_import(t, imp)
    open(p, 'w', encoding='utf-8').write(t)
    print('ported', cls)

# senders
RULES = [
    (r'RatsNetworkHandler\.CHANNEL\.sendToServer\(', 'RatsNetworkHandler.toServer('),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.PLAYER\.with\(\(\) -> ([^)]+(?:\([^()]*\))?)\), ', r'RatsNetworkHandler.toPlayer(\1, '),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.PLAYER\.with\(event::getPlayer\), ', r'RatsNetworkHandler.toPlayer(event.getPlayer(), '),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.TRACKING_ENTITY_AND_SELF\.with\(\(\) -> ([\w.]+)\), ', r'RatsNetworkHandler.toTrackingAndSelf(\1, '),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.TRACKING_ENTITY\.with\(\(\) -> ([\w.]+)\), ', r'RatsNetworkHandler.toTracking(\1, '),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.TRACKING_CHUNK\.with\(\(\) -> rat\.level\(\)\.getChunkAt\(rat\.blockPosition\(\)\)\), ', 'RatsNetworkHandler.toTracking(rat, '),
    # per-entity / per-block state: only the players that see it
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), new UpdateRatFluidPacket\(this\.rat\.getId\(\)', 'RatsNetworkHandler.toTracking(this.rat, new UpdateRatFluidPacket(this.rat.getId()'),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), new SyncThrownBlockPacket\(thrownBlock\.getId\(\)', 'RatsNetworkHandler.toTracking(thrownBlock, new SyncThrownBlockPacket(thrownBlock.getId()'),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), new UpdateCurdlerFluidPacket\(this\.targetBlock\.asLong\(\)', 'RatsNetworkHandler.toTrackingBlock(this.rat.level(), this.targetBlock, new UpdateCurdlerFluidPacket(this.targetBlock.asLong()'),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), new UpdateCurdlerFluidPacket\(this\.getBlockPos\(\)\.asLong\(\)', 'RatsNetworkHandler.toTrackingBlock(this.getLevel(), this.getBlockPos(), new UpdateCurdlerFluidPacket(this.getBlockPos().asLong()'),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), new UpdateCurdlerFluidPacket\(pos\.asLong\(\)', 'RatsNetworkHandler.toTrackingBlock(level, pos, new UpdateCurdlerFluidPacket(pos.asLong()'),
    (r'RatsNetworkHandler\.CHANNEL\.send\(PacketDistributor\.ALL\.noArg\(\), ', 'RatsNetworkHandler.toAll('),
]
for d, _, fs in os.walk(BASE):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            t = open(p, encoding='utf-8').read()
            o = t
            for a, b in RULES:
                t = re.sub(a, b, t)
            if t != o:
                t = re.sub(r'import net\.neoforged\.neoforge\.network\.PacketDistributor;\n', '', t)
                open(p, 'w', encoding='utf-8').write(t)
            for i, line in enumerate(t.splitlines(), 1):
                if 'CHANNEL.send' in line:
                    print('manual', os.path.relpath(p, BASE), i, line.strip())
