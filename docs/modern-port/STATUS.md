# Status

Updated: 2026-10-20 · Branch: `main` (M5 client QA follow-up merged)

## Where we are

- M1-M5 implementation is merged; Liam reported the requested M5 integrated checks passed, except that automatic lab completion had no chat feedback. A small M5 follow-up adds a translated server-wide completion notice after the successful lab transaction. Research is global; completed/reloaded/competing labs must not announce again. M6 gameplay remains unstarted. Protected refs, toolchain pins and mod version are unchanged.
- M5 adds an original tissue sample item, bounded optional exact-ID/#tag reward mappings, accepted-death reward guards/player attribution, actual sample storage/withdrawal/destruction recovery, and pickup mission observations. No new runtime dependencies, NPCs or structures. See [Samples And Threats](samples-and-threats.md).
- Final follow-up `clean build --no-daemon --stacktrace` passed after all runtime exits (5s): 50 progression +98 foundation +74 building +41 sample/threat assertions (263 total). Jar metadata and the new completion translation were inspected. Added checks preserve the completed gate across another lab/reload; existing tests cover mapping/transfer/persistence/reward guards.
- First dedicated run reached Done/stopped cleanly: live config reload skipped invalid/NaN entries, missing-mod IDs/tags did not reward pig, exact zombie and bundled-tag rewards worked, chance 0 suppressed samples, chance 1 produced a bounded stack of 64, mob_drops=false suppressed samples but not points, and an already-processed victim granted nothing. Temporary deterministic/all-death overrides were restored to defaults before restart.
- Four dedicated runs reached Done/accepted stop/exited cleanly. Special Division Training resumed saved partial work, spent exactly 100 local supplies/5 samples and rewarded 25 points once. A disposable datapack probe stayed at work 0 with insufficient samples; two ready labs then spent/rewarded only once (winner 0 supplies/0 samples, other 3/2). Repeat completion and another restart kept 5,278 points and the same banks/receipts. The valid probe pack is retained disabled; default mappings, 60-second empty-server pausing and no QA force-load ticket are restored.
- Default attribution rejected an actual console /kill without changing points. Normal crate destruction returned 65 samples as 64+1; another test preserved physical sample recovery despite global supply overflow. Liam's integrated pass covers the requested player kill/pickup/menu checks; no standalone-server player or external threat mod was connected during agent tests.
- Restarted client entered, saved/reopened modern worlds and fully exited normally (`runClient`, BUILD SUCCESSFUL, 16m). Liam reported Mods/sample icon/name, zombie rewards/drops, carry-count mission/repeat protection, all building Store/Take/full-inventory checks, local research consumption, normal breaking and persistence passed, with the missing completion message as the only reported issue. The new notice's actual client display remains a manual retest, not a claimed visual pass.
- Follow-up dedicated QA observed one translated completion notice with two ready labs, one payment/reward (10 points), no duplicate on repeat, and zero notices after restarting/reloading both labs. Completed/competing/blocked banks stayed unchanged on reload. Both interactive runs reached Done, accepted stop and exited cleanly; an initial closed-stdin run saved/exited via a guarded ignored JDK helper, not a forced kill. The disposable world is retained, its QA force-load ticket removed, and the previous server world selection restored.
- [Windows CI 39429897484](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39429897484) passed for follow-up runtime commit `fc41e3e`: Java 25 clean build, all 263 assertions and normal-jar upload. The follow-up merge adds QA documentation only beyond that tested code. `codex/m6-npcs` is brought forward from this main; no M6 gameplay is included.

## Next milestone

`Continue: M6 steps 6.1-6.4 (human NPC registration/stats/eggs, bounded vanilla AI using optional threat mappings, controlled command-post deployment, and verification) on codex/m6-npcs.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking pin/dependency decision. In a fresh disposable modern world, let a lab finish research naturally: chat should show `Research complete: <title>` once, even with the menu closed. A second lab on the same target, further ticks, GUI reopen and save/reload must not repeat it or spend/reward again. Existing worlds keep their completed research and will not replay old notices. Actual two-client delivery/network-fuzz/external-mod/packaged-installation checks remain unverified.
- If a future installed-server test requests EULA acceptance, only Liam can agree. This NeoForge development run skipped that check and did not create `eula.txt`.

## Open risks

- NeoForge 26.3.0.57-beta is a beta loader. If the build fails, follow the fallback rule in
  [building.md](building.md) and ask before changing pins.
- Local client startup logged Windows OSHI/Perflib system-report warnings but continued loading; details are in [KNOWN_ISSUES.md](../../KNOWN_ISSUES.md).
- Recheck untracked/ignored local content before replacing files. Keep `.local/modern-jdk.json`;
  back up valuable worlds/configs outside the checkout. Git branches do not back up ignored files.
- Threat-mod/library release metadata is not combined runtime proof. No extra runtime libraries were added;
  later additions need Liam's approval and the [dependency policy](foundation-scope.md).

Follow [AGENTS.md](../../AGENTS.md) for session workflow. The [roadmap](phase-roadmap.md) owns task checkboxes;
[technical-port-plan.md](technical-port-plan.md) retains design rules, and [archive/](archive/) holds historical notes.
