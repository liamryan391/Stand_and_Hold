# Modern Port Progress Log

## 2026-10-07

- Confirmed `1.12` branch exists for the Forge 1.12.2 alpha.
- Pulled `main`; it was already up to date.
- Added modern-port planning folder.
- Chose NeoForge as the primary modern loader.
- Recorded target strategy: attempt latest viable NeoForge track, currently 26.3, with a fallback to the newest stable NeoForge-supported version if the toolchain or ecosystem is not ready.
- Recorded the clean NeoForge rewrite approach instead of a direct 1.12 file port.

## 2026-10-07 - Numbered Roadmap And Preflight

- Expanded M0-M14 into numbered steps such as 1.1 and 1.2, with subtasks such as 1.1.1 for larger work. Added per-phase verification, dependency details, and progress rules.
- Verified live GitHub refs: remote `main` and `1.12` both match local `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- Verified GitHub CLI authentication as `liamryan391` and ADMIN repository permission. No commits, merges, pushes, or branch changes were performed in this preflight pass.
- Recorded the installed JDK mismatch: PATH uses Java 25; JAVA_HOME uses Java 8. Select a process/IDE JDK explicitly in step 0.3.3.
- Clarified that 26.3 is still a candidate, not a verified target. Exact official versions and available compatible mods remain to be checked before implementation.
- Corrected the NeoForge config API reference to ModConfigSpec and separated an EULA stop from dedicated-server readiness.
- Added [Preflight Checks](archive/preflight-checks.md) with evidence and remaining checks.
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
- Documentation checks passed: 15 phases, 129 unique tasks, correct completion/parent states, 47 relative links, six consistent next-step locations, and whitespace validation. `git diff --check` passed; source/build/configuration diff was empty and both local branch refs remained at the preserved commit.
- No build or client/server launch was run for this research/documentation step. Legacy source/build files and branches are unchanged; planning work remains local and uncommitted/unpushed.

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

## 2026-10-13 - Step 0.3.4 Complete

- Began from clean `main` at `94efe4d2ecd5e01dbfc149ee6d82af7f168c50e4`; fetched GitHub and confirmed no divergence. Remote `1.12` remains at the preserved `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- Added [Windows Build And IDE Setup](building.md): safe current legacy command, guarded future modern preparation/build commands, expected JVM/artifact checks, IntelliJ setup, isolated modern cache/run-directory guidance, and M1 launch/evidence requirements.
- Checked official JetBrains/Gradle guidance and the pinned MDK's client/server configuration. Documented JVM property/daemon-criteria overrides and the distinction between terminal success and IDE Gradle verification.
- Defined fallback triage: fix environment problems first, reproduce in the minimal pinned MDK, defer failing optional integrations, and require maintainer approval before a coherent 26.2 fallback or other pin change. No versions or dependencies were changed.
- Expanded 0.3.4 into four numbered substeps. Current handoffs now point to 0.4; parent step 0.3 closes as a toolchain/setup decision, not modern build proof.
- Verification passed: all seven PowerShell examples parse under PowerShell 7.6.5 and Windows PowerShell 5.1; the preparation block rejects the legacy checkout without changing environment values or invoking Gradle; the saved Java 25 selector resolves successfully. User/machine JAVA_HOME remains unchanged.
- Documentation checks passed: 13 documents, 93 relative links, 15 phases, 136 unique task IDs, consistent parent/child completion, nine current next-step references, and `git diff --check`. Source, scripts, build files, CI, and ignore rules have no diff.
- Modern Gradle/build, artifact inspection, IDE sync/build, client, and dedicated-server checks were not run. The current wrapper still belongs to Forge 1.12.2; executing the modern commands is an M1 task. No gameplay, script, build, CI, or global JDK settings were changed.
- Final commit/push follows validation under the checkpoint title above; the preserved branch is excluded from publication changes.

## 2026-10-14 - Step 0.4 Complete

