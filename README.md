# Stand and Hold

Stand and Hold is a Minecraft Forge 1.12.2 mod about a human military resistance forming in a parasite-infected world.

## Mod Overview

The mod adds a human-side escalation layer to worlds where parasite-style threats are getting stronger. Humanity earns progression by killing configured parasite/test entities, recovering parasite samples, completing research, building infrastructure, defending outposts, and maintaining supply lines.

The current implementation is a compileable foundation for a larger mod. It already includes persistent world progression, supplies, research, missions, sample drops, basic military buildings, generated checkpoints and Main Bases, human NPC tiers, a prototype ranged weapon, simple GUIs, networking, optional SRP compatibility mapping, dynamic outpost events, threat tracking, performance throttling, and first-pass stability fixes.

This repository is packaged as `0.1.0-alpha.1`. The emphasis is still foundation quality: the systems are intentionally small, server-authoritative where needed, data-driven where possible, and expandable for later phases. Phase 33 documents the first playable vertical slice, Phase 34 improves optional SRP compatibility testing, Phase 35 adds a simple original alpha art identity pass, Phase 36 improves structure foundations, and Phase 37 expands the command-driven mission chain. Gameplay systems such as full scientist AI, advanced weapons, larger generated bases, complex reload/ammo mechanics, physical convoy entities, complex raid waves, mission GUIs, and full Scape and Run: Parasites integration are intentionally deferred.

## Current Scope

- Forge 1.12.2 project scaffold
- Java 8 source target
- Main mod class
- Shared mod constants
- Basic Forge config class
- Basic Log4j logger
- `mcmod.info`
- Basic language/resource files
- Persistent global human progression data
- Persistent global supply point data
- Admin/debug progression command
- Configurable human stage thresholds
- Configurable entity-death point rewards
- Optional Scape and Run: Parasites loaded-state detection
- Data-driven research entries
- Persistent completed research IDs
- Config-backed mission entries with persistent objective progress
- Automatic mission progress hooks for samples, field command, supplies, research, stages, outpost defence, and structure discovery
- Early alpha mission chain from sample recovery through Main Base foundation
- Basic Parasite Tissue Sample item
- Configurable sample drops from configured entity deaths
- Field Command Post block and tile entity
- Persistent Field Command Post position registration
- Passive Field Command Post point generation
- Passive Field Command Post supply generation and saved local supply storage
- Persistent Field Command Post upgrade levels
- Research Lab block and tile entity
- Lab-local saved research progress, stored parasite samples, and stored supplies
- Supply Crate block/item form for recovering supplies
- Army, Elite, and Special Division armour item sets
- Basic Anti-Parasite Blade melee weapon
- Prototype ranged weapon item and custom projectile entity
- Equipment recipes and stage/research use gates
- Simple repair recipes for equipment
- Command Post and Research Lab GUI screens
- SimpleNetworkWrapper packet channel
- Server-to-client progression and tile data sync packets
- Server-authoritative GUI actions for command post upgrades and lab research selection/completion
- Config-backed supply transfer controls for local building stockpiles and global supplies
- Dedicated optional SRP compatibility helper with config-backed mappings
- Tile-local logistics route placeholder for loaded Field Command Posts
- Tile-local dynamic outpost attack and reinforcement events
- Admin dynamic event debug commands
- Dynamic event cooldown/status command and nearby warning messages
- Throttled command-post manager checks and cached config parsing
- Client item/model registration for Forge 1.12.2
- Missing item model coverage for Parasite Tissue Samples
- Base human NPC entity class
- Tiered human unit entities with spawn eggs and simple parasite targeting AI
- Field Command Post outpost defender spawning with limits
- Small Army Checkpoint world generation
- Rare Main Base foundation generation
- Persistent Main Base registration and activation state
- Special Parasite Division Operative stage/research gating
- Regional threat response records, threat decay, and reinforcement triggers
- Admin outpost and structure debug commands

## Requirements

- Java 8 JDK for Gradle builds
- Gradle wrapper from this repository
- Minecraft Forge 1.12.2 tooling downloads on first build

This scaffold uses the maintained anatawa12 ForgeGradle 2.3 fork because current Forge Maven metadata no longer works cleanly with the old official `net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT` setup. The mod target remains Minecraft Forge 1.12.2, with Forge `14.23.5.2847` as the development artifact because it publishes the legacy `userdev` classifier expected by this toolchain.

ForgeGradle 2.3 is an older build toolchain and must be run with Java 8. Modern Java versions can fail before compilation with errors such as `Unable to get mutable Windows environment variable map` and `java.lang.reflect.InaccessibleObjectException`.

## Build

Make sure `JAVA_HOME` points to a Java 8 JDK before running Gradle. Full build notes are in [docs/building.md](docs/building.md).

From the repository root:

```sh
./gradlew build
```

On Windows PowerShell:

```powershell
.\scripts\build-java8.ps1
```

The helper prints `java -version`, checks `JAVA_HOME`, stops old Gradle daemons, and runs `.\gradlew.bat clean build --no-daemon --stacktrace`.

