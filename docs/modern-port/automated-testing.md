# Automated Testing

The QA mod uses only APIs and libraries already shipped by the pinned Minecraft/NeoForge platform. `src/qa/` is a separate source set and mod; normal client/server runs load only Stand and Hold. `build` compiles QA and checks the production jar contains neither QA classes/templates nor its mod metadata.

## Run The Ladder

In PowerShell at the repository root:

```powershell
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat clean build --no-daemon --stacktrace
.\gradlew.bat runGameTestServer --no-daemon --stacktrace
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-restart.ps1
.\gradlew.bat runQaClient --no-daemon --stacktrace
```

Do not run multiple QA instances against the same run directory concurrently. No normal player world or configuration is reused. All disposable worlds/results are retained; cleanup is a separate maintainer decision, not part of these commands. None of the scripts accepts an EULA.

## In-World Server Tests

`runGameTestServer` is the built-in headless test server, not a simulated logic check. Required test failures propagate as nonzero process/Gradle exits. Each invocation gets a fresh UUID universe because vanilla's test launcher resets its universe directory. Never replace this with an existing world path.

The original 64-block grass/dirt arena template is generated with the platform NBT writer. Distinct test environments serialize shared SavedData mutations; cleanup touches only each test arena and its synthetic players. Open sky is necessary for real structure heightmaps; an artificial test ceiling would test the harness instead of superflat terrain.

Coverage includes checkpoint/Main Base generation and occupied-container preservation; real raid entities, reservations, quota/terrain messages; real defender AI damaging zombies/skeletons/creepers and protecting players/animals; blade issue/retention; timed lab cost/reward/message once; base notice receipts; real player-attributed deaths, drops and pickup mission rewards. Headless synthetic players capture messages but deliberately do not negotiate graphical menus; `qaClient` covers real menu transport separately.
M13 adds actual non-op support-command aiming/unlocks/payment, two-player shared-bank contention, cooldown refund, post/area caps, unsupported terrain and dormant/stale/distant/spectator rejection. Four synthetic players also send 80 serialized requests across four posts: exactly four units/debits, no extra loaded chunks. These are server-player actions, not a network-latency or sustained-load benchmark. XML must contain every declared test exactly once, with no failures/skips, even if the native process exits zero.

Reports: `run/game-tests/reports/<uuid>.xml`, plus `run/game-tests/logs/latest.log`. CI runs these and the restart fixture after the normal build, retaining reports even on failure. GameTests work on the pinned beta; the scripted-fixture fallback is not needed for their in-world cases.

## Actual Process Restart

`test-restart.ps1` starts two dedicated JVMs using the same newly named QA world, each reaching server readiness and stopping itself. The first writes actual SavedData, blocks and a live issued-blade NPC. The second verifies progression/supplies, research receipt, partial mission, paused lab target/work/storage, crate inventory, site registration, Main Base notice bits, the same NPC/blade at Stage 0, and actual raid reservation/cooldown/counters. A result is mandatory; crashes or missing/failed result files fail the Gradle task.
The fixture also pays for one real M13 reinforcement and checks its exact supply debit, same defender UUID/home, roster and reinforcement cooldown/counter in the second JVM. Server shutdown uses the vanilla `stop` command after writing each result.

Only this isolated fixture force-loads its small arena to test real entity chunk persistence. Production code and cleanup never gain chunk-loading privileges. The fixture binds loopback port 25578, not a public server. Results: `run/qa-server/qa-<uuid>-{write,read,receipt}.properties` and preserved world `qa-<uuid>/`.

## Screenshots

`runQaClient` self-creates a fresh Creative flat world, sets up original buildings/NPCs, opens real server menus, checks synced menu state, captures the scene, Command Post, Crate, Lab states at normal/large scales and `/standandhold guide`, then saves and quits. Screenshot IO completes before shutdown. A timeout or incomplete result fails the task.
It also sends the support command from the real client and verifies one deployment/debit on the integrated server before capturing the feedback.

Open **every** PNG named in `run/qa-client/<uuid>-result.properties`, under `screenshots/`. Judge text fit, textures, visible state and progress bar yourself; a successful exit is not visual approval. Synthetic grants are fixtures, never proof of an earned-resource playthrough. These are deterministic views, not motion/fun or real multiplayer proof. Test a packaged launcher separately; it must not list `standandhold_qa`.

Observed first baseline (2026-10-27): 15 required GameTests passed, two dedicated JVMs verified persisted state, normal jar metadata/isolation passed, and all nine `90e73aa0-1e1d-48af-a0a2-f29c25dd8875-*.png` views were opened and reviewed. Labels/buttons fit, ready/running/paused/blocked/completed states and the completed bar were clear; the large-scale guide retained its first/last lines. A vanilla tutorial toast appears in several captures without covering the tested values. Initial harness-only failures (artificial roof, exposed test floor, unnegotiated headless menu and unsynced player teleport) were corrected without relaxing gameplay assertions; the original failed reports/worlds remain.

M13 local verification: 22 required tests passed; the support-inclusive two-JVM fixture passed; all ten `33c9bba4-b76a-4a9f-bfeb-8fba65a18522-*.png` views were opened/reviewed, including `-support-call-in.png`, `-lab-completed-large.png` and `-guide-large.png`. The real client command deployed/charged once and its wrapped feedback was readable. Tests exposed a mismatched body/head look ray in the new chunk guard; both guard and pick now use the head/view vector. The headless fixture sets its head rotation explicitly; its restart call waits for the real post-registration hook.

M15 adds seven required cases: an actual menu upgrade followed by automatic/event deployment; successful paid use of an upgraded slot; adjacent posts sharing the loaded-area cap; legacy ceiling/zero/master-disable/invalid-profile behavior; retained unloaded and over-cap reservations; stage/dormancy/stale-tile gates; and natural reinforcement using upgraded capacity. Config overrides are QA-only, restored on completion/failure and never saved to the user's config. The original Level-1 and egg-area-cap cases remain unchanged.

Observed M15 verification (2026-10-28): clean build passed with 7,910 logic assertions and 29 required GameTests. Two real JVMs retained a level-2 post with unchanged local storage, five identical defender UUIDs/homes and existing cooldown/payment/research/mission/notice state; a lower post ceiling refused deployment without deleting excess reservations. All 14 `46dbb4e2-e03f-4bcd-b744-cc112e84921e-*.png` captures were opened/reviewed, including `-command-post-upgraded-large.png`, `-command-post-full-large.png`, `-command-post-disabled.png` and `-command-post-dormant-large.png`. The 300x230 menu fits at normal/large scales, with readable counts/blocked reasons and no overlapping controls; lab/guide/support regressions remained readable. No new runtime dependency, global scan, saved-data schema or physical structure growth was introduced. Real multiplayer/packaged installs and perceived force size remain separate checks.
