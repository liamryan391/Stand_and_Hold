# Technical Test Candidate

Prepared for controlled testing, **not published or tagged as a modern release**. The reused `0.1.0-alpha.1` string and jar filename do not make this compatible with the historical Forge 1.12.2 alpha tag. Never open a legacy world.

## Exact Target

| Component | Required target |
| --- | --- |
| Minecraft | 26.3 |
| NeoForge | 26.3.0.102 |
| Java runtime | 25, 64-bit |
| Local build | Pinned wrapper Gradle 9.2.1 / ModDevGradle 2.0.148 |
| Normal artifact | `build/libs/standandhold-0.1.0-alpha.1.jar` |
| Extra required mod/library | None |
| External test mods | None for the candidate baseline. See the separate limited Undead Nights result in [Compatibility](compatibility.md). |

Use [Building](building.md) for the pinned Java selector/wrapper, and inspect `META-INF/neoforge.mods.toml`: mod ID `standandhold`, version `0.1.0-alpha.1`, Minecraft `[26.3]`, NeoForge `[26.3.0.102,26.4)`. Do not use a sources jar or the legacy GitHub release. Installer/profile instructions follow the selected loader's official documentation linked in the toolchain/build guides; do not substitute a moving latest loader.

Use [Isolated Packaged Testing](packaged-testing.md) for checksum manifests, fresh launcher/server profiles, EULA boundaries and observed installation outcomes. A GitHub Actions build artifact is separate from a published mod release. Source history identifies its implementation; no new tag is created by preparation.

## Scope Of This Candidate

The [first playable loop](showcase-loop.md) connects existing progression, samples, shared missions, global/local supplies, Command Post/Lab/Crate menus, research, deployment and one bounded attack. Read-only help/guide improve orientation without granting costs or rewards. No new balance, advanced weapons, entities, large structures or runtime libraries are introduced for the showcase.

M11's external gameplay/modded-world removal gate was explicitly deferred by Liam; Contagion is not compatible. Separately, Liam's 2026-10-27 testing-policy decision assigns alpha QA to him and defers independent unfamiliar-tester feedback to beta after a stable alpha. Neither decision passes an unrun test: earned-loop alpha checks, packaged installation and multiplayer proof remain distinct. See [Compatibility](compatibility.md), [the alpha test procedure](showcase-loop.md), [Changelog](../../CHANGELOG.md) and [Known Issues](../../KNOWN_ISSUES.md).

## Test Procedure

1. Back up valuable modern worlds/configs outside the checkout. Use the exact target in a separate profile and a fresh disposable modern world.
2. Run the Java selector and set `.gradle-home/modern` before `clean build --no-daemon --stacktrace`. Inspect normal-jar metadata and absence of bundled external mods/test classes. Verify the relevant CI result separately.
3. For development smoke tests, use `runShowcaseClient` and `runShowcaseServer`; they use `run/showcase-client` and `run/showcase-server`, not normal or M11 compatibility directories. The dedicated console must reach Done, accept status/help/guide/save/stop and exit cleanly. Only Liam accepts an EULA if requested.
4. Development source-run success is not packaged-launcher proof. Test the normal jar in a separate exact-loader client/server installation before broad distribution; do not put a second same-ID jar into a source-run mods directory.
5. Follow the play sheet for about 15 loaded minutes. Record familiarity, startup/menu/world/reload, real sample acquisition, supply order, explicit lab Start/Pause, one defence, unit counts, repeated warnings and command dependence.
6. Retain `latest.log`, crash reports, profiler output and original screenshots/clip. On the pinned target, `/debug stop` emits its elapsed/tick/TPS summary to chat/log; it does not itself create a profile-results file. Preserve that summary. Singleplayer pause time can affect its wall-time average, so run a separate unpaused controlled sample before performance claims. This describes a local integrated server, not real multiplayer or long-session scaling. Record failures and tests not run without promoting them to passes.

The [26.3.0.102 packaged baseline](packaged-testing.md) observed normal-jar Modrinth world entry/reload and production dedicated server save/restart. The M31 rebuild and that installed trial jar have identical archive entries except CRLF/LF line endings in four model JSON files. The installed trial does not prove a fresh launcher install of every rebuild. The older M22 install used the beta pin.

## M31 Integration Fixtures

Run `pwsh -NoProfile -File .\scripts\test-integration-load.ps1` from the repo with the configured Java 25 JDK. The script makes a fresh `run/integration/m31-.../server` with 2 GiB max heap, Normal Superflat, a fixed 4-by-4 loaded chunk grid, no natural mobs and no client. It compares 0/1/2/4 Level-1 checkpoints within the same shared quota area at Stage 2, 65 seconds of warm-up plus five tick queries spaced 5 seconds apart per scenario. Each nonzero scenario attempts one ordinary Stand and Hold raid during sampling. Deployment intervals and all caps stay at their shipped values. The fixture saves native tick output, console counts/reservations and a JFR recording; this is a bounded local smoke test, not a latency/load ceiling for a production server.

Predeclared budgets: each reported mean tick time at most 25 ms, at least four of five reported p95 values at most 35 ms in each scenario, and no defender cap breach (3 reservations per post, 8 loaded defenders within the shared area) or raid cap breach (4 reservations per post, 8 loaded hostiles in range). Record p99 as reported by the game, not inferred from a mean. No packets are expected in the zero-client benchmark; measure network separately. Report actual counts, not assumed saturation.

