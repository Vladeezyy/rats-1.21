#!/usr/bin/env python3
"""Draws the mod's textures (no hand-made image files in git): python3 tools/gen_assets.py (needs Pillow)"""
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "common/src/main/resources/assets/rats/textures"


def example_block():
    rnd = random.Random(1)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-12, 12)
            edge = x in (0, 15) or y in (0, 15)
            base = (70, 40, 110) if edge else (110, 70, 160)
            img.putpixel((x, y), tuple(max(0, min(255, c + v)) for c in base) + (255,))
    return img


def main():
    (OUT / "block").mkdir(parents=True, exist_ok=True)
    example_block().save(OUT / "block/example_block.png")


if __name__ == "__main__":
    main()
