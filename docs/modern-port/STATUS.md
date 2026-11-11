# Status
Updated: 2026-11-11

## Position
- M27 verified and merged/pushed; codex/m28-specialist-tiers prepared. Windows CI 42938656507 passed build, native tests, restart and artifact upload.
- Four-tick carry/aim blend and one five-tick shot cue only after accepted spawn. No firing, damage, balance, save-schema, dependency or pin changes.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Clean build passed 33,940 logic assertions. All 154 required native cases passed, including real aim/sight/stage transitions and cooldown/friendly guards.
- Final qaClient passed (83 captures); reviewed all 10 motion/Launcher PNGs and three tier regressions with prefix 27219c98. Reload, held and inventory views render.
- Two-JVM qa-de4c8c25 passed. The exact normal jar reached Done, summoned a Rifleman and stopped under dedicated DEV bootstrap with source mod and QA mod unloaded.
- Eight-source art export, packaged preparation and normal-jar metadata/QA exclusion passed. M22 production profile and user worlds remain untouched.

## Risks And Decisions
- No fresh production install, real multiplayer/load, other launcher/OS or external-threat proof. Contagion remains unsupported. Motion feel belongs to Liam's isolated review.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M28 steps 28.1-28.4 (specialist roles, original registration and visuals, shared gated recruitment and automated verification) on codex/m28-specialist-tiers.`
