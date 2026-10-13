# M0 Decision Review

Phase M0, step 0.5.1. Reviewed 2026-10-14. Result: **complete**. The toolchain, setup, scope, and dependency/save decisions are consistent and published. Subsequent branch preparation is recorded in [Scaffold Branch Handoff](scaffold-handoff.md).

## Consolidated Decisions

| Decision | Accepted result | Owning document |
| --- | --- | --- |
| Toolchain | Minecraft 26.3, NeoForge 26.3.0.57-beta, Java 25 (local baseline Temurin 25.0.4+7), Gradle 9.2.1, ModDevGradle 2.0.148, Foojay resolver 1.0.0 | [Toolchain](toolchain.md), including MDK commit/checksum |
| Loader status | Beta-loader risk accepted for the scaffold; actual build/launch evidence remains required in M1 | [Toolchain](toolchain.md) |
| Windows/IDE setup | Explicit Java 25 selection for modern work, reviewed wrapper commands, IDE JVM settings, and separate modern caches/run directories | [Building](building.md) |
| Failure handling | Diagnose environment/scaffold failures and reproduce the minimal template before proposing a coherent fallback | [Building](building.md#fallback-decision-rule) |
| M1 scope | Minimal entry point/logger/config shell, identity/metadata, build/CI support, and launch verification | [Foundation policy](foundation-scope.md) |
| Dependencies | Platform/build tooling only in M1; no added runtime library or approved exception. AI/animation evaluation stays in later phases; threat mods remain optional | [Foundation policy](foundation-scope.md) |
| Saves and local files | Separate modern worlds; no legacy world/config/NBT conversion. Preserve ignored local files separately from Git history | [Foundation policy](foundation-scope.md#saves-and-local-files) |
| Implementation route | Prepare `codex/modern-neoforge-scaffold`, inventory and replace legacy implementation in M1, then merge verified work into `main` | [Technical plan](technical-port-plan.md) and [Roadmap](phase-roadmap.md) |

No decision correction, version change, or new dependency was needed. Deferred library choices and future gameplay are recorded in their owning phases and do not block branch preparation. Research reports retain their dated evidence; this review does not refresh release availability or claim a tested mod combination.

## Publication Evidence

The reviewed baseline is `da8b1d05e36301834f9f44445908ba66c622f3d1`. After `git fetch origin`, local HEAD and `origin/main` matched that commit. Live `git ls-remote` confirmed the same remote head. All four checkpoints below passed `git merge-base --is-ancestor <commit> origin/main`:

| Checkpoint | Published commit |
| --- | --- |
| Roadmap and release/ecosystem research | [bf44b469c2d677da0b5beec3228fd0ea22249b01](https://github.com/liamryan391/Stand_and_Hold/commit/bf44b469c2d677da0b5beec3228fd0ea22249b01) |
| Exact toolchain and Java selector | [94efe4d2ecd5e01dbfc149ee6d82af7f168c50e4](https://github.com/liamryan391/Stand_and_Hold/commit/94efe4d2ecd5e01dbfc149ee6d82af7f168c50e4) |
| Windows/IDE instructions and fallback rule | [af988c62e4f47527db808879b516b91ea1d2861b](https://github.com/liamryan391/Stand_and_Hold/commit/af988c62e4f47527db808879b516b91ea1d2861b) |
| Foundation scope and dependency/save policy | [da8b1d05e36301834f9f44445908ba66c622f3d1](https://github.com/liamryan391/Stand_and_Hold/commit/da8b1d05e36301834f9f44445908ba66c622f3d1) |

Existing commits satisfy publication of the decisions. This review adds only its evidence and progress/handoff updates, under checkpoint title `docs: record consolidated M0 decision review`. Use Git history and live refs to confirm that review checkpoint's publication; its verified hash is reported at session completion.

## Preservation And Verification

- Local and remote `1.12` match `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`.
- Local and remote `v0.1.0-alpha.1` tag refs match `28fd40a81a50ea2f2382489e60d9015b4725cc92`.
- The initial working tree was clean. Source, legacy build files/wrapper, CI, and the Java 8 helper have no differences from the preserved branch. M0 additions remain planning documents, the modern Java selector, and its ignore rule.
- Modern Gradle/build, jar, IDE, client, and dedicated-server checks remain outstanding in M1. The current root project remains Forge 1.12.2 / Java 8; no modern build or launch was run during this review.

## Handoff

This review created no branch or scaffold. Step 0.5.2 subsequently prepared `codex/modern-neoforge-scaffold`; [Scaffold Branch Handoff](scaffold-handoff.md) records its baseline, checkout path, local files, and publication route. Legacy file replacement belongs to M1's inventory/transition steps.

**Next: Phase M1, step 1.1.2 - preserve modern planning notes and project identity, and move legacy-only documentation references to the 1.12 branch in the modern README.** The subsequent [inventory](legacy-inventory.md) is complete.
