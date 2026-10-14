# Status

Updated: 2026-10-14 · Branch: `codex/m3-foundation`

## Where we are

- M1/M2 are verified and merged into main; the preserved `1.12` branch/tag, toolchain pins, and version are unchanged. M2 post-merge [Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38643948363) passed. The M3 branch starts from that main.
- M3 / 3.1 is implemented: bounded JSON research/mission definitions, stable IDs, costs/unlocks, prerequisite validation, cycle rejection, and optional server datapack overrides. See [Foundation Data](foundation-data.md).
- `clean build --no-daemon --stacktrace` passed with 50 existing progression assertions and 20 definition assertions. No M3 transactions, saved-state changes, or runtime launch proof yet.

## Next milestone

`Continue: M3 steps 3.1-3.4 (research definitions, supply/research transactions, persistent missions, and verification) on codex/m3-foundation.`
In progress through the whole milestone; definitions are verified, transactions/missions/runtime gates remain.
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking decision. Liam reported the M2 singleplayer checks passed and closed the client. A private multiplayer non-operator check remains: status allowed, addpoints/setstage denied.
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
