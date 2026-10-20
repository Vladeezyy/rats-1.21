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

| Issue | Problem | Status in the port |
|---|---|---|
| [#838](https://github.com/AlexModGuy/Rats/issues/838) (17 comments) | Tamed rat stuck holding cheese, does nothing | Fixed: taming no longer copies the cheese into the hand; a working rat drops food it may not eat (test) |
| [#818](https://github.com/AlexModGuy/Rats/issues/818), [#844](https://github.com/AlexModGuy/Rats/issues/844) | Lumberjack rat stops working; breaks only near trees, picks up one item | Fixed: unreachable trees skipped for a minute; paths cover the whole radius-staff area (they ended at the follow range, 12 blocks) (test). One item per trip without the Platter is the upgrade's design |
| [#887](https://github.com/AlexModGuy/Rats/issues/887), [#920](https://github.com/AlexModGuy/Rats/issues/920), [#902](https://github.com/AlexModGuy/Rats/issues/902) | Rats stop interacting with chests/crops after a while; planter does nothing | Fixed: pickup/deposit give up on a chest they cannot reach and retry later; the farmer plants any seed on dry farmland (test) |
| [#850](https://github.com/AlexModGuy/Rats/issues/850), [#945](https://github.com/AlexModGuy/Rats/issues/945) | Rats stop working when the owner is offline / world opened to LAN | Fixed: vanilla's sit goal sat every pet with an offline owner; the rats' own sit goal only sits on command (test) |
| [#907](https://github.com/AlexModGuy/Rats/issues/907) | Rat tubes do nothing | Fixed (broken since 1.14): tamed rats path through tube networks (any tube, only through open or joined sides, up and down shafts), climb shafts, and get the 1.12 tube inside (10 px; the 8 px of the model caught a 0.49 rat on the edges) (tests: a sealed room behind a tube, a room 3 blocks up a shaft) |
| [#871](https://github.com/AlexModGuy/Rats/issues/871) | Ethereal transport rat does not phase through blocks | Fixed: noPhysics and no gravity back (lost in 1.19), straight paths (test) |
| [#787](https://github.com/AlexModGuy/Rats/issues/787) | Patrol paths cleared on restart | Fixed: the loaded path is a new list, so it is synced (test) |
| [#827](https://github.com/AlexModGuy/Rats/issues/827) | Crash `NoSuchElementException` while ticking (Plague Doctor) | Fixed: purifying liquid / Dutchrat sword damage sources take the owner (null-safe) |
| [#913](https://github.com/AlexModGuy/Rats/issues/913) | Crash when taming | Log expired (mclo.gs 404); taming by thrown and creative cheese tested, no crash |
| [#885](https://github.com/AlexModGuy/Rats/issues/885) | `StackOverflowError` with plague beast / mounts (1.16) | 1.16 code; mounts and bosses tested for 10 s without errors |
| [#822](https://github.com/AlexModGuy/Rats/issues/822) | Bee-upgrade rat on Follow Me does not attack the owner's target | Fixed: a flying rat in the air had no path, so its melee goal never started (test) |
| [#848](https://github.com/AlexModGuy/Rats/issues/848) | Flying Dutchrat never melees | Fixed: 1.19 made it hover 4 blocks over the target (1.16: 1) and flee to the north-west after a throw (test) |
| [#847](https://github.com/AlexModGuy/Rats/issues/847) | Rat "fell from a great height" after the owner teleports | Fixed: fall distance reset on teleport to owner |
| [#870](https://github.com/AlexModGuy/Rats/issues/870) | Bucket upgrade works every tick (sound spam) | Fixed: fluid sounds at most once a second |
| [#864](https://github.com/AlexModGuy/Rats/issues/864) | The "disable plastic" config option is gone | Fixed: "Plastic in Recipes" config, glass recipes when off (both loaders' recipe conditions) (test) |
| [#867](https://github.com/AlexModGuy/Rats/issues/867) | Server lag with rats (1.20.1) | Measured (`tools/stress.sh`, below): 300 working rats +2.2 ms per tick (7.5 µs per rat), 4.4× a wild wolf. Fixed: area scans (expensive check first for every block), item search every 2 ticks, static maps that grew, navigation re-created every 3 s, stuck rats re-pathing forever (#887) |

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

## 2026-10-20 — gameplay checks and the original's issues

`GameplayTest` (selftest, both loaders): scenarios on the dev server, each on its own patch of ground, with a timeout and
a line of detail when it fails. Every scenario of a bug was run once against the old code too (two of them passed there
and were rewritten: a bee rat that landed found a path; "came within reach once").

### Bugs of the port fixed
- The rat sack kept only its first rat (1.21 `custom_data` is a copy: the other rats were written into the copy).
- Ratlantis data: 1.20.5 number providers (`{"type": "uniform", "value": {...}}` → flat), the mob category in the biome
  spawners (`rats:rats`), `#c:tools/shear` in the pirat leaves loot, prismarine shards in the psychic upgrade recipe.
- Fabric: `MobCategory` / `Rarity` values missing from their codecs (mixin now adds them right after `$VALUES` in
  `<clinit>`); custom signs had no block entity data (vanilla's `isValidBlockState`).
- Packets checked who sends them: commands, upgrade visibility, rat tags and staff only for the rat's owner (or ops),
  dismount only for the rat's vehicle, dancing within 64 blocks, the crafting table only with its menu open.
- Tamed rats had the vanilla navigation until their first navigator check (5 s), and every check re-created it (the
  current path was dropped every 3 s). Now their own from the start, switched only when the kind changes.
- Tube shapes: built once per block state (a long string key per collision query before) in thread-safe maps.

### The original's bugs fixed (see the table above for the issues)
- #850/#945, #787, #838, #887/#818/#920/#902, #827, #870, #864, #871, #822, #848, #844, #907 — in the table.
- The ethereal rat's gravity came back every tick (`setNoGravity(isFlying())`), so it never floated either.
- The Flying Dutchrat fled to the north-west after every sword throw (`nextInt(6) - 12`; 1.16: `nextInt(15) - 7`).
- `RatMeleeAttackGoal.getAttackReachSqr` was dead since 1.20.2 (vanilla reach now counts the vehicle).
- Tube climbing: 1.19+ only had `onClimbable`, which climbs while the rat pushes against a wall; 1.12's lift is back.
- Plague beasts and clouds lost their owner (the Black Death) on reload: saved as "OwnerUUID", read as "Owner".
- Marbled cheese grass never spread: it looked for vanilla dirt and turned it into a short grass plant; now it spreads
  onto marbled cheese dirt like vanilla grass.

### World upgrade from 1.20.1 fixed (found on the 26.x port)
- Combined/list upgrades of 1.20.1 worlds keep their stored items in custom data as 1.20.1 wrote them ("Count",
  "tag"); the move into the container component parsed them as 1.21 stacks, so every stack became 1 item without its
  NBT. Now vanilla's item fixers (DFU, from data version 3465) bring each entry up first.

### Tests
`tools/selftest.sh` (both loaders): 31 → 41 checks. `JAVA_TOOL_OPTIONS=-Drats.selftest.only=a,b` runs the scenarios
whose names contain one of the parts. `tools/stress.sh` (server load, below), `tools/scenetest.sh -Pscene=perf`
(client FPS with 100 rats in view).

### Stress test (#867), NeoForge 1.21.1, dev server (Apple M-series), 2026-10-20
`tools/stress.sh` (`RATS=n`, `StressTest`): a stage of n cells, each with a carrier (two chests, refilled every 20 s),
a farmer (3x3 wheat around water) or a wanderer; mean tick time (`getAverageTickTimeNanos`, one value per 100 ticks)
with the stage empty, with n wild wolves, then with n tamed rats at work; JFR over the run, `tools/jfr_hot.py` lists
the Rats methods in the server thread's samples of the rat phase.

| Run | Empty stage | n wild wolves | n working rats | Items carried in 70 s |
|---|---|---|---|---|
| 100, before | 0.54 ms | (tamed: sitting, 0.53) | 1.22 ms | (not counted) |
| 100, after the scans | 0.50 ms | (tamed: sitting, 0.55) | 1.16 ms | 2848 |
| 300, after the scans | 0.91 ms | 1.42 ms | 3.31 ms | 8596 |
| 300, after the throttles | 0.99 ms | 1.56 ms | 3.23 ms | 8532 |

Rat phase in the profile (300 rats): path searches 31 % → 19 % of the server thread (the item search of gathering rats
every 2 ticks, 7 %, is gone), crop scans 8 % → 5 %; the rest is the vanilla mob tick (movement, collisions, goal
selection) and the carriers' two path searches per trip (one item per trip without the Platter, by design).
What changed: one pass over the search area with the cheap block checks first (crops, mining, bottler, shears,
fisherman, gardener, farm, quarry: the original listed every position and ran the break/protection check, an event,
for each block), searches back off 1→2→4→8× the harvest delay while they find nothing, the lumberjack tries trees
nearest first and keeps its canopy height itself (the static map grew with every trunk looked at), the tree flood fill
counts per call (another static map), tube shapes keyed by state (a string per collision query), the item search
throttled like vanilla's target goals.

Client, `tools/scenetest.sh -Pscene=perf` (NeoForge, 1600x900, no frame limit, a glass pen in view): empty stage
230 FPS, 100 tamed rats wandering 207 FPS (0.48 ms a frame, ~5 µs a rat), 100 wild wolves 232 FPS; a second run
204 / 178 / 207 FPS (rats −13 %). Wolves cost nothing visible on this GPU; rats' Rampart models with their layers do.


### The original's own mixins were not registered (found during the 26.1.2 port)
- `PiglinAiMixin` (a mob carrying a rat with the idol upgrade counts as wearing gold for piglins) and `WardenMixin`
  (wardens do not target rats with the sculked upgrade) sat in `common` but in no mixin config, so both upgrades did
  nothing. Now `rats.common.mixins.json`, loaded by NeoForge (`neoforge.mods.toml`) and Fabric (`fabric.mod.json`);
  two checks in `SelfTest`. Both loaders: `tools/selftest.sh` 43/43.

### The rat cage advancement accepted any item (found during the 26.3 port)
- `rat_cage_decoration`: the hammock and igloo criteria checked the used item with the 1.20 item predicate key
  `"tag"`, which 1.21 ignores, so any item used on a decorated cage counted. Now `"items": "#rats:hammocks"` /
  `"#rats:igloos"`.

### Pick block on five blocks did nothing on Fabric (found during the 26.3 port)
- The milk and cheese cauldrons, the decorated rat cage, the rat tube and the rat hole defined NeoForge's
  `getCloneItemStack(state, hit, level, pos, player)`, which Fabric never calls (and NeoForge 26.x has another
  signature): middle click gave nothing or the wrong item. Now they override vanilla's
  `getCloneItemStack(level, pos, state)`, which NeoForge's version calls too (`tools/port/fix_lost_overrides.py`,
  found with `tools/port/OverrideCheck.java`). A check in `SelfTest`.
- The Ratlantis portal on arrival read the worldgen heightmap of a loaded chunk, which 1.21 rebuilds every time (and
  logs as an error in dev); the final one now (on 26.x the worldgen one is gone and the portal ended at the bottom).

## 2026-10-20 — 26.1.2 (branch `mc/26.1.2`): NeoForge + Fabric

Rampart `1.0.0+26.1.2` (render states). Unobfuscated Minecraft, Java 25, NeoForge 26.1.2.121, Fabric API 0.155.3,
JEI 29.47. The common code went from 4459 compile errors to 0, then the loaders (NeoForge 76, Fabric ~40).

### What changed (beyond renames)
- **Rendering on render states and submits.** Every renderer extracts a state (`RatsMobRenderState`,
  `RatRenderState`, Rampart's `RampartLivingRenderState`) and submits through `SubmitNodeCollector`. Models drawn
  later read their pose from the state (Rampart) or are posed inside a custom-geometry submit (`PosedModels`: trap,
  trash can, bell, wheel, protector rats); shared models that differ per entity got one instance per use (rat king
  rats, idle and firing gun, Ratlantis armor per slot). Layers read the entity from the Rampart state
  (`EntityLayer`). Items and blocks in renderers go through `ItemModelResolver` / `BlockModelResolver` (`SubmitHelper`).
- Particles: `SingleQuadParticle` with fixed layers; the running rats are submitted from the world-overlay hook
  (particle groups are fixed in 26.x). Shaders: the Ratlantis portal and the glowing translucent type as
  `RenderPipeline`s, pride glints with their own texturing; the synesthesia effect is a `post_effect`.
- GUI on the extract API; the rat crafting table is an `AbstractRecipeBookScreen` with its own recipe book component
  modelled on vanilla's crafting one. 26.x clients have no recipes: the chef/archeologist/cauldron recipes are synced
  (NeoForge `OnDatapackSyncEvent.sendRecipes`, Fabric `RecipeSynchronization`) and read by JEI from `ClientRecipes`;
  the table's guide result comes through the menu's container data.
- Items: `assets/rats/items` definitions (`tools/port/convert_26.py`): the overrides as `select` / `range_dispatch`
  / `condition` on our properties (`rats:rat_count`, `rats:special`, `rats:soul`) and vanilla's (trims, bow), the item
  colors as tints, the block entity items as `minecraft:special` models (`rats:block_entity`), spawn eggs with
  textures made from the 1.21.1 template and the original's colors.
- NeoForge capabilities are the transfer API: our item, fluid and energy handlers are exposed as transactional
  `ResourceHandler`s (changes rolled back from a snapshot when a transaction aborts), other mods' handlers are used in
  a root transaction per call (`NeoForgeTransfer`).
- Data: recipes with string ingredients, global loot modifiers without the list file, trim materials without
  ingredient/model index (the items provide them), the Ratlantis dimension type, noise router, biome and flower patch
  in the 26.1 formats, game rule keys `gamerule.rats.*`, `minecraft:chain` → `iron_chain`, `c:music_discs`.

### Bugs found and fixed
- The original's **common mixins were not registered** in the 1.21.1 port either: the idol upgrade did not keep
  piglins calm, wardens hunted rats with the sculked upgrade. Now `rats.common.mixins.json` on both loaders (also on
  `main`), with tests.
- Server crash in 26.x: two upgrade items passed client models as `Model` in their render methods, and the bytecode
  verifier loaded client classes on the dedicated server. The submits moved to the client layers; all server classes
  are checked with `tools/port/ServerLinkCheck.java` (links every class on the server jar).
- NPEs in the Ratlantean automaton, the Flying Dutchrat, the rat beast mount and the feral Ratlantean: in 26.x the hit
  can kill the target and clear it before the knockback; the target is kept in a local now.
- The Ratlantis portal searched the ground with a worldgen heightmap that 26.x does not keep in loaded chunks (the
  portal would land at the bottom of the world); now `WORLD_SURFACE`.
- Client crashes and data errors found by the client scene: the Dutchrat bell ticked on the client with a cast to a
  server level (also the rat, the laser portal, the sirens and the automaton head: they use the level's difficulty
  now); the demon upgrade switch recipe could not be sent to clients (JEI syncs crafting recipes; its network codec
  encoded one fixed instance); the humanoid `hat` part is a child of `head` in 26.x (black death, hats); the Ratlantis
  trim materials moved from the Ratlantis pack to the mod's data (the gem and the ingot provide them, a missing
  material stopped worlds without Ratlantis from loading); the pirat sign items take their block's name.

### Tests
- `tools/selftest.sh` (26.x dev servers pause after 60 s without players: the script sets
  `pause-when-empty-seconds=0`; `-Ponly=<part>` runs chosen scenarios): **NeoForge 43/43, Fabric 43/43** (the three
  regression scenarios from `main` and the two mixin checks included). The #848 scenario's target is unhurt by
  resistance V instead of invulnerable: 26.x mobs drop invulnerable targets.
- `tools/scenetest.sh` (dev client, new world, screenshots): Fabric 5/5; NeoForge 4/5 before the sign name fix (the
  translation check is the same code on both loaders). All 28 mobs, the blocks and the inventory without missing
  textures, JEI loads.

### Stress test, NeoForge 26.1.2, dev server (Apple M-series), 2026-10-20
`RATS=300 sh tools/stress.sh`: empty stage 4.88 ms, 300 wild wolves 8.67 ms, 300 working rats 10.02 ms mean tick (9463
items carried). The 26.x dev server is much slower on the same machine than 1.21.1 (empty stage 0.99 ms there, wolves
+0.57 ms), so the versions are compared to vanilla within the run: the rats add 1.36× what the wolves add here (+5.14
ms against +3.79 ms) (1.21.1: 3.9×, +2.24 ms against +0.57 ms). Rat phase profile: path searches 14 %, the rest the
vanilla mob tick (movement 24 %, goal selection 22 %) and dropped items (17 %).
`sh tools/scenetest.sh -Pscene=perf` (1600x900, no frame limit): empty stage 171 FPS, 100 tamed rats wandering 140 FPS,
100 wild wolves 145 FPS (rats −3.5 % against wolves; 1.21.1: −10 %).

### Found during the 26.3 port (26.x)
- Pick block: 26.x vanilla takes `getCloneItemStack(level, pos, state, includeData)` (the merged 1.21.1 fix uses it here).
- Thrown blocks were no longer solid for other entities: `canBeCollidedWith` takes the other entity since 1.21.2.
  Checks in `SelfTest`.

## 2026-10-20 — 26.2 (branch `mc/26.2`, on `mc/26.1.2`)

NeoForge 26.2.0.93, Fabric API 0.161.0, JEI 30.47, Rampart `1.0.0+26.2`. 115 compile errors in common, 1 in NeoForge,
none in Fabric.

### What changed
- Structure processors are an interface with `codec()`; the processor registry holds their `MapCodec`s
  (`CopyInputStateRuleProcessor`: the rule reads the world state itself, the template state comes from the processed
  block info).
- Advancement triggers and predicates moved (`advancements.triggers`, `advancements.predicates`); entity type
  constants are in `EntityTypes`; `knockback(power, x, z, source, damage)`; `isBaby` is final (the demon rat's
  override went: it is never a baby); the strider navigation's lava path type check is vanilla's now.
- Screens, toasts and the HUD are under `Minecraft.gui` (`gui.screen()`, `gui.setScreen`, `gui.hud`); the plague hearts
  read the heart state from `Hud` (access transformer / widener moved).
- Render pipelines take bind group layouts (`BindGroupLayouts.*`, vertex bindings, primitive topology); the shaders
  are unchanged. `MobCategory` has a debug abbreviation (`RAT` for the rats' category). NeoForge has no name tag
  distance hook any more.
- Data: entity predicates are maps of registered sub-predicates (`minecraft:entity_type`, `minecraft:flags`,
  `minecraft:type_specific/fishing_hook`, ...); tree configurations need `below_trunk_provider` (the Ratlantis trees get
  vanilla's rule: dirt unless the block is in `#cannot_replace_below_tree_trunk`). Both in `tools/port/convert_26.py`.

### Tests
- `tools/selftest.sh`: NeoForge 43/43, Fabric 43/43. `tools/scenetest.sh`: NeoForge 5/5, Fabric 5/5 (all 28 mobs, blocks,
  inventory, 488 translations). `tools/port/ServerLinkCheck.java` on the 26.2 server jar: no server class needs a
  client class.
- Stress, NeoForge 26.2 dev server, `RATS=300 sh tools/stress.sh`: empty stage 2.87 ms, 300 wild wolves 7.95 ms, 300
  working rats 9.61 ms mean tick (9494 items carried); the rats add 1.33× what the wolves add (+6.74 ms against +5.08
  ms; 26.1.2: 1.36×).
