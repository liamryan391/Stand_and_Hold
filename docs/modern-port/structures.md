# Structures

These original Java layouts are small modern alpha foundations, not the legacy base system. No schematic library, threat mod or extra runtime dependency is required. They use Minecraft's normal block resources in game; no Minecraft or other-mod assets are copied into this project.

## Layouts

| Site | Default footprint | Contents |
| --- | --- | --- |
| Small Army Checkpoint | 9 x 9 | Stone-brick platform, low cobblestone perimeter, three-wide south entrance, three torches, sheltered Field Command Post, empty Research Lab and one Supply Crate with 2 local supplies |
| Main Base foundation | 13 x 13 | Two-high perimeter/entrance, sheltered command area, Research Lab, Supply Crate with 5 local supplies, green command/cyan research/grey barracks/yellow supply floor markers and a simple barracks bench |

Both have one level-1 Command Post, not a vanilla command block. No samples, equipment, free completed research or pre-spawned army are included. The existing bounded post deployment is their only automatic defender source; it retains stage gates, UUID quotas and area caps. The barracks marker/bench is a layout marker, not a new profession or building system.

Main Base command posts are marked in tile NBT with `main_base`. Their passive generation and deployment require Stage 5 by default, rechecked at the existing building intervals. Dropping below that stage makes them dormant again; existing defenders are not deleted. Labs, crates, storage menus and upgrades remain usable. The first viewer to open a base in each state receives one dormant/active chat notice; per-base `main_base_notices` receipts persist through reload, so later openings/players/stage toggles do not repeat already-seen notices. A dormant menu shows zero effective generation; the status command remains available for repeat inspection. A normal player-placed Command Post is not a Main Base and keeps the ordinary early-stage behaviour.

Initial crate supplies are seeded once during placement, never on chunk load or restart. They are local storage and must be exported before spending globally.

New natural/debug checkpoints of all supported sizes include one empty, idle lab inside the perimeter, left of the command shelter. It grants no samples, supplies or completed research; import resources and explicitly Start research as usual. Existing checkpoints are **not** retrofitted on reload; place a lab manually there or generate a new site in clear terrain. Main Base lab contents/layout are unchanged.

## Placement And Safety

- Natural generation is a registered `Feature`/placed feature added to Overworld-tagged biomes at `top_layer_modification`, using the pinned 26.3 API. It runs in the current `WorldGenRegion` chunk, not a chunk-load event, global scan or tick queue. Existing chunks are not retro-generated.
- Each square spacing region has one seeded candidate. Checkpoints default to 12-chunk regions with a 1-in-2 attempt; bases use 64-chunk regions and 1-in-4. These are attempt rates, **not** guaranteed structures or exact minimum distances: candidates in neighbouring regions can be close. Main Base candidates get first refusal in their chunk, then an eligible checkpoint may try.
- All reads/writes stay within the generating/loaded chunk. The footprint is centred at local x/z 8; configurable radii cannot reach another chunk. No chunk is requested for placement, discovery or cleanup. Empty allowed dimensions disables placement, including debug commands.
- Every footprint column is checked before any write: build height/world border, surface variation (default at most 2 blocks), two dry full natural ground blocks, and five clear/replaceable-flora blocks above the highest floor. Trees, containers, existing buildings, fluids/waterlogged blocks and unsuitable support reject the attempt. Only a short foundation gap is filled; terrain is not excavated.
- The accepted ground whitelist is dirt, grass blocks, Overworld base stone, sand, terracotta and gravel (with full-collision/dry checks). Minecraft 26.3 separates `grass_blocks` from `dirt`; both tags are explicitly accepted so ordinary grass-over-dirt/Superflat works. It is intentionally conservative. It cannot tell player-placed dirt/stone from natural ground; use debug placement only in disposable/clear areas. A two-block support test is not a deep cave survey.
- Changed cells are remembered and restored on ordinary placement failure; required building tiles must exist before registration/seeding. This is not a power-loss journal or protection against another mod throwing during rollback. No site is registered merely because a candidate was selected.
- Feature workers never access world SavedData. Required tiles are saved in the chunk, and their server-thread `onLoad` registers the post/base when the chunk actually loads. A generated but never-loaded site is therefore absent from registry commands. Debug placement in an already-loaded chunk registers immediately after successful placement.

