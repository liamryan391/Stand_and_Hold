# Status
Updated: 2026-11-13

## Position
- On `main`: M32 First Earned Outpost (PR #4), M33 Army Look And Sound I (PR #6), the D5 GeckoLib Rifleman (PR #7), M34 Defend The Post (PR #8), the Roadmap 3-9 plan (PR #9) and the Stand and Hold License (PR #10). Liam's earned run and M33/M34 checks are outstanding.
- M35 Feel Pass I on `claude/m35-feel-pass` (draft PR for Codex): natural kit sound with CC0 cloth, calm and urgent radio, clearly visible Rifleman motion with a run, Riflemen always armed with an original service rifle, a raid bar, optional JEI info pages.
- Decided 2026-11-13: D6 configurable HQ (default Establish HQ order), D10 CC0 sound with credits, D11 optional JEI, D12 own Field Manual first then GuideME/FTB Quests as optional extras, D15 Blockbench/builder MCP tools after licence checks. D13 (factions) and D14 (gun mods) wait for their roadmaps.
- Claude owns [Roadmap 3](phase-roadmap.md#roadmap-3---earned-survival-start-then-a-living-army) (M33-M41) and the [future roadmaps](phase-roadmap.md#future-roadmaps-provisional); work reaches `main` through PRs reviewed by the other agent (handoff.md). NeoForge-first. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148; GeckoLib 5.5.7 required, JEI API 31.17.0.113 compile-only.

## Verified
- M35 (`91694b9`): Windows CI 43356710864, 186/186 GameTests incl. `defence_raid_bar` and `presentation_rifleman_rifle`; art, sound (centroid bands), compatibility and equipment checks; art export check; restart.
- M34 after review: Windows CI 43333642686, 184/184 GameTests, restart with a persisted warned raid. M33 (`a66ffe3`): CI 43275994632 and Codex's independent Windows run; 103 QA captures.
- M31 baseline: [isolated loader trial](compatibility.md#m30-closeout), packaged standalone client/server and [0/1/2/4-post load and two loopback clients](alpha-test-candidate.md#m31-integration-fixtures).

## Risks And Decisions
- D5 frame time is unmeasured: the first PC run hit vanilla's AFK cap (33.4 ms for both renderers). The fixture is fixed and the rerun is queued. A packaged install including GeckoLib is still unverified (M41).
- No cheats-off earned Survival run yet (M32 32.6). Checkpoint terrain pass rate on real seeds, authenticated remote multiplayer and sustained load remain unverified. Optional threat mods stay excluded; Contagion unverified.
- Runtime libraries, pins and EULA prompts keep their approval boundaries. Preserve `1.12`, the alpha tag, fixtures, worlds and configs. No legacy world migration.

## Next Milestone
Codex reviews the M35 PR; the M35 QA runs (default, native Rifleman, with JEI) wait on Liam's PC runner (`start-runner.cmd`); Liam's listen-and-watch check. Then:
`Continue: M36 alert levels (Calm/Alert/Contact/Overrun) and quick-reaction backup after the M35 PR merges.`
