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

## Saves And Discovery

Modern global schema 5 adds `main_bases` and `discovered_main_base` to the existing progression, research/mission receipts, posts and defender reservations. Modern schemas 0..4 migrate non-destructively; unknown data survives. Missing/malformed current-schema fields fail closed rather than forgetting history. This is not a legacy-world importer. A world and its chunks must be backed up together.

Right-clicking a registered Main Base command post as a non-spectator records a shared discovery receipt, even while dormant. Simply generating/listing a base does not count as player discovery. An active `establish_main_base` mission observes that receipt; starting it later also observes the earlier visit. Prerequisites still apply; `defend_outpost` counts credited reserved-raider kills through the event system. Admin mission commands are only shortcuts for isolated QA, not earned-loop proof. Completion rewards are once-only (default +50 points/+25 supplies).

Normal removal unregisters the post and base. Admin edits that suppress removal effects can leave stale records until inspection. Cleanup and removal do **not** reset discovery, completed missions or surviving defender reservations. Other blocks in a ruined structure are not tracked/demolished automatically. Replacing its removed command post with an ordinary item does not restore the Main Base marker/registration.

## Manual QA

1. In a fresh disposable world, inspect a newly placed checkpoint's lab/entrance/lighting/shelter for feel and accessibility. Automated GameTests cover required tiles, empty lab, explicit research, obstruction refusal and all checkpoint radii; the two-JVM fixture covers generated lab storage/work and crate non-refill. No manual numeric readback is needed.
2. Move at least 64 blocks to clear terrain in another chunk; generate a Main Base. Inspect its distinct footprint, zones and lab/crate menus. Confirm 5 supplies, dormant chat and zero post generation at Stage 0. Stage 5 should enable generation/capped deployment after loaded intervals; it does not unlock an unimplemented new NPC tier.
3. Re-run placement in occupied terrain. Try water, a steep slope, a protected chest, an unloaded coordinate via console, disallowed dimension and world border. Expect refusal without damage or new registry entries.
4. Open the base post, start/complete prerequisite missions with the admin flow in [Foundation Data](foundation-data.md), then start `establish_main_base`. Verify discovery completion/reward once; repeat, restart, remove the base and check no reset/regrant.
5. Export some crate storage, save/quit and reopen: no refill; marker, storage, registration and completed mission survive. Check both list/nearest commands, then remove the post normally. For stale-record testing in a disposable copy, admin-clear the tile without removal updates and inspect again; unloaded records must remain.
6. Natural QA uses new chunks and disposable dense settings as above. Confirm actual natural posts/base flags appear after loading, not merely that the datapack parses; then restart and check no duplication. Restore sparse defaults and retain the QA world separately.

## Limits And Planned Layouts

These are one-chunk open-air foundations, not finished bases, road networks, terrain blending or `/locate structure`-indexed template structures. Strict safety means many forested, watery, uneven, modified or crowded sites are skipped. Generation in custom dimensions/biomes needs explicit configuration/datapack support. Other mods' later terrain/features can still modify a site; cleanup validates only the command tile, not the entire building. Large exploration/performance and external-mod combinations need separate tests.

Later original layouts may include outposts, research facilities, barracks, supply depots, Main Base expansion and Special Parasite Division facilities. They are not added by these foundations.

### Base Evolution Proposal

Liam's M12 feedback proposes growing individual checkpoints/outposts alongside the army. Keep the **global human stage** (technology/unit availability) distinct from each site's **local building/base level** (paid upgrades, infrastructure and eventual footprint). Current Command Posts already save a level, but upgrading does not enlarge a generated structure.

- Illustrative future targets: Stage 0 small checkpoints with 3-5 defenders, Stage 1 larger sites with 5-8, then later tiers. These are design examples, not new defaults, measured performance budgets or approved balance changes.
- Physical growth needs bounded loaded-only footprint preflight, protected player blocks/storage/tiles, partial-failure safety and persistent site ownership. A global stage increase must not silently overwrite nearby builds or reset defender reservations/cooldowns.
- Existing per-site building levels now control bounded defender capacity (see [capacity contract](buildings.md#defender-capacity-contract)); shared nearby quotas still apply. This does not enlarge generated layouts. Physical expansion and larger shared population budgets need a separate design/load test; the showcase work itself introduced neither.

The [outpost expansion design](outpost-expansion-design.md) defines a small outer-apron candidate, explicit eligibility/terrain budgets and conservative recovery. Its admission is provenance/read-only preview first; no expansion command, automatic resizing or crash-recovery system is implemented by M16.
