# Status
Updated: 2026-11-02

## Position
- M22 22.1 passed on codex/m22-packaged-alpha; 22.2-22.4 remain open. Do not merge until real packaged client/server gates pass.
- Fresh manifest/normal-jar pack and loopback-only production server prepared; original Modrinth pack/worlds and legacy references untouched.
- No gameplay, balance, dependency, version or toolchain changes. NeoForge-first; vehicles/aircraft remain deferred.
- NeoForge-first; Fabric M14 14.4-14.5 deferred. Pins: MC 26.3 / NeoForge 26.3.0.57-beta / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- M22 clean build/native GameTests passed: 30,177 assertions/98 required tests, no XML failures/errors/skips; offline pack checks and 7-model art check passed.
- Source two-JVM restart passed; all 47 e4a69ae3-prefix qaClient PNGs reviewed (lab-completed-large, yard-watch-view, tiers-front included).
- Normal jar metadata/QA exclusion checked; 409,863 bytes, SHA256 5dddffba3c052414dc29c8d7f665bd46542799b29801756314c227da007a119a. Work-branch CI pending publication.

## Risks And Decisions
- Packaged client blocked before launch by Modrinth Microsoft sign-in failure; Liam handles account prompts. No client entry/reload pass claimed.
- Production installer passed; first boot detected exact pins/normal jar then requested EULA and exited before Done. Only Liam may accept the fresh server/eula.txt; it remains false.
- Session: .local/packaged-qa/m22-20261102-143650-32816cbd. See packaged-testing.md. Multiplayer/external threat combinations remain unverified; Contagion unsupported.
- Only Liam accepts EULAs; new runtime libraries/test-mod installations and pin changes need approval. Preserve `1.12`, alpha tag, worlds/configs and original code/art.
- No power-loss/claim-mod guarantee, automatic repair or offline population catch-up. Independent beta testing and public release approval remain separate.

## Next Milestone
`Continue: M22 steps 22.2-22.4 (finish packaged client/server and restart proof after Liam handles EULA/sign-in, then regression/CI closure) on codex/m22-packaged-alpha.`
