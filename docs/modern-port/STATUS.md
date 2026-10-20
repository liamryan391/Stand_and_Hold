# Status

Updated: 2026-10-20 - Branch: `main` (M6 merge handoff)

## Where we are

- M1-M6 are verified and merged. M6 adds Survivor Defender/Army Rifleman entities, configurable stats/stage gates, original UV-matched uniforms/test eggs, bounded vanilla melee AI and command-post deployment. No new runtime libraries, toolchain/version changes or protected legacy-ref changes. See [Human NPCs](human-npcs.md).
- Automatic deployment uses saved per-post UUID reservations (including unloaded units), a loaded-area early-exit quota query and bounded safe spawn candidates. Modern global schema 4 retains earlier progression/receipts/registrations/unknown fields. Missing/malformed current quota data refuses deployment rather than forgetting units; unresolved reservations remain conservative.
- Liam reported the M6 integrated checks passed: eggs/uniforms, combat, no player retaliation, stage-selected deployment, caps and save/reopen. He noted slow target detection, retained as later AI polish. At his request the lab now has explicit Start/Pause, separate Next/Clear controls, saved running state, Work/Left values and a progress bar; he reported the focused UI/pause/reload/completion retest passed too. Old modern labs retain work but load paused if the new optional flag is missing. See [Buildings](buildings.md).
- Final `clean build --no-daemon --stacktrace` passed after all runtimes exited (5s): 50 progression +98 foundation +89 building +41 sample/threat +51 NPC assertions (329). Coverage includes quotas/reload/malformed data, stage selection, pause/start/conservation, bar bounds and opaque 64x32 texture-face coverage. Jar metadata/assets/test-class exclusion were inspected; the jar remains `build/libs/standandhold-0.1.0-alpha.1.jar`.
- Six private dedicated `runServer` launches reached Done, accepted stop and exited cleanly. Tests observed distinct 20/30 health, zombie melee damage, stage 0 Survivor deployment, a Stage 1 Rifleman replacement, shared cap 8 with 3/3/2 post groups, unchanged populations after restart and loaded missing-home release. A final edge-case fix excludes the post's own column: grounded control spawned 3; isolated floating post spawned none across multiple attempts.
- Concrete config Loading/Reloading listeners rebuild cached threat rules. Server tests covered empty rules, live tag activation, malformed-entry warnings and missing-mod IDs/tags without pig fallback. A night/fire-cleared test isolated NPC damage: empty rules held zombie health stable, restored defaults resumed attacks. Daylight burning was not treated as proof of NPC targeting.
- Paused lab work/storage survived restart; resuming/competing labs spent/rewarded only once and stopped. Blocked prerequisites did not advance. Both client runs loaded resources, entered/saved/reopened worlds and fully exited normally (7m 43s, 4m 8s); completion chat was observed in the client log. The final column-exclusion correction received the dedicated retest, not an additional visual pass.
- [Windows CI 39437110203](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39437110203) passed final runtime commit `44ec4b3`: Java 25 clean build, all 329 assertions and normal-jar upload. The M6 merge adds final evidence documentation only. M7's branch is prepared from updated main; no M7 gameplay has started.
- The disposable `m6-npcs-qa` world is retained, QA force-load tickets removed, default threat mappings/deployment interval/enable setting restored, and prior server world selection/60-second empty-server pausing restored. No ignored/user worlds or local files were deleted.

## Next milestone

`Continue: M7 steps 7.1-7.5 (bounded structure placement/config, Small Army Checkpoint, Main Base foundation, safe registration/cleanup, and verification) on codex/m7-structures.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking decision or repeat of the passed integrated checks is required. Genuine two-client/network-fuzz, external threat-mod and packaged-installation coverage remains unverified. [NPC QA](human-npcs.md) and [building QA](buildings.md) keep those tests separate from singleplayer/pure checks.
- If a future installed-server test requests EULA acceptance, only Liam can agree. Development runs skipped that check; no acceptance file was written.

## Open risks

- The loader remains beta; follow the [fallback rule](building.md) and ask before changing pins.
- Target discovery can feel slow because acquisition is throttled and requires visibility/reachability. Uniforms and melee-only riflemen remain alpha placeholders; no advanced squad or firearm behavior is implied.
- Quotas/chunk files and world data are not a power-loss journal. Partial restores/external entity edits can strand conservative reservations; back up whole worlds. Never infer death from an unloaded UUID.
- Windows OSHI/Perflib startup warnings continued through successful loads; no Windows registry changes were made. See [known issues](../../KNOWN_ISSUES.md).
- Preserve `.local/modern-jdk.json` and ignored worlds/configs. Branches do not back up ignored files. External-mod/library metadata is not combined runtime proof; later dependencies require approval under [policy](foundation-scope.md).

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes and [technical plan](technical-port-plan.md) retains design rules.
