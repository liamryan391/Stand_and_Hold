# Status

Updated: 2026-10-22 - Branch: `codex/m10-threat-response`

## Where we are

- M1-M9 are verified and merged. M10 is in progress on `codex/m10-threat-response`. Added a separate modern `threat_response` SavedData schema 1 with capped 64x64-block regions, lazy decay, persistent event cooldowns and conservative raider reservations; progression schema 5 is unchanged. No pin/version/dependency/protected-ref changes.
- Foundation `clean build --no-daemon --stacktrace` passed (9s, 2,112 assertions including 62 response checks). Gameplay hooks, event spawning/patrols, runtime evidence, CI and merge are still pending.
- Added synced equip/use gates, ten ordinary recipes/discovery/repair tags and original item/worn art. Recipes/possession are not gated; normal armour equip and weapon use are. Already-worn/admin-edited armour is not forcibly stripped after stage resets.
- Bolts save bounded age/lifetime/damage, stop at unavailable chunks and protect players/humans/allies. Riflemen can receive one non-dropping launcher in an empty hand after unlock, with line of sight, a 12-block firing limit, saved shot timing and existing melee/home/target/deployment safeguards. No ammo/reload or new unit.
- Final post-client `clean build --no-daemon --stacktrace` passed (5s, 2,050 assertions: 1,363 prior +687 equipment). Normal jar metadata matches Minecraft 26.3/NeoForge 26.3.0.57-beta and version 0.1.0-alpha.1; ten recipes are packaged and test classes excluded. [Windows CI 39691907933](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39691907933) passed the final M9 branch tip, including exports/build/jar upload (1m11s). The merge handoff changes only docs, not verified code/assets.
- Two private M9 dedicated launches reached Done, accepted status/research, saved/stopped/exited normally (3m13s, 2m). Restart retained 180 points/Stage 1 and two completed research entries. Constructors accepted the new entity types; subsequent selectors found no loaded entities, so no physical combat/flight proof is claimed from those commands. Prior server selection/60-second pausing are restored; disposable worlds retained.
- Liam reported the combined equipment/gate/combat/crafting/reload check passed, but found an inward held-launcher pose, detached Elite Helmet icon pixels and zombie-only targeting. The focused fixes add explicit held transforms, corrected original icon art and configurable Enemy-or-mapped targeting (no extra rewards). Broader combat includes normally neutral Enemy types such as Endermen; disable `targetHostileMobs` for mapping-only NPC defence. Armour/gun detail remains later art work.
- Liam reported the focused pose/icon/hostile-targeting retest passed. [Unedited F2 references](../../art/references/README.md) show the corrected icon, outward launcher and worn Army set; the client saved and exited normally (2m38s). A third dedicated launch of this code reached Done, retained 180 points/Stage 1/two completed research entries, accepted save/stop and exited normally (15s). Prior world selection/pausing are restored; no user files were deleted.

## Next milestone

`Continue: M10 steps 10.1-10.4 (bounded regional threat state, event hooks and decay, capped outpost attacks/reinforcements and patrol targets, and verification) on codex/m10-threat-response.`

Complete the phase on the prepared branch. Keep pins/version unchanged, threat mods optional and ticking/spawns bounded; retain existing deployment reservations and cooldown safety.

## Needs Liam

- No blocking client check or pin/dependency decision remains. Actual two-client combat, left-main-arm visual coverage, custom enchantment/trim combinations and physical in-flight reload remain separate coverage unless observed.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Art remains intentionally simple alpha art. Blockbench desktop import/save round-trips, clip-driven animation, custom fonts/shaders/resource packs and unusual display scales are separate unverified coverage.
- Building fronts are fixed north; no new facing/collision behavior is implied. Slow NPC acquisition, two-player/network-fuzz, external threat/worldgen mod combinations and packaged/long-load testing remain separate work.
- Structures use conservative one-chunk placement and loaded-only registration cleanup; they are open-air foundations, not finished bases or a full support survey. Cross-file world/chunk saves are not a power-loss journal. Whole-world backups are required.
- Beta-loader and Windows OSHI/Perflib warning risks remain in [Known Issues](../../KNOWN_ISSUES.md). No legacy save conversion or Windows registry edits are made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs; Git branches do not back them up.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