If PowerShell blocks local scripts, run the helper with a one-run policy bypass:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1
```

If Java 8 is already active, the direct command is:

```powershell
.\gradlew.bat clean build --no-daemon --stacktrace
```

The compiled mod jar will be created under `build/libs/`. For this alpha, use `build/libs/standandhold-0.1.0-alpha.1.jar`.

## Alpha Readiness Docs

- [First Playable Alpha Loop](docs/first-playable-loop.md)
- [Mission System](docs/missions.md)
- [Optional SRP Compatibility Guide](docs/srp-compatibility.md)
- [SRP Compatibility Test Checklist](docs/srp-test-checklist.md)
- [Build Guide](docs/building.md)
- [Wider Alpha Testing Checklist](docs/alpha-testing-checklist.md)
- [Alpha QA Results Template](docs/alpha-qa-results-template.md)
- [Phase 31 QA Session 001](docs/qa-sessions/phase-31-alpha-qa-session-001.md)
- [Release Checklist](docs/release-checklist.md)
- [Known Issues](KNOWN_ISSUES.md)
- [Changelog](CHANGELOG.md)

## First Playable Alpha Loop

The current playable slice is a Creative/admin alpha test loop: kill configured test parasites, recover Parasite Tissue Samples, use a Field Command Post and Research Lab, complete the first research, reach an early army stage, spawn bounded defenders, and trigger or observe a simple outpost event.

Start with:

```text
/standandhold help
/standandhold status
```

Then follow [docs/first-playable-loop.md](docs/first-playable-loop.md) for step-by-step expected results and troubleshooting.

## Installation

For a normal test install:

1. Install Minecraft Forge `1.12.2-14.23.5.2847` or a compatible Forge 1.12.2 build.
2. Build this project with Java 8 using `.\scripts\build-java8.ps1` on Windows or `./gradlew clean build --no-daemon --stacktrace` when Java 8 is active.
3. Copy `build/libs/standandhold-0.1.0-alpha.1.jar` into the Minecraft instance `mods` folder.
4. Start the game or dedicated server once to generate `config/standandhold.cfg`.
5. Edit the generated config for your pack, especially parasite registry IDs and balance values.

Scape and Run: Parasites is optional. Stand and Hold does not import SRP classes, so it should load without SRP installed. If SRP is installed, add verified SRP entity registry IDs to the config mappings before relying on parasite rewards, drops, or targeting.

For dedicated servers, install the same jar in the server `mods` folder and configure the server as usual for Forge 1.12.2. The dev `runServer` task may stop at the normal Minecraft EULA gate until `eula.txt` is accepted by the server owner.

## Command Reference

The base command is:

```text
/standandhold
```

General commands:

```text
/standandhold help
/standandhold status
/standandhold addpoints <amount>
/standandhold setstage <0-6|stage_name>
```

Research commands:

```text
/standandhold research list
/standandhold research status
/standandhold research complete <id>
```

Supply commands:

```text
/standandhold supplies status
/standandhold supplies add <amount>
```

Building and base commands:

```text
/standandhold commandpost list
/standandhold commandpost nearest
/standandhold commandpost status [x] [y] [z]
/standandhold commandpost upgrade [x] [y] [z]
/standandhold mainbase list
/standandhold mainbase status [x] [y] [z]
/standandhold mainbase activate [x] [y] [z]
```

Threat, event, structure, and mission debug commands:

```text
/standandhold threat status
/standandhold threat list
/standandhold threat reinforce
/standandhold threat reset
/standandhold event status [x] [y] [z]
/standandhold event outpostattack [x] [y] [z]
/standandhold event reinforcement [x] [y] [z]
/standandhold structure checkpoint
/standandhold structure mainbase
/standandhold mission list
/standandhold mission status [id]
/standandhold mission active
/standandhold mission start <id>
/standandhold mission progress <id> <amount>
/standandhold mission complete <id> [force]
/standandhold mission reset <id>
```

Most status/list/active commands are safe for normal use. Mutating commands such as adding points, setting stages, forcing structures, activating Main Bases, threat changes, event triggers, and mission debug actions require admin permission level 2.

## Config Overview

Forge writes the config as `config/standandhold.cfg`. Important categories:

- `Progression`: human stage point thresholds.
- `Point Sources`: configured parasite/test kill rewards and sample drop chances.
- `Compatibility`: optional Scape and Run: Parasites loaded check and SRP mapping entries.
- `Research`: data-driven research definitions, lab progress timing, and lab storage caps.
- `Missions`: data-driven mission/objective definitions.
- `Infrastructure`: Field Command Post generation, upgrade, defender, and building timing values.
- `Supply`: global supplies, local building storage, transfer amounts, and logistics placeholder settings.
- `Equipment`: armour, melee weapon, prototype ranged weapon, unlock gates, repair balance, damage, cooldown, and projectile polish.
- `Human NPCs`: human unit stats, stage gates, targeting, and Special Parasite Division deployment values.
- `World Generation`: checkpoint and Main Base generation chances, dimensions, and allowed dimensions.
- `Threat Response`: regional threat scoring, decay, reinforcement cooldowns, and caps.
- `Dynamic Events`: natural outpost attack and reinforcement event timing, warning radius, and spawn caps.

Defaults are balanced for testing rather than final SRP pack difficulty. Real parasite packs should tune kill rewards, sample chances, threat values, and human unit stats after verifying entity registry IDs and playtesting.

## Progression Stages

Human progression is global per world save and is stored in `HumanWorldData`. The current default thresholds are:

- Stage 0, `0` points: Survivors
- Stage 1, `150` points: Local Army Response
- Stage 2, `500` points: Organised Military
- Stage 3, `1200` points: Elite Units
- Stage 4, `2600` points: Super Elite Units
- Stage 5, `5200` points: Special Parasite Division
- Stage 6, `10000` points: Main Base / Endgame Counter-Offensive

Points can come from configured parasite/test kills, research completion, building upgrades, missions, passive command post generation, and later systems that call `HumanPointManager`.

## Research System

Research is data-driven through config entries:

```text
id|category|name|description|pointReward|requiredResearchIds|sampleCost|supplyCost
```

Research completion persists in `HumanWorldData`. Completed research can unlock later features, building upgrades, equipment use, Special Parasite Division deployment, and future systems. Research can be completed by command or through loaded Research Labs that generate progress over time and consume stored Parasite Tissue Samples when a target is ready.

## Building System

Current military infrastructure:

- Field Command Post: core outpost block, local supply storage, passive human point/supply generation, upgrade levels, defender spawning, dynamic event anchor, logistics placeholder anchor, and GUI.
- Research Lab: stores research progress, parasite samples, and local supplies; contributes to research completion over time.
- Supply Crate: recoverable supply block/item used for the first supply economy.
- Small Army Checkpoint: simple generated/debug structure containing a Field Command Post.
- Main Base: rare generated/debug structure with command post, lab areas, defender pads, persistent registration, and activation checks.

Building systems are deliberately tile-local or saved-data-driven. They avoid global world scans and rely on saved positions, loaded tile entities, and bounded spawn limits.

## SRP Compatibility

Scape and Run: Parasites compatibility is optional and string-based. The mod uses `Loader.isModLoaded` and configured registry IDs through `SRPCompat`; it does not reference SRP classes directly, copy SRP internals, or require SRP to launch.

Full setup and testing notes are in [docs/srp-compatibility.md](docs/srp-compatibility.md) and [docs/srp-test-checklist.md](docs/srp-test-checklist.md).

SRP mapping format:

```text
entityId|killReward|sampleDropChance|humanTarget
```

Example after verifying the actual registry ID in your SRP build:

```text
srparasites:example_parasite|25|0.35|true
```

No verified SRP entity IDs are enabled by default. The default `minecraft:zombie` entries are safe test values and should be replaced or supplemented in real parasite packs.

During Phase 34, the supplied `SRParasites-1.10.6.jar` metadata was inspected only to confirm its mod id is `srparasites`. Entity registry IDs still need runtime verification in the exact SRP build being tested.

## Phase 1 Progression Core

Human progression is stored in `HumanWorldData`, a Forge `WorldSavedData` record attached to the overworld save. This keeps the point total and current stage persistent across world saves and reloads.

Stages are recalculated from configurable point thresholds whenever points are added:

- Stage 0: Survivors
- Stage 1: Local Army Response
- Stage 2: Organised Military
- Stage 3: Elite Units
- Stage 4: Super Elite Units
- Stage 5: Special Parasite Division
- Stage 6: Main Base / Endgame Counter-Offensive

Use these commands in a world or dedicated server:

```text
/standandhold status
/standandhold addpoints <amount>
/standandhold setstage <0-6|stage_name>
```

`status` is available to any command sender. `addpoints` and `setstage` require permission level 2.

## Phase 2 Kill Rewards

Stand and Hold listens for Forge `LivingDeathEvent` on the server. When the dead entity's registry ID matches a configured reward entry, the mod adds human progression points through `HumanPointManager`.

Reward entries are configured as:

```text
modid:entity_registry_name=points
```

The default config includes:

```text
minecraft:zombie=3
```

That zombie entry is only a safe test value so the feature can be verified without Scape and Run: Parasites installed. No SRP entity IDs are hardcoded. Once verified, add SRP registry IDs to the normal reward lists or to the dedicated SRP compatibility mapping list.

Parasite sample drops are configured separately:

```text
modid:entity_registry_name=chance
```

The default sample drop test entry is:

```text
minecraft:zombie=0.15
```

## Phase 3 Research

Research entries are configured as pipe-separated data:

```text
id|category|name|description|pointReward|requiredResearchIds|sampleCost|supplyCost
```

Use comma-separated `requiredResearchIds`, or leave that field blank. `sampleCost` consumes Parasite Tissue Samples where supported, and `supplyCost` consumes saved global supply points.

Current research commands:

```text
/standandhold research list
/standandhold research status
/standandhold research complete <id>
```

Completed research IDs are stored in `HumanWorldData`, so they persist with the world save. Later systems can use `ResearchManager.isResearchComplete(world, id)` to gate features.

## Phase 4 Samples

The `standandhold:parasite_tissue_sample` item is the first physical progression item. It appears on the Stand and Hold creative tab and can drop from configured parasite/test entities. The default `parasite_samples` research entry requires one sample.

## Phase 5 Field Command Post

The `standandhold:field_command_post` block is the first military infrastructure placeholder. It has a tile entity, placeholder blockstate/model JSON, and registers its dimension/position in `HumanWorldData` when placed or loaded.

Right-clicking a Field Command Post opens the current command post GUI. Sneak-right-clicking still attempts a debug upgrade path for quick testing.

## Phase 6 Passive Generation

Loaded Field Command Posts generate human points over time using tile-local tick logic. There is no global block scan; each loaded tile only checks its saved `LastPointGenerationTime` against the configured interval.

Config options:

```text
enableFieldCommandPostPointGeneration=true
fieldCommandPostPointsPerInterval=1
fieldCommandPostTickInterval=2400
```

When points are awarded, the normal `HumanPointManager` path updates `HumanWorldData`, recalculates stages, and marks the world data dirty.

## Phase 7 Building Upgrades

Field Command Posts now save an upgrade level in their tile entity NBT:

- Level 1: Field Camp
- Level 2: Reinforced Outpost
- Level 3: Military Outpost
- Level 4: Fortified Base
- Level 5: Main Base

Sneak-right-click a Field Command Post to attempt the next upgrade. You can also use the admin command:

```text
/standandhold commandpost status <x> <y> <z>
/standandhold commandpost upgrade <x> <y> <z>
/standandhold commandpost list
/standandhold commandpost nearest
```

Upgrade requirements are configured as:

```text
targetLevel|requiredHumanPoints|requiredResearchIds|parasiteSampleCost|pointReward
```

Human points are checked as a progression requirement and are not spent. Parasite Tissue Samples are consumed from the upgrading player unless they are in creative mode. Successful upgrades can award human progression points through `HumanPointManager`.

## Phase 8 Research Labs

The `standandhold:research_lab` block is the first lab foundation. It has a tile entity that stores:

- Current target research ID
- Current lab-local research progress
- Stored Parasite Tissue Sample count
- Last progress tick time

Right-clicking the lab shows its current target, progress, and stored samples. Right-clicking with a Parasite Tissue Sample stores one sample in the lab. Loaded labs automatically pick the first incomplete research entry whose prerequisite research is complete, then add progress over time.

Config options:

```text
enableResearchLabProgress=true
researchLabTickInterval=400
researchLabProgressPerInterval=8
researchLabProgressRequired=160
researchLabMaxStoredSamples=16
```

When lab progress reaches the configured requirement, the lab completes the target research if it has enough stored samples for that research entry's `sampleCost`. Completion uses the normal `ResearchManager` path, so research IDs persist in `HumanWorldData` and completion point rewards still flow through `HumanPointManager`.

Scientist AI is deferred. Phase 8 includes only a placeholder scientist support class so later phases have a clean place to grow entity behavior.

## Phase 9 Human NPC Foundation

The first human NPC foundation added:

- `EntityHumanNpc`, a base class for future human units
- `EntitySoldier`, a basic melee soldier
- Forge entity registration with a spawn egg for testing
- Client renderer using a vanilla humanoid model with tier-specific original placeholder textures

Human units use simple, stable AI only:

- Swim
- Wander
- Watch nearby players
- Melee attack configured parasite/test entity IDs

Human units do not target players by default, and they ignore other Stand and Hold human NPCs.

## Phase 10 Army Unit Tiers

The human NPC foundation now has six spawn-egg-testable unit tiers:

- Survivor Defender
- Army Rifleman
- Heavy Soldier
- Elite Soldier
- Super Elite Soldier
- Special Parasite Division Operative

Each unit tier uses the same simple AI foundation from Phase 9, but has configurable health, damage, and required human stage:

```text
enableHumanUnitParasiteTargeting=true
humanUnitTargetEntityIds=["minecraft:zombie"]
humanUnitStats=[
  "survivor_defender|16|2|0",
  "army_rifleman|20|4|1",
  "heavy_soldier|28|6|2",
  "elite_soldier|36|8|3",
  "super_elite_soldier|48|11|4",
  "special_parasite_division_operative|60|14|5"
]
```

The default `minecraft:zombie` target is only for safe testing without Scape and Run: Parasites installed. Add verified SRP registry IDs to `humanUnitTargetEntityIds` or the dedicated SRP compatibility mapping list later.

Stage unlocks are enforced when units spawn. If a unit is spawned before the world's human stage meets that tier's `requiredHumanStage`, the entity is removed immediately instead of joining the world. Special Parasite Division Operatives are additionally clamped to Stage 5 or higher.

## Phase 11 Outpost Defence

Until generated outposts exist, loaded Field Command Posts act as outpost anchors. Each anchor can spawn a small number of assigned defenders over time.

Defender selection scales from the current human stage by choosing the strongest configured unit tier whose required stage is unlocked. For example, Stage 0 uses Survivor Defenders, Stage 1 can use Army Riflemen, and later stages move toward elite and Special Parasite Division units.

Spawn control is deliberately conservative:

- Spawning happens only from loaded Field Command Post tile entities
- Each outpost has its own saved spawn cooldown
- Each outpost counts only living defenders assigned to that outpost
- Defenders receive a patrol/home radius around their outpost
- No global world scans are used

Config options:

```text
enableOutpostDefenderSpawning=true
outpostMaxDefenders=3
outpostDefenderSpawnInterval=2400
outpostDefenderPatrolRadius=16
outpostDefenderSpawnSearchRadius=4
```

## Phase 12 Structure Generation

Small Army Checkpoints are the first generated structure. They are intentionally compact: a stone-brick platform with a cobblestone path, low cobblestone-wall perimeter, front gate opening, torches, a central Field Command Post, and a couple of Supply Crates for alpha testing.

Generation is chunk-local and conservative. The generator checks the configured dimension, applies a per-chunk chance, clamps dimensions to fit safely inside one chunk, and skips terrain that is too uneven, liquid, or occupied by tile-entity blocks in the clearance area. It does not use global scans or template systems yet.

Config options:

```text
enableArmyCheckpointGeneration=true
armyCheckpointSpawnChance=180
armyCheckpointWidth=9
armyCheckpointDepth=9
armyCheckpointWallHeight=2
armyCheckpointMaxTerrainHeightDifference=2
armyCheckpointAllowedDimensions=[0]
```

Admins can force-test checkpoint placement in the current chunk:

```text
/standandhold structure checkpoint
```

The debug command ignores the configured spawn chance but still uses the same safe terrain checks.
It also respects the configured allowed dimensions and reports the generated Command Post position plus registration state.

More structure details are in [docs/structures.md](docs/structures.md).

## Phase 13 Main Base Foundation

Main Bases are rare, chunk-local generated foundations for late-game human escalation. The first version is intentionally small and procedural: a fortified footprint with a central Stand and Hold Field Command Post, marked defender spawn pads, Research Lab blocks, Supply Crates, simple zone markers, and a clearer command area.

Main Bases do not use vanilla `minecraft:command_block`. The command center is built from this mod's `standandhold:field_command_post`.

Activation is based on the current human stage. If a Main Base generates while the world is at or above the configured activation stage, its central Field Command Post is set to Main Base level and a small initial defender group can spawn on the marked pads. Below that stage, the base still generates as a dormant foundation.

Config options:

```text
enableMainBaseGeneration=true
mainBaseSpawnChance=3600
mainBaseWidth=15
mainBaseDepth=15
mainBaseWallHeight=3
mainBaseMaxTerrainHeightDifference=2
mainBaseActivationStage=5
mainBaseInitialDefenders=3
mainBaseDefenderPatrolRadius=24
mainBaseAllowedDimensions=[0]
```

Admins can force-test Main Base placement in the current chunk:

```text
/standandhold structure mainbase
```

The debug command reports the generated origin, central Command Post, registration state, activation state, initial defender count, and total registered Main Bases.

## Phase 14 Main Base Activation and Special Parasite Division

Generated Main Bases are now registered in `HumanWorldData` using their central Field Command Post position. Active Main Bases are tracked separately, so dormant bases persist across saves and can activate later.

Admin commands:

```text
/standandhold mainbase list
/standandhold mainbase status <x> <y> <z>
/standandhold mainbase activate <x> <y> <z>
```

Loaded dormant Main Bases can activate once the world reaches the configured activation stage, clamped to Stage 5 or higher. Activation upgrades the central Field Command Post to Main Base level and can spawn a small, bounded defender group.

Special Parasite Division Operatives now require:

- Human Stage 5 or higher
- `special_division_training` research, unless the config gate is disabled

The default research list includes:

```text
special_division_training|SPECIAL_PROJECTS|Special Division Training|Train select operatives for anti-parasite rapid deployment and containment work.|150|parasite_samples,outpost_doctrine|3
```

Active loaded Main Bases can rarely deploy Special Parasite Division Operatives. Deployment is bounded per Main Base, uses tile-local cooldowns, and only spawns operatives assigned to that base.

Config options:

```text
enableSpecialParasiteDivisionResearchGate=true
specialParasiteDivisionTrainingResearchId=special_division_training
enableSpecialParasiteDivisionDeployments=true
specialParasiteDivisionDeploymentInterval=12000
specialParasiteDivisionDeploymentChance=4
specialParasiteDivisionMaxOperativesPerMainBase=2
specialParasiteDivisionDeploymentRadius=8
```

## Phase 15 Threat Response

Threat response is the first foundation for humanity reacting to parasite pressure. Threat is tracked by configurable chunk regions and persisted in `HumanWorldData`.

Tracked events:

- Configured parasite/test entity deaths
- Stand and Hold human unit losses
- Assigned outpost defenders being attacked by configured parasite/test entities

Threat records store:

- Threat score and threat level
- Parasite kills
- Human losses
- Outpost attacks
- Reinforcements sent
- Last event, reinforcement, outpost attack, and decay world times

Admin commands:

```text
/standandhold threat status
/standandhold threat list
/standandhold threat reinforce
/standandhold threat reset
```

Automatic reinforcement triggers require an active loaded Main Base, a loaded threat target region, the configured threat threshold, and the region cooldown. Manual reinforcement uses the current threat region and bypasses threshold/cooldown, but still respects loaded chunks, active Main Base availability, spawn safety, and per-region reinforcement caps.

Threat decay is intentionally opportunistic in this first version. It runs when threat records are touched by commands, new threat events, or deployment checks, so the mod avoids adding a heavy global world tick scan.

Config options:

```text
enableThreatTracking=true
enableThreatReinforcements=true
threatRegionChunkSize=4
threatLevelThresholds=[0,12,35,70]
parasiteKillThreatIncrease=2
humanLossThreatIncrease=10
outpostAttackThreatIncrease=6
outpostAttackThreatCooldownTicks=200
threatReinforcementThreshold=25
reinforcementCooldownTicks=18000
reinforcementUnitsPerTrigger=2
maxReinforcementsPerThreatRegion=6
reinforcementSpawnRadius=8
reinforcementPatrolRadius=32
enableThreatDecay=true
threatDecayIntervalTicks=24000
threatDecayAmount=1
maxThreatScore=100
maxThreatRecords=128
```

Special Parasite Division deployments now prefer loaded high-threat regions when available, while still falling back to the Main Base area. Threat-response reinforcements keep their source Main Base assignment but receive a patrol target at the high-threat region, giving them a basic order to move toward the fight without global pathfinding management.

## Phase 16 Supply Economy and Threat Refinement

Phase 16 adds the first lightweight supply economy. Supplies are stored globally in `HumanWorldData`, and loaded buildings also save local supply stockpile counters. Phase 21 makes those local counters part of the first logistics loop through GUI import/export controls.

Current supply sources:

- Placing and right-clicking a Supply Crate block adds global supplies
- Depositing a Supply Crate item/block into a Field Command Post adds local building supplies
- Depositing a Supply Crate item/block into a Research Lab adds local building supplies
- Passive Field Command Post supply generation adds local building supplies over a configurable interval

Supply commands:

```text
/standandhold supplies status
/standandhold supplies add <amount>
```

Research entries now accept an optional supply cost:

```text
id|category|name|description|pointReward|requiredResearchIds|sampleCost|supplyCost
```

Field Command Post upgrades now accept a supply cost:

```text
targetLevel|requiredHumanPoints|requiredResearchIds|parasiteSampleCost|supplyCost|pointReward
```

Supply config options:

```text
supplyCrateValue=10
commandPostMaxStoredSupplies=64
researchLabMaxStoredSupplies=32
enableCommandPostSupplyGeneration=true
commandPostSuppliesPerInterval=1
commandPostSupplyTickInterval=2400
enableGuiSupplyTransfers=true
guiSupplyTransferAmount=10
enableCommandPostLogisticsRoute=true
commandPostLogisticsRouteInterval=2400
commandPostLogisticsRouteTransferAmount=5
commandPostLogisticsRouteMinimumLocalSupplies=10
```

Supply spending currently draws from the saved global supply pool. Building-local stockpiles can be exported into that global pool from the building GUIs, which keeps the first logistics layer explicit without scanning the world.

## Phase 17 Basic Human Equipment

Phase 17 adds simple equipment items. Phase 18 extends that foundation with one prototype ranged weapon while still avoiding advanced firearm mechanics.

Added equipment:

- Army armour set
- Elite armour set
- Special Division armour set
- Anti-Parasite Blade melee weapon
- Prototype Ranged Weapon

The equipment is registered as normal Forge items and appears in the Stand and Hold creative tab. The current alpha pass uses simple original Stand and Hold item and armour textures so equipment can be distinguished during testing before final art is produced.

Phase 18 adds simple JSON recipes for the armour sets, Anti-Parasite Blade, and Prototype Ranged Weapon. Higher-tier equipment is also protected by runtime use gates, so crafting or creative access does not bypass human progression.

Config-backed equipment values:

```text
armyArmorDurability=18
armyArmorReductions=[2,5,6,2]
armyArmorEnchantability=9
armyArmorToughness=0.0
eliteArmorDurability=28
eliteArmorReductions=[3,6,8,3]
eliteArmorEnchantability=10
eliteArmorToughness=1.0
specialDivisionArmorDurability=36
specialDivisionArmorReductions=[3,6,8,3]
specialDivisionArmorEnchantability=14
specialDivisionArmorToughness=2.0
antiParasiteBladeHarvestLevel=2
antiParasiteBladeMaxUses=320
antiParasiteBladeEfficiency=6.0
antiParasiteBladeAttackDamage=5.0
antiParasiteBladeEnchantability=12
armyEquipmentRequiredStage=1
armyEquipmentRequiredResearch=field_communications
eliteEquipmentRequiredStage=3
eliteEquipmentRequiredResearch=outpost_doctrine
specialDivisionEquipmentRequiredStage=5
specialDivisionEquipmentRequiredResearch=special_division_training
antiParasiteBladeRequiredStage=2
antiParasiteBladeRequiredResearch=parasite_samples
prototypeRangedWeaponRequiredStage=1
prototypeRangedWeaponRequiredResearch=field_communications
prototypeRangedWeaponDamage=6.0
prototypeRangedWeaponParasiteDamageMultiplier=1.25
prototypeRangedWeaponCooldownTicks=24
prototypeRangedWeaponMaxUses=384
prototypeRangedWeaponVelocity=2.0
prototypeRangedWeaponInaccuracy=1.5
prototypeProjectileHitParticles=8
enablePrototypeProjectileHitSound=true
enableArmyRiflemanRangedWeapon=true
armyRiflemanRangedAttackInterval=40
armyRiflemanRangedAttackRange=18.0
```

The Prototype Ranged Weapon fires `EntityAntiParasiteProjectile`, a small custom projectile entity. The projectile only damages configured parasite/test entities, using the same data-driven entity checks as parasite kill rewards and human targeting. Players use a cooldown instead of ammo/reload mechanics, and Army Rifleman NPCs can use the weapon through a simple ranged attack task when enabled.

Phase 19 adds hit feedback for that projectile: configured parasite/test hits can apply a configurable damage multiplier, spawn simple crit particles, and play a light impact sound. It also adds shapeless repair recipes for armour, the Anti-Parasite Blade, and the Prototype Ranged Weapon.

## Phase 19 Basic GUIs

Phase 19 introduces two simple server-opened block GUIs:

- Field Command Post GUI: human points, human stage, command post level, global supplies, local stockpile, passive point generation, and defender cap/timer.
- Research Lab GUI: target research ID/name, progress bar, completion requirements, stored parasite samples, local supplies, global supplies, and blocked/ready status.

The first GUI pass uses Forge containers for server-client sync. Numeric values are sent through normal container window properties. The screens are intentionally plain and stable so later phases can replace them with richer layouts and controls.

## Phase 20 Networking and GUI Actions

Phase 20 adds a small `SimpleNetworkWrapper` channel for Stand and Hold GUI data. The first packets are deliberately narrow:

- `PacketSyncHumanProgression` sends human points, stage, and supply points from the server to the client.
- `PacketSyncTileData` sends Field Command Post or Research Lab tile data needed by the current GUI.
- `PacketGuiAction` lets GUI buttons request server-side actions.

The Field Command Post GUI now has an `Upgrade` button that calls the existing server-side upgrade manager. The Research Lab GUI now shows the currently selected research target and has `Next` and `Complete` buttons for cycling available research and attempting completion. The client only sends requests; the server validates range, tile type, requirements, costs, and saved world data before applying changes.

## Phase 21 Balancing Config and Supply Transfers

Important gameplay values are config-backed and documented with Forge config comments:

- Stage thresholds
- Configured parasite/test kill point rewards and sample drop chances
- Data-driven research rewards, prerequisites, sample costs, and supply costs
- Field Command Post upgrade requirements and rewards
- Passive building point/supply intervals
- Outpost defender spawn limits and intervals
- Structure generation chances, dimensions, and allowed dimensions
- Unit health, damage, targeting, deployment, and reinforcement values
- Equipment stats, unlock gates, projectile damage, cooldown, and polish values

Phase 21 also makes the first logistics loop explicit. Supply Crates deposited into a Field Command Post or Research Lab now fill that building's local stockpile. Field Command Post passive supply generation also fills local storage. The building GUIs include `Import` and `Export` buttons:

- `Import` moves a configurable amount from global supplies into the open building's local storage.
- `Export` moves a configurable amount from the open building's local storage into global supplies.

Transfers are handled by the server through `PacketGuiAction`, validate the open tile entity and player range, and only touch the open building. There are no global block scans.

New supply config options:

```text
enableGuiSupplyTransfers=true
guiSupplyTransferAmount=10
```

## Phase 22 SRP Compatibility and Logistics Placeholder

Phase 22 adds `SRPCompat`, a dedicated optional compatibility helper for Scape and Run: Parasites. It only uses `Loader.isModLoaded` and registry ID strings; it does not import or reference SRP classes, so Stand and Hold still loads normally when SRP is missing.

SRP mappings are configurable as:

```text
entityId|killReward|sampleDropChance|humanTarget
```

Example after verifying an entity registry ID in the exact SRP build being used:

```text
srparasites:example_parasite|25|0.35|true
```

No built-in SRP entity IDs are enabled yet because the registry IDs have not been verified in this repository. Existing fallback/test entries such as `minecraft:zombie=3` still work without SRP.

Compatibility config options:

```text
enableScapeAndRunParasitesCompatibility=true
scapeAndRunParasitesModId=srparasites
srpParasiteMappings=[]
enableVerifiedSrpDefaultMappings=false
```

Phase 22 also adds the first logistics route placeholder. Loaded Field Command Posts can periodically export a small amount of local supplies into the global supply pool. This is server-side, tile-local, and bounded by a per-command-post saved cooldown; it does not scan for route endpoints or spawn physical convoy entities yet.

## Phase 23 Dynamic Events and Raids

Phase 23 adds the first lightweight dynamic event layer. Loaded Field Command Posts can periodically roll for a natural event using a saved per-tile cooldown, so there is still no global world scan.

Implemented event types:

- Outpost attack: spawns configured attacker entity registry IDs near the command post and records an outpost attack threat event.
- Human reinforcement: spawns the strongest currently unlocked human unit tier near the command post and assigns it to defend that outpost.

Event difficulty scales with the current human stage by increasing attacker or reinforcement counts up to configurable caps. Outpost attackers default to `minecraft:zombie` as a safe test value and should be replaced with verified parasite IDs in real packs.

Dynamic event config options:

```text
enableDynamicEvents=true
enableNaturalDynamicEvents=true
dynamicEventIntervalTicks=12000
dynamicEventChance=5
outpostAttackWeight=3
humanReinforcementWeight=1
dynamicEventSpawnRadius=12
outpostAttackEntityIds=[minecraft:zombie]
outpostAttackBaseCount=1
outpostAttackersPerStage=1
outpostAttackMaxCount=6
humanReinforcementBaseCount=1
humanReinforcementsPerStage=0
humanReinforcementMaxCount=2
humanReinforcementPatrolRadius=24
```

Admin debug commands:

```text
/standandhold event status [x] [y] [z]
/standandhold event outpostattack [x] [y] [z]
/standandhold event reinforcement [x] [y] [z]
```

Phase 24 adds `event status`, which shows the nearest or targeted Field Command Post natural event cooldown, interval, and chance settings. Natural outpost attacks and reinforcement events can also send a configurable chat warning to nearby players.

Additional dynamic event config options:

```text
enableDynamicEventWarnings=true
dynamicEventWarningRadius=64
```

## Phase 24 Missions and Objectives

Phase 24 introduced the first persistent mission foundation. Phase 37 expands it into a small early alpha objective chain. Mission definitions are config-backed and mission progress is saved in `HumanWorldData`, alongside the existing progression, research, supply, base, and threat state.

Default alpha mission chain:

- `recover_parasite_sample`: recover one Parasite Tissue Sample.
- `establish_field_command`: place or discover a Field Command Post.
- `stockpile_supplies`: build a small supply reserve.
- `complete_first_research`: complete one research entry.
- `reach_local_response`: reach Stage 1 Local Army Response.
- `defend_outpost`: trigger or survive one outpost attack event.
- `establish_main_base`: generate, discover, or register a Main Base foundation.

Mission config format:

```text
id|name|description|objectiveType|requiredCount|pointReward|supplyReward|researchRewardIds
```

Current objective types:

- `RECOVER_PARASITE_SAMPLE`: progress updates from Parasite Tissue Sample pickup events.
- `ESTABLISH_FIELD_COMMAND`: progress updates when a Field Command Post is registered.
- `STOCKPILE_SUPPLIES`: progress updates from global supplies, local building stockpiles, and GUI supply transfers.
- `COMPLETE_RESEARCH`: progress updates when research completes.
- `REACH_HUMAN_STAGE`: progress updates from the saved human stage.
- `DEFEND_OUTPOST`: progress updates when a dynamic outpost attack is created as the first placeholder for defence objectives.
- `DISCOVER_STRUCTURE`, `DISCOVER_CHECKPOINT`, and `DISCOVER_MAIN_BASE`: progress updates when generated Stand and Hold structures are placed by world generation or debug commands.
- `MANUAL`: progress is controlled by command for future scripted or event-driven objectives.

Commands:

```text
/standandhold mission list
/standandhold mission status [id]
/standandhold mission active
/standandhold mission start <id>
/standandhold mission progress <id> <amount>
/standandhold mission complete <id> [force]
/standandhold mission reset <id>
```

`list`, `status`, and `active` are inspection commands. `start`, `progress`, `complete`, and `reset` are admin/debug commands for test worlds. Completing without `force` checks tracked objective progress; `force` is available for testing worlds and scripted setup.

## Phase 25 Balance and Integration

Phase 25 tightens default pacing while keeping everything config-driven. The defaults are intentionally conservative because SRP-style parasite packs can become extremely dangerous, but Stand and Hold should not hand out late-game human escalation too quickly in ordinary test worlds.

Balance assumptions:

- Vanilla `minecraft:zombie` remains a test parasite only, so it gives low point rewards and a modest sample chance.
- Human stages now require a wider point curve: `0, 150, 500, 1200, 2600, 5200, 10000`.
- Passive command-post point and supply generation is slower, so building spam is less rewarding.
- Research labs progress more slowly and late research has higher supply/sample requirements.
- Command Post upgrades require more points, supplies, and samples while returning smaller completion point rewards.
- Outpost defenders and dynamic reinforcements are bounded more tightly to avoid free army growth.
- Dynamic raids are rarer, but still stage-scale attackers so later worlds stay pressured.
- Threat reinforcements require higher regional threat and have longer cooldowns.
- Special Parasite Division remains strong and stage/research-gated; it is the main long-term counterweight for verified high-power parasite mappings.
- SRP compatibility remains data-driven. Verified SRP mappings should tune kill rewards, sample rates, and human targeting per parasite tier instead of relying on one universal reward value.

## Phase 26 Performance Review

Phase 26 keeps gameplay behavior intact while reducing avoidable server work:

- Field Command Posts still tick because they own local storage and timers, but heavier manager checks for logistics, dynamic events, outpost defence, and Main Base activation are now staggered to once per second per tile.
- Main Bases no longer scan for nearby Special Parasite Division operatives every tick. The entity query now runs only after deployment cooldown and chance checks pass.
- Research and mission config entries are cached behind lightweight array hashes, so labs, commands, and mission hooks do not repeatedly parse the same config strings.
- Dynamic outpost attacks now perform one nearby-human target query per event instead of one query per spawned attacker.
- Spawn limits remain enforced by existing caps: outpost defender counts, Main Base operative limits, threat reinforcement region caps, and dynamic event max counts.
- World generation remains chance-gated, dimension-gated, and footprint-limited. The current generated structures still validate surface terrain before placing blocks.

The remaining intentional scans are bounded and event-driven: command/debug listing commands inspect saved position sets, threat commands inspect saved threat records, and generated structures only examine their small configured footprints.

## Phase 27 Stability Testing

Phase 27 fixed stability issues found during the first focused review:

- Added Forge 1.12.2 client model registration for Stand and Hold item and block item models.
- Added the missing Parasite Tissue Sample item model JSON.
- Kept equipment unlock checks server-authoritative so client-side right-click prediction does not read server `WorldSavedData`.

Validation used:

```text
./gradlew build --no-daemon --offline
./gradlew runServer --no-daemon
```

The build passed. The dev dedicated-server run reached the normal Minecraft EULA gate; the repository does not change or accept `run/eula.txt`.

## Phase 28 Documentation

Phase 28 is documentation-only. It reorganizes the README with a clearer mod overview, installation notes, command reference, config overview, progression summary, research and building explanations, SRP compatibility notes, roadmap, and known issues.

## Phase 29 Alpha Release Prep

Phase 29 prepares the first alpha test jar, `0.1.0-alpha.1`.

Release validation:

```text
./gradlew build --no-daemon --offline
./gradlew runServer --no-daemon --offline
```

The dedicated server smoke test accepts the local dev `run/eula.txt` when explicitly requested, starts the server, waits for `Done`, then sends `stop` through server stdin. In the latest validation, Stand and Hold loaded on the dedicated server, registered its server commands, saved the world, and stopped cleanly.

## Roadmap

Near-term roadmap:

- Keep testing save/load behavior for `HumanWorldData`, tile entities, missions, threat records, and Main Base activation.
- Tune point, supply, and threat values from actual SRP-style playtesting.
- Add more polished item, block, projectile, and NPC assets while keeping the foundation stable.
- Expand logistics from local/global transfer placeholders toward visible routes or convoy entities.
- Add richer raid outcomes, warning messages, and cooldown displays.
- Add scientist behavior and stronger lab integration without heavy global ticking.
- Add more generated structures only after the current checkpoint and Main Base generators are stable.
- Keep SRP compatibility data-driven until registry IDs are verified against the exact SRP build being supported.

Longer-term roadmap:

- More human unit roles, deployment rules, and faction behavior.
- Larger outposts, fortified bases, and late-game counter-offensive systems.
- Research-gated infrastructure, advanced equipment, and anti-parasite tools.
- Optional pack-specific balance presets for SRP-heavy modpacks.

## Known Issues

- Java 8 is required for local builds. Java 17+ or other modern Java versions can fail before compilation with legacy Gradle/ForgeGradle reflection errors.
- SRP entity registry IDs are not verified or enabled by default. The shipped `minecraft:zombie` entries are test values.
- Visual assets are simple original alpha placeholders and still need final art.
- GUIs are clearer after Phase 35 but still need later UX/art passes.
- The prototype ranged weapon has no ammo or reload system yet.
- Scientist AI, full structure generation, physical convoy entities, and complex raids are deferred.
- Stale saved Field Command Post and Main Base positions should be tested if blocks are broken, structures are removed, or worlds are edited externally.
- Dev `runServer` may need online Gradle dependency resolution the first time, then stops at the normal EULA gate until the server owner accepts it.
- Dedicated-server smoke testing passes in the ForgeGradle dev workspace, but should still be repeated in a normal installed Forge server before public distribution.
- ForgeGradle dev runs can print old Forge/FML warnings or errors about Maven library paths, missing FML signature data, or console appenders. The alpha server smoke is considered healthy when Stand and Hold loads, the server reaches `Done`, and shutdown is clean.

See [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for the focused wider-alpha issue list.

## Planned Next Phase

Phase 30 should build on the point, sample, supply, research, mission, lab, command-post, unit-tier, outpost-defence, checkpoint, Main Base, Special Parasite Division, threat-response, equipment, simple ranged weapon, GUI, networking, logistics, SRP compatibility, dynamic event, balance, performance, stability, documentation, and alpha packaging foundations without jumping into the entire final system at once.
