# Threat Response And Events

## Regional State

This system is server-owned and independent of external mods. Existing ID/tag mappings in `standandhold-threats.toml` identify infection/test threats; the default is still `minecraft:zombie`. Ordinary hostile targeting does not automatically grant infection rewards/counters. No new entity type or runtime library is added.

Regions are fixed 4x4-chunk (64x64-block) squares, floor-divided at negative coordinates. Keys are `dimension@regionX,regionZ`, not block/chunk coordinates. Each stores score, mapped deaths, player/human-attributed mapped kills, human NPC losses, outpost attack windows and decay timestamps.

- Default score: +2 per credited mapped kill, +8 per human NPC death (including accidents), +4 per raid/attack window on an assigned defender. Attack windows are counted at most once per region per 20 ticks. Canceled deaths/damage are ignored; each entity's death is processed once.
- `standandhold-response.toml` is server-owned/synced. Scores cap at 1,000,000; counters saturate rather than overflow. Default region budget: 256, hard maximum 1,024. Full storage skips new regions without evicting history. Lowering the limit keeps existing records; operator reset frees region capacity.
- Lazy decay removes 2 score per 1,200 elapsed game ticks by default, on relevant events, loaded-post checks or inspection. Counters never decay. No global tick scan, chunk request or wall-clock/offline simulation is added; elapsed saved game time is accounted on next inspection. Rewound clocks rebase safely.

## Events And Bounds

Loaded, writable, operational Command Posts check once per 200 loaded ticks by default. Dormant Main Base posts do not run events. The first response access saves 1,200 ticks of initial grace, including when accessed through status/debug commands. There is no offline catch-up.

| Rule | Default |
| --- | --- |
| Natural raid | Local score >=10, non-spectator player within 64 blocks, one-in-30 chance per eligible check |
| Successful raid cooldown | 6,000 saved game ticks |
| Raid size | `min(cap, 1 + stage/2)`; stages 0/1=1, 2/3=2, 4/5=3, 6=4 with default cap 4 |
| Area cap | 8 living loaded Enemy/mapped mobs in a 32-block box, including ordinary monsters/nearby raids; at most 128 mobs inspected, then fail closed |
| Spawn work | At most 8 horizontal candidates and 5 vertical offsets, 8-12 blocks from post; already-loaded, dry solid support, air/collision/border/height checks |
| Raider lifetime | 2,400 game ticks, checked only while loaded, once per 20 entity ticks |
| Natural reinforcement | Score >=20 in the local/adjacent 3x3 region grid; existing stage-appropriate defender |
| Successful reinforcement cooldown | 2,400 saved ticks; failed eligible attempts save 400-tick backoff |
| Defender caps | Existing deployment enable/stage checks, 3 saved reservations per post, 8 loaded humans in the configured area; no extra budget |
| Patrol | 1,200 game ticks toward the highest nearby threat region, clamped inside home radius; combat has priority, path requests at most once per 40 ticks |

Raid attackers are ordinary vanilla zombies with an event marker and local human-target goal. They use vanilla combat/navigation and normal daylight behavior. No spawn-finalization jockey/gear generation, custom parasite AI or forced threat-mod spawns are added. Peaceful refuses raids. This is not a counter to every external threat.

Raider UUIDs are reserved **before** world addition; failed additions release them. Confirmed death, discard/expiry or dimension transfer releases them; unload/shutdown does not. Unresolved unloaded UUIDs block further waves at that post, even after cooldown. Loaded tagged zombies expire/fail closed without loading their former home. Lowered caps never delete existing defenders. Nearby posts share area caps, but separated posts are not a world-global population budget.

Reinforcements reuse the existing defender reservation transaction. Their saved patrol target/deadline is an order near home, not permission to traverse unloaded regions. Walls/terrain can prevent movement; no teleporting or squad AI is added.

## Operator Commands And Missions

All commands below require gamemaster/operator level 2. Event commands select the nearest **loaded** post within 64 blocks. Debug bypasses natural chance/score/player checks, but retains initial grace, cooldowns, operational stage, quotas, terrain and failed-spawn backoff.

```text
/standandhold threat status
/standandhold threat list
/standandhold threat reset
/standandhold threat reset "minecraft:overworld@0,0"
/standandhold event status
/standandhold event raid
/standandhold event reinforcement
```

Status shows counters or nearest-post cooldown ticks/reserved raiders/event totals/patrol region. List prints at most 20 regions sorted by decayed score. Reset removes one region's score/counters, **not** event cooldowns, raiders, defender quotas or mission receipts. Missing/invalid arguments fail safely. Successful events warn nearby non-spectators once; `playerWarnings=false` suppresses warnings, not operator replies. Failed natural attempts never spam chat.

An active `outpost_defence` mission advances once per reserved raider killed by a player/human. `/kill`, environmental expiry, unloaded/missing UUIDs and repeated removal do not grant progress. Existing prerequisite/start/once-only rewards apply. Default `defend_outpost`: 5 credited raiders, 25 points/10 supplies once, potentially across waves. Point/sample rewards remain separate mapping/attribution rules; no campaign auto-start or mission GUI is added.

## Saves And Limitations

`<world>/data/standandhold/threat_response.dat` uses schema 1, separate from progression schema 5. Empty version-zero data migrates non-destructively. Current missing/malformed maps, bad counters/UUIDs/positions, oversized collections or future versions refuse access and preserve the original payload, not free quota. Unknown root/record fields survive edits. Up to 4,096 post response records retain cooldown history even after post removal; full/malformed storage pauses new sites. Conservative ledger repair tooling is deferred.

Cross-file world/chunk/entity saves are not a power-loss journal. Whole-world backups are required; external edits/partial restores can strand reservations. Missing loaded lookup is not proof of death. No legacy conversion or automatic ledger clearing is supported. Singleplayer/pure tests do not prove multiplayer, external-mod coexistence or long-session performance.

## Manual QA

1. Fresh Creative/commands-enabled Superflat, Normal, night: generate a checkpoint, set stage 2, inspect event status. Wait 60 loaded seconds or `/tick sprint 1400t`; verify initial grace survives an intervening reload.
2. Trigger reinforcement with quota available: one eligible human, warning, saved cooldown, bounded patrol. Immediate repeat must not deploy again. Fill per-post/area quota; nearby posts must not bypass it. Unsupported/blocked/unloaded terrain must refuse safely.
3. Trigger raid: up to 2 zombies at stage 2, subject to area/terrain checks. Expect one warning and human/zombie combat. Immediate repeat must refuse. Kill raiders normally; inspect threat list for deaths/kills/score. One human NPC death adds a loss. Unconfigured animals do not add infection kills/deaths.
4. Save/reopen during a raid/cooldown: no extra wave, unresolved UUIDs retained. Loaded expiry grants no defence progress. Threat reset must not clear event cooldowns/quotas. Break a post/remove terrain without crashes or forced loading.
5. Start `defend_outpost` after its prerequisites; kill 5 reserved raiders through player/human combat across waves. Progress/reward must save once. `/kill`/environmental removals must not advance it.
6. In a **separate disposable config/world**, set natural chance to 1, thresholds to 0, grace/check interval to valid minima and shorter valid cooldowns. Observe natural events without debug triggers, decay, multiple posts and bounded counts across reload. Disable natural events to check they stop. Restore the original config. Do not claim this natural/long-session test from pure checks alone.
7. Check dormant Main Bases, Peaceful, non-operator commands, invalid keys, full ledgers, mapping removal and no per-tick log spam. Actual two-client combat, physical patrol reload and longer multi-outpost counts need their own observed evidence.

Observed results and the live milestone belong only in STATUS.md.
