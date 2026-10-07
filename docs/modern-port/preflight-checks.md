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

1. Step 0.3.4: document Windows build commands, IDE Gradle JVM setup, and the fallback procedure. Pins and local JDK selection are complete in 0.3.3.
2. Step 0.4: finalize initial dependencies and the separate-world testing policy.
3. Step 0.5: publish the final M0 decisions and prepare the scaffold branch from a rechecked baseline.
4. Phase M1: create and verify the modern scaffold before replacing `main` through a normal merge.

## Follow-Up: Step 0.3.2

[Ecosystem Compatibility](ecosystem-compatibility.md) verifies exact published NeoForge library and threat-mod releases for 26.3 and 26.2, plus fallback options. Eight small jars were inspected in memory and their SHA-512 hashes matched release records; no jar was executed or installed. Spore's large jar was not downloaded. Combined client/server runtime checks remain outstanding.

GitHub access was rechecked before this step's publication: `main` and `1.12` still matched the original baseline, authentication worked, and the account retained ADMIN repository permission. The user requested the accumulated planning/research changes be committed and pushed at the end; consult Git history and the final session result for publication evidence.

## Follow-Up: Step 0.3.3

[Selected Modern Toolchain](toolchain.md) records Minecraft 26.3, NeoForge 26.3.0.57-beta, Java 25 (local Temurin 25.0.4+7), Gradle 9.2.1, ModDevGradle 2.0.148, and resolver plugin 1.0.0. Official release/template metadata was rechecked. The Java selector aligns JAVA_HOME and PATH for a modern shell using an ignored local path; global settings and the legacy Java 8 helper are unchanged. Script verification is not Gradle/Minecraft runtime proof.

**Next phase and step: M0 / 0.3.4.**
