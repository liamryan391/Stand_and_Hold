# Status

Updated: 2026-10-20 · Branch: `codex/m5-samples`

## Where we are

- M1-M4 are verified and merged; [M4 Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38719872847) passed. M5 starts from that main. Protected refs, toolchain pins, mod version and ignored user files remain preserved.
- M5 adds an original tissue sample item, bounded optional exact-ID/#tag reward mappings, accepted-death reward guards/player attribution, actual sample storage/withdrawal/destruction recovery, and pickup mission observations. No new runtime dependencies, NPCs or structures. See [Samples And Threats](samples-and-threats.md).
- `clean build --no-daemon --stacktrace` passed (7s): 50 progression +98 foundation +71 building +41 sample/threat assertions. Jar metadata, sample resources/tag and test-class exclusion were inspected. Tests cover malformed/absent mappings, precedence, chance bounds, transfer limits, carry-count mission persistence/repeat/overflow guards.
- First dedicated run reached Done/stopped cleanly: live config reload skipped invalid/NaN entries, missing-mod IDs/tags did not reward pig, exact zombie and bundled-tag rewards worked, chance 0 suppressed samples, chance 1 produced a bounded stack of 64, mob_drops=false suppressed samples but not points, and an already-processed victim granted nothing. Temporary deterministic/all-death overrides were restored to defaults before restart.
- A disposable M5 lab save holds Stage 5 prerequisites, 100 local supplies/5 samples and partial work. Restart/consumption/reward-repeat/destruction tests are underway; empty-server pausing is temporarily disabled in ignored server.properties for loaded-time tests only. Previous test worlds were not replaced/deleted.
- M5 client mod discovery/resource loading completed without missing sample assets in logs; Liam's combined pickup/menu/inventory/lab/save-reload checks, final clean and remote CI/publication are pending. Actual external-mod, two-client and packet-fuzz tests are not claimed.

## Next milestone

`Continue: M5 steps 5.1-5.4 (sample items, optional threat identification/rewards, pickup missions and lab sample consumption, verification, and mapping documentation) on codex/m5-samples.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking pin/dependency decision. The M5 client is open for Liam's sample pickup, mission, menu transfer/full inventory, destruction recovery and sample-funded research checks. Fully quit after testing for the final clean build. Actual two-client/network-fuzz/external-mod/packaged-installation checks remain separate unverified coverage.
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
