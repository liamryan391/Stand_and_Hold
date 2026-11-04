# Status
Updated: 2026-11-04

## Position
- M25 25.1-25.3 complete on codex/m25-level3-preview; 25.4 local gates passed, CI/publication pending.
- Read-only 63-cell upper-deck candidate; 1,521-cell loaded preflight. No application/receipt, cost, refill or mode change.
- No stats, spawn/cost limits, dependencies, version or pins changed. Legacy references, packaged M22 profile and user worlds/configs preserved.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M25 clean build/native run passed (33s): 33,802 logic assertions and all 131 required GameTests, including 10 new strict read-only cases.
- Valid direct/apron histories, every proposed edit/roof replacement, protected terrain/headroom/entities, unknown receipts, menu/command authority and no automatic work passed.
- Two-JVM restart qa-0ea93fbf passed: same 63-cell candidate after reload; no receipt, resource reset or automatic application.
- runQaClient passed (2m1s); five new 66af355b-prefix preview PNGs and eight representative menu/apron regressions opened/reviewed. Render waits fixed early captures.
- Art/packaging-preparation checks passed; normal jar metadata/QA exclusion inspected. Final publication waits for green Windows CI.

## Risks And Decisions
- Prior QA risks: one pre-menu native client exit and one duty_blocked_backoff chunk-count failure; retained checks passed in M25's full native run. Causes unverified.
- M22 isolated fixture retained; its one-server EULA authorization does not apply to fresh prompts. No account handling or reusable acceptance flag added.
- Real multiplayer/load, other launcher/OS and external threats remain unverified; Contagion unsupported. No new earned-loop/player-only drop pass or public release claimed.
- Runtime libraries/test mods, pins and fresh EULA prompts retain their approval boundaries. Preserve `1.12`, alpha tag, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline catch-up. Independent beta/public release approval and wider reliability remain separate.

## Next Milestone
`Continue: M25 steps 25.1-25.4 (Level-3 layout preservation contract, bounded read-only planner, preview controls and automated verification) on codex/m25-level3-preview.`
