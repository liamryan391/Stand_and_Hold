# Status
Updated: 2026-11-02 - Work branch: `codex/m21-outpost-layout`

## Position
- M21 local gate passed; CI's QA-raider lookup/isolation race is fixed and rechecked. Replacement CI pending; main is unchanged.
- Level-2 Growth opens a 13x13 yard with a small watch deck; direct sites and sealed old aprons retain buildings, work, storage, identities and reservations.
- Same opt-in Manual/Automatic controls and local level-2 gate; no new cost/reward/population, automatic level purchase, repair or adoption.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Final clean build: 30,177 assertions/all 98 required GameTests passed, including protected removals, navigation, faults, old receipts and prior regressions; jar metadata/QA exclusion inspected.
- Three consecutive two-JVM fixtures passed after the QA-only isolation fix (latest qa-9993754d-b85a-4d99-8d2c-2c8627c879cb); normal runServer reached Done/stop/clean exit.
- qaClient passed; all 47 0627d8c3-prefix PNGs opened/reviewed. Initial Windows CI 41442706684 passed; follow-up 41443407792 failed fixture isolation. No new human play pass claimed.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- Mixed/unknown/newer-save state pauses growth; server disable wins. No atomic power-loss promise, automatic repair or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M21 steps 21.1-21.4 (useful Level-2 layout, protected planning/application, manual/automatic integration and automated verification) on codex/m21-outpost-layout.`
