# Stand and Hold

Stand and Hold is a Minecraft Forge 1.12.2 mod about a human military resistance forming in a parasite-infected world.

This repository is currently in Phase 2: a small, compileable Forge project foundation with persistent human progression points, stage commands, and configurable entity-death point rewards. Gameplay systems such as research, military buildings, scientists, units, outposts, and full Scape and Run: Parasites compatibility are intentionally left for later phases.

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

## Planned Next Phase

Phase 3 should build on the point system without jumping into full structures or mobs:

- Tune point rewards and stage thresholds from playtesting
- Add first research/sample command or item placeholder
- Keep Scape and Run: Parasites compatibility data-driven until entity IDs are verified
