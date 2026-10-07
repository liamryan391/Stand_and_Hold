# Modern Port Progress Log

## 2026-10-07

- Confirmed `1.12` branch exists for the Forge 1.12.2 alpha.
- Pulled `main`; it was already up to date.
- Added modern-port planning folder.
- Chose NeoForge as the primary modern loader.
- Recorded target strategy: attempt latest viable NeoForge track, currently 26.3, with a fallback to the newest stable NeoForge-supported version if the toolchain or ecosystem is not ready.
- Recorded that the next implementation step should be a clean NeoForge scaffold, not a direct 1.12 file port.

## Next Checklist

Task completion is maintained in the [canonical roadmap](phase-roadmap.md). Keep this section as the current handoff rather than a second set of checkboxes.

- Current phase: M0 - Preserve And Decide.
- Completed steps: 0.1 (preservation/access checks), 0.2 (planning and numbered task tracking), 0.3.1 (release/MDK availability), 0.3.2 (published ecosystem matches), 0.3.3 (exact pins and verified local JDK selection).
- **Next: 0.3.4 - document Windows build commands, IDE Gradle JVM setup, and the fallback decision rule.**
- After that: 0.4 confirms scope; 0.5 publishes the final M0 decisions and prepares the scaffold branch.
- First implementation phase: M1, starting at 1.1.1 after M0's remaining gates pass.

The research checkpoint `bf44b46` (`docs: record modern port roadmap and ecosystem compatibility`) was committed and pushed. Step 0.3.3 uses checkpoint title `build: pin modern toolchain and configure local Java selection`; use Git history and `origin/main` for publication confirmation, with the actual commit ID in the final session report. Earlier "local/uncommitted" entries below are historical. Final setup/scope decisions in 0.3.4-0.5 still precede scaffolding.

## 2026-10-07 - Numbered Roadmap And Preflight

