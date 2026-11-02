# Status
Updated: 2026-11-02

## Position
- M24 24.1-24.4 exit gate passed on codex/m24-local-storage; Windows implementation CI 41546801428 green. M23 feel feedback passed.
- Explicit opt-in levelled Command Post storage preserves flat defaults and lab/crate limits; paid upgrades add empty capacity, never a refill.
- No stats, spawn/cost limits, dependencies, version or pins changed. Legacy references, packaged M22 profile and user worlds/configs preserved.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M24 exact clean build passed (5s), 30,656 assertions. Full 121-case native run passed (34s) with all required assertions retained; one prior intermittent failure is recorded below.
- Two-JVM restart qa-c504c2a9 passed: paid level 4, 6,001 over-cap supplies/2 samples retained; mode-disable/export conserved resources and did not refill or clamp.
- Isolated runQaClient passed (1m56s); all 62 d5b5259e-prefix PNGs opened/reviewed, including storage-level-two-large.png and storage-upgrade-applied-large.png. Art/packaging checks passed.
- Normal jar metadata/QA exclusion inspected; SHA256 311ef7d3495f1242a30b1aae9ee5401946038bc434641e4dbe4b9900d1f3e2e8. No M24 packaged-install pass claimed.

## Risks And Decisions
- Known QA risks: one pre-menu native client exit and one duty_blocked_backoff chunk-count failure; unchanged checks passed afterward. Causes unverified; never skip failures.
- M22 isolated fixture retained; its one-server EULA authorization does not apply to fresh prompts. No account handling or reusable acceptance flag added.
- Real multiplayer/load, other launcher/OS and external threats remain unverified; Contagion unsupported. No new earned-loop/player-only drop pass or public release claimed.
- Runtime libraries/test mods, pins and fresh EULA prompts retain their approval boundaries. Preserve `1.12`, alpha tag, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline catch-up. Independent beta/public release approval and wider reliability remain separate.

## Next Milestone
`Continue: M25 steps 25.1-25.4 (Level-3 layout preservation contract, bounded read-only planner, preview controls and automated verification) on codex/m25-level3-preview.`
