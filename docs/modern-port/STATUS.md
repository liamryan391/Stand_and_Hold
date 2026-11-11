# Status
Updated: 2026-11-11

## Position
- M29 exit gate passed. New Main Bases have a lit central lane, covered command/research/supply bays and a barracks nook. Unversioned bases are not retrofitted.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build passed 34,620 logic assertions; all 168 required GameTests passed. Two-JVM qa-51d41b29 preserved both layout eras, custom blocks, resources, paused work and notices.
- qaClient passed (94 captures); reviewed all seven Main Base overhead/interior/night/menu views with prefix abdf41ed.
- Exact normal jar-only dedicated DEV run verified natural v1 placement, all three tiles after restart, safe repeat refusal and default dormant/active generation. Art export, packaged preparation and metadata/QA exclusion passed.
- Windows CI 42946651760 passed build, native tests, restart and artifact upload.

## Risks And Decisions
- No fresh production install, real multiplayer/load, other launcher/OS or external-threat proof. Contagion remains unsupported; M30 needs exact test-artifact approval or explicit deferral.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M30 steps 30.1-30.4 (exact optional threat candidate, approved isolated coexistence/removal tests or explicit deferral, and support documentation) on codex/m30-threat-coexistence.`
