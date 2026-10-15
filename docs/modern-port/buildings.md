# Core Buildings

These are the small modern M4 buildings, not generated structures or the legacy base system. Use fresh modern worlds. All three are available in the Stand and Hold creative tab or through operator `/give` commands:

```text
/give @s standandhold:field_command_post
/give @s standandhold:research_lab
/give @s standandhold:supply_crate
```

## Operation And Defaults

Right-click opens a server-owned menu. Escape closes it. Ordinary nearby players may operate shared buildings; debug/currency commands still require operator level 2. There is no ownership/claim system. Menus have no item slots yet.

| Building | Saved state | Loaded behavior |
| --- | --- | --- |
| Field Command Post | Level 1-5, local supplies/samples | +1 global human point and +1 local supply every 1,200 loaded ticks (60 seconds at 20 TPS) |
| Research Lab | Target ID, work units, local supplies/samples | +1 work per 100 loaded ticks, 12 work per research; blocked/completed targets do not consume resources or gain work |
| Supply Crate | Local supplies/samples | Storage/transfer only, no ticking |

Storage capacity defaults to 2,000 supplies per building; each Import/Export button moves exactly 10 between global and local storage. Insufficient balance, overflow, or insufficient capacity rejects the whole batch. Lowering capacity does not destroy existing storage; exporting still works while above the new cap. Stored samples have a hard 4,096 limit. Physical sample items/inventory insertion are M5 work; M4 adds only the bounded building API, not an obtainable sample currency.

Lab selection cycles through the server's research definitions. Selecting/clearing a target resets that lab's work, not world research. Costs are checked throughout work and deducted from **local** storage only on completion; research points/supply rewards and the once-only receipt are global. Multiple labs cannot reward the same research twice. Completed targets stay visible. `/standandhold research complete` remains an operator alternative using **global** supplies; the two paths share the same completion receipt. Nonzero sample requirements remain unavailable through normal M4 play.

Each loaded Command Post is registered by dimension and position (up to 4,096). Registration does not force-load chunks. A started `establish_field_command` mission can observe an existing registration or complete when a post loads; its reward is permanent and does not repeat. Supply/research/stage missions also refresh at successful building transactions and bounded world-affecting building intervals. No mission starts automatically, and block-local refreshes do not broadcast chat every interval.

## Upgrades

Human points are a minimum gate, **not spent**. The other costs are spent from that Command Post only. No defenders, tier stat bonuses, or generated main base are introduced by these level names.

| Upgrade | Human points | Local supplies | Local samples | Completed research |
| --- | --- | --- | --- | --- |
| 1 Field Camp -> 2 Reinforced Outpost | 150 | 20 | 0 | Initial Sample Analysis |
| 2 -> 3 Military Outpost | 500 | 50 | 0 | Field Communications |
| 3 -> 4 Fortified Base | 1,200 | 100 | 2 | Field Communications |
| 4 -> 5 Main Base | 5,200 | 200 | 5 | Special Division Training |

Building balance is in `config/standandhold-buildings.toml` (server-owned, synced): generation rewards/interval, capacity/batch, lab interval/work, and the four-entry upgrade point/supply/sample/research lists. Research definitions remain [JSON data](foundation-data.md). Defaults for M2/M3 thresholds/research/rewards are unchanged.

## Safety And Persistence

- Schema 3 global SavedData adds `command_posts`, preserving schema 0-2 modern progression/research/mission data. Block entities use `building_version: 1` and a `building` compound. Unknown nested building fields survive ordinary saves; future/invalid building schemas pause mutation rather than spending resources.
- Generation and work run only in loaded ticking chunks, with constant-time interval counters. No offline catch-up, global world scan, or per-tick dirty writes. Partial interval time restarts on reload; completed lab work is saved.
- Normal breaking/explosion side effects unregister posts and return local supplies to the global pool once. Blocks drop fresh level-1 items, not saved storage/levels; upgrades are not portable. Samples have no item recovery until M5. Drain storage before destructive admin edits: vanilla `/setblock ... replace/strict`, `/fill`, or structure edits may suppress block-entity side effects and discard storage. `/setblock ... air destroy` exercises normal removal.
- If the global balance is at the signed-long limit, destruction cannot return overflowing local storage; a single warning records the loss. Export before breaking when balances are near the limit. There is no cross-file crash journal: building chunks and global SavedData save separately. Normal save/reload is supported, but power loss/partial backup restoration can leave mixed snapshots. Back up the whole world, not individual data/chunk files.
- Vanilla menu packets select fixed server-known actions; no client currency, target ID, coordinates, or upgrade result is trusted. Actions recheck the current menu/owner, spectator state, server thread, same dimension, same live block entity, range (8 blocks), costs, and valid action ID. The server serializes competing actions. Long balances use four signed-short limbs to avoid vanilla menu truncation.
- Definitions are prepared once per server resource reload, not parsed by each building tick. Invalid definitions pause affected actions; a reload invalidates an already-open menu until it is reopened. Research receipts survive definition removal/restoration.

## Inspection And QA

Operator commands inspect registrations without loading chunks:

```text
/standandhold commandpost list
/standandhold commandpost nearest
```

They clean malformed keys and stale **loaded, current-dimension** records only; unloaded and other-dimension records are retained. List output is capped at 20; nearest considers this dimension. Labs/crates are not globally registered.

1. In fresh Creative/commands-enabled singleplayer, confirm the creative tab, translated names, and three distinct textures. Place each block, open each GUI, close/reopen, and check no crash/missing texture or overlapping text. Try a small window/large GUI scale too.
2. `supplies add 50`; import 10 into the lab, select Initial Sample Analysis. At defaults it reaches 12/12 in ~60 loaded seconds, spends 10 local supplies, and rewards 10 human points once (a nearby Command Post may independently add points). Repeat selection/reopen/reload: no duplicate reward/cost. Select Field Communications at Stage 0: blocked, no work/cost.
3. Import/export a crate batch; total global + local supplies must be conserved. Import with fewer than 10 global or insufficient capacity and export with fewer than 10 local: no partial transfer. Compare `/standandhold supplies status` with the menu.
4. After first research, `setstage 1`; import 20 into a Command Post. Upgrade reaches Level 2, spends 20 local supplies, leaves points unchanged apart from passive generation. Repeat without new funds/stage: blocked. Save/reopen and check level/storage/target/work/receipt.
5. Start/complete the sample mission with operator commands (samples are not obtainable yet), start `establish_field_command`, and place/find a registered post; mission completes once. Remove a post normally and inspect list. An admin replacement should leave at most a stale record until list/nearest cleans it. Unload a registered post's chunk; inspection must not load/delete it or generate resources offline.
6. Dedicated multiplayer: two nearby players open the same building and try simultaneous import/export/upgrade/research selection. Check both screens converge and currency is conserved. Non-operators can use buildings, but cannot use admin commands. Move >8 blocks away, change dimension, become spectator, break/replace the block while a menu is open, and attempt stale/unknown actions: no mutation. Packet-fuzz checks require a separate development client/harness; do not treat pure transaction checks as actual network evidence.
7. Save/restart with partial lab work, full/over-cap storage, an upgraded post, and a completed receipt. Recheck registration, no duplicate rewards, no startup renderer errors on a dedicated server, and no repeated per-tick warning spam. Malformed/newer save tests belong in disposable backups only.

All commands in the numbered recipe except `/give` start with `/standandhold`. Exact observed results and remaining manual coverage belong in STATUS.md, not this reference guide.
