# Stand and Hold

Stand and Hold is a Minecraft Forge 1.12.2 mod about a human military resistance forming in a parasite-infected world.

This repository is currently in Phase 6: a small, compileable Forge project foundation with persistent human progression points, stage commands, configurable entity-death point rewards, parasite tissue samples, a basic data-driven research system, the first military infrastructure block, and passive point generation from loaded command posts. Gameplay systems such as structure generation, scientists, units, outposts, and full Scape and Run: Parasites compatibility are intentionally left for later phases.

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

## Planned Next Phase

Phase 7 should build on the point, sample, research, and command-post systems without jumping into full structure generation:

- Tune point rewards and stage thresholds from playtesting
- Add first lab/scientist placeholder behavior or simple block interaction
- Keep Scape and Run: Parasites compatibility data-driven until entity IDs are verified
