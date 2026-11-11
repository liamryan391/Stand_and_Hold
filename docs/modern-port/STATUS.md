# Status
Updated: 2026-11-11

## Position
- M28 exit gate passed. Super Elite and Special Division share all recruitment paths; one reserved Division slot per active Main Base, inside existing caps.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build passed 34,571 logic assertions; all 161 required GameTests passed. Two-JVM qa-947d13ce preserved type, home, health, gear and the Division reservation.
- qaClient passed (87 captures); reviewed specialist front/back/torchlight, lineup and eggs with prefix f5032356.
- Normal jar-only dedicated DEV run reached Done, summoned both specialists and stopped. Ten-source art export, packaged preparation and metadata/QA exclusion passed.
- Windows CI 42943525114 passed build, native tests, restart and artifact upload.

## Risks And Decisions
- No fresh production install, real multiplayer/load, other launcher/OS or external-threat proof. Contagion remains unsupported. Specialist feel is a separate play check.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M29 steps 29.1-29.4 (usable Main Base zones, guarded new-site placement, existing system integration and automated verification) on codex/m29-mainbase-layout.`
