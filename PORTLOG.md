# Port log: Rats 8.1.3 (Forge 1.20.1) → NeoForge + Fabric 1.21.1, then 26.x

Source: `AlexModGuy/Rats`, branch `1.20` (last commit fcf30564, 2024-11-15, "add fishing indicator to fisherman rats"),
mod version 8.1.3, LGPL-3.0. 644 Java files, ~57k lines, 159 files use Forge API.

Dependency: Citadel (model boxes, animations). Citadel has no Fabric build and no 26.x build, so the classes Rats
uses (`AdvancedEntityModel`, `AdvancedModelBox`, `BasicModelPart`, `ModelAnimator`, `Animation`, `AnimationHandler`,
`IAnimatedEntity`) live in our library **Rampart** (`Vladeezyy/rampart`, NeoForge + Fabric, package
`dev.vladeezyy.rampart`). Rampart sends the animation packet only to the players tracking the entity (Citadel: to
every player of the server) and renders model boxes without per-vertex allocations. `PacketBufferUtils.readTag/writeTag`
→ vanilla `FriendlyByteBuf.readNbt/writeNbt`.

## Plan

1. NeoForge 1.21.1 first (the code in `neoforge/`), mechanical converters from the Ice and Fire port in `tools/port/`
   (`port_java.py`: imports, registries, `ResourceLocation`, Citadel → Rampart; `convert_resources.py`: 1.21 data
   folders, recipe/loot/advancement format, `forge:` → `c:` tags).
2. Then hand porting: registries (`DeferredHolder`), capabilities → data attachments, network → payloads, item NBT →
   data components, enum extensions (`MobCategory`, `Rarity`, raider type) → NeoForge enum extender, loot modifiers
   (codecs), Ratlantis built-in datapack, structures, rendering API.
3. Split: vanilla code to `common/`, NeoForge-only parts behind `Platform`, then the Fabric module.
4. Tests (self-test, client scene, net test, stress with FPS/MSPT vs vanilla), fixes of the original's bugs.
5. 26.1.2 / 26.2 / 26.3 as branches `mc/<version>` (Rampart first).

## Bugs of the original to fix in the port

Open issues of `AlexModGuy/Rats` (72 open, 2026-10-19), the ones about 1.19–1.20 behaviour:

| Issue | Problem | Plan |
|---|---|---|
| [#838](https://github.com/AlexModGuy/Rats/issues/838) (17 comments) | Tamed rat stuck holding cheese, does nothing | Find the held-item/food state that never clears |
| [#818](https://github.com/AlexModGuy/Rats/issues/818), [#844](https://github.com/AlexModGuy/Rats/issues/844) | Lumberjack rat stops working; breaks only near trees, picks up one item | Lumberjack goal: target search and drop pickup |
| [#887](https://github.com/AlexModGuy/Rats/issues/887), [#920](https://github.com/AlexModGuy/Rats/issues/920), [#902](https://github.com/AlexModGuy/Rats/issues/902) | Rats stop interacting with chests/crops after a while; planter does nothing | Harvest/deposit goals and their cooldowns |
| [#850](https://github.com/AlexModGuy/Rats/issues/850), [#945](https://github.com/AlexModGuy/Rats/issues/945) | Rats stop working when the owner is offline / world opened to LAN | Goals must not need the owner entity |
| [#907](https://github.com/AlexModGuy/Rats/issues/907) | Rat tubes do nothing | Tube pathing |
| [#871](https://github.com/AlexModGuy/Rats/issues/871) | Ethereal transport rat does not phase through blocks | Navigation of ethereal rats in transport mode |
| [#787](https://github.com/AlexModGuy/Rats/issues/787) | Patrol paths cleared on restart | Save the patrol path with the rat |
| [#827](https://github.com/AlexModGuy/Rats/issues/827) | Crash `NoSuchElementException` while ticking (Plague Doctor) | Guard the empty collection |
| [#913](https://github.com/AlexModGuy/Rats/issues/913) | Crash when taming | Read the crash, guard |
| [#885](https://github.com/AlexModGuy/Rats/issues/885) | `StackOverflowError` with plague beast / mounts (1.16) | Check the mount code for recursion |
| [#822](https://github.com/AlexModGuy/Rats/issues/822) | Bee-upgrade rat on Follow Me does not attack the owner's target | Target goal of flying rats |
| [#848](https://github.com/AlexModGuy/Rats/issues/848) | Flying Dutchrat never melees | Boss attack goal |
| [#847](https://github.com/AlexModGuy/Rats/issues/847) | Rat "fell from a great height" after the owner teleports | Reset fall distance on teleport to owner |
| [#870](https://github.com/AlexModGuy/Rats/issues/870) | Bucket upgrade works every tick (sound spam) | Cooldown |
| [#864](https://github.com/AlexModGuy/Rats/issues/864) | The "disable plastic" config option is gone | Bring it back (recipe condition) |
| [#867](https://github.com/AlexModGuy/Rats/issues/867) | Server lag with rats (1.20.1) | Profile with the stress test (MSPT vs vanilla) |
