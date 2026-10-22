# Status

Updated: 2026-10-22 - Branch: `codex/m10-threat-response`

## Where we are

- M1-M9 are verified and merged. M10 implementation is complete; combined runtime checks, final clean build/jar/CI and merge remain in progress. No toolchain/version/dependency/protected-ref changes.
- Added independent `threat_response` SavedData schema 1: capped 64x64-block regions, lazy score decay, mapped death/kill counters, human losses, outpost attack windows and conservative saved raider reservations/cooldowns. Progression schema 5 is unchanged. See [Threat Response](threat-response.md).
- Loaded operational posts can run bounded vanilla-zombie raids and stage-appropriate reinforcements under existing defender quotas. Added near-home saved patrol orders, active defence-mission kill hooks, operator diagnostics/triggers and one-shot nearby warnings. No new mob type, global tick scan or forced chunk loading.
- Integrated `clean build --no-daemon --stacktrace` passed (7s, 2,250 assertions including 200 response checks). After future-progression guards and pre-spawn cooldown reservation, `build --no-daemon --stacktrace` also passed (6s). Final post-client clean/runtime check remains pending.
- First dedicated M10 run reached Done, handled status/list/reset and invalid region/no-loaded-post errors, saved/stopped/exited normally (1m43s). No loaded actor combat proof is inferred; original server world selection/60-second pause restored and disposable world retained.
- Client initialized Stand and Hold and loaded resources. Logs show one reinforcement and a two-zombie debug raid; Liam's combined combat/quota/reload check is pending. Natural-event frequency, multi-outpost long-load, multiplayer and physical patrol reload remain unverified until observed.

## Next milestone

`Continue: M10 steps 10.1-10.4 (bounded regional threat state, event hooks and decay, capped outpost attacks/reinforcements and patrol targets, and verification) on codex/m10-threat-response.`

Finish verification/publication on this branch. Keep mappings optional and existing quotas/cooldowns conservative.

## Needs Liam

- Report the combined M10 check, any naturally triggered events separately, then fully Quit Game before final clean verification. No pin/dependency decision is needed.
- Only Liam may accept an installed-server EULA if requested. The development run did not request acceptance; no acceptance file was written.

## Open risks

- Full/malformed response storage pauses new sites. Unloaded UUIDs remain reserved and removed-post cooldown history is retained; external edits/partial backups can strand conservative records. Whole-world backups are required; cross-file saves are not a power-loss journal.
- Vanilla raid zombies still burn/despawn normally. Patrols remain near home and may be obstructed; no full squad AI, distant deployments or new threat-mod mechanics are implied.
- Art/equipment remain simple alpha visuals. Left-main-arm, two-client combat, physical in-flight projectile reload, custom fonts/shaders, external threat/worldgen mods and packaged/long-load coverage remain separate work.
- Building fronts are fixed north; structures use conservative one-chunk placement and loaded-only cleanup, not a full terrain survey. See [Known Issues](../../KNOWN_ISSUES.md) for beta-loader/Windows OSHI warnings and retained limitations.
- Preserve `.local/modern-jdk.json`, ignored worlds/configs and the protected legacy branch/tag. No legacy world/config conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
