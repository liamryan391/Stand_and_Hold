# Status

Updated: 2026-10-20 · Branch: `main` (M5 merge handoff)

## Where we are

- M1-M4 are verified and merged; M5 implementation is merged on the automated/server evidence below, with hands-on coverage explicitly unverified. Protected refs, toolchain pins, mod version and ignored user files remain preserved by agent actions.
- M5 adds an original tissue sample item, bounded optional exact-ID/#tag reward mappings, accepted-death reward guards/player attribution, actual sample storage/withdrawal/destruction recovery, and pickup mission observations. No new runtime dependencies, NPCs or structures. See [Samples And Threats](samples-and-threats.md).
- Final `clean build --no-daemon --stacktrace` passed after normal client/server exit (4s): 50 progression +98 foundation +71 building +41 sample/threat assertions. Jar metadata, sample resources/tag and test-class exclusion were inspected. Tests cover malformed/absent mappings, precedence, chance bounds, transfer limits, carry-count mission persistence/repeat/overflow guards.
- First dedicated run reached Done/stopped cleanly: live config reload skipped invalid/NaN entries, missing-mod IDs/tags did not reward pig, exact zombie and bundled-tag rewards worked, chance 0 suppressed samples, chance 1 produced a bounded stack of 64, mob_drops=false suppressed samples but not points, and an already-processed victim granted nothing. Temporary deterministic/all-death overrides were restored to defaults before restart.
- Four dedicated runs reached Done/accepted stop/exited cleanly. Special Division Training resumed saved partial work, spent exactly 100 local supplies/5 samples and rewarded 25 points once. A disposable datapack probe stayed at work 0 with insufficient samples; two ready labs then spent/rewarded only once (winner 0 supplies/0 samples, other 3/2). Repeat completion and another restart kept 5,278 points and the same banks/receipts. The valid probe pack is retained disabled; default mappings, 60-second empty-server pausing and no QA force-load ticket are restored.
- Default attribution rejected an actual console /kill without changing points. Normal crate destruction returned 65 samples as 64+1; another test preserved physical sample recovery despite global supply overflow. No standalone-server player was connected, so the default positive player/projectile path and successful pickup/menu delivery are not claimed as runtime passes.
- Client mod discovery/resources and the development window were observed without missing sample asset errors. No client world was entered and no Liam test result was received. The idle client was closed through its normal window-close request (BUILD SUCCESSFUL, 18m 40s), not killed. Hands-on Mods/name/icon, actual player kill/pickup/partial/full inventory, menu transfer/reload and multiplayer tests remain unverified.
- [Windows CI 39415621033](https://github.com/liamryan391/Stand_and_Hold/actions/runs/39415621033) passed for runtime commit `70211b8`: Java 25 clean build, all 260 assertions and normal-jar upload. M5 merge adds final evidence documentation only. `codex/m6-npcs` starts from this main; no M6 gameplay is included.

## Next milestone

`Continue: M6 steps 6.1-6.4 (human NPC registration/stats/eggs, bounded vanilla AI using optional threat mappings, controlled command-post deployment, and verification) on codex/m6-npcs.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking pin/dependency decision. Run the hands-on [sample QA recipe](samples-and-threats.md) before treating the loop as play-tested: player kills/projectiles, carry 1/2/3 and repeat pickup, actual Store/Take including full inventory, lab consumption, normal breaking and save/reopen. The dev client is closed; use the modern runClient instructions to reopen. Actual two-client/network-fuzz/external-mod/packaged-installation checks are also unverified.
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
