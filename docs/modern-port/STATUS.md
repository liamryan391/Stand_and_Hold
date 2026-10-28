# Status
Updated: 2026-10-28 - Handoff branch: `codex/m18-outpost-application`

## Position
- M1-M13, M15-M17 have verified exit gates; M18 confirmed application/recovery is scoped, not implemented.
- Liam confirmed NeoForge-first; Fabric-specific M14 14.4-14.5 remain deferred, not approved or run.
- Design verdict: provenance/read-only preview first; actual expansion/confirm deferred. No existing site resized/retrofitted.
- Pins remain Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M17 final clean build: 15,178 assertions and all 40 required GameTests passed; normal jar metadata/QA isolation checked.
- Two real JVMs retained site UUID/level/storage and identical preview delta, plus all prior persistence checks.
- M17 qaClient passed; all 19 captures reviewed. Windows implementation CI 40757351989 passed the full ladder/artifacts.
- Only proposed cells are returned; physical expansion/confirm, old-site adoption and global-cap changes are excluded.

## Risks And Decisions
- No new runtime dependency/pin change approved; only Liam accepts an EULA if prompted.
- Contagion 2.1.1 fails pinned-loader bootstrap; external infection gameplay remains deferred, not passed.
- Worker-proxy tests are not concurrent natural-worldgen proof; broad-terrain/load, packaged and multiplayer QA remain separate.
- [Expansion design](outpost-expansion-design.md) admits M17 preview only; missing old-site provenance must not be guessed.
- Preserve legacy `1.12`, alpha tag, ignored worlds/configs and `.local/`; no legacy save conversion.

## Next Milestone
`Continue: M18 steps 18.1-18.4 (bounded operation receipts, confirmation policy, guarded apron application and failure/restart verification) on codex/m18-outpost-application.`
