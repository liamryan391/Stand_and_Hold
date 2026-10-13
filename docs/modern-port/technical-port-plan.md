# Technical Port Plan

## Branch Strategy

- `1.12`: preserved Forge 1.12.2 alpha branch.
- `main`: modern NeoForge rewrite branch once scaffolding starts.
- Prepared work branch: `codex/modern-neoforge-scaffold`, using the existing checkout. [Scaffold Branch Handoff](scaffold-handoff.md) records its baseline, local-file inventory, publication route, and active checkout. Merge the verified M1 scaffold into `main`; do not replace the preserved `1.12` branch.

Before replacing files on `main`, confirm:

- `1.12` exists on GitHub.
- `git status` is clean.
- Planning docs are committed separately, with their push status recorded.
- Local worlds and unrelated user files have been identified and are excluded from the replacement.

## Target Selection

Selected target for M1:

- Loader: NeoForge.
- Minecraft: 26.3 / NeoForge 26.3.0.57-beta. Beta risk is accepted for the scaffold, with actual build/runtime proof still required.
- Java: 25, local baseline Temurin 25.0.4+7. Gradle: 9.2.1. ModDevGradle: 2.0.148. MDK Foojay resolver: 1.0.0.

Fallback target:

- Minecraft 26.2 is the closest verified alternative with non-beta NeoForge builds. Also retain 26.1.2, 1.21.11, and 1.21.1 for the ecosystem comparison.
- Step 0.3.2 confirms matching libraries and Undead Nights/Contagion for both 26.3 and 26.2. See [Ecosystem Compatibility](ecosystem-compatibility.md). A non-beta loader label or matching metadata is not proof of modpack runtime compatibility.

Step 0.3.3 is complete. [Selected Modern Toolchain](toolchain.md) owns the pins, rationale, distribution checksum, and immutable MDK snapshot. It deliberately selects loader 26.3.0.57-beta instead of the MDK default 26.3.0.52-beta while retaining the template's plugin/wrapper. Verify that choice with the M1 build; no root legacy Gradle files are changed yet.

The ecosystem check supports the selected 26.3 target. If Spore specifically becomes mandatory, 1.21.1 is the verified match and would require an explicit target decision change, not an automatic downgrade.

The 2026-10-07 preflight found Java 25 on PATH but Java 8 in JAVA_HOME. Step 0.3.3 added and verified `scripts/use-modern-java.ps1` with ignored local path storage; it selects Java 25 in the calling PowerShell process without changing global settings. The current legacy helper still intentionally selects Java 8. Step 0.3.4 completed [Windows/IDE setup and the fallback procedure](building.md); do not run the legacy wrapper under Java 25. M1 must verify actual Gradle/IDE behavior and isolate modern client/server run directories from legacy worlds.

## Clean Scaffold Work Breakdown

Use the canonical checkboxes and substeps in [Phase Roadmap](phase-roadmap.md); these references describe the sequence without creating another task numbering system.

| Step | Result |
| --- | --- |
| 0.3 | Exact compatible toolchain selected, with source links and fallback criteria |
| 0.4 | Initial dependency policy and save compatibility scope recorded |
| 0.5 | Planning committed; preserved refs checked; scaffold work branch prepared |
| 1.1 | Inventoried legacy implementation/build files replaced with the selected MDK layout on the work branch |
| 1.2 | Pinned build, wrapper, metadata, run profiles, helper, and CI configuration |
| 1.3 | Minimal `standandhold` entry point, logger, and config under `com.liamryan.standandhold` |
| 1.4 | Actual build, artifact, client, and dedicated-server results recorded |
| 1.5 | Verified scaffold committed and merged into `main`; publication status recorded |

## 1.12 To Modern API Map

| 1.12 concept | Modern NeoForge replacement |
| --- | --- |
| `WorldSavedData` | `SavedData` |
| `TileEntity` | `BlockEntity` |
| `ICommand` style commands | Brigadier commands |
| `SimpleNetworkWrapper` | Modern NeoForge networking payloads |
| manual registry events | `DeferredRegister` |
| old config class | `ModConfigSpec` and registered NeoForge mod config files |
| item/block model registration code | JSON/datagen-first resources |
| structure placement helper code | modern structure/worldgen APIs or bounded custom placement |
| entity AI goals only | vanilla Goal/Brain APIs, optional AI library later |
| entity registry ID string matching | entity tags, resource locations, and config/datapack mappings |

