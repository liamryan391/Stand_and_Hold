# Project Memory

Last updated: 2026-10-07

## Preserved History

- Stand and Hold began as a Forge 1.12.2 Java 8 technical alpha.
- The 1.12.2 alpha is preserved on the `1.12` branch.
- The old alpha reached `0.1.0-alpha.1` and included progression, research, missions, buildings, NPC foundations, structures, threat response, supplies, optional SRP mappings, and alpha art.
- `main` still contains the 1.12.2 code until the modern scaffold phase intentionally replaces it.
- On 2026-10-07, live GitHub `main` and `1.12` both matched local commit `62bb07b84eb614b5967ef318ccb98aef1f22a5d8` (`Fix recursive mission completion crash`).

## Current Modern Direction

- Loader: NeoForge first.
- Minecraft target: 26.3 is confirmed released with official MDKs, but its NeoForge builds remain beta. Keep it as the latest-release candidate, not a pinned target.
- Closest non-beta-loader alternative: 26.2. Exact NeoForge releases of GeckoLib, AzureLib, SmartBrainLib, Undead Nights, and Contagion are verified for both 26.3 and 26.2. Runtime combinations remain untested.
- Recommend 26.3 for the next pinning step given the latest-release preference; choose 1.21.1 instead only if immediate Spore support becomes the deciding requirement. No target is pinned yet.
- Secondary loader later: Fabric, after NeoForge has a stable foundation and the shared design is proven.
- Prepare the scaffold on `codex/modern-neoforge-scaffold`, then merge verified work into `main`; leave `1.12` unchanged.

## Resume Point And Working Rules

- Current phase: M0 - Preserve And Decide. Steps 0.1, 0.2, 0.3.1, and 0.3.2 are complete; the parent step 0.3 remains open.
- Next step: **0.3.3 - select exact Minecraft/NeoForge/Java/Gradle/plugin versions and resolve local build JDK selection**.
- The [roadmap](phase-roadmap.md) owns task IDs and checkboxes; the [progress log](progress-log.md) records results and the current next step. Use `1.1`, `1.2`, and deeper IDs such as `1.1.1` for large tasks.
- Keep these planning files in the project repository. The research checkpoint commit is titled `docs: record modern port roadmap and ecosystem compatibility`; use Git history and remote refs to confirm publication rather than inferring it from a local file's existence.
- Current work is release/MDK/ecosystem research; no modern scaffold has been created.
- The [preflight record](preflight-checks.md) confirms live GitHub access and admin permission. No push was attempted during that check.
- Local Java 8 and Java 25 JDKs are installed. PATH selects Java 25 while JAVA_HOME selects Java 8. Choose the build JDK explicitly for each project; do not change the legacy build's requirement by accident.
- Modern build/client/server tests have not been run. Legacy QA results do not prove the modern port works.

## Verified Release Snapshot

See [Release Verification](release-verification.md) for official sources, immutable MDK commits, and the complete candidate table checked on 2026-10-07.

- Minecraft latest release: 26.3. Latest snapshot: 26.4-snapshot-3, excluded from the scaffold candidates.
- Latest 26.3 NeoForge: 26.3.0.57-beta; inspected official MDK default: 26.3.0.52-beta. Both are published, but neither was build-tested here.
- Latest 26.2 NeoForge: 26.2.0.88, also the inspected MDK default, without a beta suffix.
- Inspected ModDevGradle templates use Gradle 9.2.1 and plugin 2.0.148. Their 26.x targets require Java 25; inspected 1.21.x alternatives require Java 21.
- These are research findings, not project dependency pins. The published ecosystem fit is now checked in 0.3.2; choose exact versions in 0.3.3.

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
- Build in phases and verify each phase.

## Open Decisions Before Scaffolding

- Exact Minecraft and NeoForge build to pin.
- Java version required by the chosen Minecraft/NeoForge track.
- Exact Gradle and build-plugin versions supplied by the chosen MDK, plus IDE/build JDK configuration.
- Validate the NeoForge-only initial dependency policy; record an exception only if a dependency is essential.
- Whether to use AzureLib or GeckoLib for animated entities and equipment later.
- Whether to use an AI helper library later or start with vanilla Brain/Goal APIs.
- Which optional test combination to launch after scaffolding; published matching artifacts do not prove runtime compatibility. Legacy SRP availability is not modern compatibility.

## Notes From The 1.12 Alpha

- World progression, research, mission, and threat systems should be rebuilt first because they define the mod loop.
- Entity and structure work should stay bounded until persistence and commands are stable.
- Compatibility mappings should remain config/tag-driven.
- Art matters earlier than expected. The Phase 35 texture pass proved that visual identity affects perceived quality even in alpha.
- Mission guidance made the first playable loop easier to test; modern Stand and Hold should keep that lesson.
