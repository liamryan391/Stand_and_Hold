# Status

Updated: 2026-10-20 - Branch: `main` (M7 merge handoff)

## Where we are

- M1-M7 are verified and merged. M7 adds original one-chunk Small Army Checkpoint/Main Base foundations, sparse seeded natural placement, admin placement/inspection commands, dormant Main Base activation and loaded-only cleanup. No toolchain/version/runtime dependency/protected legacy-ref changes. See [Structures](structures.md).
- Global modern schema 5 preserves progression, supplies, research/mission receipts, post registrations, defender reservations and unknown fields, adding bounded Main Base registration and shared discovery. A non-spectator's command-post interaction records discovery; generation/listing alone does not. Cleanup never resets historical discovery or mission rewards.
- Liam's initial client check found ordinary grass rejection: Minecraft 26.3 separates `grass_blocks` from `dirt`. Both are now explicitly accepted without weakening fluid/container/occupancy/slope checks, and failures include coordinates. Dedicated grass-over-dirt placement and Liam's corrected integrated structure/menu/activation/removal/save-reload checks passed.
- At Liam's request, each base now saves one dormant and one active notification receipt, shared by viewers. Opening/reloading or toggling back to an already-seen state does not repeat its chat. Liam reported the focused reopening/reload retest passed. The corrected integrated client and focused client both fully exited normally; the earlier failed-test client also exited without a crash.
- Final `clean build --no-daemon --stacktrace` passed after all runtimes exited (5s): 50 progression +98 foundation +107 building +41 sample/threat +51 NPC +610 structure assertions (957). Jar metadata, structure resources, translations and test-class exclusion were inspected; output remains `build/libs/standandhold-0.1.0-alpha.1.jar`.
- Five private dedicated launches reached Done, accepted stop and exited cleanly. Evidence covered debug and actual natural generation, Stage 0 dormancy/Stage 5 generation and deployment, registry/base-flag/storage reload, unloaded preservation, strict-edit stale cleanup, and refusal of disallowed dimensions, unavailable chunks, occupied structures, water, a chest and steep terrain. Natural QA used new flat chunks with explicit biome decoration and temporary dense settings, not only successful datapack loading.
- Natural base registration count remained 19 across restart. A natural crate changed from 5 supplies to 1 remained at 1 after another restart; no reseeding occurred. QA force-load tickets were removed, sparse defaults/deployment toggles and the prior server world/type/settings/60-second pausing restored. Disposable M7 worlds remain; no user files were deleted.
- [Windows CI 39459174705](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39459174705) passed final code commit `fca39a6`: Java 25 clean build, all 957 assertions and normal-jar upload. Earlier M7 foundation CI passed too. This merge adds final evidence documentation only; M8 implementation is unstarted.

## Next milestone

`Continue: M8 steps 8.1-8.5 (visual style guide, Blockbench/export pipeline, original tier and building/item visuals, GUI readability, and verification) on codex/m8-art.`

Keep the pinned toolchain and version `0.1.0-alpha.1`; evaluate animation support before proposing any runtime library, and ask Liam before adding one.

## Needs Liam

- No blocking decision or repeat of the passed M7 checks is required. Two-player shared-notice/network-fuzz, external threat/worldgen mod combinations, packaged installations and large exploration/load tests remain separate unverified coverage.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Strict sparse placement intentionally skips many sites. Default Superflat disables biome decoration; use debug commands there, or explicitly enable features in disposable natural-generation QA.
- These are open-air foundations, not finished bases, deep support surveys or a locate-structure template system. Other mods/later features can modify sites. Registration validates the command tile, not every wall.
- Cross-file saves are not a power-loss journal. Back up whole worlds; partial restores/external edits can strand conservative NPC reservations. Defence mission gameplay remains deferred; discovery rewards retain prerequisites and once-only receipts.
- Beta-loader risk, original placeholder visuals, slow NPC acquisition and Windows OSHI/Perflib warnings remain in [Known Issues](../../KNOWN_ISSUES.md). One long-interruption server timing warning was not treated as a performance benchmark. No Windows registry edits or legacy save conversion were made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs; Git branches do not back them up.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
