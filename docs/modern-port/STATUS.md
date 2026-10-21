# Status

Updated: 2026-10-21 - Branch: `codex/m9-equipment`

## Where we are

- M1-M8 are verified and merged. M9 is in progress on `codex/m9-equipment`: an Anti-Threat Blade, Army/Elite armour and a cooldown-based launcher with server-owned unlocks and safe Rifleman use. See [Equipment](equipment.md). No pin, version, runtime dependency, global schema or protected legacy-ref changes.
- Added synced equip/use gates, ten ordinary recipes/discovery/repair tags and original item/worn art. Recipes/possession are not gated; normal armour equip and weapon use are. Already-worn/admin-edited armour is not forcibly stripped after stage resets.
- Bolts save bounded age/lifetime/damage, stop at unavailable chunks and protect players/humans/allies. Riflemen can receive one non-dropping launcher in an empty hand after unlock, with line of sight, a 12-block firing limit, saved shot timing and existing melee/home/target/deployment safeguards. No ammo/reload or new unit.
- Initial `clean build --no-daemon --stacktrace` passed (6s, 1,932 assertions). After final guards/tags/discovery, `build --no-daemon --stacktrace` passed (7s, 1,938: 1,363 prior +575 equipment). Final clean verification, jar inspection and remote M9 CI remain pending.
- Two private M9 dedicated launches reached Done, accepted status/research, saved/stopped/exited normally (3m13s, 2m). Restart retained 180 points/Stage 1 and two completed research entries. Constructors accepted the new entity types; subsequent selectors found no loaded entities, so no physical combat/flight proof is claimed from those commands. Prior server selection/60-second pausing are restored; disposable worlds retained.
- Client resources loaded with the mod listed; Liam's combined equipment/gate/combat/crafting/reload check is in progress. Army armour F2 front/back frames were inspected with a visible face and no missing texture. Final client exit, clean build, CI and merge are unverified.

## Next milestone

`Continue: M9 steps 9.1-9.4 (equipment roles and unlock gates, anti-threat melee and armour, a simple ranged prototype, and verification) on codex/m9-equipment.`

Complete the phase on the prepared branch. Keep pins/version unchanged, retain optional threat mappings and defer reload/ammo systems.

## Needs Liam

- Report the combined M9 check and fully Quit Game before final clean verification. No pin/dependency decision is needed. Actual two-client combat, custom enchantment/trim combinations and physical in-flight reload remain separate coverage unless observed.
- Only Liam may accept an installed-server EULA if requested; development runs skipped that check and no acceptance file was written.

## Open risks

- Art remains intentionally simple alpha art. Blockbench desktop import/save round-trips, clip-driven animation, custom fonts/shaders/resource packs and unusual display scales are separate unverified coverage.
- Building fronts are fixed north; no new facing/collision behavior is implied. Slow NPC acquisition, two-player/network-fuzz, external threat/worldgen mod combinations and packaged/long-load testing remain separate work.
- Structures use conservative one-chunk placement and loaded-only registration cleanup; they are open-air foundations, not finished bases or a full support survey. Cross-file world/chunk saves are not a power-loss journal. Whole-world backups are required.
- Beta-loader and Windows OSHI/Perflib warning risks remain in [Known Issues](../../KNOWN_ISSUES.md). No legacy save conversion or Windows registry edits are made.
- Preserve `.local/modern-jdk.json` and ignored worlds/configs; Git branches do not back them up.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
