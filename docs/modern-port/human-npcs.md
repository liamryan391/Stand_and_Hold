# Human NPC Foundation

## Tier Contract

Heavy Soldier is a slower armoured melee role (44 health, 6 base damage, 0.24 speed, 8 armour); Elite Soldier is a mobile ranged role (36 health, 7 base melee damage, 0.30 speed, 6 armour). Heavy defaults to Stage 2 and Elite to Stage 3; both require completed `standandhold:field_communications`. Each new tier's required research ID and stage are configurable. Missing/malformed/removed research fails closed for that tier, falling back to the highest eligible lower role. Survivor/Army gates and stats are unchanged.

Super Elite is a tougher melee role (52 health, 8 base damage, 0.25 speed, 10 armour) unlocked at Stage 4 with Field Communications. The Special Parasite Division Operative is a mobile ranged role (42 health, 7 base melee damage, 0.31 speed, 7 armour) unlocked at Stage 5 with Special Division Training. An operative can recruit only from a registered, operational Main Base with `specialDivisionDeploymentEnabled` and no existing Division reservation.

All automatic, event and paid deployments prefer Division (at an eligible base), then Super Elite, Elite, Heavy, Army and Survivor. World stage chooses the role; local level sets capacity. No extra population, cooldown bypass or tier transformation is introduced. Survivors, Heavies and Super Elites share Blade issue and retention; Riflemen, Elites and Division share the [Prototype Launcher](equipment.md) issue and current-use gates. Existing held items are never overwritten, issued gear never drops, and a stage drop never removes issued gear or changes entity identity. Retaining a launcher does not bypass its current-use gate; melee remains available. Ranged roles share projectile damage/interval. Player access remains separate from NPC gates.

Six server-controlled humanoid tiers use vanilla pathfinding/goals and original alpha models/uniforms. No AI/animation library or threat mod is required. This is not the legacy army system.

## Units And Access

| Entity ID (`standandhold:` prefix) | Health | Unarmed damage | Armor | Automatic minimum stage |
| --- | --- | --- | --- | --- |
| `survivor_defender` | 20 | 3 | 2 | 0 |
| `army_rifleman` | 30 | 5 | 4 | 1 |
| `heavy_soldier` | 44 | 6 | 8 | 2 + Field Communications |
| `elite_soldier` | 36 | 7 | 6 | 3 + Field Communications |
| `super_elite` | 52 | 8 | 10 | 4 + Field Communications |
| `special_division_operative` | 42 | 7 | 7 | 5 + Special Division Training + active Main Base |

Their named, original spawn eggs are in the Stand and Hold creative tab. For operator testing:

```text
/give @s standandhold:survivor_defender_spawn_egg
/give @s standandhold:army_rifleman_spawn_egg
/give @s standandhold:heavy_soldier_spawn_egg
/give @s standandhold:elite_soldier_spawn_egg
/give @s standandhold:super_elite_spawn_egg
/give @s standandhold:special_division_operative_spawn_egg
/summon standandhold:survivor_defender ~ ~ ~
/summon standandhold:army_rifleman ~ ~ ~
```

**Test eggs and admin summons bypass recruitment gates/post quotas, not equipment gates.** The two specialist eggs only work in Creative and cannot set spawners or be used by dispensers. Test units still count toward the area cap while loaded. No natural biome spawns are registered. All six tiers are persistent, including in Peaceful; issued gear never drops.

`config/standandhold-npcs.toml` is server-owned/synced. Each tier has health, damage, movement speed, armor and required-stage settings with bounded ranges and comments. Stat changes apply to newly spawned/reloaded NPCs; reload clamps current health instead of healing damage. Stage/deployment settings apply to subsequent attempts. No existing progression thresholds or research costs are changed.

## Targeting And Movement

NPCs float, wander, look at nearby players and fight visible, reachable hostile mobs or configured threats. Vanilla `Goal` APIs are sufficient for this foundation; a Brain/library would not simplify these tasks. Target acquisition accepts vanilla `Enemy`-marked mobs by default, plus the existing [optional ID/tag mappings](samples-and-threats.md). A mapping identifies a target even if its point/drop rewards are zero. Empty, malformed or missing mappings never turn into an infection fallback target or extra rewards.

