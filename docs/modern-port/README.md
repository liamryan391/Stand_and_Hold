# Stand and Hold Modern Port

This folder is the planning memory for moving Stand and Hold from the Forge 1.12.2 alpha foundation to a modern Minecraft build.

## Current Decision

- Preserve the Forge 1.12.2 version on the `1.12` branch.
- Use `main` for the modern rewrite once the scaffold phase starts.
- Primary loader: NeoForge.
- Selected target: Minecraft 26.3 / NeoForge 26.3.0.57-beta, Java 25 (local baseline Temurin 25.0.4+7), Gradle 9.2.1, and ModDevGradle 2.0.148. See [Selected Modern Toolchain](toolchain.md). These pins are not yet build-tested.
- Closest non-beta-loader alternative: Minecraft 26.2. Matching animation/AI libraries, Undead Nights, and Contagion are verified for both primary candidates. Minecraft 1.21.1 is the verified alternative for Spore.
- Future track: Fabric after the NeoForge version has a stable gameplay foundation.

Step 0.3.3 selected the exact toolchain and configured local JDK selection. Finish setup/fallback instructions in 0.3.4, then prove the pins with the clean scaffold build in M1.

## Current Position

Phase M0 steps 0.1, 0.2, and 0.3.1-0.3.3 are complete. Exact release artifacts, selected dependency manifests, and the Java selector have been checked; a combined runtime test has not. The source tree is still the Forge 1.12.2 alpha. Planning lives in this repository alongside it; see Git history for publication checkpoints.

**Next: Phase M0, step 0.3.4 - document Windows build commands, IDE Gradle JVM setup, and the fallback decision rule.** See the progress log for the latest completion and publication notes.

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

The [Phase Roadmap](phase-roadmap.md) is the canonical numbered checklist. Larger tasks have subtasks such as `1.1.1`; update their checkboxes as they pass verification. The progress log records evidence, blockers, commits, and the exact next task so work can resume across sessions.

## Modern Port Principles

- No hard dependency on any infection mod.
- No copied code, textures, models, names, or assets from other mods unless a license and explicit permission allow it.
- Server authority first: persistent progression, missions, bases, and AI decisions live on the server.
- Data-driven where it helps modpack authors: config, tags, datapacks, loot tables, missions, research, structure pools.
- Build a small playable vertical slice before adding large systems.
- Keep performance budgets visible: no global scans, bounded ticking, capped spawns, loaded-chunk checks only.
