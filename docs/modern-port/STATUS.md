# Status
Updated: 2026-10-29 - Handoff branch: `codex/m20-unit-tiers`

## Position
- M1-M13, M15-M19 exit gates passed; M20 Heavy/Elite and M21 useful Level-2 layout remain unimplemented.
- Status/Growth tabs provide Expand/Retry and saved Manual/Automatic choice; original admin commands remain, with grow/mode/help added.
- The same opt-in apron still requires earned local level 2; no automatic level purchases, new cost/reward/population, old-site adoption or repair.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Final clean build passed 15,437 assertions/all 65 GameTests, including real chunk unload/reload and newer-save refusal; jar metadata/QA exclusion inspected.
- Two-JVM fixture qa-b6564654-478b-4e21-a11d-fd04644516f0 passed, retaining five growth modes/states and all prior receipts; normal runServer reached Done/stop/clean exit.
- Final qaClient passed; all 35 e2ca06a4-prefix screenshots opened/reviewed. Windows implementation CI 40793286116 passed build/GameTests/restart/artifacts.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- Mixed/unknown/newer-save state pauses growth; server disable wins. No atomic power-loss promise, automatic repair or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M20 steps 20.1-20.4 (Heavy/Elite unit roles, registration and original visuals, gated shared deployments and automated verification) on codex/m20-unit-tiers.`