Set `targetHostileMobs=false` in `standandhold-npcs.toml` to restore mapping-only defence. The default broad classification includes normally neutral monsters such as Endermen, not every angry animal. Optional threat mobs outside `Enemy` still need explicit IDs/tags. Targeting a skeleton/creeper does not grant points or samples under the default zombie-only reward mapping.

**M35: Riflemen are always armed.** An Army Rifleman is created holding a non-dropping service rifle and fires it through the vanilla ranged goal at a target in sight between 3 and `serviceRifleRange` blocks (default 14, at most the 16-block target range), every `serviceRifleIntervalTicks` (default 30) for `serviceRifleDamage` (default 4). The shot is hitscan: one block ray and one entity box along the shot, no projectile entity. It holds fire while a player or another soldier is in the line of fire, and the hit chance falls from 90% up close to 60% at full range. Shots play the original `rifle.fire` report, trigger the long-gun aim, recoil and GeckoLib fire clip, and leave muzzle smoke and hit sparks. The rifle's cooldown is not saved (at most one early shot after a reload). These four keys are in `standandhold-npcs.toml`; existing files gain them with the defaults.

After the equipment stage/research unlock, the prototype launcher replaces the issued rifle (or fills an empty main hand). Vanilla ranged navigation uses the same target/home safety, checks line of sight and fires only within 12 blocks, at least 40 ticks apart by default. Inside 3 blocks or when ranged use is unavailable, melee remains the fallback. Weapon state and next-shot timestamp save with the entity; no new deployment source/quota is added. Existing held items are not overwritten, and the original intrinsic armour/melee stats do not change. See [Equipment](equipment.md) for config, friendly-hit protection and QA limits.

`survivorBladeEnabled` controls new gated issue to empty-handed Survivors. The held modifier adds to intrinsic damage (default 8 raw, 12 against mapped threats before mitigation), not a new base stat. The issued marker/stack save normally and retain use after stage drops or disabling new issue. New/unarmed defenders still need the gate; losing the physical blade cannot reissue one below it. Unrelated held items are not replaced and issued gear does not drop. Player weapons can now hurt human mobs like other mobs, but NPCs cannot attack/retaliate against players or humans.

Players, other Stand and Hold humans and allies are excluded, even if explicitly mapped. There is no player retaliation goal. Intrinsic attack damage **and attack knockback** attributes are registered before combat. The server rechecks target eligibility before damage; removed/dead/ineligible targets are abandoned. Search is local (maximum 16 blocks) and throttled to at least 20 ticks; empty rules skip acquisition only when hostile targeting is disabled. Vanilla reach/line-of-sight/stuck-path checks handle blocked targets. No cross-world search or chunk ticket is used. The pinned pathfinding region reads `getChunkNow` and treats missing chunks as empty rather than requesting them.

NPC kills do **not** earn points/samples with the default `requirePlayerKill=true`. The existing all-deaths option enables those rewards too, but also enables environmental farms. Threat mods stay optional; no SRP classes, IDs or assets are imported.

## Command-Post Deployment

A loaded, writable Field Command Post may deploy one unlocked unit every **1,200 loaded ticks** (about 60 seconds at 20 TPS). An eligible Main Base can reserve just one Division operative; a second attempt uses the highest other eligible tier, subject to the same shared cap. A normal checkpoint never recruits Division. There is no automatic-deployment supply cost. Upgrading a post changes capacity, not unit stats, recruitment gates or spawn timing. Physical growth remains the separately guarded opt-in system.