## Configuration

`config/standandhold-structures.toml` is a synced server config. Comments explain the individual settings:

| Settings | Meaning |
| --- | --- |
| `allowedDimensions` | Exact dimension IDs; default only `minecraft:overworld`. Natural generation also requires the supplied Overworld biome modifier or a deliberate datapack override |
| `checkpointsEnabled`, `mainBasesEnabled` | New natural attempts only; disabling keeps existing sites and allows explicit debug placement |
| `checkpointSpacingChunks`, `mainBaseSpacingChunks` | Seeded candidate region sizes, 1..256 chunks |
| `checkpointChance`, `mainBaseChance` | One-in-N candidate attempt, 1..10000 |
| `checkpointRadius`, `mainBaseRadius` | Odd footprints `2r+1`: checkpoint 7..11, Main Base 11..13 blocks |
| `maximumHeightDifference` | Full-footprint slope limit, 0..3 blocks; safety checks cannot be disabled |
| `mainBaseActivationStage` | Command generation/deployment stage, 0..6; default 5 |
| `checkpointCrateSupplies`, `mainBaseCrateSupplies` | One-time local starting supplies, also clamped to building capacity |
| `expansionEnabled` | **False by default.** Explicit operator preparation/application of proven level-2+ radius-4 checkpoints only. No automatic growth, extra cost/reward or population increase; whole-world backup required |

Generation settings are not saved attempt receipts. Changing them only affects subsequent chunk generation; it never intentionally repeats generation in an existing chunk. For deterministic natural QA, use a **new disposable** flat world with biome decoration explicitly enabled (`features: true` in its generator settings), spacing/chance 1 and only the desired site enabled. Default Superflat disables natural biome features; its debug commands still work. Disable automatic NPC deployment in dense QA to avoid an artificial concentration of posts. Restore defaults afterwards; do not use dense QA settings in a normal world.

## Admin Commands

Operator level 2, including console `execute positioned ...`:

```text
/standandhold structure checkpoint
/standandhold structure mainbase
/standandhold commandpost list
/standandhold commandpost nearest
/standandhold mainbase list
/standandhold mainbase nearest
/standandhold mainbase status
```

Placement uses the **current chunk**, centred at local x/z 8, with y found from terrain. It ignores natural chance/spacing/enable switches, but never dimension, loaded-chunk, terrain, occupancy, border or registry-cap checks. There is no force/overwrite option. Success reports the command-post dimension/position, changed block count and registry counts. Failure explains the rejected condition without retrying elsewhere. Repeat placement on an existing structure should fail safely.

Registry inspection is bounded to 4,096 records per registry and 20 output rows. Nearest/status selects this dimension only. `[stage eligible]` means the stage condition is met, not that an unloaded site is currently ticking or its NPC quota has room. Inspection cleans malformed position keys and missing/wrong **loaded, current-dimension** tiles; it never loads, inspects or removes an unloaded/other-dimension site. Labs/crates do not need a global position registry.

## Outpost Preview

`/standandhold outpost preview` is **operator level 2, read-only, player-only**. Close the menu and look directly at a loaded Field Command Post within six blocks. A newly generated default-radius checkpoint must have its generation receipt, local level >=2 and original lab/crate tiles. Upgrading still uses the existing point/research/local-resource gates; world stage alone never enlarges a site. Generation at other supported radii still works, but the first expansion preview only supports radius 4.

Eligible output reports site UUID, old/new x/z bounds and floor, 137 proposed cells (88 ground replacements, 45 walls, four lights), unchanged buildings/capacity and a terrain-ownership warning. It describes a 9x9-to-13x13 outer apron; **no blocks, resources, tokens, markers, registrations or dirty flags change**. Separate preparation/application is opt-in; preview itself never authorizes construction. There is no force or remote-coordinate option. Preview is not an external claim-mod approval: identical player-placed grass/dirt cannot be distinguished from terrain.

Old/M16-or-earlier sites and manually placed/replacement posts are never adopted. Main Bases, unsupported/future/malformed site data, missing original buildings, level-1 posts, changed anchors/dimensions, unsuitable/wet/occupied/unloaded/out-of-border terrain and entity obstruction refuse safely. One bounded loaded-entity inspection stops at 128 candidates and refuses if saturated. The pure plan reads 616 cells across the bounded envelope; the service adds three fixed tile identity checks, not a global scan. No chunk is requested.

