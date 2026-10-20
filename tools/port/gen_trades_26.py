"""26.x villager trades are data (villager_trade, trade_set, villager_trade tags). The 1.21.1 port's trades
(RatsEvents.addPetShopTrades, PlagueDoctorTrades), same items, counts, uses, xp and price multiplier:

    python3 tools/port/gen_trades_26.py common/src/generated/resources
"""
import json
import pathlib
import sys

root = pathlib.Path(sys.argv[1]) / 'data' / 'rats'


def emerald_for(item, cost, uses, xp):
    # EmeraldForItems(item, cost, maxUses, xp): the trader buys `cost` items for an emerald
    return {'wants': {'id': item, 'count': cost}, 'gives': {'id': 'minecraft:emerald'}, 'max_uses': uses, 'xp': xp, 'reputation_discount': 0.05}


def items_for(item, emeralds, count, uses, xp):
    # ItemsForEmeralds(item, emeraldCost, numberOfItems, [maxUses = 12], xp)
    return {'wants': {'id': 'minecraft:emerald', 'count': emeralds}, 'gives': {'id': item, 'count': count}, 'max_uses': uses, 'xp': xp, 'reputation_discount': 0.05}


def items_and_emeralds(item, item_count, emeralds, to, to_count, uses, xp, multiplier):
    # ItemsAndEmeraldsToItems: emeralds first, the item as the second cost (as 1.21.1's offer)
    return {'wants': {'id': 'minecraft:emerald', 'count': emeralds}, 'additional_wants': {'id': item, 'count': item_count},
            'gives': {'id': to, 'count': to_count}, 'max_uses': uses, 'xp': xp, 'reputation_discount': multiplier}


