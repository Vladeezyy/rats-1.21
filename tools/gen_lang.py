#!/usr/bin/env python3
"""Writes the language files from one table (six languages): python3 -I tools/gen_lang.py"""
import json
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "common/src/main/resources/assets/examplemod/lang"
LANGS = ("en_us", "ru_ru", "uk_ua", "de_de", "es_es", "pt_br")
STRINGS = {
    "itemGroup.examplemod": ("Example Mod", "Пример мода", "Приклад мода", "Beispielmod", "Mod de ejemplo", "Mod de exemplo"),
    "block.examplemod.example_block": ("Example Block", "Пример блока", "Приклад блока", "Beispielblock", "Bloque de ejemplo", "Bloco de exemplo"),
}


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for i, lang in enumerate(LANGS):
        data = {key: values[i] for key, values in STRINGS.items()}
        (OUT / f"{lang}.json").write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
