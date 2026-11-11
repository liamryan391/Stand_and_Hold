# Status
Updated: 2026-11-11

## Position
- NeoForge 26.3.0.102 promoted with Liam's approval. M31 technical candidate verified; earned-loop feedback is still open.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Java 25 clean build, 168 GameTests, two-JVM restart, 94 QA captures and package preparation passed. No gameplay code changed.
- The [isolated loader trial](compatibility.md#m30-closeout) passed packaged standalone client/server, Undead Nights coexistence/removal and Windows CI 43079580782.
- [M31](alpha-test-candidate.md#m31-integration-fixtures): 0/1/2/4-post load passed preset budgets (0/2/5/8 defenders); two actual loopback clients rejoined with state intact.

## Risks And Decisions
- Optional threat mods are excluded from M31. Bounded Undead Nights trial proof does not cover natural waves, packaged external installs or infection; Contagion remains unverified.
- Authenticated remote multiplayer, sustained production load, a fresh packaged install of this rebuild and the earned-loop feel review remain unverified.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M31 steps 31.3-31.4 closeout (record Liam's earned-loop feedback and decide the internal alpha candidate's disposition; no release or tag without approval).`
