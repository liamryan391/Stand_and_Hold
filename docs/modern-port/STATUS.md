# Status
Updated: 2026-10-28 - Handoff branch: `codex/m18-outpost-application`

## Position
- M1-M13, M15-M17 have verified exit gates; M18 implementation/fault gate passed, final client/CI/publication underway.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- M18 adds one default-disabled confirmed apron; no automatic growth, old-site adoption or capacity/reward changes.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M18 final clean build: 15,409 assertions/all 55 GameTests passed; normal jar metadata/QA isolation checked.
- Two JVMs retained prepared/mixed/applied/all-after states, rejected old tokens and verified explicit metadata-only recovery.
- Final M18 qaClient screenshot review and commit CI are pending; no pass claimed yet.
- Native tests include first/middle/last faults, callback/container/protected-state conflicts, late entities and permissions.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Worker-proxy tests are not concurrent natural-worldgen proof; broad-terrain/load, packaged and multiplayer QA remain separate.
- [Expansion contract](outpost-expansion-design.md): no fsync/atomic-power-loss promise; mixed/unknown state stops for review.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M18 steps 18.1-18.4 (bounded operation receipts, confirmation policy, guarded apron application and failure/restart verification) on codex/m18-outpost-application.`
