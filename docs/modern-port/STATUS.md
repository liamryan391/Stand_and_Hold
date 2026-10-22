# Status

Updated: 2026-10-22 - Branch: `codex/m10-threat-response`

## Where we are

- M1-M9 are verified and merged. M10 runtime checks passed; the final blade-retention follow-up is built and awaiting publication/CI before merge. No toolchain/version/dependency/protected-ref changes.
- Added independent `threat_response` SavedData schema 1: capped 64x64-block regions, lazy score decay, mapped death/kill counters, human losses, outpost attack windows and conservative saved raider reservations/cooldowns. Progression schema 5 is unchanged. See [Threat Response](threat-response.md).
- Loaded operational posts can run bounded vanilla-zombie raids and stage-appropriate reinforcements under existing defender quotas. Added near-home saved patrol orders, active defence-mission kill hooks, operator diagnostics/triggers and one-shot nearby warnings. No new mob type, global tick scan or forced chunk loading.
- Latest `clean build --no-daemon --stacktrace` passed (8s, 2,337 assertions: 203 response and 771 equipment). Jar metadata confirms the unchanged version/pins and no packaged test classes. Windows CI 39820976964 passed all-mob player hits/Survivor blade issue; the newer retention follow-up needs its own CI.
- First dedicated M10 run reached Done, handled status/list/reset and invalid region/no-loaded-post errors, saved/stopped/exited normally (1m43s). No loaded actor combat proof is inferred; original server world selection/60-second pause restored and disposable world retained.
- Liam reported combined combat/quota/reload/reset/dormant-base checks passed; the first client saved/exited normally (10m43s). Reviewed F2 frames show a two-zombie raid/combat and reset retaining cooldowns. On feedback, natural grace is now separate from first admin use, and default player equipment access is free while NPC gates remain mandatory; older M10 cooldowns/quotas are retained.
- Liam passed the grace/free-player-equipment and fast-natural check; client saved/exited normally (9m06s). Logs independently show natural raid/reinforcement warnings without admin trigger replies. All eight temporary development config values are restored to their originals; `.local/m10-response-before-fast-qa-20261022-001.toml` is retained, no user files deleted. Repository event defaults never changed.
- Liam passed the final all-mob player hits and Survivor Blade issue/save check; the third client saved/exited normally (6m59s). A third dedicated run reached Done/save/stop/clean exit (48s). On his latest feedback, already-issued blades now remain usable after stage drops; new/unarmed units still need the gate. This final policy change has automated coverage but no new manual retention/reload check. Long-load/multiplayer/physical patrol reload remain separate unverified coverage.

## Next milestone

`Continue: M10 steps 10.1-10.4 (bounded regional threat state, event hooks and decay, capped outpost attacks/reinforcements and patrol targets, and verification) on codex/m10-threat-response.`

Finish verification/publication on this branch. Keep mappings optional and existing quotas/cooldowns conservative.

## Needs Liam

- No blocking decision. Optional focused retest: issue a Survivor blade at Stage 1 + Initial Sample Analysis, reset to Stage 0, spawn a new Survivor and save/reopen. The old unit should retain its usable blade; the new unit should stay unarmed.
- Only Liam may accept an installed-server EULA if requested. The development run did not request acceptance; no acceptance file was written.

## Open risks

- Full/malformed response storage pauses new sites. Unloaded UUIDs remain reserved and removed-post cooldown history is retained; external edits/partial backups can strand conservative records. Whole-world backups are required; cross-file saves are not a power-loss journal.
- Vanilla raid zombies still burn/despawn normally. Patrols remain near home and may be obstructed; no full squad AI, distant deployments or new threat-mod mechanics are implied.
- Art/equipment remain simple alpha visuals. Left-main-arm, two-client combat, physical in-flight projectile reload, custom fonts/shaders, external threat/worldgen mods and packaged/long-load coverage remain separate work.
- Building fronts are fixed north; structures use conservative one-chunk placement and loaded-only cleanup, not a full terrain survey. See [Known Issues](../../KNOWN_ISSUES.md) for beta-loader/Windows OSHI warnings and retained limitations.
- Preserve `.local/modern-jdk.json`, ignored worlds/configs and the protected legacy branch/tag. No legacy world/config conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