R = 'rats:'
PET_SHOP = {
    1: [('bone', emerald_for('minecraft:bone', 12, 12, 1)), ('rotten_flesh', emerald_for('minecraft:rotten_flesh', 11, 12, 1)),
        ('cod', emerald_for('minecraft:cod', 10, 12, 1)), ('string', emerald_for('minecraft:string', 12, 12, 1)),
        ('egg', emerald_for('minecraft:egg', 16, 12, 1)), ('raw_rat', emerald_for(R + 'raw_rat', 11, 12, 1)),
        ('cheese', items_for(R + 'cheese', 1, 5, 12, 1))],
    2: [('cooked_rat', items_for(R + 'cooked_rat', 1, 5, 12, 5)),
        ('plastic_waste', items_and_emeralds(R + 'garbage_pile', 10, 3, R + 'plastic_waste', 10, 12, 5, 0.05)),
        ('marbled_cheese_raw', items_for(R + 'marbled_cheese_raw', 1, 8, 12, 5)), ('garbage_pile', items_for(R + 'garbage_pile', 1, 6, 12, 5)),
        ('raw_plastic', emerald_for(R + 'raw_plastic', 5, 12, 5)), ('rat_flute', emerald_for(R + 'rat_flute', 1, 12, 5))],
    3: [('archeologist_hat', items_for(R + 'archeologist_hat', 12, 1, 12, 10)), ('santa_hat', items_for(R + 'santa_hat', 20, 1, 12, 15)),
        ('party_hat', items_for(R + 'party_hat', 20, 1, 12, 15)), ('halo_hat', items_for(R + 'halo_hat', 8, 1, 12, 10)),
        ('exterminator_hat', items_for(R + 'exterminator_hat', 14, 1, 12, 10))],
    4: [('rat_upgrade_basic', items_for(R + 'rat_upgrade_basic', 5, 1, 12, 20)), ('rat_upgrade_jury_rigged', items_for(R + 'rat_upgrade_jury_rigged', 16, 1, 12, 20)),
        ('rat_upgrade_blacklist', items_for(R + 'rat_upgrade_blacklist', 8, 1, 12, 20)), ('rat_upgrade_whitelist', items_for(R + 'rat_upgrade_whitelist', 8, 1, 12, 20)),
        ('rat_upgrade_speed', items_for(R + 'rat_upgrade_speed', 10, 1, 12, 20)), ('rat_upgrade_health', items_for(R + 'rat_upgrade_health', 10, 1, 12, 20)),
        ('rat_upgrade_armor', items_for(R + 'rat_upgrade_armor', 10, 1, 12, 20))],
    5: [('plague_doctorate', items_for(R + 'plague_doctorate', 10, 1, 12, 30)), ('plague_tome', items_for(R + 'plague_tome', 32, 1, 12, 30)),
        ('rat_papers', items_for(R + 'rat_papers', 5, 1, 12, 30)), ('charged_creeper_chunk', items_for(R + 'charged_creeper_chunk', 3, 1, 12, 30)),
        ('tiny_coin', items_for(R + 'tiny_coin', 1, 8, 12, 30))],
}
PLAGUE_DOCTOR = {
    # five of these per plague doctor
    'common': (5, [('raw_rat', emerald_for(R + 'raw_rat', 10, 12, 1)), ('bone', emerald_for('minecraft:bone', 10, 12, 1)),
                   ('rotten_flesh', emerald_for('minecraft:rotten_flesh', 10, 12, 1)), ('spider_eye', emerald_for('minecraft:spider_eye', 5, 12, 1)),
                   ('ghast_tear', emerald_for('minecraft:ghast_tear', 2, 12, 2)), ('phantom_membrane', emerald_for('minecraft:phantom_membrane', 4, 12, 2)),
                   ('poisonous_potato', emerald_for('minecraft:poisonous_potato', 2, 12, 2)), ('contaminated_food', emerald_for(R + 'contaminated_food', 5, 12, 2)),
                   ('cooked_rat', items_for(R + 'cooked_rat', 1, 5, 12, 1)), ('herb_bundle', items_and_emeralds('minecraft:poppy', 5, 1, R + 'herb_bundle', 3, 12, 2, 0.05)),
                   ('treacle', items_for(R + 'treacle', 1, 2, 12, 1)), ('garbage_pile', items_for(R + 'garbage_pile', 1, 4, 12, 3)),
                   ('cursed_garbage', items_for(R + 'cursed_garbage', 1, 2, 12, 3)), ('purified_garbage', items_for(R + 'purified_garbage', 1, 2, 12, 3)),
                   ('pied_wool', items_for(R + 'pied_wool', 3, 1, 5, 5)), ('plague_doctor_mask', items_for(R + 'plague_doctor_mask', 15, 1, 2, 5)),
                   ('rat_skull', items_for(R + 'rat_skull', 3, 1, 12, 3))]),
    # three different ones of these
    'rare': (3, [('plague_leech', items_for(R + 'plague_leech', 2, 1, 12, 5)), ('plague_stew', items_for(R + 'plague_stew', 5, 1, 12, 7)),
                 ('filth', items_for(R + 'filth', 3, 1, 12, 7)), ('purifying_liquid', items_for(R + 'purifying_liquid', 8, 1, 12, 7)),
                 ('token_fragment', items_for(R + 'token_fragment', 5, 1, 2, 5)), ('rat_upgrade_basic', items_for(R + 'rat_upgrade_basic', 1, 2, 12, 5)),
                 ('plague_essence', items_for(R + 'plague_essence', 2, 1, 12, 5)), ('golden_rat_skull', items_for(R + 'golden_rat_skull', 4, 1, 12, 7)),
                 ('plague_tome', items_for(R + 'plague_tome', 32, 1, 1, 15))]),
    # without the Ratlantis pack, a plague doctor that will despawn sells the Ratlantis machines
    'no_ratlantis': (3, [('upgrade_combiner', items_and_emeralds(R + 'rat_upgrade_god', 1, 40, R + 'upgrade_combiner', 1, 1, 30, 0.05)),
                         ('upgrade_separator', items_and_emeralds(R + 'rat_upgrade_jury_rigged', 1, 4, R + 'upgrade_separator', 1, 1, 30, 0.05)),
                         ('rat_upgrade_combined', items_for(R + 'rat_upgrade_combined', 6, 1, 12, 1))]),
}


def dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + '\n', encoding='utf-8')


def trade_set(name, amount, trades):
    ids = []
    for trade_name, trade in trades:
        dump(root / 'villager_trade' / name / (trade_name + '.json'), trade)
        ids.append(R + name + '/' + trade_name)
    dump(root / 'tags' / 'villager_trade' / (name + '.json'), {'values': ids})
    dump(root / 'trade_set' / (name + '.json'), {'amount': amount, 'trades': '#' + R + name, 'random_sequence': R + 'trade_set/' + name})


for level, trades in PET_SHOP.items():
    # two of each level, as 1.21.1's villagers
    trade_set('pet_shop_owner/level_' + str(level), 2, trades)
for name, (amount, trades) in PLAGUE_DOCTOR.items():
    trade_set('plague_doctor/' + name, amount, trades)
print('trades written')