Successful **new checkpoint generation only** attaches a separate `site` compound (`site_version: 1`, UUID, dimension, checkpoint kind, `checkpoint_lab_v1` layout/revision, command anchor/floor, radius, south orientation, debug/worldgen origin, applied revision 0). All new checkpoint sizes get a receipt, but only the default size is preview-eligible. Center is derived from the command anchor: one block south and one block below. Building schema remains 1; HumanWorldData remains 5. Reading a receipt never normalizes it; unknown fields and malformed/future raw data survive normal building saves, while site actions fail closed. Storage/research remain usable independently. Older mod binaries may discard this new field on downgrade: use whole-world backups, never guessed provenance.

The [expansion design](outpost-expansion-design.md) records the contract and qualified failure semantics. M17 added no resizing; the opt-in M18 application is described below, not a power-loss journal.

## Confirmed Apron Application

Back up the **whole disposable modern world** before opting in. In its server-side `config/standandhold-structures.toml`, set `expansionEnabled = true` and restart the instance (do not reset unrelated config). Leave it false on servers that do not accept bounded ground replacement or have unverified claim/protection mods. Old/manual posts and Main Bases never become eligible. Upgrade a new supported checkpoint's post normally to local level 2; setting world stage alone does not upgrade it.

Close menus, look directly at that post within six blocks as an operator, then:

```text
/standandhold outpost preview
/standandhold outpost prepare
/standandhold outpost expand <token-from-prepare>
/standandhold outpost status
/standandhold outpost recover
```

`prepare` reports ground-replacement consent and issues a single-use token, but saves no world receipt or blocks. Its coloured Confirm line **only fills chat**; review it, then press Enter to apply. It expires after 200 unpaused game ticks (10 seconds at 20 TPS), is bound to that player/dimension/site/level/stage/config and exact before-state/protected-building digest, and is invalid after disconnect/restart or another preparation by that player. The transient server cache holds at most 64 entries. Once confirmation looks up an owned token it is consumed, even if revalidation fails; prepare again, never use a force flag. Normal building/lab progress or player edits during those ten seconds can invalidate consent safely.

`expand` rechecks the complete loaded-only terrain/entity preflight, saves a bounded intent on the original post, then attempts exactly 88 floors, 45 walls and four torches, synchronously. The original 9x9 footprint, lab/crate/post NBT, storage, resources, defender reservations/cooldowns and capacity stay intact. **No extra debit, reward, loot or units.** Inner walls remain; this is an outer apron, not a full base rebuild. Once applied, breaking its blocks never causes repair/refill or allows another application.

Safety bounds: one chunk, 169 terrain columns, at most 1,999 explicit block-state reads on the successful confirmation path, 137 unique writes (hard ceiling 256), one loaded-entity query capped at 128 candidates, and fixed tile-identity checks. Preparation also hashes the original 9x9 x 6 block envelope. Extra block entities inside that envelope refuse before hashing their contents; each of the three original buildings has a 256-KiB serialized-NBT safety limit. Oversized/unsupported data is retained, not erased. No chunk tickets, global ticker or whole-world scan is added. The fixture overrides used for screenshot determinism never change normal config defaults.

`status` is a read-only observation of the saved operation UUID, phase, all-before/all-after/mixed/conflict state and protected-state digest. The recorded index is diagnostic, **not** a durable write counter. Protection mismatch can also mean legitimate later storage/building changes. `recover` is explicit operator **metadata sealing only**: an unsealed receipt must have every expected-after cell and the matching protected footprint. It works even with application disabled; it never places, repairs, rolls back, spends or grants anything. It cannot repair a sealed site. Prepared/all-before state needs fresh preparation/consent; REVIEW, mixed, conflicting, removed/replaced-anchor or unsupported state refuses application for operator inspection/whole-world restore. No automatic on-load replay or repair occurs.