`pwsh -NoProfile -File .\scripts\test-integration-load.ps1 -PrepareNetwork` prepares a separate private loopback-only offline-auth fixture for distinct development client seats. In each shell select the JDK and Gradle user home as in [Building](building.md), then use `runIntegrationServer -PintegrationId=<session>` and `runIntegrationClient -PintegrationId=<session> -PintegrationSeat=A` (or `B`). Both clients connect to `127.0.0.1:25579`; never use these settings for a shared server. Two actual client JVMs can prove local wire behavior, not authenticated launcher accounts, remote latency or broad multiplayer support.

### Observed Load And Network Results

Windows 11 Pro build 26300, Ryzen 7 9800X3D, 61.6 GiB RAM, Temurin 25.0.4+7, 2 GiB server heap. Session `m31-51e4f3f5055f` met the predeclared budgets. Timings below are the worst *reported* values across five 100-tick windows per scenario; counts are at the end of each window group, not continuous high-water marks. Each nonzero scenario spawned a two-zombie wave; no zombies or raid reservations remained at its final readback.

| Posts | Loaded defenders | Defender reservations by post | Max mean / p95 / p99 (ms) |
| --- | --- | --- | --- |
| 0 | 0 | None | 0.2 / 0.2 / 7.5 |
| 1 | 2 | 2 | 0.8 / 1.7 / 11.1 |
| 2 | 5 | 3 / 2 | 0.5 / 0.8 / 7.6 |
| 4 | 8 | 3 / 3 / 1 / 1 | 0.4 / 0.8 / 1.9 |

`test-integration-load.ps1 -ReviewSession m31-51e4f3f5055f` checked all results. Raw log and JFR are under `run/integration/<session>/server/`; native recording reported zero network packets for the zero-client benchmark. Early harness attempts failed before measuring (working-directory guard, then Windows log-sharing); neither is included in these samples.

Separate session `m31-11e7cd9b0c9f` used two rendered, non-operator clients with real loopback connections. Both opened one Crate: one 10-supply import moved stock 2 to 12 and the other open screen synchronized and refused a second import. The Lab ran once and spent its 10 local supplies; both seats saw completion. The post upgraded once for 20 local supplies; Patrol persisted. One support call spent 10 global supplies, and the other seat received an insufficient-supplies refusal. Operator setup grants are not earned-loop evidence. Native GameTests cover stale/competing transactions separately; these two live clicks were sequential, not a simultaneous packet race.

Seat A went to the Nether and returned across the Overworld chunk edge; Seat B died and respawned. Both rejoined after a dedicated JVM restart. Stage 2, zero global supplies, both completed research entries, Level 2, Patrol, five reservations, Crate 12, Lab work 12, and raid/reinforcement counters survived. The 713-second JFR reported 33,687 sent packets / 9,217,194 bytes and 24,187 received / 66,256 bytes, including chunk traffic; this is not a latency measurement. Logs and the private, operator-triggered 26-second `m31-defence.mp4` are in the session directory. The recording shows a real two-raider wave and defender response, not a staged render or an earned-playthrough claim.

### Candidate Verification And Review

- Java 25 `clean build`, 168 required `runGameTestServer` cases, `test-restart.ps1` (session `qa-782bc4f5`), `runQaClient` (94 captures) and `test-packaged-qa.ps1` passed. Normal jar SHA-256 on this checkout: `b85549498b7266f0f7e5d4f902742abf03862c3d74fc35cbc9d719f9df8c8140`. Metadata requires Minecraft 26.3 and NeoForge `[26.3.0.102,26.4)`; no QA classes, bundled jar or external mod content was found.
- Reviewed the 1280x720 `checkpoint-with-lab`, `yard-overhead`, `deck-built-overhead` and `lab-completed-large` PNGs with prefix `3bd4aeef-8bc1-43b8-a330-77a20b416041-` in `run/qa-client/screenshots/`. These are in-world QA fixture views, including the Level-2-to-Level-3 before/after pair. Reviewed Seat B's network captures `2026-11-11_19.34.45.png`, `19.36.55` and `19.37.48`, and frames from the unedited defence clip. The first-use vanilla tutorial toast overlapped the edge of a compact menu; ordinary QA captures were clear.
- Keep the verified candidate for internal QA only. The [merged Windows CI run](https://github.com/liamryan391/Stand_and_Hold/actions/runs/43092501245) passed. This is not a stable alpha or public release decision.
- On 2026-11-12 Liam reported that everything worked as intended and raised no new issues. The separate `M31 Earned Review` save started at zero points, Stage 0 and zero global supplies, with natural site generation off and experimental growth enabled. Its log shows checkpoint placement, guide output and a clean save/exit; the last recorded guide still showed the recovery mission unstarted. This is positive maintainer feedback and a smoke check, not evidence of a complete [grant-free playthrough](showcase-loop.md#m31-maintainer-review) or a pacing measurement. The earlier exploration save was preserved separately.
- No authenticated remote session, fresh install of this rebuild, public distribution or new tag is claimed.

## Release-Note Draft

Stand and Hold's modern technical foundation has a small guided alpha loop: sample recovery, a field command/lab setup, funded research, human defenders and an outpost attack, with saved world/building progress. Defaults remain testing-focused. Art, structures and weapons are still simple, and infrastructure/mission starts in the demonstration are operator-assisted. No complete Survival campaign, verified external infection support, vehicle system, cross-loader build or broad multiplayer readiness is promised.

Actual publishing requires a separate approval, release/version/tag decision, checked artifact, supported-combination statement and release checklist. The preserved `1.12` branch and `v0.1.0-alpha.1` tag must not be moved or reused.
