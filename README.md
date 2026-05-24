# Stand and Hold

Stand and Hold is a Minecraft Forge 1.12.2 mod about a human military resistance forming in a parasite-infected world.

This repository is currently in Phase 0: a small, compileable Forge project foundation. Gameplay systems such as human progression points, army stages, research, military buildings, scientists, units, outposts, and Scape and Run: Parasites compatibility are intentionally left for later phases.

## Current Scope

- Forge 1.12.2 project scaffold
- Java 8 source target
- Main mod class
- Shared mod constants
- Basic Forge config class
- Basic Log4j logger
- `mcmod.info`
- Basic language/resource files

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

## Phase 0 Design Notes

The current mod only registers a Forge mod entrypoint and a basic config file. It does not add gameplay behavior yet. That keeps the first pass easy to load, test, and expand.

## Planned Next Phase

Phase 1 should add the human progression foundation:

- `HumanWorldData` using `WorldSavedData`
- `HumanStage` enum
- `HumanPointManager`
- Server-side command for status and admin point changes
- Entity death event hook with configurable test entity matching
- Configurable stage thresholds and point rewards

The first gameplay pass should still avoid hardcoding Scape and Run: Parasites internals until compatibility has been verified against that mod's public IDs/API behavior.
