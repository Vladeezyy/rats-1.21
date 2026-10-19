# Vladeezyy mod template

The start of a new mod for **NeoForge and Fabric** (Minecraft 1.21.1): one common codebase, the loader layers, the
test harness and the scripts, the way our mods are built (Raids & Renegades, Ender Wilds). License: LGPL-3.0-only.

## Start a mod

1. Copy the template (GitHub: *Use this template*, or `gh repo create <name> --private --template Vladeezyy/vladeezyy-mod-template`).
2. `python3 -I tools/rename.py <mod_id> "<Mod Name>" <repo-name>` — renames ids, package, classes, metadata and folders.
3. In `gradle.properties`: `mod_description`, `mod_credits` (and the source / issue links if the repo name differs).
4. Write `DESIGN.md` (what the mod adds, how it differs from what inspired it), then replace the example block.
5. `./gradlew build`, then the tests below.

## Layout

| Path | What |
|---|---|
| `common/` | the mod's code and resources, vanilla Minecraft only (NeoForm + Parchment) |
| `common/.../platform/Platform.java` | what the common code needs from the loader: registration, creative tab, spawn eggs, block entity types, packets, config dir |
| `neoforge/`, `fabric/` | entry points, the `Platform` implementations, loader metadata |
| `common/src/selftest/` | tests shared by both loaders (dev runs only, never in the jar) |
| `tools/gen_assets.py` | textures drawn by code (`python3 tools/gen_assets.py`, needs Pillow) |
| `tools/gen_lang.py` | the language files from one table, six languages (en_us, ru_ru, uk_ua, de_de, es_es, pt_br) |

## Tests (long runs: start them in the background)

| Script | What it does |
|---|---|
| `tools/selftest.sh` (`LOADER=fabric`) | dev server on a fresh world, `SelfTest` checks, `PASS n/n` |
| `tools/scenetest.sh` (`-Pscene_lang=ru_ru`, `LOADER=fabric`) | client scene: screenshots in `<loader>/run/screenshots/scene_*.png`, missing textures, translations |
| `tools/nettest.sh` (`LOADER=fabric`) | a dev client joins a dev dedicated server and plays (`NetScene`) |
| `prodtest/serve.sh` | the release jar on a real NeoForge server (put the server in `prodtest/<version>/`) |
| `prodtest/serve-fabric.sh` | the Fabric release jar on a production Fabric server with Fabric API |

`-Pwith_compat` adds Sodium, Iris, Lithium, FerriteCore, ModernFix and Jade to the NeoForge dev runs;
`-Pneo_version=21.1.197` checks the oldest supported NeoForge.

## Conventions

Reports in Russian, commits as Vladeezyy with no AI attribution, README in English with Russian in `<details>`,
release notes in English, six languages, LGPL-3.0-only (GPL originals stay GPL). The full rules are in
[modding-kb](https://github.com/Vladeezyy/modding-kb).
