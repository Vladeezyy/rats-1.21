"""Turns the SimpleChannel messages of the original into NeoForge 1.21.1 payloads (one-off, already applied).

Each message keeps its fields, read/write and handler; it gains TYPE, CODEC and type(). Handlers take an
IPayloadContext. Usage: python -I tools/port_network.py .
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
BASE = os.path.join(ROOT, 'neoforge', 'src', 'main', 'java', 'com', 'github', 'alexthe666', 'rats')
FILES = [os.path.join(BASE, 'message', f) for f in os.listdir(os.path.join(BASE, 'message'))
         if f.startswith('Message') and f != 'MessageHandler.java']
FILES.append(os.path.join(BASE, 'entity', 'props', 'SyncEntityData.java'))

NEW_IMPORTS = [
    'net.minecraft.network.RegistryFriendlyByteBuf',
    'net.minecraft.network.codec.StreamCodec',
    'net.minecraft.network.protocol.common.custom.CustomPacketPayload',
    'net.minecraft.resources.ResourceLocation',
    'net.neoforged.neoforge.network.handling.IPayloadContext',
    'com.github.alexthe666.iceandfire.IceAndFire',
]


def snake(name):
    name = name[len('Message'):] if name.startswith('Message') else name
    return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()


for path in FILES:
    with open(path, encoding='utf-8', newline='') as f:
        text = f.read()
    if 'CustomPacketPayload' in text:
        continue
    nl = '\r\n' if '\r\n' in text else '\n'
    cls = os.path.basename(path)[:-len('.java')]
    header = (f'public class {cls} implements CustomPacketPayload {{{nl}'
              f'    public static final Type<{cls}> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "{snake(cls)}"));{nl}'
              f'    public static final StreamCodec<RegistryFriendlyByteBuf, {cls}> CODEC = StreamCodec.of((buf, message) -> write(message, buf), {cls}::read);{nl}{nl}'
              f'    @Override{nl}'
              f'    public Type<{cls}> type() {{{nl}'
              f'        return TYPE;{nl}'
              f'    }}{nl}')
    text, n = re.subn(r'public class ' + cls + r'\s*\{\r?\n', header, text, count=1)
    assert n == 1, path
    text = re.sub(r'final Supplier<NetworkEvent\.Context> \w+', 'final IPayloadContext context', text)
    text = re.sub(r'Supplier<NetworkEvent\.Context> \w+', 'IPayloadContext context', text)
    text = re.sub(r'[ \t]*NetworkEvent\.Context context = \w+\.get\(\);\r?\n', '', text)
    text = re.sub(r'\b(context|contextSupplier)\.get\(\)', 'context', text)
    text = text.replace('context.getSender()', 'context.player()')
    text = text.replace('context.getDirection().getReceptionSide()', 'context.flow().getReceptionSide()')
    text = re.sub(r'[ \t]*context\.setPacketHandled\(true\);\r?\n', '', text)
    text = re.sub(r'import net\.neoforged\.neoforge\.network\.NetworkEvent;\r?\n', '', text)
    if 'Supplier' not in re.sub(r'import java\.util\.function\.Supplier;', '', text):
        text = re.sub(r'import java\.util\.function\.Supplier;\r?\n', '', text)
    imports = ''.join(f'import {i};{nl}' for i in NEW_IMPORTS if f'import {i};' not in text)
    text = re.sub(r'(package [^;]+;\r?\n\r?\n)', lambda m: m.group(1) + imports, text, count=1)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(text)
    print('ported', cls)
