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

## 2026-10-19 — NeoForge 1.21.1: the code compiles (2457 compile errors → 0)

Mechanical converters in `tools/port/` (imports, holders, 1.21 data, API renames, payloads, item NBT), then by hand.
What changed beyond renames, and why:
- **Data generators dropped**: the original generated its JSON with datagen classes; the generated JSON (converted to
  1.21 folders and formats, including the built-in Ratlantis datapack) is now the source. Tag classes keep only their
  `TagKey` constants.
- **Item data**: item NBT → `minecraft:custom_data` with the same keys (1.20.1 items keep their data, `ItemNbt`).
  Items stored inside combined upgrades and the black/white lists → `minecraft:container`, parsed once; the original
  parsed the NBT list every time a rat checked an upgrade (several times per tick per rat). 1.20.1 stacks are
  migrated on first read (`StoredItems`).
- **Capabilities**: the selected rat is a per-player map (it was never saved); block and entity item/fluid/energy
  handlers are registered with `RegisterCapabilitiesEvent`; lookups go through `RatsCaps`.
- **Network**: 19 `SimpleChannel` messages → payloads. Entity/block state (rat fluid, thrown block, curdler tank) now
  goes to the players tracking the entity/chunk, not to every player of the server.
- **Enums**: the rats spawn category (`rats:rats`), the Ratlantis rarity and the Pied Piper raider type are NeoForge
  enum extensions (`META-INF/enumextensions.json`, `RatsEnums`).
- **Data-driven 1.21 registries**: armor materials (registry entries; durability as item property, same values),
  banner patterns (`data/rats/banner_pattern`), jukebox songs (`data/rats/jukebox_song`), hero gifts (NeoForge data
  map `raid_hero_gifts`), entity type tags `can_breathe_under_water` (pirat boat) and `illager` (Pied Piper), item tags
  `meat` and `dyeable`.
- **Vanilla API moves**: projectiles (arrows take a pickup stack and weapon; the Ratlantis bow is vanilla's bow with
  `createProjectile`, so Power/Punch/Flame/Infinity work as enchantment effects), riding offsets → attachment points,
  eye heights → dimensions, dimension change → `DimensionTransition`, path evaluators → `PathfindingContext`,
  recipes → `RecipeHolder` / recipe inputs / codecs, loot conditions/modifier → `MapCodec`, follow-owner goal (the
  1.21 check is final: the rat keeps its own rules, so rats on their mounts still follow).

### Bugs of the original fixed on the way
- Client-bound packets `UpdateRatFluidPacket` and `SyncThrownBlockPacket` read the sender player, which is always null
  on the client: they never did anything (rats' carried fluid and thrown blocks were not synced).
- `UpdateSelectedRatPacket` crashed with a null rat when the selection was cleared.
- `SyncArmSwingPacket` trusted the item stack sent by the client (any plague scythe damage); the server now uses the
  item in the player's hand.
- `SyncPlaguePacket` sent the effect id as a byte (wrong past 255 effects); only the plague is synced, so no id.
- Projectiles fired from dispensers (cheese cannonball, rat capture net, vial of sentience) started at 0, 0, 0.
- #847: a rat teleported to its owner kept its old fall distance ("fell from a great height").
- `Animal.isFood` defaulted to wheat in 1.20.1; kept explicitly (1.21 made it abstract).

## 2026-10-20 — NeoForge + Fabric 1.21.1: common code, both loaders

The game code lives in `common/` (compiled against vanilla 1.21.1 / NeoForm) and calls the loader through
`dev.vladeezyy.rats.platform` (`Platform`, `ClientPlatform`); `neoforge/` and `fabric/` wire it up.

- **Registration**: `Registrar` / `RegistryEntry` keep `DeferredRegister` / `DeferredHolder`'s calls. NeoForge: deferred
  registers. Fabric: entries are queued and registered in NeoForge's order (attributes, data components, armor materials,
  then vanilla order, then modded registries), so suppliers see the same state on both. A `RegistryEntry` is not a
  `Holder`: vanilla APIs get `entry.holder()`, the registry's own reference (vanilla compares holders by identity).
- **Storages**: `ItemHandler` / `FluidHandler` / `EnergyHandler` (the methods of NeoForge 1.21.1's capabilities, own
  code). NeoForge adapts them to capabilities both ways; Fabric uses the mod's own handlers, vanilla containers
  (`InvWrapper` / `SidedInvWrapper`), the transfer API for other mods (fluids: 81 droplets per mB). Energy: NeoForge
  only (Fabric has no common energy API). Milk: NeoForge's milk fluid; Fabric registers `rats:milk` (tank-only fluid,
  milk bucket ⇄ bucket through the transfer API, the original's milk textures).
- **Events**: the original's Forge subscribers are `RatsEvents` / `RatsClientEvents` (vanilla arguments). NeoForge:
  `NeoForgeGameEvents`, `RatsNeoForgeClient`. Fabric: Fabric API callbacks plus mixins where Fabric has no event
  (target change, game events, item expiry, effects, healing, natural-spawn finalize, fishing, raid waves, loot
  modifiers; client: FOV, fog, living render, left click on air).
- **Data of NeoForge read on Fabric**: biome modifiers (`data/rats/neoforge/biome_modifier`, from Natura's port), the
  global loot modifiers, the hero gift data map (→ `GiveGiftToHero.GIFTS`), the built-in Ratlantis pack (the build copies
  it to `resourcepacks/ratlantis`). Enum extensions on Fabric: `MobCategory` / `Rarity` values added by mixin accessors;
  the Pied Piper joins raid waves by a mixin (0,0,1,0,0,1,1,2 per wave, as the NeoForge raider type).
- **Config**: `ConfigSpec` with ModConfigSpec's builder calls; NeoForge builds a real ModConfigSpec (same files),
  Fabric reads/writes the same TOML (from Natura's port).
- NeoForge extension overrides with vanilla types stay in the common classes without `@Override` (NeoForge still
  calls them); where vanilla has the same hook it is used instead (`getPickResult`, `onDestroyed(ItemEntity)`,
  `processBlock`, `decreaseAirSupply` for water-breathing rats, `goDownInWater` for the pirat boat, `setLevel` for the
  crafting table's `onLoad`).
- Hats and the Ratlantis armor bake their armor model once (the original baked a new model every frame for every
  entity wearing one).

### Bugs of the port fixed (found while splitting)
- **Global loot modifiers never ran**: the resource converter moved `data/forge/loot_modifiers/global_loot_modifiers.json`
  to `data/c/...` (it treated every `forge:` path as a common tag) and the condition `forge:loot_table_id` became
  `c:loot_table_id`. Cat morning gifts (raw rat / tiny coin), charged creeper chunks and the aristocrat's tiny coins
  never dropped. Now `data/neoforge/loot_modifiers/...` and `neoforge:loot_table_id`; the converter is fixed.
- **Ratlantis was always "disabled"**: the built-in pack was registered as `rats:ratlantis`, but the game checks the
  world's enabled packs for `ratlantis` (the original's id), so `RATLANTIS_DATAPACK_ENABLED` stayed false (Ratlantis
  items with the "disabled" tooltip, no Ratlantis fishing loot). The pack id is `ratlantis` again (1.20.1 worlds keep it
  enabled); the check also accepts Fabric's `rats:ratlantis`.
