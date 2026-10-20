# Status

Updated: 2026-10-20 - Branch: `codex/m7-structures`

## Where we are

- M1-M6 are verified and merged. M7 implements original one-chunk Checkpoint/Main Base foundations, sparse seeded natural placement, terrain/dimension/border/occupancy preflight, admin placement/inspection, dormant Main Base activation and loaded-only cleanup. No pins, version, runtime dependencies or protected legacy refs changed. See [Structures](structures.md).
- Global modern schema 5 preserves earlier progression, research/mission receipts, post registrations, defender reservations and unknown fields, adding bounded Main Base registration and a shared discovery receipt. Right-clicking a registered base command post records discovery; generation/listing alone does not. Cleanup never resets discovery/completed missions.
- Initial `clean build --no-daemon --stacktrace` passed with 939 assertions (610 structure, 329 existing), but Liam's first client check exposed grass-ground rejection. The pinned 26.3 `dirt` tag excludes the separate `grass_blocks` tag; the whitelist now accepts both. A clean rebuild passed, and actual grass-over-dirt dedicated placement succeeded for both sites with correct registrations and 2/5-supply crates. The failure was not treated as a visual pass.
- Dedicated tests observed Stage 0 dormancy versus Stage 5 generation/deployment, save/reload of the base flag/storage, preservation of unloaded records and removal of loaded stale base/post records after strict admin edits. Disallowed dimensions, unavailable chunks, water, a chest, occupied structures and steep terrain were refused. Natural checkpoint and Main Base generation were observed in new flat chunks with explicit biome decoration and temporary dense settings, not inferred from JSON loading.
- Normal jar metadata/resources were inspected: `0.1.0-alpha.1`, Minecraft 26.3 and NeoForge 26.3.0.57-beta; no test classes or extra dependency were packaged. Final clean verification, publication/CI and the corrected client retest are still in progress.
- The interrupted dedicated server was stopped normally; no user files/worlds were deleted. Disposable M7 worlds remain separate. Dense generation and deployment toggles have been restored to defaults; the final save/reload test and prior server-world selection restoration remain to finish.

## Next milestone

`Continue: M7 steps 7.1-7.5 (bounded structure placement/config, Small Army Checkpoint, Main Base foundation, safe registration/cleanup, and verification) on codex/m7-structures.`

## Needs Liam

- The fixed client is open for the combined structure/menu/activation/save-reload/removal retest. Initial grass rejection is fixed in code and verified on a dedicated server, not yet recorded as a client pass.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Sparse natural generation intentionally skips unsuitable terrain. Default Superflat disables biome features; use debug placement there, or explicitly enable decoration in disposable natural-generation QA.
- These are open-air foundations, not finished bases, deep support surveys or a full locate-structure system. Other mods/later features can modify a site. Registration validates the command tile, not every perimeter block.
- Cross-file saves are not a power-loss journal. Back up entire worlds; partial restores/external edits can strand conservative NPC reservations. Two-client/network-fuzz, external threat-mod and packaged-installation coverage remains separate and unverified.
- Beta-loader risk, original placeholder visuals/slow target acquisition and Windows OSHI/Perflib warnings remain documented in [Known Issues](../../KNOWN_ISSUES.md). No Windows registry changes or legacy world conversion were made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs. Git branches do not back up ignored files.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
