# Modern Port Preflight Checks

Initial check: 2026-10-07. The tables preserve that preflight snapshot; follow-ups are recorded below. Repeat remote and working-tree checks before scaffolding and use Git history for subsequent publication.

## Repository And GitHub

| Check | Result | Evidence |
| --- | --- | --- |
| Current branch and tracking | PASS | `main` tracks `origin/main` |
| Fetch and push remote | PASS | Both use `https://github.com/liamryan391/Stand_and_Hold.git` |
| Live remote access | PASS | `git ls-remote --heads origin main 1.12` returned both branches |
| Preserved legacy branch | PASS | Local and remote `1.12` point to `62bb07b84eb614b5967ef318ccb98aef1f22a5d8` |
| Main synchronization | PASS | Local HEAD and live remote `main` point to that same commit |
| GitHub authentication | PASS | `gh auth status --hostname github.com` authenticated `liamryan391` |
| Repository permission | PASS | `gh repo view ... --json nameWithOwner,url,defaultBranchRef,viewerPermission,isArchived` returned ADMIN, default `main`, not archived |
| Local repository integrity | PASS | `git fsck --no-dangling` completed successfully |
| Local planning work | PENDING COMMIT | Root README modification and new `docs/modern-port/` files are local/uncommitted |
| Push execution | NOT ATTEMPTED | Access/permission were verified; this check did not publish or alter remote branches |

The sandbox initially blocked network checks. Retrying the read-only checks with approved network access succeeded; the initial sandbox authentication failure was not evidence of invalid account credentials.

## Local Toolchain

| Check | Result |
| --- | --- |
| `java -version` on PATH | Temurin 25.0.4 |
| `javac -version` on PATH | 25.0.4 |
| Process/machine JAVA_HOME | Temurin Java 8 installation |
| Installed JDK directories | Temurin 8.0.492.9 and 25.0.4.7 |
| Direct Java 8 executable check | `java -version` from the Java 8 installation returned `1.8.0_492` |
| Existing project | Minecraft 1.12.2, Forge 14.23.5.2847, Java 8, Gradle 4.10.3 |
| Mod version | `0.1.0-alpha.1`, unchanged |
| Existing helper and CI | Still target Java 8 for the current legacy project |
| Modern toolchain | Not yet pinned; do not infer a version choice just from installed JDKs |

Before a modern build, select a compatible JDK for both Gradle and the IDE and inspect wrapper `--version` output. Keep the preserved legacy project on Java 8. No global Java settings were changed by this pass.

## Documentation And Verification Scope

