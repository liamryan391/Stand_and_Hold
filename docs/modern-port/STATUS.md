# Status

Updated: 2026-10-20 - Branch: `codex/m7-structures`

## Where we are

- M1-M6 are verified and merged. M7 implements original one-chunk Checkpoint/Main Base foundations, sparse seeded natural placement, terrain/dimension/border/occupancy preflight, admin placement/inspection, dormant Main Base activation and loaded-only cleanup. No pins, version, runtime dependencies or protected legacy refs changed. See [Structures](structures.md).
- Global modern schema 5 preserves earlier progression, research/mission receipts, post registrations, defender reservations and unknown fields, adding bounded Main Base registration and a shared discovery receipt. Right-clicking a registered base command post records discovery; generation/listing alone does not. Cleanup never resets discovery/completed missions.
- Initial `clean build --no-daemon --stacktrace` passed with 939 assertions (610 structure, 329 existing), but Liam's first client check exposed grass-ground rejection. The pinned 26.3 `dirt` tag excludes the separate `grass_blocks` tag; the whitelist now accepts both. A clean rebuild passed, and actual grass-over-dirt dedicated placement succeeded for both sites with correct registrations and 2/5-supply crates. The failure was not treated as a visual pass.
- Dedicated tests observed Stage 0 dormancy versus Stage 5 generation/deployment, save/reload of the base flag/storage, preservation of unloaded records and removal of loaded stale base/post records after strict admin edits. Disallowed dimensions, unavailable chunks, water, a chest, occupied structures and steep terrain were refused. Natural checkpoint and Main Base generation were observed in new flat chunks with explicit biome decoration and temporary dense settings, not inferred from JSON loading.
- Normal jar metadata/resources were inspected: `0.1.0-alpha.1`, Minecraft 26.3 and NeoForge 26.3.0.57-beta; no test classes or extra dependency were packaged. [Windows CI 39458559999](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39458559999) passed initial M7 commit `2d23561`, including clean build and jar upload. Liam reported the corrected structure/menu/activation/removal/save-reload checks passed.
- Liam requested removing repeated base-state chat on every opening. Added saved per-base dormant/active notification receipts shared by viewers; each state is announced once and stays claimed after reload. Clean build passed with 957 assertions (107 building, 610 structure, 240 other). A short focused client check and follow-up CI are pending; the rest of the M7 checks need not repeat.
- All five dedicated runs reached Done, accepted stop and exited cleanly, including the interrupted server after credit recovery. Natural base count remained 19 after restart; a crate changed from 5 to 1 remained at 1 after another restart. QA tickets were removed and sparse generation/deployment/prior world selection restored; disposable worlds were retained and no user files deleted.

## Next milestone

`Continue: M7 steps 7.1-7.5 (bounded structure placement/config, Small Army Checkpoint, Main Base foundation, safe registration/cleanup, and verification) on codex/m7-structures.`

## Needs Liam

- A short focused client check verifies one dormant/active notice per base, no repeats on reopening or save/reload. The corrected integrated structure checks are already reported passed.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Sparse natural generation intentionally skips unsuitable terrain. Default Superflat disables biome features; use debug placement there, or explicitly enable decoration in disposable natural-generation QA.
- These are open-air foundations, not finished bases, deep support surveys or a full locate-structure system. Other mods/later features can modify a site. Registration validates the command tile, not every perimeter block.
- Cross-file saves are not a power-loss journal. Back up entire worlds; partial restores/external edits can strand conservative NPC reservations. Two-client/network-fuzz, external threat-mod and packaged-installation coverage remains separate and unverified.
- Beta-loader risk, original placeholder visuals/slow target acquisition and Windows OSHI/Perflib warnings remain documented in [Known Issues](../../KNOWN_ISSUES.md). No Windows registry changes or legacy world conversion were made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs. Git branches do not back up ignored files.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
