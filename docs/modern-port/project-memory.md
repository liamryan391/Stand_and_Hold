# Project Memory

Last updated: 2026-10-14

## Preserved History

- Stand and Hold began as a Forge 1.12.2 Java 8 technical alpha.
- The 1.12.2 alpha is preserved on the `1.12` branch.
- The old alpha reached `0.1.0-alpha.1` and included progression, research, missions, buildings, NPC foundations, structures, threat response, supplies, optional SRP mappings, and alpha art.
- `main` still contains the 1.12.2 code until the modern scaffold phase intentionally replaces it.
- On 2026-10-07, live GitHub `main` and `1.12` both matched local commit `62bb07b84eb614b5967ef318ccb98aef1f22a5d8` (`Fix recursive mission completion crash`).

## Current Modern Direction

- Loader: NeoForge first.
- Selected target: Minecraft 26.3 / NeoForge 26.3.0.57-beta. Keep the beta-loader risk explicit; this is selected, not build-proven.
- Closest non-beta-loader alternative: 26.2. Exact NeoForge releases of GeckoLib, AzureLib, SmartBrainLib, Undead Nights, and Contagion are verified for both 26.3 and 26.2. Runtime combinations remain untested.
- Build pins: Java 25 (local Temurin 25.0.4+7), Gradle 9.2.1, ModDevGradle 2.0.148, Foojay resolver 1.0.0. [Selected Modern Toolchain](toolchain.md) owns the decision, checksum, immutable MDK source, and local setup.
- Secondary loader later: Fabric, after NeoForge has a stable foundation and the shared design is proven.
- Accepted [foundation scope and dependency policy](foundation-scope.md): M1 is a minimal NeoForge scaffold with no added runtime libraries or approved exceptions. Build tooling/platform dependencies remain required. Gameplay returns in its owning later phases.
- Work branch prepared: `codex/modern-neoforge-scaffold`. Use the existing checkout at `C:\Users\liamr\Documents\ChatGPT\Stand and Hold`; merge verified M1 work into `main` and leave `1.12` unchanged. [Scaffold Branch Handoff](scaffold-handoff.md) owns the branch/local-file evidence.

## Resume Point And Working Rules

- Current phase: M1 - Clean NeoForge Scaffold. M0 and 1.1.1 (preservation/legacy inventory) are complete; documentation/file transition and modern build/IDE/runtime tests remain open.
- Next step: **1.1.2 - preserve modern planning notes and project identity, and move legacy-only documentation references to the 1.12 branch in the modern README**.
- The [roadmap](phase-roadmap.md) owns task IDs and checkboxes; the [progress log](progress-log.md) records results and the current next step. Use `1.1`, `1.2`, and deeper IDs such as `1.1.1` for large tasks.
- Keep these planning files in the project repository. The research checkpoint commit is titled `docs: record modern port roadmap and ecosystem compatibility`; use Git history and remote refs to confirm publication rather than inferring it from a local file's existence.
- [M0 Decision Review](m0-decision-review.md) verified the published decisions; its checkpoint `dbf0de8` was pushed and became the initial branch-preparation baseline. M0 completion checkpoint title: `docs: complete M0 and prepare modern scaffold branch`, published on `main` and the work branch. Use Git history/live refs and the session result for the final hash. The work branch is prepared; the modern scaffold is not implemented yet.
- The [preflight record](preflight-checks.md) confirms live GitHub access and admin permission. No push was attempted during that check.
- Local Java 8 and Java 25 JDKs are installed. Global settings are unchanged. Run `& .\scripts\use-modern-java.ps1` in each modern shell to align JAVA_HOME/PATH to the verified JDK saved in ignored `.local/modern-jdk.json`. It does not run Gradle. Use the unchanged Java 8 helper for the current legacy project.
- Future modern commands are guarded against the legacy checkout. Use the wrapper, Java 25 in both terminal and IDE, and a separate `.gradle-home/modern` cache. Diagnose environment failures before considering a fallback; require a minimal reproduction and maintainer approval before changing target.
- Local inventory at preparation: no untracked non-ignored files; `.local/modern-jdk.json` is the only ignored file. Its saved JDK metadata is Temurin 25.0.4+7 and required files exist. No runtime worlds, outputs/caches, or external jars are present in this checkout. Retain the local selection and reconsider new files before M1 replacement.
- [Legacy inventory](legacy-inventory.md) records all 199 tracked files at `6df4c3f`, including 73 Java files, 85 resources, seven build/wrapper files, both scripts/workflows, and retain/archive documentation/control groups. Its manifest includes exact paths/blob IDs/modes; all legacy implementation remains identical to `1.12`. Inventory checkpoint title: `docs: inventory legacy files for the modern scaffold`, published only on the work branch. `main` remains at the M0 handoff.
- Modern build/client/server tests have not been run. Legacy QA results do not prove the modern port works.

