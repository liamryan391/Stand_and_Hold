# Status
Updated: 2026-11-11

## Position
- M30 closed. Liam approved promoting the trial's exact NeoForge 26.3.0.102 loader for standalone M31 integration.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.102 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Promoted pin clean build and packaged preparation passed (34,620 logic assertions). No gameplay code changed.
- The [isolated loader trial](compatibility.md#m30-closeout) passed packaged standalone client/server, Undead Nights coexistence/removal and Windows CI 43079580782.

## Risks And Decisions
- Optional threat mods are excluded from M31. Bounded Undead Nights trial proof does not cover natural waves, packaged external installs or infection; Contagion remains unverified.
- Real multiplayer, load and the new earned-loop rehearsal remain open.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M31 steps 31.1-31.4 (bounded load baseline, real multiplayer, earned-loop review and internal alpha candidate) on codex/m31-alpha-integration.`
