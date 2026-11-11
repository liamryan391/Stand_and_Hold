# Status
Updated: 2026-11-11

## Position
- M30 closed: keep the verified NeoForge 26.3.0.102 compatibility trial separate. M31 uses the existing standalone pin.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M30 standalone clean build (34,620 logic assertions) and all 168 required GameTests passed; normal jar SHA-256 matches M29. M29 two-JVM qa-51d41b29 preserved both layout eras and saved state.
- qaClient passed (94 captures); reviewed all seven Main Base overhead/interior/night/menu views with prefix abdf41ed.
- Exact normal jar-only dedicated DEV run verified natural v1 placement, all three tiles after restart, safe repeat refusal and default dormant/active generation. Art export, packaged preparation and metadata/QA exclusion passed.
- Windows CI 42946651760 passed the pinned build, native tests, restart and artifact upload.
- The [isolated loader trial](compatibility.md#m30-closeout) passed packaged standalone client/server, Undead Nights coexistence/removal and Windows CI 43079580782.

## Risks And Decisions
- Undead Nights 2.2.7 and Contagion 2.1.2 are not supported on the current pin. The trial is not a claim for other variants, natural Horde Nights, infection gameplay or packaged external installs.
- Real multiplayer and load remain unverified. Establish the standalone M31 baseline before revisiting an explicitly approved loader promotion.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M31 steps 31.1-31.4 (bounded load baseline, real multiplayer, earned-loop review and internal alpha candidate) on codex/m31-alpha-integration.`
