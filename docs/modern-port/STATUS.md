# Status
Updated: 2026-11-04

## Position
- M25 merged/pushed with green CI. M26 26.1-26.3 implemented on codex/m26-level3-application; local exit checks passed, CI/merge pending.
- Guarded 63-cell deck uses additive receipts and existing Manual/Automatic controls; one delta per attempt, no bonus/cost/budget changes.
- No stats, spawn/cost limits, dependencies, version or pins changed. Legacy references, packaged M22 profile and user worlds/configs preserved.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Final clean build passed: 33,877 logic assertions. All 152 required native cases passed, including real stair ascent/descent, 18 application/fault cases and retained earlier checks.
- Reproduced/fixed the east duty point inside new stairs; no teleport/index/home reset. Exact-arrival QA uses vanilla zero-range paths, not the patrol order's stopping radius.
- Final two-JVM qa-d717c268 passed: applied/prepared/mixed/all-after deck receipts, 20 sites, original lab/storage, watch UUID/health/gear and explicit recovery retained without replay.
- Final qaClient passed (73 captures); 16 8e472def-prefix deck/gating/menu/duty/lab/storage PNGs opened/reviewed. Normal jar metadata/QA exclusion, art export and packaged preparation passed.

## Risks And Decisions
- Reproduced delayed duty count failures from unrelated chunk unloads. QA now audits newly added coordinates in both pending/visible maps every tick, with a deliberate-load control; 152 required tests passed. A prior pre-menu native client exit remains unexplained.
- Real multiplayer/load, other launcher/OS and external threats remain unverified; Contagion unsupported. No new earned-loop/player-only drop pass or public release claimed.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline catch-up. Independent beta/public release approval and wider reliability remain separate.

## Next Milestone
`Continue: M26 steps 26.1-26.4 (Level-3 receipts, guarded application, growth controls and failure/restart verification) on codex/m26-level3-application.`
