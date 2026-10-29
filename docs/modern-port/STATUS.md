# Status
Updated: 2026-10-29 - Handoff branch: `codex/m21-outpost-layout`

## Position
- M1-M13, M15-M20 exit gates passed; M21 useful Level-2 layout remains unimplemented.
- Four original human roles; Heavy defaults to Stage 2/Elite Stage 3 + Field Communications. Shared deployments retain quotas/cooldowns, old identities and issued kit.
- The same opt-in apron still requires earned local level 2; no automatic level purchases, new cost/reward/population, old-site adoption or repair.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build passed 15,826 assertions/all 80 GameTests, including new roles/eggs/config/AI/projectiles/shared deployment and prior regressions; normal jar metadata/QA exclusion inspected.
- Final two-JVM fixture qa-3a7fcf36-0d3b-4c68-99d9-b3fffc6ff8ed passed with Heavy/Elite UUIDs/kit/damaged health/rosters plus prior receipts; normal runServer reached Done/stop/clean exit.
- Final qaClient passed; all 43 e4881307-prefix PNGs opened/reviewed. Windows implementation CI 40799057223 passed build/GameTests/restart/artifacts; no new human play pass claimed.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- Mixed/unknown/newer-save state pauses growth; server disable wins. No atomic power-loss promise, automatic repair or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M21 steps 21.1-21.4 (useful Level-2 layout, protected planning/application, manual/automatic integration and automated verification) on codex/m21-outpost-layout.`
