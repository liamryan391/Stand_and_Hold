# Status
Updated: 2026-10-28 - Branch: `codex/m15-outpost-progression`

## Position
- M1-M13 are merged; automated QA and one bounded paid-reinforcement prototype are verified.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- M15 implemented and locally verified; branch CI/merge pending. Physical structure expansion remains excluded.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M15 clean build passed: 7,910 assertions, isolated normal jar; all 29 required GameTests passed.
- Two dedicated JVMs retained upgraded level/storage, five exact defender UUIDs, cooldowns and prior systems.
- Self-quitting qaClient passed; all 14 screenshots reviewed (46dbb4e2 prefix), normal/large capacity states readable.
- Existing explicit post ceilings (including 3 and 0) remain authoritative; fresh profile 3/5/6/7/8, area cap 8.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Independent feedback is deferred to beta; broad-terrain, sustained-load, packaged and multiplayer QA remain separate.
- [Loader evaluation](multi-loader-evaluation.md) remains DEFER; no new pin, loader or runtime library selected.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M15 steps 15.1-15.4 (outpost-level defender capacity, shared spawn limits, Command Post status and automated verification) on codex/m15-outpost-progression.`