- Level targets: **3/5/6/7/8 saved reservations** at levels 1-5, including unloaded defenders. Effective capacity is clamped by the server ceiling, area ceiling and 16-entry roster hard limit; see the [capacity/config rollout contract](buildings.md#defender-capacity-contract). Existing configs that explicitly say `maxDefendersPerPost = 3` still cap every level at three; fresh configs use ceiling 16. Raise an existing ceiling deliberately to opt into growth, rather than resetting/deleting configs.
- Default area cap: **8 living loaded humans within a 48-block box**, including nearby posts and manually spawned test units. The local query stops when it reaches the cap.
- At most 8 horizontal candidates within 6 blocks, with 5 vertical offsets each, never in the post's own column. Spawn checks require already-loaded neighboring chunks, safe walkable ground, two air blocks, no fluid at the support, collision clearance and the world border. A lone floating post is not a spawn platform. Unsuitable terrain simply skips that interval; there is no retry every tick.
- Deployed units have a saved dimension/position home and a 16-block vanilla home/pursuit radius. They can move back toward home; they never teleport or force-load it. No offline catch-up occurs. The attempt timer restarts after unload; returning to a post is not an immediate spawn.
- Caps and deployment enable/interval/radii are configurable. Setting the per-post cap to 0 disables deployment. Lowering a cap never deletes existing humans. Test eggs can exceed quotas manually, but still count against automatic area deployment.
- Automatic spawns, natural/admin reinforcements and paid support all use the same effective capacity. The menu shows saved assigned/reserved count (not nearby loaded humans) and slots/full/disabled/dormant/stage-locked status. Slots still require the area/terrain/cooldown checks when spawning; opening a menu never scans entities.

The area cap is not a global population budget. Widely separated posts can each recruit a small group. Do not use mass admin egg/spawner automation as performance evidence.

## Local Duty Contract

Command Posts default to **Guard**, retaining existing home/wander behaviour. The Duties tab or admin `/standandhold commandpost duty guard|patrol` selects a saved per-post policy; `duty status` inspects the nearest loaded post within 64 blocks and at most its 16 reserved UUIDs. Ordinary menu users need the same local ownership/range/validity checks as other building actions. Switching policy never recruits, pays, grants rewards, changes quotas or adopts test eggs.

Patrol uses two short waypoints south of the post. A proven applied revision-2 yard additionally offers exactly one watch slot at its west deck's open centre. Only existing home-assigned **ledger members** may claim it. The UUID reservation lives in the post, including across unload/restart. A missing loaded-entity lookup does not release it; confirmed roster removal permits reassignment. Guard explicitly clears the slot. An occupied, damaged, unloaded or unsupported destination is not repaired or teleported to.

Vanilla combat outranks temporary response orders, which outrank local duties. A response order holds its destination until its existing expiry; local work resumes afterward. Combat interrupts local travel and returns to a valid duty afterward. Missing loaded posts release homes through the existing removal check; unloaded homes suspend duty without assuming death. Dormant Main Bases suspend local work. Watch is a position assignment, not a new sensor, turret, sight-range bonus or spawn source.

Post/ledger/waypoint resolution is throttled to 40 ticks per assigned NPC. Navigation requests are at most one per 40 ticks; three unproductive attempts incur 200 ticks of backoff. There are two waypoints and one watch target, with loaded support/headroom/fluid/border/collision checks and vanilla non-loading pathfinding. No global tick handler, entity scan, chunk ticket, population catch-up or teleport is added. The existing target search and home radius remain unchanged.

Optional post `duties` version 1 stores mode and watch UUID; unknown fields survive writes, while unknown/malformed policies remain retained and disable only duty controls. Missing data means Guard. Entity home/UUID/type/health/gear remain existing saved data, with one clamped waypoint index added. Runtime path/backoff counters restart without offline catch-up. This is not a world-schema upgrade or legacy conversion.

## Saved Quotas And Removal

Modern `HumanWorldData` schema **4** adds a bounded `defenders` compound: dimension-aware post key to at most 16 reserved entity UUIDs. Existing modern schema 0-3 data migrates without deleting progression, research, mission receipts, registrations or unknown fields. This does not import 1.12 worlds. The ledger dirties only when a reservation changes.

Ordinary defender markers remain byte `1`; byte `2` reserves the single Division slot inside the same roster. Unload/restart keeps that marker. Death or confirmed removal releases it. Duplicate Division markers or unknown values fail closed. Back up before upgrading: older builds do not understand the new entity IDs or the Division marker, so do not downgrade a specialist-era save in place.

Spawning reserves a slot before adding the entity and releases it if addition fails. Confirmed death/discard/dimension transfer releases the old reservation even if the home chunk is unloaded. Chunk unload/server shutdown does **not** release reservations. Loaded NPCs detect missing home posts on a throttled check, clear their home and release the slot; surviving NPCs are not deleted. List/nearest cleanup still never loads missing chunks.

Unknown or malformed roster data refuses deployment rather than silently resetting into free quota. An unresolved UUID remains reserved: absence from the loaded entity lookup is **not** proof of death. Crash/partial backup restores or external entity-file edits can leave conservative stale reservations and reduce deployment. Back up whole worlds; do not delete the ledger to recover slots while units may survive. Cross-file power-loss recovery/repair tooling is deferred.

## Original Visuals

The [art pipeline](art-pipeline.md) holds editable original Blockbench projects and deterministic exports. Models use an explicit **64x64 box-UV atlas**, with classic humanoid body rectangles in the top half and accessory UVs below, not the player-skin layout. Survivor has a soft cap/brim, red jacket and ochre pack; Rifleman has a teal helmet, graphite vest and double sleeve marks. Geometry follows vanilla humanoid animation and held-item transforms, with no new collision/stats/equipment behavior. Tests check opaque base/accessory faces and exact source/runtime texture parity. `GenerateNpcTextures.java` seeds uniforms/eggs, but embedded Blockbench PNGs are authoritative after editing. These remain simple alpha visuals, not finished rigged characters or clip-driven animation.

Heavy adds an olive/charcoal uniform, amber markings, deeper helmet, broad vest and arm-following shoulder plates. Elite has a blue-grey uniform, mint markings, a short helmet/visor and compact torso-following pack. Super Elite has charcoal armour, brass accents, broad shoulders and a plated chest. Division has an ivory/teal shell, asymmetric scanner and rear pod, and shares the Rifleman's native launcher motion. All four use original editable `.bbmodel` sources. The normal exporter and CI `-Check` validate all ten original source models.

Since M33 every soldier also gets one of 12 UUID-derived appearance variants and a slightly different render-only build (see [Soldier Variants](art-pipeline.md#soldier-variants-m33)). It changes nothing about stats, hitboxes or AI. Soldier steps add a quiet gear rattle, launchers use an original launch-and-cycle sound, and Command Posts play occasional client-side radio chatter.

For a disposable operator test, add 30 supplies, set stage 1, complete `parasite_samples` and `field_communications`, then set stage 2/3 before a normal deployment/event/paid call-in. All commands begin `/standandhold`; see the existing support/event docs for cooldowns. These are grants for technical QA, not earned-loop evidence. Existing explicit population ceilings remain authoritative, and a full lower-tier roster is not replaced just because a higher tier unlocks.

## Manual QA

1. Use a disposable modern world with commands enabled, no external mods. Check Mods, both egg names/icons and complete, distinct body textures from front/back/sides, walking and melee swings. No missing-texture checkerboard or transparent limbs should appear.
2. Spawn each tier near a zombie, then a skeleton/creeper on clear ground. Both should approach and damage eligible mobs. Observe a blocked/unreachable zombie, then remove it: no crash or repeated log spam. An unconfigured pig must be ignored. In Survival, lightly hit a human with no hostile mob nearby; it must not retaliate or attack another human.
3. At stage 0 place a post with safe open ground. Wait 60 loaded seconds for a Survivor. Set stage 1 before all slots fill; the next available deployment should be a Rifleman. Existing Survivors stay Survivors. `/summon` and eggs must still work below the automatic gate.
4. A level-1 post stops at 3 assigned units. With the server ceiling allowing growth, level 2 permits 5; adjacent posts still stop at 8 loaded humans total (manual eggs count). Kill one assigned defender and allow one interval: only one replacement. Disable deployment/cap 0, or raise both required stages: no automatic spawn. Mechanics have GameTest coverage; manual play should judge crowding/feel rather than repeat numeric readbacks.
5. Save/reload and move far enough to unload/reload a post. Units, homes and quotas remain; no immediate flood. Break a loaded post: no crash, and surviving assigned units lose their home. Retest list/nearest. Water/lava, blocked and unsupported platforms must not produce unsafe spawns.
6. In a separate disposable test config with `targetHostileMobs=false`, try the bundled `#standandhold:test_threats` tag, an empty list, a missing mod ID/tag and malformed rules. Live mapping removal should stop attacks; missing IDs must not target pigs. Restore defaults and confirm ordinary hostile targeting returns without changing reward mappings. External-mod coexistence, multiple real clients and packaged installations require separate evidence.

Build and runtime evidence are recorded in STATUS.md, not inferred from the existence of these tests.
