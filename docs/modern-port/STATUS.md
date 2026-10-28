# Status
Updated: 2026-10-28 - Handoff branch: `codex/m17-outpost-preview`

## Position
- M1-M13 and M15 are merged; M16 adds the requested empty lab to new checkpoints and completes expansion design.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- Design verdict: provenance/read-only preview first; actual expansion/confirm deferred. No existing site resized/retrofitted.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M16 clean build passed: 7,930 assertions, normal-jar metadata/QA isolation checked; all 33 required GameTests passed.
- Two dedicated JVMs retained generated lab storage/paused work, no crate refill and all prior persistent systems.
- Self-quitting qaClient passed; all 16 screenshots reviewed (8f1f18ee prefix), including checkpoint/lab menu.
- Proposed expansion is one-chunk outer apron; budgets/recovery/ownership and future test gates are documented, not implemented.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Independent feedback is deferred to beta; broad-terrain, sustained-load, packaged and multiplayer QA remain separate.
- [Expansion design](outpost-expansion-design.md) admits M17 preview only; missing old-site provenance must not be guessed.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M17 steps 17.1-17.4 (new-site provenance, bounded expansion planner, read-only operator preview and automated verification) on codex/m17-outpost-preview.`
