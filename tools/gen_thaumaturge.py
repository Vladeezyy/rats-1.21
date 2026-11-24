#!/usr/bin/env python3
"""Thaumaturge compat data (NeoForge only): python3 -I tools/gen_thaumaturge.py

Writes the built-in data pack neoforge/src/main/resources/data/rats/datapacks/thaumaturge, which
ThaumaturgeCompat registers only when Thaumaturge is loaded (without it NeoForge would warn about data map
files of a data map type that does not exist):
  data/rats/thaumaturge/aspect/caseus.json               the Caseus (cheese) aspect: Victus + Permutatio
  data/thaumaturge/data_maps/item/base_aspects.json      aspects of the items that have no plain recipe
  data/thaumaturge/data_maps/entity_type/entity_aspects.json  what the thaumometer reads from the mobs
Items crafted, smelted or stonecut from these get their aspects from the recipe (Thaumaturge derives them).
Item ids are checked against the en_us language file, mobs against the entity registries, so a renamed one fails
here and not in game.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PACK = ROOT / "neoforge/src/main/resources/data/rats/datapacks/thaumaturge"
LANG = ROOT / "common/src/generated/resources/assets/rats/lang/en_us.json"
ENTITY_REGISTRIES = [ROOT / "common/src/main/java/com/github/alexthe666/rats/registry" / f
                     for f in ("RatsEntityRegistry.java", "RatlantisEntityRegistry.java")]
# data pack format of the branch's Minecraft version (26.1.2: 101; since 1.21.9 min_format and max_format instead of pack_format)
PACK_FORMAT = {"min_format": 101, "max_format": 101}

CASEUS = {
    "tag": "caseus",
    "color": 0xF2B33D,
    "components": ["thaumaturge:victus", "thaumaturge:permutatio"],
    "texture": "rats:textures/aspects/caseus.png",
}

# item -> aspects (short names: thaumaturge:<name>, caseus: rats:caseus)
ITEMS = {
    # cheese
    "cheese": "caseus 5, victus 2",
    "blue_cheese": "caseus 5, victus 2, alkimia 2",
    "nether_cheese": "caseus 5, ignis 3, victus 1",
    "string_cheese": "caseus 3, fabrico 1",
    "creative_cheese": "caseus 50, praecantatio 20, ordo 20",
    "block_of_cheese": "caseus 25, victus 8",
    "block_of_blue_cheese": "caseus 25, victus 8, alkimia 8",
    "block_of_nether_cheese": "caseus 25, ignis 12, victus 4",
    "cheese_ore": "caseus 5, terra 5",
    "cheese_cannonball": "caseus 5, aversio 3, terra 2",
    "chunky_cheese_token": "caseus 10, desiderium 10, praecantatio 5",
    "token_fragment": "desiderium 3, praecantatio 1, caseus 1",
    "marbled_cheese_raw": "caseus 3, terra 5, ordo 1",
    "marbled_cheese": "caseus 2, terra 5, ordo 1",
    "black_marbled_cheese": "caseus 2, terra 5, tenebrae 2",
    "marbled_cheese_dirt": "caseus 1, terra 5",
    "marbled_cheese_grass": "caseus 1, terra 5, herba 2",
    # rats
    "raw_rat": "bestia 5, victus 5, perditio 1",
    "rat_pelt": "bestia 5, praemunio 2",
    "rat_paw": "bestia 5, motus 5, desiderium 2",
    "rat_skull": "mortuus 10, bestia 5, spiritus 5",
    "rat_nugget": "metallum 2, bestia 1",
    "tangled_rat_tails": "bestia 5, vinculum 5",
    "feral_rat_claw": "bestia 5, aversio 5",
    "psionic_rat_brain": "cognitio 10, sensus 10, alienis 5, bestia 3",
    "rat_papers": "cognitio 3, bestia 1",
    "rat_king_crown": "desiderium 15, metallum 10, bestia 10, ordo 5",
    "ratfish": "bestia 5, aqua 5, victus 3",
    # plague and garbage
    "plague_essence": "perditio 10, vitium 5, mortuus 5",
    "plague_leech": "bestia 3, victus 3, vitium 2",
    "filth": "perditio 5, vitium 2",
    "filth_corruption": "perditio 10, vitium 10, tenebrae 5",
    "contaminated_food": "perditio 5, vitium 3, victus 1",
    "plastic_waste": "perditio 3, fabrico 1",
    "raw_plastic": "fabrico 3, permutatio 1",
    "garbage_pile": "perditio 5, terra 3",
    "crimson_liquid": "alkimia 5, perditio 5",
    "purifying_liquid": "alkimia 5, ordo 5, victus 2",
    # plants and food
    "ratglove_petals": "herba 3, sensus 2",
    "ratglove_flower": "herba 5, sensus 2, victus 1",
    "herb_bundle": "herba 5, victus 2",
    "assorted_vegetables": "herba 5, victus 3",
    "centipede": "bestia 3, victus 3, motus 2",
    "little_black_worm": "bestia 2, terra 2, victus 1",
    "pirat_log": "herba 5, aqua 2",
    "pirat_wood": "herba 5, aqua 2",
    "stripped_pirat_log": "herba 5, aqua 2",
    "stripped_pirat_wood": "herba 5, aqua 2",
    "pirat_leaves": "herba 3",
    "pirat_sapling": "herba 5, victus 3, aqua 1",
    # Ratlantis
    "gem_of_ratlantis": "praecantatio 15, desiderium 10, vitreus 10, caseus 5",
    "idol_of_ratlantis": "praecantatio 20, desiderium 15, caseus 10, bestia 5",
    "raw_oratchalcum": "metallum 10, praecantatio 5",
    "oratchalcum_ingot": "metallum 10, praecantatio 5, ordo 2",
    "oratchalcum_ore": "metallum 10, praecantatio 5, terra 5",
    "ratlantean_gem_ore": "vitreus 10, praecantatio 10, terra 5",
    "ratlantean_flame": "ignis 10, praecantatio 5, spiritus 3",
    "ghost_pirat_ectoplasm": "spiritus 10, exanimis 5, aqua 3",
    "charged_creeper_chunk": "potentia 10, perditio 5, ignis 3",
    "arcane_technology": "machina 10, praecantatio 10, cognitio 5",
    "ancient_sawblade": "metallum 10, instrumentum 10, aversio 5",
    "dragon_wing": "volatus 10, bestia 5, ignis 5",
    "feathery_wing": "volatus 10, bestia 3",
    "ratbow_essence": "lux 10, praecantatio 5, sensus 5",
    "vial_of_sentience": "cognitio 15, spiritus 10, alkimia 10",
    "tiny_coin": "desiderium 2, metallum 1",
    # no plain recipe either (chef, crafting table, dropped or bought)
    "rat_arrow": "aversio 5, bestia 2",
    "treacle": "desiderium 3, victus 3, aqua 1",
    "plague_tome": "cognitio 10, vitium 10, mortuus 5",
    "confit_byaldi": "victus 10, herba 5, desiderium 3",
    "little_black_squash_balls": "victus 5, herba 3, desiderium 2",
    "potato_knishes": "victus 5, herba 3, fabrico 1",
    "party_hat": "fabrico 3, sensus 2, desiderium 1",
    "music_disc_mice_on_venus": "sensus 15, aer 5, desiderium 10",
    "music_disc_living_mice": "sensus 15, aer 5, desiderium 10",
    "rat_nugget_ore": "metallum 3, terra 3, bestia 2",
    "rat_toga": "fabrico 3, bestia 2, humanus 1",
    "dutchrat_wheel": "spiritus 5, aqua 5, herba 3, instrumentum 3",
    "biplane_wing": "volatus 10, machina 5, fabrico 3",
    "ratfish_bucket": "vacuos 5, metallum 3, aqua 5, bestia 10",
    "ratbot_barrel": "machina 5, metallum 5, potentia 3",
    "ratlantis_reactor": "machina 15, potentia 15, praecantatio 10",
    "marbled_cheese_rat_head": "caseus 2, terra 5, bestia 2",
    "rat_quarry_platform": "terra 3, instrumentum 2",
}

ENTITIES = {
    "rat": "bestia 5, caseus 3, desiderium 2",
    "tamed_rat": "bestia 5, caseus 3, sensus 2",
    "demon_rat": "bestia 5, ignis 5, vitium 3",
    "rat_king": "bestia 20, caseus 10, desiderium 10, vinculum 5",
    "black_death": "humanus 10, mortuus 15, vitium 15, perditio 10",
    "plague_beast": "bestia 15, vitium 10, mortuus 5",
    "plague_cloud": "vitium 10, aer 5, perditio 5",
    "plague_doctor": "humanus 15, victus 5, alkimia 5",
    "pied_piper": "humanus 10, sensus 5, praecantatio 5",
    "pirat": "bestia 5, humanus 5, aqua 3, aversio 3",
    "ghost_pirat": "spiritus 10, exanimis 5, aqua 3",
    "dutchrat": "spiritus 15, exanimis 10, aqua 10, aversio 5",
    "rat_baron": "bestia 10, volatus 10, machina 5, aversio 5",
    "ratfish": "bestia 3, aqua 5, victus 2",
    "ratlantean_spirit": "spiritus 10, ignis 5, praecantatio 5",
    "ratlantean_automaton": "machina 20, metallum 15, praecantatio 10, caseus 5",
    "ratlantean_ratbot": "machina 10, metallum 5, bestia 3",
    "feral_ratlantean": "bestia 15, aversio 10, praecantatio 5",
    "neo_ratlantean": "alienis 10, praecantatio 10, cognitio 5",
    "rat_protector": "spiritus 5, praemunio 10, bestia 3",
}

THAUMATURGE_ASPECTS = {
    "aer", "alienis", "alkimia", "aqua", "auram", "aversio", "bestia", "cognitio", "desiderium", "exanimis", "fabrico",
    "gelum", "herba", "humanus", "ignis", "instrumentum", "lux", "machina", "metallum", "mortuus", "motus", "ordo",
    "perditio", "permutatio", "potentia", "praecantatio", "praemunio", "sensus", "spiritus", "tenebrae", "terra",
    "vacuos", "victus", "vinculum", "vitium", "vitreus", "volatus",
}


def aspects(spec, owner):
    out = []
    for part in spec.split(","):
        name, amount = part.split()
        if name == "caseus":
            aspect = "rats:caseus"
        elif name in THAUMATURGE_ASPECTS:
            aspect = "thaumaturge:" + name
        else:
            sys.exit(f"{owner}: unknown aspect {name}")
        out.append({"aspect": aspect, "amount": int(amount)})
    return out


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")


def main():
    lang = json.loads(LANG.read_text(encoding="utf-8"))
    for item in ITEMS:
        if f"item.rats.{item}" not in lang and f"block.rats.{item}" not in lang:
            sys.exit(f"no item rats:{item}")
    # the language file has names of mobs that are no entity type (plague_rat: a rat with the plague)
    registries = "".join(f.read_text(encoding="utf-8") for f in ENTITY_REGISTRIES)
    for entity in ENTITIES:
        if f'"{entity}"' not in registries:
            sys.exit(f"no entity type rats:{entity}")
    write(PACK / "pack.mcmeta", {"pack": {"description": {"translate": "pack.rats.thaumaturge"}, **PACK_FORMAT}})
    write(PACK / "data/rats/thaumaturge/aspect/caseus.json", CASEUS)
    write(PACK / "data/thaumaturge/data_maps/item/base_aspects.json",
          {"values": {f"rats:{k}": aspects(v, k) for k, v in ITEMS.items()}})
    write(PACK / "data/thaumaturge/data_maps/entity_type/entity_aspects.json",
          {"values": {f"rats:{k}": aspects(v, k) for k, v in ENTITIES.items()}})
    print(f"{len(ITEMS)} items, {len(ENTITIES)} mobs -> {PACK.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
