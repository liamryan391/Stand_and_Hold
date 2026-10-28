# Status
Updated: 2026-10-28 - Handoff branch: `codex/m19-outpost-controls`

## Position
- M1-M13, M15-M18 exit gates passed; M19 growth controls, M20 Heavy/Elite and M21 useful Level-2 layout are planned, not implemented.
- Liam wants both: simple player menus/manual-or-automatic growth AND retained, expandable admin/testing commands; shared server-side rules.
- M18 still ships one default-disabled token-confirmed apron; this docs pass changes no gameplay/defaults/dependencies.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Prior M18 clean build: 15,409 assertions/all 55 GameTests; two-JVM receipt/recovery persistence; 25 client captures reviewed.
- Prior M18 Windows implementation CI 40772187791 passed the full ladder/artifacts; not rerun locally for this docs-only review.
- [Reference review](gameplay-direction-and-references.md): author pages/file listings and selected repository licence boundaries checked; no external runtime tests.

## Risks And Decisions
- No new dependency/pin approval; only Liam accepts EULAs. Public source/downloads do not grant blanket asset reuse; original code/art policy retained.
- Undead Nights/GeckoLib/SecurityCraft/Lost Cities have listed 26.3 candidates, not tested combinations; Contagion's bootstrap failure remains open.
- M18 mixed/unknown receipts pause for review; no atomic power-loss promise, automatic repair, old-site adoption or claim-mod support.
- Natural-worldgen concurrency/load, real multiplayer and packaged installs remain separate; preserve legacy `1.12`, alpha tag and ignored local files.

## Next Milestone
`Continue: M19 steps 19.1-19.4 (player-friendly Command Post growth controls, saved manual/automatic policy, bounded apron application and automated verification) on codex/m19-outpost-controls.`