API details must follow the pinned target's docs. The config name above was checked against the [official NeoForge configuration documentation](https://docs.neoforged.net/docs/misc/config/).

## Initial Package Shape

```text
com.liamryan.standandhold
com.liamryan.standandhold.config
com.liamryan.standandhold.common.progression
com.liamryan.standandhold.common.world
com.liamryan.standandhold.common.command
com.liamryan.standandhold.common.block
com.liamryan.standandhold.common.item
com.liamryan.standandhold.common.entity
com.liamryan.standandhold.common.mission
com.liamryan.standandhold.common.research
com.liamryan.standandhold.common.network
com.liamryan.standandhold.client
com.liamryan.standandhold.datagen
```

## Dependency Policy

The accepted [Foundation Scope And Dependency Policy](foundation-scope.md) governs M1 and future dependency admission. **M1 has no additional third-party gameplay/runtime library dependencies and no approved exceptions.** Minecraft/NeoForge's normal dependencies and the pinned official MDK build tooling remain required; "NeoForge-only" does not mean removing those dependencies.

Deferred candidates, not current requirements or compatibility promises:

- Prefer a GeckoLib prototype for animated entities when needed; AzureLib is a verified alternative. Both have exact 26.3 and 26.2 NeoForge releases. Choose one for Stand and Hold's models.
- SmartBrainLib has exact releases for both primary candidates, but start with bounded vanilla goals and add it only if AI complexity warrants it.
- JEI/EMI integration for recipe/research visibility.
- Jade/WTHIT integration for base block status.
- Curios or an equivalent accessory system only if equipment modules need extra slots.
- Patchouli-style guidebook only after the core loop is stable.

Each later addition needs a recorded purpose, exact target artifact and source, licence/provenance, physical-side/transitive requirements, verification results, and maintenance/removal cost. An M1 library exception or other scope expansion needs explicit maintainer approval first. A library directly used by core code is a required library, not an optional integration.

Undead Nights and Contagion are optional test-instance mods, not scaffold dependencies. Spore is a verified 1.21.1-only candidate among the checked targets. Keep all compatibility behind tags/config/optional adapters and test both integrated and dedicated servers.

No new libraries or threat versions are selected by step 0.4. Recheck dated ecosystem evidence when the owning phase actually needs a dependency. Do not copy, bundle, or download external mods at runtime by default.

## Data Design

Modern Stand and Hold should treat save data as first-class from phase one:

- version every saved data file
- keep migrations small
- store bases and threat records by dimension and chunk/BlockPos
- never force-load chunks for cleanup
- keep global state server-side
- sync only the minimum needed for GUIs
- use separate modern test worlds; legacy world/config/NBT conversion is out of scope, while migrations between modern schemas start with the relevant persistence phase

A preserved Git branch is not a backup of ignored worlds or local configs. Inventory and back up valuable local files before the scaffold replacement; see the [save/local-file policy](foundation-scope.md#saves-and-local-files). This policy adds no M1 save system.

## Compatibility Design

Compatibility should be built around:

- entity tags such as `standandhold:threats`
- config entries for point/sample rewards
- datapack-driven research and mission definitions where practical
- optional integration classes loaded only when target mods are present

Avoid direct imports from threat mods unless a small optional compat module is explicitly planned.

## Verification Gates

Every modern phase should answer:

- Does `gradlew build` pass?
- Does the client launch?
- Does the dedicated server reach readiness and shut down cleanly? An EULA stop is a recorded prerequisite, not successful runtime verification.
- Does save/reload preserve the new data?
- Does the feature work without external infection mods?
- Does the feature fail safely with malformed config?
- Does the feature avoid unbounded scans, ticks, or spawns?

For documentation-only changes, check links, numbered tasks, and `git diff --check`; record that a runtime build was not required/run. For implementation, keep failures or unavailable tests open in the progress log. Complete a phase only when its relevant exit gates have evidence.
