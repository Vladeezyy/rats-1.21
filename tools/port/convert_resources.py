"""Converts the 1.20.1 data of Rats (src/main/resources, src/generated/resources) to the 1.21.1 format in place.

Run once on the imported original data: python -I tools/convert_resources.py .
"""
import json
import os
import shutil
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
TREES = [os.path.join(ROOT, 'common', 'src', 'main', 'resources'), os.path.join(ROOT, 'common', 'src', 'generated', 'resources')]

DIR_RENAMES = {
    'advancements': 'advancement', 'recipes': 'recipe', 'loot_tables': 'loot_table', 'structures': 'structure',
}
TAG_DIR_RENAMES = {
    'items': 'item', 'blocks': 'block', 'entity_types': 'entity_type', 'fluids': 'fluid', 'game_events': 'game_event',
}
# forge:<path> -> c:<path> where the name changed in the common conventions of 1.21
C_RENAMES = {
    'string': 'strings', 'leather': 'leathers', 'stone': 'stones', 'slimeballs': 'slime_balls', 'obsidian': 'obsidians',
    'glass/colorless': 'glass_blocks/colorless', 'cobblestone': 'cobblestones', 'tools/bows': 'tools/bow',
    'tools/tridents': 'tools/spear', 'shears': 'tools/shear', 'raw_pork': 'foods/raw_meat', 'raw_mutton': 'foods/raw_meat',
    'raw_chicken': 'foods/raw_meat', 'raw_beef': 'foods/raw_meat', 'cooked_pork': 'foods/cooked_meat',
    'cooked_mutton': 'foods/cooked_meat', 'cooked_chicken': 'foods/cooked_meat', 'cooked_beef': 'foods/cooked_meat',
}
FORGE_TYPES = {  # loader object types, not tags
    'forge:add_spawns': 'neoforge:add_spawns', 'forge:add_features': 'neoforge:add_features',
    'forge:remove_spawns': 'neoforge:remove_spawns', 'forge:remove_features': 'neoforge:remove_features',
    'forge:mod_loaded': 'neoforge:mod_loaded', 'forge:not': 'neoforge:not', 'forge:and': 'neoforge:and',
    'forge:or': 'neoforge:or', 'forge:tag_empty': 'neoforge:tag_empty', 'forge:item_exists': 'neoforge:item_exists',
    'forge:true': 'neoforge:always', 'forge:false': 'neoforge:never',
    'forge:loot_table_id': 'neoforge:loot_table_id', 'forge:can_tool_perform_action': 'neoforge:can_item_perform_ability',
}


def c_tag(name):
    """forge:x or #forge:x -> c:y (#c:y)."""
    prefix = '#' if name.startswith('#') else ''
    path = name[len(prefix) + len('forge:'):]
    return prefix + 'c:' + C_RENAMES.get(path, path)


# ids renamed by vanilla since 1.20.1
RENAMED_IDS = {'minecraft:grass': 'minecraft:short_grass', 'minecraft:scute': 'minecraft:turtle_scute'}


def fix_strings(node):
    if isinstance(node, dict):
        return {k: fix_strings(v) for k, v in node.items()}
    if isinstance(node, list):
        return [fix_strings(v) for v in node]
    if isinstance(node, str):
        if node in FORGE_TYPES:
            return FORGE_TYPES[node]
        if node in RENAMED_IDS:
            return RENAMED_IDS[node]
        if node.startswith('forge:') or node.startswith('#forge:'):
            return c_tag(node)
    return node


NUMBER_PROVIDERS = ('minecraft:uniform', 'minecraft:clamped', 'minecraft:biased_to_bottom', 'minecraft:clamped_normal',
                    'minecraft:trapezoid', 'minecraft:weighted_list', 'minecraft:constant', 'uniform', 'clamped')


def flatten_providers(node):
    """1.20.5 dropped the "value" wrapper of int/float providers: {"type": uniform, "value": {min, max}} -> {"type", min, max}."""
    if isinstance(node, dict):
        if node.get('type') in NUMBER_PROVIDERS and isinstance(node.get('value'), dict) and len(node) == 2:
            merged = {'type': node['type']}
            merged.update(flatten_providers(node['value']))
            return merged
        return {k: flatten_providers(v) for k, v in node.items()}
    if isinstance(node, list):
        return [flatten_providers(v) for v in node]
    return node


# mob categories added by mods get the mod id as prefix in 1.21 (NeoForge enum extensions): biome spawners keys
SPAWN_CATEGORY_RENAMES = {'rats': 'rats:rats'}


def fix_conditions(obj):
    if isinstance(obj, dict) and 'conditions' in obj and isinstance(obj['conditions'], list) \
            and all(isinstance(c, dict) and 'type' in c for c in obj['conditions']):
        obj['neoforge:conditions'] = obj.pop('conditions')
    return obj


def fix_recipe(r):
    fix_conditions(r)
    res = r.get('result')
    if isinstance(res, str):
        r['result'] = {'id': res}
    elif isinstance(res, dict) and 'item' in res:
        res['id'] = res.pop('item')
        if 'nbt' in res:
            raise ValueError('recipe result with nbt')
    return r


def fix_item_predicate(p):
    if not isinstance(p, dict):
        return p
    if 'tag' in p:
        p['items'] = '#' + p.pop('tag')
    items = p.get('items')
    if isinstance(items, list) and len(items) == 1:
        p['items'] = items[0]
    if 'enchantments' in p:
        p.setdefault('predicates', {})['minecraft:enchantments'] = p.pop('enchantments')
    return p


