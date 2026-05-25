# Stand and Hold

Stand and Hold is a Minecraft Forge 1.12.2 mod about a human military resistance forming in a parasite-infected world.

This repository is currently in Phase 17: a compileable Forge project foundation for a future large-scale Stand and Hold mod. It includes persistent human progression points, supply points, stage commands, configurable entity-death point rewards, parasite tissue samples, a basic data-driven research system, military infrastructure blocks, passive point and supply generation from loaded command posts, Field Command Post upgrade levels, a basic Research Lab, tiered human NPC test units, bounded outpost defender spawning, a generated Small Army Checkpoint, persistent Main Base registration/activation, Special Parasite Division gating, regional threat tracking, threat decay, bounded reinforcement triggers, and the first simple human equipment items. Gameplay systems such as full scientist AI, advanced weapons, larger generated bases, complex gun mechanics, and full Scape and Run: Parasites compatibility are intentionally left for later phases.

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

- Java 8 JDK
- Gradle wrapper from this repository
- Minecraft Forge 1.12.2 tooling downloads on first build

This scaffold uses the maintained anatawa12 ForgeGradle 2.3 fork because current Forge Maven metadata no longer works cleanly with the old official `net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT` setup. The mod target remains Minecraft Forge 1.12.2, with Forge `14.23.5.2847` as the development artifact because it publishes the legacy `userdev` classifier expected by this toolchain.

ForgeGradle 2.3 is an older build toolchain and should be run with Java 8. Newer Java versions may fail before compilation or while processing old dependencies.

## Build

Make sure `JAVA_HOME` points to a Java 8 JDK before running Gradle.

From the repository root:

```sh
./gradlew build
```

On Windows PowerShell:

```powershell
.\gradlew.bat build
```

The compiled mod jar will be created under `build/libs/`.

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
minecraft:zombie=5
```

That zombie entry is only a safe test value so the feature can be verified without Scape and Run: Parasites installed. No SRP entity IDs are hardcoded. Once verified, add SRP registry IDs to the config list.

Parasite sample drops are configured separately:

```text
modid:entity_registry_name=chance
```

The default sample drop test entry is:

```text
minecraft:zombie=0.25
```

## Phase 3 Research

Research entries are configured as pipe-separated data:

```text
id|category|name|description|pointReward|requiredResearchIds|sampleCost
```

Use comma-separated `requiredResearchIds`, or leave that field blank. `sampleCost` consumes Parasite Tissue Samples from the player completing the research command.

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

The `standandhold:field_command_post` block is the first military infrastructure placeholder. It has a basic tile entity, placeholder blockstate/model JSON, and registers its dimension/position in `HumanWorldData` when placed or loaded.

Right-clicking a Field Command Post shows the current human point total and army stage. It does not generate structures, open a GUI, or drive base mechanics yet.

## Phase 6 Passive Generation

Loaded Field Command Posts generate human points over time using tile-local tick logic. There is no global block scan; each loaded tile only checks its saved `LastPointGenerationTime` against the configured interval.

Config options:

```text
enableFieldCommandPostPointGeneration=true
fieldCommandPostPointsPerInterval=1
fieldCommandPostTickInterval=1200
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
researchLabTickInterval=200
researchLabProgressPerInterval=10
researchLabProgressRequired=100
researchLabMaxStoredSamples=16
```

When lab progress reaches the configured requirement, the lab completes the target research if it has enough stored samples for that research entry's `sampleCost`. Completion uses the normal `ResearchManager` path, so research IDs persist in `HumanWorldData` and completion point rewards still flow through `HumanPointManager`.

Scientist AI is deferred. Phase 8 includes only a placeholder scientist support class so later phases have a clean place to grow entity behavior.

## Phase 9 Human NPC Foundation

The first human NPC foundation added:

- `EntityHumanNpc`, a base class for future human units
- `EntitySoldier`, a basic melee soldier
- Forge entity registration with a spawn egg for testing
- Client renderer using a vanilla humanoid model/texture

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

The default `minecraft:zombie` target is only for safe testing without Scape and Run: Parasites installed. Add verified SRP registry IDs to `humanUnitTargetEntityIds` later.

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

Small Army Checkpoints are the first generated structure. They are intentionally simple: a small stone platform with low cobblestone walls, a front opening, torches, and a Field Command Post anchor in the middle.

Generation is chunk-local and conservative. The generator checks the configured dimension, applies a per-chunk chance, clamps dimensions to fit safely inside one chunk, and skips terrain that is too uneven or unsafe. It does not use global scans or template systems yet.

Config options:

```text
enableArmyCheckpointGeneration=true
armyCheckpointSpawnChance=120
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

