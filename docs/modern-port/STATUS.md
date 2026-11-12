# Status
Updated: 2026-11-12

## Position
- M32 First Earned Outpost is on `main` (PR #4, Codex re-review confirmed); Liam's cheats-off earned run is still outstanding.
- M33 Army Look And Sound I (soldier variants, original sounds) passed Codex's independent review; PR #5 landed on the M32 branch, so a follow-up PR carries it to `main` after Liam's play check.
- D5 (approved 2026-11-12): GeckoLib Rifleman experiment on `claude/m33-d5-geckolib`, draft PR #7. M34 Defend The Post on `claude/m34-defend-the-post` (stacked on M33): warned natural raids from a named direction; the defence order auto-starts at Stage 1 with threat in Orders.
- Claude owns [Roadmap 3](phase-roadmap.md#roadmap-3---earned-survival-start-then-a-living-army) (M33-M37 provisional) and the [future roadmaps 4-7](phase-roadmap.md#future-roadmaps-provisional). M31 remains the internal QA baseline.
- Work reaches `main` through pull requests reviewed by the other agent (handoff.md); STATUS remains authoritative. NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

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
Liam's play check (fresh cheats-off Survival world: Orders tab, soldier variety, gear and radio sounds; [checklists](phase-roadmap.md#phase-m33---army-look-and-sound-i)), then the M33 PR to `main` merges. The D5 experiment (33.4, draft PR #7) and M34 (this branch) wait on review and play checks. D6 is needed only before M35. Then:
`Continue: M34 review and play check, then M35 Phase 0 world and the HQ (needs D6).`
