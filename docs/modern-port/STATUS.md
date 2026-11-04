# Status
Updated: 2026-11-04

## Position
- M25 25.4 passed; Windows CI 41868122995 green after deterministic QA raid setup. Closing/merging M25 and starting codex/m26-level3-application.
- M26 scope: apply only the 63-cell deck through additive receipts and existing Manual/Automatic controls; no bonus/cost/budget changes.
- No stats, spawn/cost limits, dependencies, version or pins changed. Legacy references, packaged M22 profile and user worlds/configs preserved.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Exact clean build passed (6s): 33,802 assertions. All 131 required GameTests passed twice (33s combined / 28s repeat); no skipped/weakened checks.
- Valid direct/apron histories, every proposed edit/roof replacement, protected terrain/headroom/entities, unknown receipts, menu/command authority and no automatic work passed.
- Two-JVM restart qa-0ea93fbf passed: same 63-cell candidate after reload; no receipt, resource reset or automatic application.
- runQaClient passed (2m1s); five new 66af355b-prefix preview PNGs and eight representative menu/apron regressions opened/reviewed. Render waits fixed early captures.
- Art/packaging-preparation checks passed; normal jar metadata/QA exclusion inspected. SHA256 458ffc20adff44368aa98cce619161e8a5a38d56517592ea9d8b7c89a48a131d.

## Risks And Decisions
- Prior QA risks: one pre-menu native client exit and one duty_blocked_backoff chunk-count failure; retained checks passed in M25's full native run. Causes unverified.
- Initial M25 CI failed the unseeded corner-pad raid fixture before deck checks; fixed QA seed only. All 133 native cases and two-JVM qa-5f831630 passed locally and Windows CI passed. Fresh metadata avoids stale queue displays.
- Real multiplayer/load, other launcher/OS and external threats remain unverified; Contagion unsupported. No new earned-loop/player-only drop pass or public release claimed.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline catch-up. Independent beta/public release approval and wider reliability remain separate.

## Next Milestone
`Continue: M26 steps 26.1-26.4 (Level-3 receipts, guarded application, growth controls and failure/restart verification) on codex/m26-level3-application.`
