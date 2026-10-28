# Status
Updated: 2026-10-28 - Handoff branch: `codex/m19-unit-tiers`

## Position
- M1-M13, M15-M18 have verified exit gates; M19 Heavy/Elite unit expansion is scoped, not implemented.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- M18 adds one default-disabled confirmed apron; no automatic growth, old-site adoption or capacity/reward changes.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M18 final clean build: 15,409 assertions/all 55 GameTests passed; normal jar metadata/QA isolation checked.
- Two JVMs retained prepared/mixed/applied/all-after states, rejected old tokens and verified explicit metadata-only recovery.
- M18 qaClient passed; all 25 captures reviewed. Windows implementation CI 40772187791 passed the full ladder/artifacts.
- Native tests include first/middle/last faults, callback/container/protected-state conflicts, late entities and permissions.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Worker-proxy tests are not concurrent natural-worldgen proof; broad-terrain/load, packaged and multiplayer QA remain separate.
- [Expansion contract](outpost-expansion-design.md): no fsync/atomic-power-loss promise; mixed/unknown state stops for review.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M19 steps 19.1-19.4 (Heavy/Elite unit roles, registration and original visuals, gated shared deployments and automated verification) on codex/m19-unit-tiers.`
