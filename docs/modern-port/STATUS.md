# Status
Updated: 2026-11-12

## Position
- M32 First Earned Outpost (PR #4) and M33 Army Look And Sound I (PR #6) are on `main`; Liam's cheats-off earned run and M33 look-and-listen check are outstanding.
- D5 (approved 2026-11-12): the GeckoLib 5.5.7 Army Rifleman is on `main` (PR #7) as a test-gated experiment. Still open: the PC frame-time comparison, a packaged install including GeckoLib, and Liam's motion check.
- M34 Defend The Post on `claude/m34-defend-the-post` (PR #8, draft): warned natural raids from a named direction; the defence order auto-starts at Stage 1 with threat in Orders. Codex's review fixes are applied.
- D6 decided 2026-11-13: the HQ is a configurable mode (default: an Establish HQ order promotes an existing Command Post; alternative: an HQ active from world start). The ruined bunker stays an optional extra. It applies from M35.
- Claude owns [Roadmap 3](phase-roadmap.md#roadmap-3---earned-survival-start-then-a-living-army) (M33-M41, reprioritised 2026-11-13) and the [future roadmaps 4-7](phase-roadmap.md#future-roadmaps-provisional); work reaches `main` through PRs reviewed by the other agent (handoff.md). STATUS remains authoritative. NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M32 on Liam's PC: clean build, 175 GameTests, two-JVM restart, 99 QA captures (Orders views and tabs reviewed). After Codex's reviews, Windows CI 43268709270: 178/178 GameTests, restart passed.
- M33 (`a66ffe3`): Windows CI 43275994632 180/180 GameTests, art/sound logic checks, restart; 103 QA captures incl. four variant lineups. Codex repeated it independently on Windows (nine clips decoded); no blocker.
- M31 baseline: [isolated loader trial](compatibility.md#m30-closeout), packaged standalone client/server and [0/1/2/4-post load and two loopback clients](alpha-test-candidate.md#m31-integration-fixtures).

## Risks And Decisions
- Optional threat mods are excluded from M31. Bounded Undead Nights trial proof does not cover natural waves, packaged external installs or infection; Contagion remains unverified.
- No cheats-off earned Survival run exists yet (M32 32.6 / M31 31.3). Checkpoint terrain pass rate on real seeds is unmeasured. Authenticated remote multiplayer, sustained load and a fresh packaged install remain unverified.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
Codex re-review of PR #8 and review of the roadmap PR #9 (Liam decided D10-D12 and D15 on 2026-11-13; D13-D14 wait for their roadmaps). Liam's first playtest (2026-11-13) reprioritised Roadmap 3: M35 Feel pass (sound, visible motion, armed Riflemen, raid bar, JEI) and M36 alert levels and backup are now P1, ahead of the Phase 0 world (now M39). The D5 and M34 QA runs are queued on Liam's PC runner (`start-runner.cmd`). Then:
`Continue: M35 Feel pass (CC0 sounds, stronger motion, armed Riflemen, raid bar, optional JEI) after PR #8 merges.`
