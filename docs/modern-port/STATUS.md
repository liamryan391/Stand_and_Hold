# Status
Updated: 2026-11-12

## Position
- M32 First Earned Outpost is implemented on `claude/m32-earned-outpost` (Claude writer) and Codex's review findings are fixed. The merge is paused only because Liam asked to play it first; D5/D6 do not affect M32.
- Claude owns [Roadmap 3](phase-roadmap.md#roadmap-3---earned-survival-start-then-a-living-army) (M33-M37 provisional) and the [future roadmaps 4-7](phase-roadmap.md#future-roadmaps-provisional). M31 remains the internal QA baseline on `main`.
- Work reaches `main` through pull requests reviewed by the other agent (handoff.md); STATUS remains authoritative.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M32 on Liam's PC: clean build, 175 GameTests, two-JVM restart, 99 QA captures (Orders views and tabs reviewed). After both Codex reviews, Windows CI 43263085596 (and PR run 43263099381): 178/178 GameTests, restart passed.
- M31 baseline: [isolated loader trial](compatibility.md#m30-closeout), packaged standalone client/server and [0/1/2/4-post load and two loopback clients](alpha-test-candidate.md#m31-integration-fixtures).

## Risks And Decisions
- Optional threat mods are excluded from M31. Bounded Undead Nights trial proof does not cover natural waves, packaged external installs or infection; Contagion remains unverified.
- No cheats-off earned Survival run exists yet (M32 32.6 / M31 31.3). Checkpoint terrain pass rate on real seeds is unmeasured. Authenticated remote multiplayer, sustained load and a fresh packaged install remain unverified.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
Waiting on Liam's cheats-off Survival run of the M32 branch (checklist in the [roadmap](phase-roadmap.md#phase-m32---first-earned-outpost)); then the M32 PR merges. D5 is needed only for the M33 animation experiment and D6 only before M35. Then:
`Continue: M32 step 32.6 closeout (fix earned-run findings, merge to main after green CI), then M33 army look and sound.`
