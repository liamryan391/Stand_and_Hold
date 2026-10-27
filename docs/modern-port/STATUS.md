# Status
Updated: 2026-10-27 - Branch: `codex/m13-vehicles`

## Position
- M1-M12 are merged; Liam accepted qualified M12 earned-core plus dedicated raid/combat/restart proof.
- M13 automated QA is implemented; integration/design and one bounded prototype remain.
- Attached AGENTS.md is adopted; human checks are for feel/animation, not command readbacks.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build: 2,442 assertions, isolated normal jar; 15 GameTests and two-JVM restart passed.
- Self-quitting qaClient passed; nine screenshots reviewed (90e73aa0 prefix). New CI jobs await push.
- M12 limitations/history: [Known Issues](../../KNOWN_ISSUES.md), [play sheet](showcase-loop.md), [log](progress-log.md).

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Independent feedback is deferred to beta; broad-terrain, sustained-load, packaged and multiplayer QA remain separate.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M13 steps 13.0-13.4 (isolated automated QA, integration review, bounded support/vehicle design, one scoped prototype after the design gate, and verification) on codex/m13-vehicles.`