- M0-M14 now have stable numbered tasks and deeper subtasks for large work.
- At the initial preflight, only preservation/access/planning tasks were marked complete. Follow-up release and ecosystem checks complete 0.3.1 and 0.3.2; feature phases remain outstanding.
- Minecraft 26.3 remains a candidate pending exact pinning and build/runtime proof; matching external artifacts are confirmed in the follow-up record.
- The technical API map now refers to `ModConfigSpec`, following [NeoForge's configuration docs](https://docs.neoforged.net/docs/misc/config/).
- An EULA stop is recorded as incomplete server launch testing; readiness and clean shutdown are separate required evidence.
- Gameplay source, Gradle files, and the existing CI workflow were not changed. No build/client/server test was run for this documentation pass.
- Documentation validation passed: all 15 phases (M0-M14), 129 unique task IDs, matching parent/child completion states, correct phase prefixes, and 37 relative links across the root README and planning folder.
- `git diff --check` and a separate whitespace check of the new planning files passed. Git reported only the existing README LF-to-CRLF checkout warning.

## Follow-Up: Step 0.3.1

Official release and MDK availability checks passed on 2026-10-07. [Release Verification](release-verification.md) records the evidence: Minecraft 26.3 is released with beta NeoForge builds; 26.2 is the closest non-beta-loader alternative. Five official ModDevGradle templates, Java requirements, published artifact POMs, and the Gradle checksum endpoint were inspected. Nothing was installed or build-tested.

## Remaining Gates

1. Phase M1, step 1.1.3: replace inventoried implementation/build inputs, preserving modern notes, identity, and local files. M0, inventory, and documentation transition are complete.
2. Remaining M1 tasks: configure the pinned build, verify the modern client/server/artifact, then merge the verified scaffold into `main`.

## Follow-Up: Step 0.3.2

[Ecosystem Compatibility](ecosystem-compatibility.md) verifies exact published NeoForge library and threat-mod releases for 26.3 and 26.2, plus fallback options. Eight small jars were inspected in memory and their SHA-512 hashes matched release records; no jar was executed or installed. Spore's large jar was not downloaded. Combined client/server runtime checks remain outstanding.

GitHub access was rechecked before this step's publication: `main` and `1.12` still matched the original baseline, authentication worked, and the account retained ADMIN repository permission. The user requested the accumulated planning/research changes be committed and pushed at the end; consult Git history and the final session result for publication evidence.

## Follow-Up: Step 0.3.3

[Selected Modern Toolchain](toolchain.md) records Minecraft 26.3, NeoForge 26.3.0.57-beta, Java 25 (local Temurin 25.0.4+7), Gradle 9.2.1, ModDevGradle 2.0.148, and resolver plugin 1.0.0. Official release/template metadata was rechecked. The Java selector aligns JAVA_HOME and PATH for a modern shell using an ignored local path; global settings and the legacy Java 8 helper are unchanged. Script verification is not Gradle/Minecraft runtime proof.

## Follow-Up: Step 0.3.4

On 2026-10-13, added [Windows Build And IDE Setup](building.md) with current/future command separation, explicit JDK/IDE selection, daemon-override troubleshooting, and a maintainer-approved fallback procedure. The preparation guard rejects the current legacy checkout before changing Java or invoking Gradle. Rechecked the saved Java 25 selector and official IDE/Gradle/MDK instructions. No modern Gradle, IDE sync, or game launch was run, and no build or gameplay files were changed.

GitHub fetch/ref checks before publication showed `main` at `94efe4d2ecd5e01dbfc149ee6d82af7f168c50e4`, matching the local baseline; `1.12` remained at `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.

## Follow-Up: Step 0.4

On 2026-10-14, finalized [Foundation Scope And Dependency Policy](foundation-scope.md). M1 includes no gameplay or added runtime libraries; platform/build dependencies remain allowed. Later dependency additions require explicit purpose, exact artifacts, provenance/licence and side/transitive checks, and verification. Threat mods remain optional. Legacy world/config conversion is excluded, and ignored local worlds/settings need backups separate from Git preservation.

GitHub fetch/ref checks showed clean `main` at `af988c62e4f47527db808879b516b91ea1d2861b`, with no remote divergence; `1.12` remained at the preserved commit. No dependency, build file, source, CI, or global JDK setting changed, and no modern build/game was run.

## Follow-Up: Step 0.5.1

On 2026-10-14, [reviewed consolidated M0 decisions](m0-decision-review.md): pins, setup/fallback, foundation scope, dependencies, save boundaries, and implementation route agree. No decision correction was required. GitHub fetch and live refs confirmed local/remote main at `da8b1d05e36301834f9f44445908ba66c622f3d1`; all four decision checkpoints passed ancestry checks against `origin/main`. Local/remote `1.12` and the legacy alpha tag also matched. Legacy source/build/CI has no diff from the preserved branch.

This review records publication evidence and advances the handoff. Modern build/IDE/client/server proof remains M1 work, and branch preparation remains 0.5.2 work.

## Follow-Up: Step 0.5.2

On 2026-10-14, refreshed GitHub refs and confirmed a clean published `main` baseline at `dbf0de8ec9e94fd6fab96e5bd80d77473f3418cd`. The requested work branch was absent locally/remotely and was created from that baseline. [Scaffold Branch Handoff](scaffold-handoff.md) records the existing checkout path, completion-checkpoint publication on both branches, and the final work-branch upstream.

Local inventory found no untracked non-ignored files and only ignored `.local/modern-jdk.json`. The saved JDK metadata reports 64-bit Temurin 25.0.4+7, and its Java/compiler/release files exist. No runtime worlds, build outputs/caches, external jars, or IDE output are present in this checkout. No local file was moved/deleted; verify the config hash after switching the branch.

Preserved `1.12` and alpha-tag refs match GitHub. No legacy source/build/CI or Java setting changed during branch preparation, and no Gradle/game launch was run. M0 closes with repository preparation; modern implementation and runtime verification begin in M1.

## Follow-Up: Step 1.1.1

On 2026-10-14, refreshed GitHub refs at work-branch baseline `6df4c3fea5a6fa31f22589339a4c978b4e6e6d89`. Local/remote `1.12` and alpha-tag refs still match preservation, and the legacy implementation/build/CI has no diff from `1.12`. The [transition inventory](legacy-inventory.md) and exact manifest cover all 199 tracked files: 73 Java, 85 resources, seven build/wrapper files, scripts/workflows, and documentation/control groups.

The only ignored local file remains the saved JDK selection; its hash is preserved. No runtime/build/CI file, version, or Java setting changed. This inventory checkpoint is published only on the work branch, leaving `main` at the M0 handoff. No build or game launch was run.

## Follow-Up: Step 1.1.2

On 2026-10-14, rewrote the root README for the modern port, explicitly distinguishing the current legacy source/build from the planned target. Project name, mod ID/namespace, package/group, author, and version values are preserved. All ten legacy guides plus the legacy README/known-issues/changelog references now route readers to verified paths on `1.12`; the modern build guide's legacy link follows the same route.

Current root known issues now describe modern preparation gaps, while the changelog retains the original alpha body under a Forge 1.12.2 label and adds an unnumbered unreleased preparation entry. All modern notes and the immutable inventory manifest remain present; legacy guide files and runtime/build/CI/scripts are unmodified. No build/launch was run. The documentation checkpoint is published only on the work branch.

**Next phase and step: M1 / 1.1.3.**
