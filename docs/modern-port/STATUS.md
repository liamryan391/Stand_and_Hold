# Status

Updated: 2026-10-21 - M8 merge handoff

## Where we are

- M1-M8 are verified and merged. M8 adds an original visual contract, five editable Blockbench sources, bounded deterministic exports, distinct Survivor cap/pack and Rifleman helmet/vest models, front/side/top building textures, item polish and clearer menus. No gameplay, schema, balance, pin, version, runtime dependency or protected legacy-ref changes. M9 implementation is unstarted.
- The Survivor prototype was UV/export/build tested and then passed Liam's front/back/side, walking and melee checks before the Rifleman variant was made. Selected F2 references are in `art/references/`. Both models use explicit 64x64 box-UV atlases and vanilla humanoid animation; no clip library is installed.
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-art.ps1 -Check` passed for all five sources. Final `clean build --no-daemon --stacktrace` passed after client exit (7s): 961 progression/foundation/building/sample/NPC/structure +402 art/text assertions = 1,363. The initial socket-restricted attempt failed before Gradle connected; the permitted rerun passed. Normal jar metadata/version/pins, 14 textures and test/source-art exclusion were inspected.
- Two M8 dedicated runs reached Done, accepted status, saved/stopped/exited normally (1m24s and 28s); the first also accepted both NPC summons. Block placement in unavailable chunks refused safely; no loaded-building/menu proof is claimed from those commands. A forced-chunk QA setup was declined and no ticket was added. Prior server world selection/60-second pausing are restored; disposable worlds are retained.
- Liam reported the final combined Rifleman/building/menu/scale check passed and fully quit the client (7m21s, exit 0); the Survivor-only prototype also exited normally. F2 references were inspected and preserved. One blocked-state frame exposed an old substring classifier mistaking prerequisite `parasite_samples` for a sample shortage: the small presentation fix and sample/stage-named prerequisite regressions passed; the changed label itself was not visually retested.
- [Windows CI 39686277770](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39686277770) passed final code `55ac8c1`: five source exports, Java 25 clean build, all 1,363 assertions and normal jar upload. This merge adds final evidence documentation only. See [Art And Identity](art-pipeline.md) for export bounds and visual QA.

## Next milestone

`Continue: M9 steps 9.1-9.4 (equipment roles and unlock gates, anti-threat melee and armour, a simple ranged prototype, and verification) on codex/m9-equipment.`

Prepare the phase branch from the updated `main`. Keep pins/version unchanged, retain optional threat mappings and start with the small equipment set, not reload/ammo systems.

## Needs Liam

- No blocking decision or repetition of the passed art check is needed. At the next client session, select Field Communications before completing Initial Sample Analysis: it should say prerequisite research incomplete, not insufficient samples. A held-stick visual check and an 80-character datapack-title/large-balance hover check remain useful separate coverage; pure bounds tests are not those visual tests.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Art remains intentionally simple alpha art. Blockbench desktop import/save round-trips, clip-driven animation, custom fonts/shaders/resource packs and unusual display scales are separate unverified coverage.
- Building fronts are fixed north; no new facing/collision behavior is implied. Slow NPC acquisition, two-player/network-fuzz, external threat/worldgen mod combinations and packaged/long-load testing remain separate work.
- Structures use conservative one-chunk placement and loaded-only registration cleanup; they are open-air foundations, not finished bases or a full support survey. Cross-file world/chunk saves are not a power-loss journal. Whole-world backups are required.
- Beta-loader and Windows OSHI/Perflib warning risks remain in [Known Issues](../../KNOWN_ISSUES.md). No legacy save conversion or Windows registry edits are made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs; Git branches do not back them up.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
