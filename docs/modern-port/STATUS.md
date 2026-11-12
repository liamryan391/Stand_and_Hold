# Status
Updated: 2026-11-12

## Position
- M32 First Earned Outpost is implemented on `claude/m32-earned-outpost` (Claude writer; D1-D4 approved 2026-11-12). Not merged: Liam's earned run and Codex review are outstanding.
- Roadmap 3 is in the [phase roadmap](phase-roadmap.md#roadmap-3---earned-survival-start-then-a-living-army); M33-M36 stay provisional. M31 remains the internal QA baseline on `main`.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M32 on Liam's PC: Java 25 clean build with logic checks, 175 GameTests (168 existing + 7 onboarding), two-JVM restart, 99 QA captures with the five Orders views and changed tabs reviewed; Windows CI 43142386768 green.
- M31 baseline: [isolated loader trial](compatibility.md#m30-closeout), packaged standalone client/server and [0/1/2/4-post load and two loopback clients](alpha-test-candidate.md#m31-integration-fixtures).

## Risks And Decisions
- Optional threat mods are excluded from M31. Bounded Undead Nights trial proof does not cover natural waves, packaged external installs or infection; Contagion remains unverified.
- No cheats-off earned Survival run exists yet (M32 32.6 / M31 31.3). Checkpoint terrain pass rate on real seeds is unmeasured. Authenticated remote multiplayer, sustained load and a fresh packaged install remain unverified.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
Blocked on Liam: one cheats-off Survival earned run of the M32 branch (checklist in the [roadmap](phase-roadmap.md#phase-m32---first-earned-outpost)) and Codex's review of `claude/m32-earned-outpost`. Then:
`Continue: M32 step 32.6 closeout (fix earned-run findings, merge to main after green CI), then plan M33 (defend the post).`
