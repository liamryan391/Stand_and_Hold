# Status
Updated: 2026-10-29 - Handoff branch: `codex/m20-unit-tiers`

## Position
- M1-M13, M15-M19 exit gates passed; M20 Heavy/Elite implementation complete, final visual/CI gate in progress. M21 remains unimplemented.
- Status/Growth tabs provide Expand/Retry and saved Manual/Automatic choice; original admin commands remain, with grow/mode/help added.
- The same opt-in apron still requires earned local level 2; no automatic level purchases, new cost/reward/population, old-site adoption or repair.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build passed 15,826 assertions/all 80 GameTests, including new roles/eggs/config/AI/projectiles/shared deployment and prior regressions; normal jar metadata/QA exclusion inspected.
- Two-JVM fixture qa-707cb4a2-38ed-493d-8687-2dbaf618c81d passed with Heavy/Elite UUIDs/kit/damaged health/rosters plus prior receipts; normal runServer reached Done/stop/clean exit.
- Final qaClient visual review and M20 CI pending; no merge or new human play pass claimed yet.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- Mixed/unknown/newer-save state pauses growth; server disable wins. No atomic power-loss promise, automatic repair or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M20 steps 20.1-20.4 (Heavy/Elite unit roles, registration and original visuals, gated shared deployments and automated verification) on codex/m20-unit-tiers.`
