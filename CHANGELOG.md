# Changelog

## Unreleased - Modern NeoForge Port

Modern port work uses one phase branch at a time, merged into `main` after verification. No modern release, version bump, or retag is announced by this entry. The source/build version remains `0.1.0-alpha.1`; observed verification results are recorded in [STATUS.md](docs/modern-port/STATUS.md).

### Combat Equipment Foundation

- Added server-gated Anti-Threat Blade, Army/Elite armour sets, ordinary crafting/discovery/repair support and original item/worn textures with synced balancing config.
- Added one cooldown-based launcher and saved short-lived bolt, loaded-only flight checks, hostile/mapped-threat damage, friendly exclusions and bounded hit feedback; no ammo/reload/runtime library.
- Added optional unlocked Rifleman loadout/ranged goal with saved shot timing, no equipment drops and existing melee/home/target/deployment safeguards.
- Human defenders now target ordinary hostile mobs as well as mapped infection threats, with a mapping-only config opt-out; kill/sample rewards still require mappings. Adjusted the held-launcher pose and removed detached Elite Helmet icon pixels after alpha feedback.
- Added equipment gate, friendly-target, flight-age, pre-config defaults and resource/recipe checks; physical multiplayer and external-mod coverage remain separate QA.

### Original Art And Menu Readability

- Added an original visual guide, five editable Blockbench projects, deterministic bounded exports and CI source/runtime parity checks; no animation runtime dependency.
- Added distinct Survivor cap/pack and Rifleman helmet/vest silhouettes, explicit 64x64 humanoid atlases, per-face utility block art and tissue-sample polish without gameplay/stat changes.
- Added translated menu states/level names, bounded two-line labels with hover details, clearer transfer/action tooltips and full completed-research bars without changing server transactions.

### Human Progression Foundation

- Added seven human stages with configurable thresholds, bounded long point totals, and explicit admin stage-reset semantics.
- Added server-global versioned SavedData, non-destructive modern migration hooks, unknown-field retention, future-schema protection, and meaningful-change dirty marking.
- Added public status and operator-only addpoints/setstage commands with invalid-input and overflow handling.
- Added dependency-free threshold/NBT regression checks to the build and documented modern progression commands, config, save contract, and runtime tests; no automatic rewards or extra runtime libraries.

### Research, Supplies, And Missions Foundation

- Added bounded JSON research/mission definitions with stable IDs, prerequisite/cycle validation, capability unlocks, and optional server datapack overrides.
- Extended modern saves to schema 2 with global supplies, research completion, mission progress, and once-only reward receipts; retained modern schema 0/1 data and unknown IDs/fields.
- Added atomic supply/research/mission operations and public inspection/operator debug commands. Overflow, insufficient costs, repeated completion, and malformed receipts fail safely.
- Added a seven-objective alpha mission chain; existing supply/research/stage state is observable now, while physical sample/building/defence hooks remain with their owning phases. No runtime library, tick handler, or physical gameplay feature is added.

### Clean NeoForge Scaffold

- Replaced legacy compile/resource inputs with the pinned official MDK layout, preserving the Forge alpha on `1.12` and retaining template notices.
- Configured Minecraft 26.3, NeoForge 26.3.0.57-beta, Java 25, Gradle 9.2.1 with distribution checksum, ModDevGradle 2.0.148, and Foojay resolver 1.0.0.
- Added a minimal side-safe entry point, identity constants, startup logger, local config shell, and expanded mod metadata; no gameplay or additional runtime libraries.
- Separated development client/server directories, enabled server console input for clean stops, and updated Windows Java 25 CI and build instructions.

### Documentation And Preparation

- Recorded M0 toolchain decisions, phase plan, dependency/save policy, and the prepared work branch.
- Inventoried the legacy implementation and exact tracked-file transition paths in M1 / 1.1.1.
- Updated the root README and current port limitations, preserving project identity and routing Forge 1.12.2 installation/gameplay/QA references to the `1.12` branch.

See the [modern progress log](docs/modern-port/progress-log.md) for task results and publication evidence. The entry below records historical Forge 1.12.2 work and validation only.

## 0.1.0-alpha.1 - 2026-05-26 (Forge 1.12.2)

Legacy history retained from the alpha. Its validation and feature list do not describe a tested modern port. The [preserved legacy changelog](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/CHANGELOG.md) remains available with its original context.

Technical foundation alpha for Stand and Hold.

### Added

- Forge 1.12.2 Java 8 mod foundation.
- Persistent human progression, stages, supplies, research, missions, threat records, command post positions, research lab positions, and Main Base registration using world-saved data.
- Configurable parasite/test kill rewards, sample drops, human targeting, and optional SRP registry mappings.
- Field Command Post, Research Lab, Supply Crate, generated Small Army Checkpoint, and rare Main Base foundation.
- Human NPC foundation with tiered units, simple parasite targeting, outpost defenders, reinforcements, and Special Parasite Division gating.
- Basic equipment, armour tiers, Anti-Parasite Blade, prototype ranged weapon, projectile entity, recipes, repair recipes, and use gates.
- Simple Command Post and Research Lab GUIs with server-authoritative actions and sync packets.
- Dynamic outpost attacks, reinforcement events, mission hooks, threat decay, and performance throttling.
- Client model registration and placeholder item/block models for alpha testing.
- CI/build verification workflow added as post-alpha readiness work.
- Java 8 Windows build helper added for local wider-alpha testing.
- Wider alpha testing checklist, release checklist, build guide, and known issues documentation.
- Phase 31 alpha QA results template and first ready-to-fill QA session file.
- Phase 32 saved data versioning, non-destructive migration hook, and loaded stale-position cleanup for alpha safety.
- Phase 33 first playable alpha loop guide and lightweight `/standandhold help` command.
- Phase 34 optional SRP compatibility guide, SRP test checklist, safer mapping detection, and clearer config comments.
- Phase 35 original alpha placeholder textures for human unit tiers, core blocks/items, armour sets, and clearer lab/command-post GUI text.
- Phase 36 improved checkpoint/Main Base layouts, safer structure generation results, clearer debug feedback, and structure documentation.
- Phase 37 early alpha mission chain, automatic mission progress hooks for the first playable loop, `mission active/reset` commands, and mission documentation.

### Validation

- `./gradlew build --no-daemon --offline` passes.
- `./gradlew runServer --no-daemon --offline` reaches `Done`, loads Stand and Hold, registers server commands, saves, and stops cleanly in the dev workspace.

### Known Issues

- SRP entity registry IDs are not verified or enabled by default; `minecraft:zombie` remains the safe test mapping.
- Visual assets are original alpha placeholders, not final art.
- GUIs are functional and clearer after the art pass, but still plain.
- Prototype ranged weapon has cooldown but no ammo or reload system.
- Scientist AI, physical convoy entities, complex raids, large authored bases, and final structure generation are deferred.
- ForgeGradle dev runs can print legacy Forge/FML warnings unrelated to Stand and Hold mod loading.

### Planned Features

- More complete SRP mapping presets after registry IDs are verified.
- Better outpost/base structure generation and activation behavior.
- Richer research/lab/scientist gameplay.
- Expanded logistics, patrols, and dynamic event outcomes.
- More polished equipment, NPC behavior, visuals, and balance presets.
