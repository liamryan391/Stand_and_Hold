# Status
Updated: 2026-11-02 - Work branch: `codex/m21-outpost-layout`

## Position
- M21 implementation and local verification passed; publication/green CI remain before merge.
- Level-2 Growth opens a 13x13 yard with a small watch deck; direct sites and sealed old aprons retain buildings, work, storage, identities and reservations.
- Same opt-in Manual/Automatic controls and local level-2 gate; no new cost/reward/population, automatic level purchase, repair or adoption.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Final clean build: 30,177 assertions/all 98 required GameTests passed, including protected removals, navigation, faults, old receipts and prior regressions; jar metadata/QA exclusion inspected.
- Final two-JVM fixture qa-aefd22c8-df57-4cf9-bea7-94ed5896a623 passed; normal runServer reached Done, answered status/help and stopped cleanly.
- Final qaClient passed; all 47 0627d8c3-prefix PNGs opened/reviewed, including yard overhead/entrance/watch/menu views. CI pending; no new human play pass claimed.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- Mixed/unknown/newer-save state pauses growth; server disable wins. No atomic power-loss promise, automatic repair or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M21 steps 21.1-21.4 (useful Level-2 layout, protected planning/application, manual/automatic integration and automated verification) on codex/m21-outpost-layout.`
