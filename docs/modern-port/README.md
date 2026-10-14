# Stand and Hold Modern Port

This folder is the planning memory for moving Stand and Hold from the Forge 1.12.2 alpha foundation to a modern Minecraft build.

## Design Decisions

- Preserve the Forge 1.12.2 version on the `1.12` branch.
- Use `main` for the modern rewrite once the scaffold phase starts.
- Primary loader: NeoForge.
- Selected target: Minecraft 26.3 / NeoForge 26.3.0.57-beta, Java 25 (local baseline Temurin 25.0.4+7), Gradle 9.2.1, and ModDevGradle 2.0.148. See [Selected Modern Toolchain](toolchain.md). These pins are not yet build-tested.
- Closest non-beta-loader alternative: Minecraft 26.2. Matching animation/AI libraries, Undead Nights, and Contagion are verified for both primary candidates. Minecraft 1.21.1 is the verified alternative for Spore.
- Future track: Fabric after the NeoForge version has a stable gameplay foundation.
- M1 scope: minimal NeoForge scaffold, logger/config shell, build/CI support, and launch verification. No gameplay or additional runtime libraries; no legacy world/config migration. See the accepted [foundation policy](foundation-scope.md).

Start with [STATUS.md](STATUS.md) for live state and [AGENTS.md](../../AGENTS.md) for the session workflow. Read reference documents only when the task needs them.

## Why A Clean Modern Port

The 1.12.2 codebase is useful design history, but the modern Minecraft APIs are too different for a direct file-by-file port. The modern port should preserve concepts, names, progression design, and lessons learned while rebuilding the technical foundation around current NeoForge patterns.

## Planning Files

- [Status](STATUS.md)
- [Phase Roadmap](phase-roadmap.md)
- [Technical Port Plan](technical-port-plan.md)
- [Ecosystem Research](ecosystem-research.md)
- [Recommendations](recommendations.md)
- [Progress Log](progress-log.md)
- [Official Release And MDK Verification](release-verification.md)
- [Library And Threat Mod Compatibility](ecosystem-compatibility.md)
- [Selected Modern Toolchain And Local JDK](toolchain.md)
- [Windows Build, IDE Setup, And Fallback Rule](building.md)
- [Foundation Scope And Dependency Policy](foundation-scope.md)
- [Legacy Transition Inventory And Exact File Manifest](legacy-inventory.md)

The [roadmap](phase-roadmap.md) owns numbered checkboxes; [STATUS.md](STATUS.md) owns live state. [AGENTS.md](../../AGENTS.md) defines concise verification and logging. Historical project memory, preflight checks, decision review, and branch preparation notes live in [archive/](archive/); they are reference material, not session-start instructions.

## Modern Port Principles

- No hard dependency on any infection mod.
- Never copy code, textures, models, or assets from other mods.
- Server authority first: persistent progression, missions, bases, and AI decisions live on the server.
- Data-driven where it helps modpack authors: config, tags, datapacks, loot tables, missions, research, structure pools.
- Build a small playable vertical slice before adding large systems.
- Keep performance budgets visible: no global scans, bounded ticking, capped spawns, loaded-chunk checks only.