## Phase 13 Main Base Foundation

Main Bases are rare, chunk-local generated foundations for late-game human escalation. The first version is intentionally small and procedural: a fortified stone footprint with a central Stand and Hold Field Command Post, marked defender spawn pads, and Research Lab blocks in the lab areas.

Main Bases do not use vanilla `minecraft:command_block`. The command center is built from this mod's `standandhold:field_command_post`.

Activation is based on the current human stage. If a Main Base generates while the world is at or above the configured activation stage, its central Field Command Post is set to Main Base level and a small initial defender group can spawn on the marked pads. Below that stage, the base still generates as a dormant foundation.

Config options:

```text
enableMainBaseGeneration=true
mainBaseSpawnChance=2400
mainBaseWidth=15
mainBaseDepth=15
mainBaseWallHeight=3
mainBaseMaxTerrainHeightDifference=2
mainBaseActivationStage=5
mainBaseInitialDefenders=4
mainBaseDefenderPatrolRadius=24
mainBaseAllowedDimensions=[0]
```

Admins can force-test Main Base placement in the current chunk:

```text
/standandhold structure mainbase
```

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
threatLevelThresholds=[0,10,25,50]
parasiteKillThreatIncrease=2
humanLossThreatIncrease=10
outpostAttackThreatIncrease=6
outpostAttackThreatCooldownTicks=200
threatReinforcementThreshold=25
reinforcementCooldownTicks=12000
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

Phase 16 adds the first lightweight supply economy. Supplies are stored globally in `HumanWorldData`, and loaded buildings also save local supply stockpile counters for later expansion.

Current supply sources:

- Placing and right-clicking a Supply Crate block
- Depositing a Supply Crate item/block into a Field Command Post
- Depositing a Supply Crate item/block into a Research Lab
- Passive Field Command Post supply generation over a configurable interval

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
```

Supply spending currently draws from the saved global supply pool. Building-local stockpiles are saved and displayed as an expandable foundation for later logistics networks, routes, and supply-transfer rules.

## Phase 17 Basic Human Equipment

Phase 17 adds simple equipment items without starting firearm mechanics.

Added equipment:

- Army armour set
- Elite armour set
- Special Division armour set
- Anti-Parasite Blade melee weapon

The equipment is registered as normal Forge items and appears in the Stand and Hold creative tab. The first pass uses vanilla model/armour texture references as placeholders so the items are compileable and testable before custom art is added.

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
```

Recipes are intentionally deferred or can be added as simple JSON recipes later. The acceptance requirement is satisfied by creative-tab entries and registered item models.

## Planned Next Phase

Phase 18 should build on the point, sample, supply, research, lab, command-post, unit-tier, outpost-defence, checkpoint, Main Base, Special Parasite Division, threat-response, and equipment foundations without jumping into the entire final system at once:

- Tune point, supply, and threat values from playtesting
- Add basic crafting recipes, repair rules, or stage/research gates for equipment
- Add basic logistics routes, convoy placeholders, or more detailed building supply transfer rules
- Add basic lab-linked scientist behavior or Main Base/Special Division deployment balancing
- Keep Scape and Run: Parasites compatibility data-driven until entity IDs are verified
