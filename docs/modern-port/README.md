# Stand and Hold Modern Port

This folder is the planning memory for moving Stand and Hold from the Forge 1.12.2 alpha foundation to a modern Minecraft build.

## Current Decision

- Preserve the Forge 1.12.2 version on the `1.12` branch.
- Use `main` for the modern rewrite once the scaffold phase starts.
- Primary loader: NeoForge.
- Selected target: Minecraft 26.3 / NeoForge 26.3.0.57-beta, Java 25 (local baseline Temurin 25.0.4+7), Gradle 9.2.1, and ModDevGradle 2.0.148. See [Selected Modern Toolchain](toolchain.md). These pins are not yet build-tested.
- Closest non-beta-loader alternative: Minecraft 26.2. Matching animation/AI libraries, Undead Nights, and Contagion are verified for both primary candidates. Minecraft 1.21.1 is the verified alternative for Spore.
- Future track: Fabric after the NeoForge version has a stable gameplay foundation.
- M1 scope: minimal NeoForge scaffold, logger/config shell, build/CI support, and launch verification. No gameplay or additional runtime libraries; no legacy world/config migration. See the accepted [foundation policy](foundation-scope.md).

Steps 0.3.3-0.3.4 selected the exact toolchain, configured local JDK selection, and documented Windows/IDE setup and fallback rules. Prove the pins with the clean scaffold build in M1 after the remaining M0 gates.

## Current Position

Phase M0 steps 0.1-0.4 and the [decision/publication review](m0-decision-review.md) in 0.5.1 are complete. All four decision checkpoints were confirmed on GitHub. Scaffold branch preparation and modern build/runtime tests remain outstanding. The source tree is still the Forge 1.12.2 alpha.

**Next: Phase M0, step 0.5.2 - recheck repository/local-file state and prepare the scaffold work branch.** See the progress log for the latest completion and publication notes.

## Why A Clean Modern Port

The 1.12.2 codebase is useful design history, but the modern Minecraft APIs are too different for a direct file-by-file port. The modern port should preserve concepts, names, progression design, and lessons learned while rebuilding the technical foundation around current NeoForge patterns.

## Planning Files

- [Project Memory](project-memory.md)
- [Phase Roadmap](phase-roadmap.md)
- [Technical Port Plan](technical-port-plan.md)
- [Ecosystem Research](ecosystem-research.md)
- [Recommendations](recommendations.md)
- [Progress Log](progress-log.md)
- [Preflight Checks](preflight-checks.md)
- [Official Release And MDK Verification](release-verification.md)
- [Library And Threat Mod Compatibility](ecosystem-compatibility.md)
- [Selected Modern Toolchain And Local JDK](toolchain.md)
- [Windows Build, IDE Setup, And Fallback Rule](building.md)
- [Foundation Scope And Dependency Policy](foundation-scope.md)
- [Consolidated M0 Decision And Publication Review](m0-decision-review.md)

The [Phase Roadmap](phase-roadmap.md) is the canonical numbered checklist. Larger tasks have subtasks such as `1.1.1`; update their checkboxes as they pass verification. The progress log records evidence, blockers, commits, and the exact next task so work can resume across sessions.

## Modern Port Principles

- No hard dependency on any infection mod.
- No copied code, textures, models, names, or assets from other mods unless a license and explicit permission allow it.
- Server authority first: persistent progression, missions, bases, and AI decisions live on the server.
- Data-driven where it helps modpack authors: config, tags, datapacks, loot tables, missions, research, structure pools.
- Build a small playable vertical slice before adding large systems.
- Keep performance budgets visible: no global scans, bounded ticking, capped spawns, loaded-chunk checks only.