## Verified Release Snapshot

See [Release Verification](release-verification.md) for official sources, immutable MDK commits, and the complete candidate table checked on 2026-10-07.

- Minecraft latest release: 26.3. Latest snapshot: 26.4-snapshot-3, excluded from the scaffold candidates.
- Latest 26.3 NeoForge: 26.3.0.57-beta; inspected official MDK default: 26.3.0.52-beta. Both are published, but neither was build-tested here.
- Latest 26.2 NeoForge: 26.2.0.88, also the inspected MDK default, without a beta suffix.
- Inspected ModDevGradle templates use Gradle 9.2.1 and plugin 2.0.148. Their 26.x targets require Java 25; inspected 1.21.x alternatives require Java 21.
- This snapshot supplied the selected pins in [Selected Modern Toolchain](toolchain.md). Root legacy build files still retain their original versions until M1.

## Verified Ecosystem Snapshot

See [Ecosystem Compatibility](ecosystem-compatibility.md) for exact release links, filenames, fallback versions, manifest ranges, and testing limits.

- NeoForge 26.3: GeckoLib 5.5.7, AzureLib 4.0.8, SmartBrainLib 2.0.3, Undead Nights 2.2.6, and Contagion 2.1.1 have matching releases.
- NeoForge 26.2: GeckoLib 5.5.6, AzureLib 4.0.0, SmartBrainLib 2.0.3, Undead Nights 2.2.5, and Contagion 2.1.1 have matching releases.
- Initial scaffold remains NeoForge-only. Prototype vanilla goals first; consider SmartBrainLib in M6 and one animation library in M8. Libraries become required only if the later implementation actually uses them.
- Use Undead Nights as the first optional external threat test, Contagion for a separate infection/coexistence test. Do not promise SRP-style parasite evolution or infected-player targeting.
- Spore 2.2.0j is verified for NeoForge 1.21.1, not the checked newer candidates. The Hordes' 1.21.1 release has a mismatched Modrinth Atlas Lib dependency link; follow the inspected manifest and perform a separate runtime check before using it.
- Eight small jar downloads were hash-checked and inspected in memory for metadata only. No external assets, binaries, or source were added to the repository; no game or jar was executed.

## Design Identity

Stand and Hold should not be "another infection mod." Its identity is the human response layer:

- human escalation against spreading threats
- bases, outposts, supply lines, command posts, labs, and research
- soldiers and specialists that respond to threat growth
- optional compatibility with multiple parasite, fungus, sculk, alien, zombie, or end-of-world threat mods
- modpack-friendly knobs for pack makers

## Hard Rules

- Do not copy assets, models, textures, code, or internal systems from other mods.
- Use other mods as market and design reference only.
- Keep compatibility optional through tags, registry IDs, datapacks, or integration modules.
- Keep the first modern build compileable before adding advanced systems.
- No legacy world/config/NBT conversion in the foundation. Use separate modern test worlds; back up ignored worlds/local settings outside the checkout before replacement. A preserved Git branch is not a backup of those files.
- Dependency additions must record exact artifacts, licensing/provenance, sides/transitives, purpose, and verification. M1 exceptions or a target/scope change require maintainer approval; threat mods must never become core requirements.
- Build in phases and verify each phase.

## M0 Completion And M1 Entry

- Version pins, process-local JDK selection, Windows commands, IDE instructions, and fallback procedure are complete. Executing the modern build/IDE/runtime checks requires M1's scaffold, not more version-selection work.
- The foundation/dependency/save policy is final for M1, with no additional runtime libraries. Step 0.5.1 found no decision corrections and confirmed all decision checkpoints are published.
- Step 0.5.2 prepared `codex/modern-neoforge-scaffold` from the published baseline and records the reused checkout/local files. M0 preparation is complete. M1 / 1.1.1 now supplies the exact tracked-file inventory; 1.1.2 handles current documentation/identity before 1.1.3 replaces implementation inputs.

## Deferred Decisions

- M6 starts with vanilla AI; evaluate SmartBrainLib only for a concrete need. M8 evaluates one animation library, with GeckoLib the prototype preference and AzureLib the alternative. These choices do not block M1.
- Later helper integrations require fresh target/license checks. Optional threat tests follow a working no-external-mod baseline; published matching artifacts do not prove runtime compatibility. Legacy SRP availability is not modern compatibility.

## Notes From The 1.12 Alpha

- World progression, research, mission, and threat systems should be rebuilt first because they define the mod loop.
- Entity and structure work should stay bounded until persistence and commands are stable.
- Compatibility mappings should remain config/tag-driven.
- Art matters earlier than expected. The Phase 35 texture pass proved that visual identity affects perceived quality even in alpha.
- Mission guidance made the first playable loop easier to test; modern Stand and Hold should keep that lesson.