- Started from clean `main` at `af988c62e4f47527db808879b516b91ea1d2861b`. Fetched GitHub: no divergence, with remote `1.12` still at `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- Added [Foundation Scope And Dependency Policy](foundation-scope.md). Final M1 scope: pinned MDK build, identity/metadata, entry point/logger/config shell, side safety, build/CI support, and actual build/launch verification. No added runtime library dependencies or exceptions are approved.
- Distinguished required platform/build-tool dependencies from later AI/animation libraries, optional integrations, and development-only additions. Defined exact-artifact, licence/provenance, physical-side/transitive, test, and maintenance records before admission; scope exceptions require maintainer approval.
- Assigned gameplay/art/vehicles/multi-loader work to existing phases rather than adding it to the scaffold. The policy uses existing dated ecosystem research; it does not refresh release claims, select new libraries, or change toolchain pins.
- Finalized separate-world testing and excluded legacy world/config/NBT conversion. Recorded that the preserved Git branch does not back up ignored local worlds/configs; future migrations concern modern schemas only.
- Expanded 0.4 into numbered substeps, including 0.4.3.1-0.4.3.3. Updated the technical plan, project memory, indexes, and current handoffs. Clarified 0.5.1 as the consolidated decision/publication review, reusing already-published commits when complete.
- Validation passed: 14 documents, 112 local links including heading anchors, 15 phases, 143 unique task IDs, parent/child completion consistency, ten current handoffs, and `git diff --check`. The diff is documentation-only; source, scripts, toolchain/build, CI, and ignore rules are unchanged. Local branches remain only `main` and `1.12`.
- No modern scaffold, work branch, dependency installation, or implementation change was made. No build/client/server/IDE runtime test was run for this documentation-only policy step; those checks remain M1 work.
- Final commit/push follows validation under the checkpoint title above, with the legacy branch unchanged.

## 2026-10-14 - Step 0.5.1 Complete

- Reviewed the selected toolchain, Windows/IDE/fallback guide, foundation scope/dependency/save policy, technical plan, and roadmap. Decisions agree and require no correction. Added [M0 Decision Review](archive/m0-decision-review.md) as the evidence record.
- Began from clean `main` at `da8b1d05e36301834f9f44445908ba66c622f3d1`. Fetched GitHub; HEAD and `origin/main` matched. Live refs confirmed the same remote head.
- Verified `bf44b469c2d677da0b5beec3228fd0ea22249b01`, `94efe4d2ecd5e01dbfc149ee6d82af7f168c50e4`, `af988c62e4f47527db808879b516b91ea1d2861b`, and `da8b1d05e36301834f9f44445908ba66c622f3d1` with `git merge-base --is-ancestor <commit> origin/main`; all passed. Existing commits satisfy decision publication.
- Local/remote `1.12` remain at `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`. The local/remote alpha tag refs also match. Legacy source, build files/wrapper, CI, and Java 8 helper have no diff from the preserved branch.
- Added 0.5.1.1-0.5.1.3 review/publication/evidence subtasks and updated current handoffs to 0.5.2. Parent 0.5 remains open until branch preparation completes.
- Documentation checks passed: 15 documents, 128 local links including heading anchors, 15 phases, 146 unique task IDs, parent/child consistency, eleven current handoffs, and `git diff --check`. This session's diff contains only modern-port documentation.
- No modern build/IDE/client/server test was run for this documentation review. No target/dependency change, work branch, or modern scaffold was created.
- Final commit/push records this review evidence and updated progress under the checkpoint title above; the decisions themselves were already published.

## 2026-10-14 - Step 0.5.2 Complete

- Began from clean `main` at `dbf0de8ec9e94fd6fab96e5bd80d77473f3418cd`; fetched GitHub and confirmed the matching remote main. Local/remote preserved `1.12` and alpha-tag refs match their recorded hashes.
- Confirmed `codex/modern-neoforge-scaffold` was absent locally/remotely, then created it locally from the published baseline with no inherited upstream. Reused `C:\Users\liamr\Documents\ChatGPT\Stand and Hold`; no clone/worktree was added.
- Inventoried local files: no untracked non-ignored files; `.local/modern-jdk.json` is the only ignored file. Saved JDK metadata and required files match Temurin 25.0.4+7, 64-bit. Runtime/world, build/cache, external-jar, and IDE-output directories are absent in this checkout. Retain the ignored selection in place.
- Added [Scaffold Branch Handoff](archive/scaffold-handoff.md), expanded 0.5.2 into preparation/publication subtasks, and updated current handoffs to M1 / 1.1.1. Legacy implementation remains untouched; modern scaffold/source replacement has not started.
- Validation passed: 16 documents, 142 local links including heading anchors, 15 phases, 150 unique task IDs, completion consistency, `git diff --check`, prepared branch baseline, and unchanged legacy implementation/local JDK configuration. All M0 tasks close with this handoff; M1 implementation tasks remain open.
- Final publication commits the M0 completion record on `main`, advances the local work branch to that record using `git merge --ff-only main`, switches this checkout to it, and pushes with its own upstream. Verify both remote heads, preserved refs, config hash, and clean status before reporting completion.
- No Gradle build/client/server/IDE launch was run during this repository/documentation preparation. Modern build/runtime proof remains M1 work.

## 2026-10-14 - Step 1.1.1 Complete

- Started from clean `codex/modern-neoforge-scaffold` at `6df4c3fea5a6fa31f22589339a4c978b4e6e6d89`, matching its GitHub upstream. Live main was also at that M0 handoff. Fetched refs and verified preserved `1.12` ancestry/hash and matching alpha-tag ref.
- Reviewed build/settings/properties/wrapper, entry/config/constants, resource metadata/formats, both scripts/workflows, and Java package/API coupling. Legacy source/resources/build/CI/Java 8 helper remain identical to `1.12`.
- Added [Legacy Transition Inventory](legacy-inventory.md) and a TSV snapshot of all 199 baseline paths with Git blob IDs/modes, proposed disposition, and transition owner. Totals: 73 Java, 85 resources, seven build/wrapper files, two scripts, two workflows, 15 modern notes, ten legacy guides, and five root docs/control files.
- Recorded exact preserve/replace/archive groups, original alpha-art references, legacy startup/API coupling, pack format/translation/recipe differences, duplicated identity/version fields, absent project licence/test source files, and the version-independent CI probe. No code, resource, CI, script, or version was modified.
- Local selection remains the only ignored file with no unrelated untracked content. No build or game/IDE launch was run; modern implementation and runtime verification remain later M1 tasks.
- Updated the roadmap with 1.1.1.1-1.1.1.4 and current handoffs to 1.1.2. Final commit/push publishes this inventory only on the work branch, leaving main/preserved refs and local settings intact.
- Validation passed: exact 199-file manifest coverage/path/blob/mode equality against the baseline tree; valid dispositions/transition owners; 17 documents, 155 local links including heading anchors, 15 phases, 154 unique task IDs, parent/child consistency, thirteen current handoffs, and whitespace checks. Runtime/build/root documentation and the ignored JDK selection have no changes.

## 2026-10-14 - Step 1.1.2 Complete

- Began from clean `codex/modern-neoforge-scaffold` at `4cac6b9900b67e71273ba0518c97ceb9177d22d5`, matching its GitHub upstream. Fetched refs; preserved branch/tag and main remain at their recorded hashes. Verified all 13 legacy documentation targets exist on `1.12`.
- Replaced the long legacy root README with modern-port status, retained project identity, selected target pins, development/scope links, and a complete legacy-reference table pointing to `1.12`. The README states that source/build/CI are still legacy until the upcoming file transition and announces no modern release.
- Rewrote current root known issues around actual preparation/verification gaps with links to archived legacy issues. Added an unnumbered unreleased documentation entry and labelled the unchanged original alpha changelog body as Forge 1.12.2 history.
- Preserved every modern planning document, the exact baseline manifest, and project values: Stand and Hold / `standandhold` / `com.liamryan.standandhold` / Liam Ryan. Source version remains `0.1.0-alpha.1`; no target/build/script/CI/resource or global Java change was made. Legacy guide files remain unchanged during staging; README readers use their branch copies.
- Added 1.1.2.1-1.1.2.4 and updated current handoffs to 1.1.3. Parent 1.1 stays open until the implementation file transition. The final commit/push publishes only documentation on the work branch; main/preserved refs and ignored local selection remain unchanged.
- Validation passed: 19 documents, 142 local links including six heading anchors, all 13 preserved legacy targets, 15 phases, 158 unique task IDs with consistent parent/child states, and 14 current handoffs. All 17 planning files remain present; the original changelog body, ten legacy guides, runtime/build/scripts/CI/control files, manifest, identity/version values, and ignored JDK selection are preserved. Whitespace checks passed.
- No Gradle build, client, server, or IDE launch was run for this documentation step. Modern build/runtime proof remains M1 / 1.4 work.

## Session Logging

Follow [AGENTS.md](../../AGENTS.md) for session workflow and the eight-line log limit. Live state belongs in [STATUS.md](STATUS.md).

## 2026-10-14 - Agent Workflow Cleanup

- Added the supplied root AGENTS.md and an updated STATUS.md; scaffold work remains on hold.
- Centralized live state, removed repeated handoffs, archived four historical notes, and carried durable rules into the technical plan.
- Updated archive links and replaced the old session-handoff instructions with the AGENTS.md pointer.
- Verification: `git diff --check` passed; reviewed the documentation-only diff.
- Not run: Gradle build, client, or server; no code, toolchain, or gameplay changes.
- Blockers: none for this cleanup; resume is controlled by STATUS.md and Liam's request.

## 2026-10-14 - M1 Scaffold And Launch Verification

- Replaced verified legacy runtime/resources/guides/helper with the pinned official NeoForge MDK scaffold; retained template notices, planning/archive notes, identity, local files, and version 0.1.0-alpha.1.
- Added the minimal side-safe entry point/logger/config, expanded metadata, isolated run directories, Java 25 CI, and updated build guidance; no gameplay or extra runtime libraries.
- Verified wrapper 9.2.1 / Java 25.0.4+7; `clean build --no-daemon --stacktrace` passed (8m 43s, then 34s after stdin fix); inspected jar metadata/content and Java 25 bytecode. No unit test sources exist.
- `runClient` initialized the mod/config; after reopening, Liam reported menu/Mods/fresh-world save/reopen passed. Logs show world entry/save/normal exit; the task finished BUILD SUCCESSFUL (3m 24s).
- `runServer --no-daemon --stacktrace --console=plain` reached Done on localhost, accepted stop after execution-time stdin forwarding, saved worlds, and exited BUILD SUCCESSFUL; NeoForge dev mode skipped EULA, with no agreement/file written.
- `git diff --check` passed; remote CI and IDE testing are unobserved. Live remaining verification and publication state belong in STATUS.md.

## 2026-10-14 - M1 Publication And M2 Progression

- Replaced AGENTS.md with the supplied batching policy and committed it separately as requested.
- Observed green Windows Java 25 build and jar upload for scaffold 836c311 (GitHub run 38632437020), then merged the scaffold into main without rewriting history.
- Added M2 stages/config, versioned global SavedData, guarded manager/commands, rules/NBT regression checks, and progression documentation without new runtime libraries or pin/version changes.
- `clean build --no-daemon --stacktrace` passed with 50 assertions; dedicated `runServer` runs reached Done, checked boundaries/invalid inputs/overflow/cross-dimension state, saved/reloaded, tested config correction, and stopped cleanly. Status-only saves did not rewrite the data file.
- Liam reported M2 integrated visual/command/save-reload checks passed; client exited BUILD SUCCESSFUL, then final clean passed (50 assertions) and jar metadata/test exclusion were checked. Private non-operator testing is unobserved.
- M2 CI run 38643554024 passed its Windows clean build, all 50 assertions, and jar upload; merged the verified phase into main with final docs-only evidence. Live milestone is in STATUS.md; legacy refs/local files remain untouched.

## 2026-10-14 - M3 Research, Supplies, And Missions

- Added bounded JSON definitions, atomic supplies/research, schema 2 migration, persistent missions/receipts, and commands without new runtime dependencies or pin/version changes.
- Final `clean build --no-daemon --stacktrace` passed (5s): 50 progression and 98 foundation assertions; jar metadata/resources/test exclusion were checked.
- Four dedicated `runServer` runs reached Done/stopped cleanly; partial/full missions, supplies, research, cross-dimension state, once-only rewards, and restart passed (chain 250/55; final QA overrides 252/54); immediate reward overflow cannot partially start a mission.
- Datapack override/reload, cyclic/malformed rejection without spending, safe restoration, and atomic immediate-start failure passed.
- Client loaded/entered/saved/exited normally (BUILD SUCCESSFUL, 7m 1s); Liam reported integrated checks passed. Private non-operator/packaged-installation tests remain unobserved.
- Windows CI run 38646896892 passed its clean build/148 assertions/jar upload; merged M3 into main with docs-only handoff. Live milestone is in STATUS.md; legacy refs/local files remain untouched.

## 2026-10-15 - M4 Core Buildings And Menus
- Added original blocks/resources, shared saved storage, schema 3 post registration/loaded-only cleanup, bounded generation/lab work, upgrades/transfers, and server-owned vanilla menus; pins/version/dependencies unchanged.
- Final `clean build --no-daemon --stacktrace` passed (6s), 219 assertions; jar metadata/resources/test exclusion inspected. Empty dimension parsing was fixed by regression coverage.
- Four dedicated runs reached Done/stopped cleanly; registration/mission hooks, generation, research once, partial/upgraded saves, normal removal/stale cleanup, unloaded preservation, and malformed/newer NBT passed.
- Resource override/reload/repeat and invalid-definition refusal passed; defaults restored with the valid QA pack disabled. Improved removed-target menu recovery/upgrade gates and bounded registry observation.
- Liam reported integrated blocks/menus/lab/transfers/upgrade/reload passed; client saved/exited BUILD SUCCESSFUL (9m 4s). Two-client/network-fuzz/removed-target visual retest/packaged install remain unobserved.
- Windows CI 38719872847 passed final runtime 29c6da5: clean build/219 assertions/jar upload; merged verified M4 into main with docs-only handoff. Live milestone is in STATUS.md; protected refs and user files were not changed by agent actions.

## 2026-10-20 - M5 Samples And Optional Threat Rewards
- Added original samples, bounded optional ID/tag mappings, server death/pickup hooks, real item transfers/recovery and carry-count mission observations; pins/version/runtime dependencies unchanged.
- Final `clean build --no-daemon --stacktrace` passed after runtime exit (4s), 260 assertions; jar metadata/resources/test exclusion inspected.
- Four dedicated runs reached Done/stopped cleanly; exact/tag/missing/invalid mappings, precedence, chance 0/1, 64-stack bound, mob_drops suppression, processed-victim/default attribution guards passed with documented QA overrides restored.
- Labs consumed exact local samples/supplies once, rejected insufficient samples, preserved partial/completed saves and blocked competing/repeated rewards. Normal breaking returned samples even during supply overflow; valid probe retained disabled, QA force ticket removed and empty-server pausing restored.
- Client resources/window loaded and closed normally (18m 40s); no world entry or Liam result. Actual player kills/pickups/menu/full-inventory/visual, external-mod/two-client/packet-fuzz/packaged tests remain unverified.
- Windows CI 39415621033 passed runtime 70211b8 (clean build/260 assertions/jar upload); merged implementation with docs-only evidence handoff. Live milestone is in STATUS.md.

## 2026-10-20 - M5 Client QA And Research Feedback
- Liam reported the integrated M5 checks passed, except missing automatic lab completion chat; added one translated server-wide notice after successful completion, with no costs/timing/schema changes.
- Final `clean build --no-daemon --stacktrace` passed after runtime exits (5s), 263 assertions; jar pins/version and completion translation inspected; `git diff --check` passed.
- Two interactive dedicated runs reached Done/accepted stop/exited cleanly: competing labs emitted once/spent once, repeat and restart emitted nothing, banks/rewards persisted. Initial closed-stdin run saved/exited via guarded ignored JDK helper; disposable world retained, QA ticket removed and prior world selection restored.
- Restarted pre-fix client entered/saved/reopened and fully exited normally (16m); new notice rendering/two-client delivery/external mods remain unverified. Live milestone is in STATUS.md.
- Windows CI 39429897484 passed runtime fc41e3e (263 assertions/normal jar upload); merged M5 follow-up with documentation, keeping M6 unstarted and protected refs unchanged.

## 2026-10-20 - M6 Human NPCs And Lab Feedback
- Added two configurable human tiers, original UV-tested uniforms/eggs, vanilla bounded AI, optional mapping reuse and schema 4 saved deployment quotas; no libraries/pin/version/legacy-ref changes.
- Added Liam's requested explicit lab Start/Pause, separate selection/clear, saved running state and progress bar; older modern labs keep work but load paused. Slow target acquisition remains documented polish.
- Final `clean build --no-daemon --stacktrace` passed after runtime exit (5s), 329 assertions; jar metadata/assets/test exclusion inspected; `git diff --check` passed.
- Six dedicated runs reached Done/accepted stop/exited cleanly; melee/stages/caps/reload/removal, optional mappings and paused/competing lab receipts passed. Night/fire isolation verified live attack disable/resume; a final own-column exclusion fixed isolated floating-post spawning (ground control 3, floating 0).
- Liam reported NPC and focused lab-control checks passed; both clients entered/reloaded/exited normally (7m 43s, 4m 8s). Two-client/external-mod/packaged tests remain unverified; QA settings/tickets restored and disposable world retained.
- Windows CI 39437110203 passed runtime 44ec4b3 (329 assertions/jar upload); merged M6 with docs-only handoff. Live milestone is in STATUS.md.

## 2026-10-20 - M7 Structure Foundations And Grass QA
- Added bounded original checkpoints/Main Bases, natural/debug placement, terrain/config checks, schema 5 registration/discovery, stage activation and loaded-only cleanup; no dependencies/pin/version/legacy-ref changes.
- Initial/grass-fix clean builds passed (939 assertions); one-time notice fix clean build passed (957). Normal jar metadata/resources/test exclusion inspected; post-client clean verification remains pending.
- Dedicated tests observed debug/natural generation, stage activation, persistence, unloaded retention, stale cleanup and safe rejection of occupied/wet/steep/unavailable/disallowed sites; interrupted server later stopped normally.
- Liam's first visual test failed on ordinary grass: 26.3 separates grass_blocks from dirt. Added both tags and rejection coordinates; corrected integrated retest passed. His repeated-chat feedback now has saved per-base once-per-state notices; focused check pending.
- Windows CI 39458559999 passed initial M7 runtime 2d23561; follow-up CI/merge pending. Five dedicated runs exited normally, natural base count stayed 19, changed crate stayed 1 after restart; defaults/tickets/world selection restored and disposable worlds retained. Live milestone is in STATUS.md.
