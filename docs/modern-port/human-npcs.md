# Human NPC Foundation

Two small server-controlled humanoid tiers use vanilla pathfinding/goals and original alpha models/uniforms. No AI/animation library or threat mod is required. This is not the legacy army system. Army Riflemen can use the [gated Prototype Launcher](equipment.md); Survivors remain melee defenders and may receive a Blade after Stage 1 + Initial Sample Analysis. Player equipment access is separate from NPC gates.

## Units And Access

| Entity ID (`standandhold:` prefix) | Health | Melee damage | Armor | Automatic minimum stage |
| --- | --- | --- | --- | --- |
| `survivor_defender` | 20 | 3 | 2 | 0 |
| `army_rifleman` | 30 | 5 | 4 | 1 |

Their named, original spawn eggs are in the Stand and Hold creative tab. For operator testing:

```text
/give @s standandhold:survivor_defender_spawn_egg
/give @s standandhold:army_rifleman_spawn_egg
/summon standandhold:survivor_defender ~ ~ ~
/summon standandhold:army_rifleman ~ ~ ~
```

**Test eggs and admin summons bypass stage/post quotas.** They are not a survival recruitment mechanic. No natural biome spawns are registered. Existing NPCs do not transform or disappear when the stage changes. Higher stages reuse the Army Rifleman until more tiers are implemented. Both tiers are persistent, including in Peaceful; there are no equipment/XP drops.

`config/standandhold-npcs.toml` is server-owned/synced. Each tier has health, damage, movement speed, armor and required-stage settings with bounded ranges and comments. Stat changes apply to newly spawned/reloaded NPCs; reload clamps current health instead of healing damage. Stage/deployment settings apply to subsequent attempts. No existing progression thresholds or research costs are changed.

## Targeting And Movement

NPCs float, wander, look at nearby players and fight visible, reachable hostile mobs or configured threats. Vanilla `Goal` APIs are sufficient for this foundation; a Brain/library would not simplify these tasks. Target acquisition accepts vanilla `Enemy`-marked mobs by default, plus the existing [optional ID/tag mappings](samples-and-threats.md). A mapping identifies a target even if its point/drop rewards are zero. Empty, malformed or missing mappings never turn into an infection fallback target or extra rewards.

Set `targetHostileMobs=false` in `standandhold-npcs.toml` to restore mapping-only defence. The default broad classification includes normally neutral monsters such as Endermen, not every angry animal. Optional threat mobs outside `Enemy` still need explicit IDs/tags. Targeting a skeleton/creeper does not grant points or samples under the default zombie-only reward mapping. Elite is an equipment tier here, not a registered Elite NPC.

After the equipment stage/research unlock, an Army Rifleman's empty main hand may receive one non-dropping prototype. Vanilla ranged navigation uses the same target/home safety, checks line of sight and fires only within 12 blocks, at least 40 ticks apart by default. Inside 3 blocks or when ranged use is unavailable, melee remains the fallback. Weapon state and next-shot timestamp save with the entity; no new deployment source/quota is added. Existing held items are not overwritten, and the original intrinsic armour/melee stats do not change. See [Equipment](equipment.md) for config, friendly-hit protection and QA limits.

`survivorBladeEnabled` controls empty-handed Survivors' gated blade issue. The held-item modifier is additional to intrinsic damage (default 8 total before the mapped bonus), not a new base stat. The issued marker/stack save normally. Losing the gate/disable clears only an auto-issued blade with no drop, restoring unarmed combat; unrelated held items are not replaced. Player weapons can now hurt human mobs just like other mobs, but NPCs still cannot attack/retaliate against players or humans.

Players, other Stand and Hold humans and allies are excluded, even if explicitly mapped. There is no player retaliation goal. Intrinsic attack damage **and attack knockback** attributes are registered before combat. The server rechecks target eligibility before damage; removed/dead/ineligible targets are abandoned. Search is local (maximum 16 blocks) and throttled to at least 20 ticks; empty rules skip acquisition only when hostile targeting is disabled. Vanilla reach/line-of-sight/stuck-path checks handle blocked targets. No cross-world search or chunk ticket is used. The pinned pathfinding region reads `getChunkNow` and treats missing chunks as empty rather than requesting them.

NPC kills do **not** earn points/samples with the default `requirePlayerKill=true`. The existing all-deaths option enables those rewards too, but also enables environmental farms. Threat mods stay optional; no SRP classes, IDs or assets are imported.

## Command-Post Deployment

A loaded, writable Field Command Post may deploy one unlocked unit every **1,200 loaded ticks** (about 60 seconds at 20 TPS). Stage 0 selects a Survivor; stage 1+ selects a Rifleman by default. There is no deployment supply cost in this foundation. Upgrading a post does not multiply its quota or improve unit stats.