- Expanded M0-M14 into numbered steps such as 1.1 and 1.2, with subtasks such as 1.1.1 for larger work. Added per-phase verification, dependency details, and progress rules.
- Verified live GitHub refs: remote `main` and `1.12` both match local `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- Verified GitHub CLI authentication as `liamryan391` and ADMIN repository permission. No commits, merges, pushes, or branch changes were performed in this preflight pass.
- Recorded the installed JDK mismatch: PATH uses Java 25; JAVA_HOME uses Java 8. Select a process/IDE JDK explicitly in step 0.3.3.
- Clarified that 26.3 is still a candidate, not a verified target. Exact official versions and available compatible mods remain to be checked before implementation.
- Corrected the NeoForge config API reference to ModConfigSpec and separated an EULA stop from dedicated-server readiness.
- Added [Preflight Checks](preflight-checks.md) with evidence and remaining checks.
- Verification passed: `git fsck --no-dangling`, `git diff --check`, and documentation checks covering 15 phases, 129 unique task IDs, parent/child status consistency, phase prefixes, 37 relative links, and whitespace in new files. Direct Java 8 executable check returned `1.8.0_492`.
- No build or Minecraft launch was run for this documentation-only update.
- Planning changes are local and uncommitted/unpushed. Source code, build configuration, mod version, and preserved branches are unchanged.

## 2026-10-07 - Step 0.3.1 Complete

- Read Mojang's live Java release manifest and per-version Java metadata, NeoForge's Maven XML/POMs, and official MDK repository contents at recorded commits.
- Confirmed latest Minecraft release 26.3 and latest snapshot 26.4-snapshot-3; excluded snapshots from the scaffold shortlist.
- Confirmed latest NeoForge 26.3.0.57-beta; no non-beta 26.3 build was listed. The inspected MDK defaults to 26.3.0.52-beta, so later pinning must resolve that difference explicitly.
- Confirmed non-beta alternatives: Minecraft 26.2 / NeoForge 26.2.0.88; 26.1.2 / 26.1.2.114; 1.21.11 / 21.11.45; 1.21.1 / 21.1.256.
- Verified both official MDK variants exist for each candidate; inspected the ModDevGradle templates, including wrapper files and mod metadata. All five inspected templates use Gradle 9.2.1 and ModDevGradle 2.0.148; 26.x uses Java 25 and the inspected 1.21.x alternatives use Java 21.
- Retrieved six NeoForge POMs and the plugin marker; artifact IDs/versions matched. Retrieved and validated the official Gradle 9.2.1 SHA-256 text. No toolchain archives were installed or executed.
- Added [Release Verification](release-verification.md) with official sources, immutable MDK snapshots, caveats, and the shortlist. Updated the roadmap, memory, technical plan, recommendations, research, index, and preflight handoff to agree.
- Recommendation for the next check: compare 26.3 and 26.2 first, keeping documented older alternatives if actual mod/library support requires them. No final toolchain was selected.
- Documentation checks passed: 15 phases, 129 unique tasks, correct completion/parent states, 47 relative links, six consistent next-step locations, and whitespace validation. `git diff --check` passed; source/build/configuration diff was empty and both local branch refs remained at the preserved commit.
- No build or client/server launch was run for this research/documentation step. Legacy source/build files and branches are unchanged; planning work remains local and uncommitted/unpushed.
- **Next phase/step: M0 / 0.3.2.** Step 0.3 remains open until ecosystem checks, version pinning, and setup notes are complete.

## 2026-10-07 - Step 0.3.2 Complete

- Rechecked live GitHub refs and account access before work: `main` and `1.12` matched `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`; authenticated as `liamryan391` with ADMIN access. Planning changes had not previously been committed or pushed.
- Verified individual Modrinth version records against BOTH Minecraft version and NeoForge. GeckoLib, AzureLib, and SmartBrainLib have published matching releases for 26.3 and 26.2.
- Found and verified Undead Nights and Contagion as external threat/coexistence candidates for both primary versions. Also recorded matching fallback artifacts for 26.1.2, 1.21.11, and 1.21.1.
- Confirmed Spore 2.2.0j for NeoForge 1.21.1. No exact match for newer candidates was found in its checked release records. Sculk Horde, From Another World, SRP, and The Flesh That Hates do not supply verified matches for the primary versions in the sources checked.
- Hash-verified eight small jars and inspected their manifests in memory, without extraction or execution. Confirmed relevant declared loader ranges; recorded The Hordes' incorrect Modrinth dependency link to Forge 1.12.2 Atlas Lib and its different actual jar requirement.
- Spore's roughly 119 MB jar exceeded the bounded metadata check and was not downloaded. Runtime compatibility, actual entity IDs, and combined raid/performance behavior remain untested.
- Added [Ecosystem Compatibility](ecosystem-compatibility.md). Updated memory, roadmap, recommendations, technical plan, index, and prior handoffs to agree. Recommend 26.3 with a 26.2 non-beta-loader fallback; use 1.21.1 if immediate Spore support becomes the deciding requirement. No exact project toolchain is pinned yet.
- Keep the scaffold NeoForge-only. Vanilla goals first, animation/AI libraries in their relevant later phases, and external threat mods only in optional test instances/adapters.
- Validation passed: 15 phases, 129 unique task IDs, correct parent/child completion states, 59 relative links across 11 documents, seven consistent next-step locations, and whitespace checks. `git diff --check` passed and the diff for source/build/configuration files was empty.
- No build or Minecraft launch was run for this research/documentation update. Legacy code/build configuration is unchanged. The user requested a final commit and push for the accumulated planning work after checks, under the checkpoint title above; `1.12` is excluded from publication changes.
- **Next phase/step: M0 / 0.3.3.** The final M0 decision commit in 0.5.1 remains open because versions/setup have not yet been selected.

## 2026-10-08 - Step 0.3.3 Complete

- Work began on 2026-10-07 and completed after local midnight. Rechecked Mojang's release/per-version Java metadata, the immutable official 26.3 MDK, published NeoForge/plugin POMs, Gradle checksum, and official Java compatibility matrix.
- Selected Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / ModDevGradle 2.0.148. Retain the MDK's Foojay resolver 1.0.0. The local JDK baseline is existing 64-bit Temurin 25.0.4+7; no JDK install was needed.
- Recorded beta-loader risk and deliberately chose the newer published loader over the template default 26.3.0.52-beta. Added [Selected Modern Toolchain](toolchain.md) with pins, rationale, immutable source, checksum, setup, and test boundaries.
- Added `scripts/use-modern-java.ps1`: validates a Java 25 JDK, optionally remembers its path in ignored `.local/modern-jdk.json`, and aligns JAVA_HOME/PATH in the calling PowerShell process. Global Java settings, the Java 8 helper, and the legacy Gradle/source/CI files remain unchanged.
- Verified successful explicit selection/save/reload, java/javac resolution, repeat-call idempotence, and rejection of Java 8, missing directories, and empty values without modifying the saved path or environment. Confirmed local path storage is ignored by Git and user/machine JAVA_HOME values remain unchanged.
- Windows PowerShell 5.1 parsing and saved selection in a fresh process passed. Documentation checks passed: 12 documents, 75 relative links, 15 phases, 132 unique tasks, consistent parent/child completion, and eight current handoffs. `git diff --check` passed; source/build/CI and the Java 8 helper have no diff.
- Fetched GitHub before publication: HEAD and origin/main matched `bf44b469c2d677da0b5beec3228fd0ea22249b01`; remote `1.12` remained at the preserved commit.
- Expanded step 0.3.3 into numbered verification/setup substeps and updated current handoffs to 0.3.4. No third-party library, mod, gameplay code, or modern scaffold was added.
- Modern Gradle/build/client/server checks were not run: the root wrapper is still the Java 8 legacy build. M1 must verify wrapper JVM, dependency resolution, build, client, and dedicated server; script checks do not substitute for those gates.
- The user-requested final commit/push is performed only after validation, with the checkpoint title above; preserve `1.12` at `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- **Next phase/step: M0 / 0.3.4.** Parent step 0.3 and the final M0 handoff remain open.

## Session Handoff Format

For each future work session, append the date and phase/step IDs, then record:

- Completed work and files changed.
- Commands/tests actually run and their results, including skipped or blocked checks.
- New decisions, unresolved risks, and changes in scope.
- Commit IDs and push status, or explicitly state local/uncommitted.
- The next exact phase and step; update Project Memory and the roadmap checkboxes to match.
