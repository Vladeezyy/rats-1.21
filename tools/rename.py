#!/usr/bin/env python3
"""Turns the template into a new mod. Run once, right after copying the template:

    python3 -I tools/rename.py <mod_id> "<Mod Name>" <repo-name>
    python3 -I tools/rename.py enderwilds "Ender Wilds" ender-wilds-1.21

mod_id: lower case letters, digits and "_" (also the Java package: dev.vladeezyy.<mod_id without "_">).
Replaces examplemod / ExampleMod / Example Mod / example-mod in every text file and in file and folder names.
The example block stays as a pattern: replace it with the mod's content.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SKIP_DIRS = {".git", ".gradle", "build", "run", "run-net", "run-prod", "gradle"}
TEXT = {".java", ".gradle", ".json", ".toml", ".properties", ".md", ".sh", ".py", ".mcmeta", ".txt", ".cfg"}


def main(mod_id, name, repo):
    if not re.fullmatch(r"[a-z][a-z0-9_]{1,63}", mod_id):
        sys.exit("mod_id must be lower case letters, digits and _")
    package = mod_id.replace("_", "")
    cls = re.sub(r"[^A-Za-z0-9]", "", name.title())
    pairs = [("dev.vladeezyy.examplemod", f"dev.vladeezyy.{package}"), ("dev/vladeezyy/examplemod", f"dev/vladeezyy/{package}"),
             ("ExampleMod", cls), ("Example Mod", name), ("example-mod", repo), ("examplemod", mod_id)]
    files = [p for p in ROOT.rglob("*") if p.is_file() and not (set(p.relative_to(ROOT).parts) & SKIP_DIRS)]
    for path in files:
        if path.suffix in TEXT or path.name in ("gradlew", ".gitignore") or path.parent.name == "services":
            if path.name == "rename.py":
                continue
            text = path.read_text(encoding="utf-8")
            new = text
            for old, value in pairs:
                new = new.replace(old, value)
            if new != text:
                path.write_text(new, encoding="utf-8")
    # file and folder names, deepest first
    for path in sorted((p for p in ROOT.rglob("*") if not (set(p.relative_to(ROOT).parts) & SKIP_DIRS)), key=lambda p: -len(p.parts)):
        new_name = path.name
        for old, value in pairs:
            new_name = new_name.replace(old.split("/")[-1], value.split("/")[-1])
        if new_name != path.name:
            path.rename(path.with_name(new_name))
    print(f"renamed to {mod_id} / {name} / dev.vladeezyy.{package} / {cls} / {repo}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    main(*sys.argv[1:])
