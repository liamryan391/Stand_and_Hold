# Status
Updated: 2026-11-02

## Position
- M22 merged/pushed after CI 41478973410 passed. M23 23.1-23.4 exit gate passed on codex/m23-defender-duties; Windows implementation CI 41513822982 green.
- Saved Guard/Patrol controls, two local waypoints and one proven yard watch slot; existing assigned roster only, combat/response priority and bounded path backoff.
- No stats, spawn/cost limits, dependencies, version or pins changed. Legacy references, packaged M22 profile and user worlds/configs preserved.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M23 clean build/native tests passed (27s): 30,182 assertions/112 required cases, including physical arrival/combat return, reassignment, bounded failures and authority.
- Two-JVM restart qa-294e577a passed: policy/watch UUID, home/waypoint, health/gear/roster retained; prior M22 packaged proof remains separate.
- runQaClient passed; all 52 f15eeaa0-prefix PNGs opened/reviewed, including duties-patrol-large.png and duty-watch-arrival.png. Seven-model export and packaged-preparation checks passed.
- Normal jar neoforge.mods.toml/QA exclusion inspected; SHA256 91ae5333517ea4e7dee3412c035d3699fe9587433a4937ce4d6a4eb31584acaa. No M23 packaged-install pass claimed.

## Risks And Decisions
- Watch is a position, not a sensor/turret; unresolved saved owners retain their slot. Blocked/damaged paths wait rather than repair or teleport.
- M22 isolated fixture retained; its one-server EULA authorization does not apply to fresh prompts. No account handling or reusable acceptance flag added.
- Real multiplayer/load, other launcher/OS and external threats remain unverified; Contagion unsupported. No new earned-loop/player-only drop pass or public release claimed.
- Runtime libraries/test mods, pins and fresh EULA prompts retain their approval boundaries. Preserve `1.12`, alpha tag, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline catch-up. Motion/feel remains Liam's final check; independent beta/public release approval stays separate.

## Next Milestone
`Continue: M24 steps 24.1-24.4 (level-aware Command Post supply capacity, conservation rules, readable upgrade benefits and automated verification) on codex/m24-local-storage.`
