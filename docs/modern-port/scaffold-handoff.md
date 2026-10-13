# Scaffold Branch Handoff

Phase M0, step 0.5.2, 2026-10-14. M0 preparation is complete. The subsequent [M1 / 1.1.1 inventory](legacy-inventory.md) is also complete; the next task is 1.1.2.

## Branch And Checkout

| Item | Prepared state |
| --- | --- |
| Work branch | `codex/modern-neoforge-scaffold` |
| Initial published baseline | `dbf0de8ec9e94fd6fab96e5bd80d77473f3418cd` (`docs: record consolidated M0 decision review`) |
| Checkout on this computer | `C:\Users\liamr\Documents\ChatGPT\Stand and Hold` |
| Checkout strategy | Reuse the existing repository; no additional clone or worktree |
| Handoff checkpoint | `docs: complete M0 and prepare modern scaffold branch`, published on `main` and the work branch |
| Branch at session completion | Work branch checked out, tracking `origin/codex/modern-neoforge-scaffold`; both remote heads initially share the handoff checkpoint |

The branch was created locally from the verified published baseline. The completion record is committed on `main`; the work branch then advances to that same commit with a fast-forward-only merge and is pushed with its own upstream. No implementation commit is merged into `main` here. Use Git history/live refs and the final session report for the checkpoint hash and publication confirmation.

Subsequent M1 commits belong on the work branch. Merge the scaffold into `main` after the [roadmap's build/runtime verification](phase-roadmap.md) is recorded. The current source and wrapper still target Forge 1.12.2/Java 8 until the planned M1 replacement.

## Preservation Checks

- Before preparation, local HEAD, `main`, and `origin/main` matched the baseline above, with a clean tracked/untracked working tree.
- Live remote refs confirmed preserved `1.12` at `62bb07b84eb614b5967ef318ccb98aef1f22a5d8` and the alpha tag ref at `28fd40a81a50ea2f2382489e60d9015b4725cc92`. They remain excluded from branch preparation and publication.
- The requested work branch was absent locally and remotely before creation; no existing branch or user commit was overwritten.
- Git reported one checkout at the path above. No separate checkout is needed for this handoff.

## Local File Inventory

This inventory covers the repository checkout only, not worlds or installations elsewhere on the computer:

| Local content | Observed state and handling |
| --- | --- |
| Untracked files outside ignore rules | None at preparation start |
| Ignored files | Only `.local/modern-jdk.json`; retain it in place, excluded from commits |
| Saved modern JDK | Configuration parses and points to existing Java/compiler/release files; metadata reports 64-bit Temurin 25.0.4+7 |
| Worlds/runtime folders | `run/` absent; no saved worlds or test-server configuration in this checkout |
| Generated outputs/caches | `build/`, `out/`, `.gradle/`, `.gradle-home/`, `.jdks/` absent |
| External jars/IDE output | `libs/`, `.idea/`, `eclipse/` absent |

No local file was removed, moved, or copied during preparation. Verify the ignored JDK configuration's content hash remains unchanged across the final branch switch. Any new local files or worlds added after this snapshot must be considered again before M1 replaces tracked files. The [foundation save/local-file policy](foundation-scope.md#saves-and-local-files) remains applicable.

## M1 Starting Point

Step 1.1.1 has [inventoried the legacy build/source/resources/scripts/CI](legacy-inventory.md) and rechecked the preserved refs. Preserve the modern planning notes, existing local selection, and project identity. Steps 1.1.2-1.1.3 handle documentation/file transition; step 1.2 installs the [selected toolchain](toolchain.md). Modern build/run commands remain behind that replacement in [Building](building.md).

Branch preparation is verified with refs/upstream/clean status, documentation checks, unchanged legacy implementation, and local-file preservation. No Gradle build, client, or server launch was run during M0 completion; these are M1 verification tasks.

**Next: Phase M1, step 1.1.2 - preserve modern planning notes and project identity, and move legacy-only documentation references to the 1.12 branch in the modern README.**
