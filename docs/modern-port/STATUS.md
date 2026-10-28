# Status
Updated: 2026-10-28 - Branch: `codex/m14-loader-evaluation`

## Position
- M1-M13 are merged; automated QA and one bounded paid-reinforcement prototype are verified.
- M14 evaluation starts from the verified NeoForge baseline; no Fabric implementation/dependency approved.
- Attached AGENTS.md is adopted; human checks are for feel/animation, not command readbacks.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build: 2,442 assertions, isolated normal jar; 22 GameTests and paid-support two-JVM restart passed.
- Self-quitting qaClient passed; ten screenshots reviewed (33c9bba4 prefix), including real call-in feedback.
- Final M13 Windows CI 40595049934 passed: build, GameTests, restart and normal jar upload.
- M12 limitations/history: [Known Issues](../../KNOWN_ISSUES.md), [play sheet](showcase-loop.md), [log](progress-log.md).

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Independent feedback is deferred to beta; broad-terrain, sustained-load, packaged and multiplayer QA remain separate.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M14 steps 14.1-14.5 (NeoForge stability review, platform audit, port strategy and gated Fabric evaluation) on codex/m14-loader-evaluation.`