def fix_damage_type(t):
    tags = []
    for key, tag in (('is_projectile', 'minecraft:is_projectile'), ('is_explosion', 'minecraft:is_explosion'),
                     ('is_fire', 'minecraft:is_fire'), ('bypasses_armor', 'minecraft:bypasses_armor'),
                     ('is_lightning', 'minecraft:is_lightning')):
        if key in t:
            tags.append({'id': tag, 'expected': t.pop(key)})
    if tags:
        t['tags'] = tags
    return t


def walk_advancement(node):
    if isinstance(node, dict):
        if 'icon' in node and isinstance(node['icon'], dict) and 'item' in node['icon']:
            node['icon']['id'] = node['icon'].pop('item')
        if 'items' in node and isinstance(node['items'], list) and node['items'] and isinstance(node['items'][0], dict):
            node['items'] = [fix_item_predicate(p) for p in node['items']]
        if 'damage' in node and isinstance(node['damage'], dict) and isinstance(node['damage'].get('type'), dict):
            fix_damage_type(node['damage']['type'])
        for v in node.values():
            walk_advancement(v)
    elif isinstance(node, list):
        for v in node:
            walk_advancement(v)
    return node


def walk_loot(node):
    if isinstance(node, dict):
        fn = node.get('function', '').replace('minecraft:', '')
        if fn == 'looting_enchant':
            node['function'] = 'minecraft:enchanted_count_increase'
            node['enchantment'] = 'minecraft:looting'
        elif fn == 'enchant_with_levels':
            if node.pop('treasure', False):
                node['options'] = '#minecraft:on_random_loot'
        elif fn in ('set_nbt',):
            raise ValueError('set_nbt in loot table')
        cond = node.get('condition', '').replace('minecraft:', '')
        if cond == 'random_chance_with_looting':
            chance = node.pop('chance')
            mult = node.pop('looting_multiplier')
            node['condition'] = 'minecraft:random_chance_with_enchanted_bonus'
            node['enchantment'] = 'minecraft:looting'
            node['unenchanted_chance'] = chance
            node['enchanted_chance'] = {'type': 'minecraft:linear', 'base': round(chance + mult, 6),
                                        'per_level_above_first': mult}
        elif cond == 'match_tool' and isinstance(node.get('predicate'), dict):
            fix_item_predicate(node['predicate'])
        for v in node.values():
            walk_loot(v)
    elif isinstance(node, list):
        for v in node:
            walk_loot(v)
    return node


def target_path(tree, path):
    """New location of a data file, relative to the tree."""
    rel = os.path.relpath(path, tree).replace('\\', '/')
    parts = rel.split('/')
    if parts[0] != 'data':
        return rel
    if parts[1] == 'forge':
        # forge's own data (the global loot modifier list) belongs to neoforge; only tags move to the common "c"
        parts[1] = 'neoforge' if len(parts) > 2 and parts[2] == 'loot_modifiers' else 'c'
    if len(parts) > 2:
        if parts[2] in DIR_RENAMES:
            parts[2] = DIR_RENAMES[parts[2]]
        elif parts[2] == 'forge' and len(parts) > 3:
            parts[2] = 'neoforge'
        elif parts[2] == 'tags' and len(parts) > 3:
            parts[3] = TAG_DIR_RENAMES.get(parts[3], parts[3])
            if parts[1] == 'c' and parts[3] != 'worldgen':
                tag = '/'.join(parts[4:])[:-len('.json')]
                parts = parts[:4] + (C_RENAMES.get(tag, tag) + '.json').split('/')
    return '/'.join(parts)


def merge_tag(dst, data):
    with open(dst, encoding='utf-8') as f:
        old = json.load(f)
    for v in data.get('values', []):
        if v not in old['values']:
            old['values'].append(v)
    return old


def main():
    moved = 0
    for tree in TREES:
        files = [os.path.join(d, f) for d, _, fs in os.walk(os.path.join(tree, 'data')) for f in fs]
        for path in files:
            new_rel = target_path(tree, path)
            dst = os.path.join(tree, new_rel)
            kind = new_rel.split('/')[2] if new_rel.count('/') > 2 else ''
            if path.endswith('.json'):
                with open(path, encoding='utf-8') as f:
                    data = json.load(f)
                data = flatten_providers(fix_strings(data))
                if new_rel.endswith('.json') and '/worldgen/biome/' in new_rel and isinstance(data.get('spawners'), dict):
                    data['spawners'] = {SPAWN_CATEGORY_RENAMES.get(k, k): v for k, v in data['spawners'].items()}
                if kind == 'recipe':
                    data = fix_recipe(data)
                elif kind == 'advancement':
                    data = walk_advancement(data)
                elif kind == 'loot_table':
                    data = walk_loot(data)
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                if os.path.abspath(dst) != os.path.abspath(path) and os.path.exists(dst) and kind == 'tags':
                    data = merge_tag(dst, data)
                if os.path.abspath(dst) != os.path.abspath(path):
                    os.remove(path)
                    moved += 1
                with open(dst, 'w', encoding='utf-8', newline='\n') as f:
                    json.dump(data, f, indent=2, ensure_ascii=False)
                    f.write('\n')
            elif os.path.abspath(dst) != os.path.abspath(path):
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                shutil.move(path, dst)
                moved += 1
        # drop empty directories left after moving
        for d, _, _ in sorted(os.walk(os.path.join(tree, 'data')), key=lambda t: -len(t[0])):
            if not os.listdir(d):
                os.rmdir(d)
    mcmeta = os.path.join(ROOT, 'common', 'src', 'generated', 'resources', 'pack.mcmeta')
    if os.path.exists(mcmeta):
        os.remove(mcmeta)
    print('moved', moved)


if __name__ == '__main__':
    main()