- Default per-post quota: **3 saved reservations**, including unloaded defenders.
- Default area cap: **8 living loaded humans within a 48-block box**, including nearby posts and manually spawned test units. The local query stops when it reaches the cap.
- At most 8 horizontal candidates within 6 blocks, with 5 vertical offsets each, never in the post's own column. Spawn checks require already-loaded neighboring chunks, safe walkable ground, two air blocks, no fluid at the support, collision clearance and the world border. A lone floating post is not a spawn platform. Unsuitable terrain simply skips that interval; there is no retry every tick.
- Deployed units have a saved dimension/position home and a 16-block vanilla home/pursuit radius. They can move back toward home; they never teleport or force-load it. No offline catch-up occurs. The attempt timer restarts after unload; returning to a post is not an immediate spawn.
- Caps and deployment enable/interval/radii are configurable. Setting the per-post cap to 0 disables deployment. Lowering a cap never deletes existing humans. Test eggs can exceed quotas manually, but still count against automatic area deployment.

The area cap is not a global population budget. Widely separated posts can each recruit a small group. Do not use mass admin egg/spawner automation as performance evidence.

## Saved Quotas And Removal

Modern `HumanWorldData` schema **4** adds a bounded `defenders` compound: dimension-aware post key to at most 16 reserved entity UUIDs. Existing modern schema 0-3 data migrates without deleting progression, research, mission receipts, registrations or unknown fields. This does not import 1.12 worlds. The ledger dirties only when a reservation changes.

Spawning reserves a slot before adding the entity and releases it if addition fails. Confirmed death/discard/dimension transfer releases the old reservation even if the home chunk is unloaded. Chunk unload/server shutdown does **not** release reservations. Loaded NPCs detect missing home posts on a throttled check, clear their home and release the slot; surviving NPCs are not deleted. List/nearest cleanup still never loads missing chunks.

Unknown or malformed roster data refuses deployment rather than silently resetting into free quota. An unresolved UUID remains reserved: absence from the loaded entity lookup is **not** proof of death. Crash/partial backup restores or external entity-file edits can leave conservative stale reservations and reduce deployment. Back up whole worlds; do not delete the ledger to recover slots while units may survive. Cross-file power-loss recovery/repair tooling is deferred.

## Original Visuals

The [art pipeline](art-pipeline.md) holds editable original Blockbench projects and deterministic exports. Models use an explicit **64x64 box-UV atlas**, with classic humanoid body rectangles in the top half and accessory UVs below, not the player-skin layout. Survivor has a soft cap/brim, red jacket and ochre pack; Rifleman has a teal helmet, graphite vest and double sleeve marks. Geometry follows vanilla humanoid animation and held-item transforms, with no new collision/stats/equipment behavior. Tests check opaque base/accessory faces and exact source/runtime texture parity. `GenerateNpcTextures.java` seeds uniforms/eggs, but embedded Blockbench PNGs are authoritative after editing. These remain simple alpha visuals, not finished rigged characters or clip-driven animation.

## Manual QA

1. Use a disposable modern world with commands enabled, no external mods. Check Mods, both egg names/icons and complete, distinct body textures from front/back/sides, walking and melee swings. No missing-texture checkerboard or transparent limbs should appear.
2. Spawn each tier near a zombie, then a skeleton/creeper on clear ground. Both should approach and damage eligible mobs. Observe a blocked/unreachable zombie, then remove it: no crash or repeated log spam. An unconfigured pig must be ignored. In Survival, lightly hit a human with no hostile mob nearby; it must not retaliate or attack another human.
3. At stage 0 place a post with safe open ground. Wait 60 loaded seconds for a Survivor. Set stage 1 before all slots fill; the next available deployment should be a Rifleman. Existing Survivors stay Survivors. `/summon` and eggs must still work below the automatic gate.
4. One post stops at 3 deployed units. Several nearby posts stop at 8 loaded humans total (manual eggs count). Kill one assigned defender and allow one interval: only one replacement. Disable deployment/cap 0, or raise both required stages: no automatic spawn.
5. Save/reload and move far enough to unload/reload a post. Units, homes and quotas remain; no immediate flood. Break a loaded post: no crash, and surviving assigned units lose their home. Retest list/nearest. Water/lava, blocked and unsupported platforms must not produce unsafe spawns.
6. In a separate disposable test config with `targetHostileMobs=false`, try the bundled `#standandhold:test_threats` tag, an empty list, a missing mod ID/tag and malformed rules. Live mapping removal should stop attacks; missing IDs must not target pigs. Restore defaults and confirm ordinary hostile targeting returns without changing reward mappings. External-mod coexistence, multiple real clients and packaged installations require separate evidence.

Build and runtime evidence are recorded in STATUS.md, not inferred from the existence of these tests.
