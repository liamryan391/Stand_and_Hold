# Changelog

## 0.1.0-alpha.1 - 2026-05-26

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

### Validation

- `./gradlew build --no-daemon --offline` passes.
- `./gradlew runServer --no-daemon --offline` reaches `Done`, loads Stand and Hold, registers server commands, saves, and stops cleanly in the dev workspace.

### Known Issues

- SRP entity registry IDs are not verified or enabled by default; `minecraft:zombie` remains the safe test mapping.
- Visual assets use vanilla placeholder textures/models.
- GUIs are functional but plain.
- Prototype ranged weapon has cooldown but no ammo or reload system.
- Scientist AI, physical convoy entities, complex raids, and final structure generation are deferred.
- ForgeGradle dev runs can print legacy Forge/FML warnings unrelated to Stand and Hold mod loading.

### Planned Features

- More complete SRP mapping presets after registry IDs are verified.
- Better outpost/base structure generation and activation behavior.
- Richer research/lab/scientist gameplay.
- Expanded logistics, patrols, and dynamic event outcomes.
- More polished equipment, NPC behavior, visuals, and balance presets.
