# Status

Updated: 2026-10-14 · Branch: `codex/m2-progression`

## Where we are

- M1 is complete: pinned MDK scaffold, identity, minimal logger/config shell, isolated runs, Java 25 CI, and observed local build/client/server verification. The scaffold is merged into `main`; the preserved `1.12` branch/tag are unchanged.
- GitHub [Build run 38632437020](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38632437020) passed for `836c31197e83e6f809aa7859e59fb37d0f0605dd`, including the Windows Java 25 build and normal-jar upload. The new AGENTS.md batching policy is committed separately.
- M2 definitions, versioned global SavedData, manager, synced threshold config, and public status/operator mutation commands are implemented. No automatic point sources or other gameplay systems were added. See [Progression](progression.md) for the save/config/command contract.
- M2 `clean build --no-daemon --stacktrace` passed under the pinned Java 25 with 50 rules/codec assertions. Plain-JVM command tests require a live loader, so actual Brigadier integration is checked in the development server instead.
- Dedicated-server testing reached Done, exercised all stages, invalid/missing arguments, long overflow, and Nether/End global state, then accepted stop cleanly. A full restart restored 175/Stage 1; read-only status plus save did not rewrite the progression file. Custom thresholds recomputed stage without changing points; an invalid list safely reset to defaults. Test world: `run/modern-server/m2-progression-qa`; final config is back to defaults.
- M2 client discovered/initialized the mod and completed resource loading. Liam reported the integrated visual/command/save-reload/invalid-input checks passed; logs show 0 -> 149 -> 150/Stage 1 -> 500/Stage 2 and world saving. M2 runtime exit evidence is complete; CI/publication remains to be observed. A later final clean was blocked by the open client's Windows jar lock; non-clean build restored outputs and passed all 50 assertions.

## Next milestone

`Continue: M3 steps 3.1-3.4 (research definitions, supply/research transactions, persistent missions, and verification).`
M2 publication must first observe green CI before merging to main.
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- Liam reported the M2 singleplayer checks passed. Quit the client normally to release the Windows jar lock. A private multiplayer non-operator check remains: status allowed, addpoints/setstage denied.
- If a future installed-server test requests EULA acceptance, only Liam can agree. This NeoForge development run skipped that check and did not create `eula.txt`.

## Open risks

- NeoForge 26.3.0.57-beta is a beta loader. If the build fails, follow the fallback rule in
  [building.md](building.md) and ask before changing pins.
- Local client startup logged Windows OSHI/Perflib system-report warnings but continued loading; details are in [KNOWN_ISSUES.md](../../KNOWN_ISSUES.md).
- Recheck untracked/ignored local content before replacing files. Keep `.local/modern-jdk.json`;
  back up valuable worlds/configs outside the checkout. Git branches do not back up ignored files.
- Threat-mod/library release metadata is not combined runtime proof. M1 adds no runtime libraries;
  later additions need Liam's approval and the [dependency policy](foundation-scope.md).

Follow [AGENTS.md](../../AGENTS.md) for session workflow. The [roadmap](phase-roadmap.md) owns task checkboxes;
[technical-port-plan.md](technical-port-plan.md) retains design rules, and [archive/](archive/) holds historical notes.
