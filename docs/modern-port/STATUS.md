# Status

Updated: 2026-10-21 - Branch: `codex/m8-art`

## Where we are

- M1-M7 are verified and merged. M8 adds an original visual contract, five editable Blockbench sources, bounded deterministic exports, distinct Survivor cap/pack and Rifleman helmet/vest models, front/side/top building textures, item polish and clearer menus. No gameplay, schema, balance, pin, version, runtime dependency or protected legacy-ref changes.
- The Survivor prototype was UV/export/build tested and then passed Liam's front/back/side, walking and melee checks before the Rifleman variant was made. Selected F2 references are in `art/references/`. Both models use explicit 64x64 box-UV atlases and vanilla humanoid animation; no clip library is installed.
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-art.ps1 -Check` passed for all five sources. `clean build --no-daemon --stacktrace` passed (7s): 957 existing +402 art/text assertions = 1,359. The initial socket-restricted attempt failed before Gradle connected; the permitted rerun passed. Normal jar metadata/version/pins, 14 textures and test/source-art exclusion were inspected.
- The M8 dedicated server reached Done, accepted status and both NPC summons, saved and stopped normally (1m24s). Block placement in unavailable chunks refused safely; no loaded-building/menu proof is claimed from those commands. A forced-chunk QA setup was declined and no ticket was added. Prior server world selection/60-second pausing are restored; the disposable M8 world is retained.
- Final M8 client resources loaded with Stand and Hold in the mod list. Liam's combined Rifleman/building/menu/scale check is in progress; final clean build, remote CI and merge remain unverified. See [Art And Identity](art-pipeline.md) for source/export bounds and visual QA.

## Next milestone

`Continue: M8 steps 8.1-8.5 (visual style guide, Blockbench/export pipeline, original tier and building/item visuals, GUI readability, and verification) on codex/m8-art.`

Finish the integrated art check, clean verification and green CI before merging. Do not repeat the passed Survivor-only prototype check unless a relevant change is made.

## Needs Liam

- Report the combined final art/menu check and fully Quit Game before the final clean build. No pin/dependency decision is needed.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Art remains intentionally simple alpha art. Blockbench desktop import/save round-trips, clip-driven animation, custom fonts/shaders/resource packs and unusual display scales are separate unverified coverage.
- Building fronts are fixed north; no new facing/collision behavior is implied. Slow NPC acquisition, two-player/network-fuzz, external threat/worldgen mod combinations and packaged/long-load testing remain separate work.
- Structures use conservative one-chunk placement and loaded-only registration cleanup; they are open-air foundations, not finished bases or a full support survey. Cross-file world/chunk saves are not a power-loss journal. Whole-world backups are required.
- Beta-loader and Windows OSHI/Perflib warning risks remain in [Known Issues](../../KNOWN_ISSUES.md). No legacy save conversion or Windows registry edits are made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs; Git branches do not back them up.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
