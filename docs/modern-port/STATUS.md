# Status

Updated: 2026-10-14 · Branch: `main` (M3 merge handoff)

## Where we are

- M1/M2 are verified and merged into main; the preserved `1.12` branch/tag, toolchain pins, and version are unchanged. M2 post-merge [Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38643948363) passed. The M3 branch starts from that main.
- M3 / 3.1-3.4 are complete: bounded JSON definitions, atomic supply/research transactions, schema 2 migration, seven small missions with persistent once-only receipts, and public inspection/operator debug commands. See [Foundation Data](foundation-data.md). No items, buildings, tick handlers, or new dependencies.
- Final local `clean build --no-daemon --stacktrace` passed after client exit (5s), with 50 progression and 98 foundation assertions covering compressed saves, unknown/malformed data, reward overflow, atomic activation, and repeated completion. Jar metadata and definition inclusion/test exclusion were inspected.
- Dedicated `runServer` reached Done and stopped cleanly over four runs. Partial mission 2/3 and 7 supplies survived restart; the full chain ended at 250 points/55 supplies. A second restart kept all receipts, with no repeated rewards. Nether/End command contexts shared state. Missing/invalid arguments, prerequisites, and insufficient costs failed safely. The final atomic-start fix was exercised in the loader: overflowing an immediately eligible reward left the mission inactive and balances unchanged.
- An isolated datapack override applied through `/reload` (one-point research reward/one-supply cost, then a one-point mission reward; final QA state 252/54). Cyclic and malformed JSON overrides refused commands without changing balances or crashing. Disabling the pack restored defaults and existing completion receipts; the ignored QA pack is valid and disabled, not deleted.
- Client mod discovery/resource loading/world entry were observed; logs show research completion/repeat protection, mission progress 2/3, world saving, and normal exit (BUILD SUCCESSFUL, 7m 1s). Liam reported the requested integrated menu/Mods/research/mission/save-reload checks passed; the full visual flow is his report, not an automated UI test.
- M3 is merged into main after [Windows CI run 38646896892](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38646896892) passed for runtime commit `46be02f`: 148 assertions, clean build, and normal-jar upload. The merge adds final documentation only. Prepare `codex/m4-buildings` from this main; no M4 implementation is included.

## Next milestone

`Continue: M4 steps 4.1-4.4 (core blocks, building state/storage, bounded generation and upgrades, server-authoritative menus/actions, and verification) on codex/m4-buildings.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking decision. Liam reported the M3 integrated checks passed and closed the client. A private multiplayer non-operator check remains: public inspection allowed, supply/research/mission mutations/debug commands denied. Packaged-installation smoke tests are also unobserved.
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
