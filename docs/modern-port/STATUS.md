# Status

Updated: 2026-10-22 - Branch: `codex/m11-compatibility` (prepared after the M10 merge)

## Where we are

- M1-M10 have passed their phase exit gates and are merged. M11 is not started. No toolchain/version/runtime dependency/protected-ref changes.
- M10 adds independent `threat_response` SavedData schema 1: bounded 64x64-block regions, lazy decay, death/kill/loss/attack counters and saved event cooldowns/raider reservations. Progression schema 5 is unchanged. See [Threat Response](threat-response.md).
- Loaded operational posts run capped vanilla-zombie raids and shared-quota reinforcements, with saved near-home patrols, defence-mission kill hooks and operator diagnostics/triggers. No new mob type, global per-tick scan or forced chunk loading.
- Final code `clean build --no-daemon --stacktrace` passed (8s, 2,337 assertions: 203 response and 771 equipment). Jar metadata/pins/test exclusion inspected. [Windows CI 39823522409](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39823522409) passed the final retention fix and uploaded the normal jar (1m42s).
- Three dedicated launches reached Done, saved, accepted stop and exited cleanly; command/invalid-region/no-loaded-post checks passed. Those console checks are not loaded-actor combat proof. Original server world selection/pause settings are restored and the disposable world is retained.
- Liam passed combined threat/combat/caps/cooldown/reload/dormant-base checks, fast-natural/grace/free-player-gear checks and the final all-mob player-hit/Survivor blade-issue checks. All three clients saved/exited normally. Logs independently show natural raid/reinforcement warnings. Eight temporary development config values are restored; backup/worlds are retained.
- On his latest feedback, already-issued Survivor blades retain use after stage drops, while new/unarmed units still require Stage 1 + Initial Sample Analysis. Default raw damage is 8 (12 against mapped threats, before mitigation). This last retention policy has automated coverage but no new manual retention/reload test.

## Next milestone

`Continue: M11 steps 11.1-11.4 (verify pinned-target threat mods, optional mapping presets/adapters, with/without-mod testing, and supported-combination documentation) on codex/m11-compatibility.`

Verify exact compatible artifacts before proposing any dependency or pin change. Threat integrations remain optional.

## Needs Liam

- No blocking decision. Optional focused retest: issue a Survivor blade at Stage 1 + Initial Sample Analysis, reset to Stage 0, spawn a new Survivor and save/reopen. The old unit should retain its usable blade; the new unit should stay unarmed.
- Only Liam may accept an installed-server EULA if requested. No EULA acceptance file was written during M10.

## Open risks

- Full/malformed response storage pauses new sites. Unloaded UUIDs remain reserved and removed-post cooldown history is retained; external edits/partial backups can strand conservative records. Whole-world backups are required; cross-file saves are not a power-loss journal.
- Vanilla raid zombies still burn/despawn normally. Patrols remain near home and may be obstructed. Long-session multi-outpost counts, physical patrol reload and real multiplayer remain unverified; short capped tests are not soak-test proof.
- Player weapons can hurt human NPCs/passive mobs; players/allied teams stay protected and NPC targeting remains separate. Art/equipment remain alpha visuals. Left-main-arm, in-flight projectile reload, custom fonts/shaders, external mods and packaged/long-load coverage remain separate work.
- Building fronts are fixed north; structures use conservative one-chunk placement and loaded-only cleanup. See [Known Issues](../../KNOWN_ISSUES.md) for beta-loader/Windows OSHI/minimized-window warnings and retained limitations.
- Preserve `.local/modern-jdk.json`, ignored worlds/configs and the protected legacy branch/tag. No legacy world/config conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
