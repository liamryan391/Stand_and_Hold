# Status
Updated: 2026-10-28 - Branch: `codex/m15-outpost-progression`

## Position
- M1-M13 are merged; automated QA and one bounded paid-reinforcement prototype are verified.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- M15 outpost-level defender capacity/status is planned, not implemented; physical expansion is excluded.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build: 2,442 assertions, isolated normal jar; 22 GameTests and paid-support two-JVM restart passed.
- Self-quitting qaClient passed; ten screenshots reviewed (33c9bba4 prefix), including real call-in feedback.
- M13 main CI 40703073861 and M14 evaluation CI 40703882149 passed build, GameTests, restart and jar upload.
- Liam reports M13 general play/feel passed; call-in usage clarified, not a new manual mechanics pass.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Independent feedback is deferred to beta; broad-terrain, sustained-load, packaged and multiplayer QA remain separate.
- [Loader evaluation](multi-loader-evaluation.md) remains DEFER; no new pin, loader or runtime library selected.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M15 steps 15.1-15.4 (outpost-level defender capacity, shared spawn limits, Command Post status and automated verification) on codex/m15-outpost-progression.`