The additive `site.operation` format (`operation_version = 1`) stores site/operation UUIDs, exact ordered 137-cell expected-before/after delta, protected digest, prepared/review/applied phase and bounded diagnostic. Known updates preserve unknown site/operation/cell fields; malformed/future raw receipts disable site actions without breaking ordinary storage. A successful seal sets only `site.applied_revision = 1` plus receipt status. Building/global schemas remain 1/5. `setChanged()` is **not fsync**: same-chunk receipts reduce scope but do not guarantee atomic snapshots, process-kill/power-loss recovery or arbitrary third-party callback safety. Never downgrade a world in place; restore a matching whole-world backup.

## Saves And Discovery

Modern global schema 5 adds `main_bases` and `discovered_main_base` to the existing progression, research/mission receipts, posts and defender reservations. Modern schemas 0..4 migrate non-destructively; unknown data survives. Missing/malformed current-schema fields fail closed rather than forgetting history. This is not a legacy-world importer. A world and its chunks must be backed up together.

Right-clicking a registered Main Base command post as a non-spectator records a shared discovery receipt, even while dormant. Simply generating/listing a base does not count as player discovery. An active `establish_main_base` mission observes that receipt; starting it later also observes the earlier visit. Prerequisites still apply; `defend_outpost` counts credited reserved-raider kills through the event system. Admin mission commands are only shortcuts for isolated QA, not earned-loop proof. Completion rewards are once-only (default +50 points/+25 supplies).

Normal removal unregisters the post and base. Admin edits that suppress removal effects can leave stale records until inspection. Cleanup and removal do **not** reset discovery, completed missions or surviving defender reservations. Other blocks in a ruined structure are not tracked/demolished automatically. Replacing its removed command post with an ordinary item does not restore the Main Base marker/registration.

## Manual QA

Mechanics belong to the [automated ladder](automated-testing.md): native structures/refusals/commands/AI, real restart persistence and client screenshots. No manual command/number readback is required.

1. In a disposable world, judge checkpoint/Main Base entrance, lab access, lighting and defender movement for feel. These are original alpha foundations, not finished bases.
2. For opt-in expansion, use the procedure above and judge the narrow outer apron/entrance and the clarity of consent. Required tests already cover exact writes, protected state, faults, expiry, permissions and restarts.
3. Natural generation across varied terrain, real multiplayer and packaged installs remain separate coverage; a deterministic worker-proxy test is not concurrent natural-worldgen proof. Retain test worlds and restore sparse defaults after dense QA.

Play for about 5 minutes. Does anything feel off or annoying?

## Limits And Planned Layouts

These are one-chunk open-air foundations, not finished bases, road networks, terrain blending or `/locate structure`-indexed template structures. Strict safety means many forested, watery, uneven, modified or crowded sites are skipped. Generation in custom dimensions/biomes needs explicit configuration/datapack support. Other mods' later terrain/features can still modify a site; cleanup validates only the command tile, not the entire building. Large exploration/performance and external-mod combinations need separate tests.

Later original layouts may include outposts, research facilities, barracks, supply depots, Main Base expansion and Special Parasite Division facilities. They are not added by these foundations.

### Base Evolution Proposal

Liam's M12 feedback proposes growing individual checkpoints/outposts alongside the army. Keep the **global human stage** (technology/unit availability) distinct from each site's **local building/base level** (paid upgrades, infrastructure and eventual footprint). Current Command Posts already save a level, but upgrading does not enlarge a generated structure.

- Illustrative future targets: Stage 0 small checkpoints with 3-5 defenders, Stage 1 larger sites with 5-8, then later tiers. These are design examples, not new defaults, measured performance budgets or approved balance changes.
- Physical growth needs bounded loaded-only footprint preflight, protected player blocks/storage/tiles, partial-failure safety and persistent site ownership. A global stage increase must not silently overwrite nearby builds or reset defender reservations/cooldowns.
- Existing per-site building levels now control bounded defender capacity (see [capacity contract](buildings.md#defender-capacity-contract)); shared nearby quotas still apply. This does not enlarge generated layouts. Physical expansion and larger shared population budgets need a separate design/load test; the showcase work itself introduced neither.

The [outpost expansion design](outpost-expansion-design.md) defines the bounded outer apron, explicit eligibility/terrain budgets and conservative recovery. M16 admitted preview first; M18 adds one default-disabled confirmed application, not automatic resizing, Main Base growth or a durable crash-recovery system.
