# Status

Updated: 2026-10-14 · Branch: `codex/m3-foundation`

## Where we are

- M1/M2 are verified and merged into main; the preserved `1.12` branch/tag, toolchain pins, and version are unchanged. M2 post-merge [Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38643948363) passed. The M3 branch starts from that main.
- M3 / 3.1-3.3 are implemented: bounded JSON definitions, atomic supply/research transactions, schema 2 migration, seven small missions with persistent once-only receipts, and public inspection/operator debug commands. See [Foundation Data](foundation-data.md). No items, buildings, tick handlers, or new dependencies.
- `clean build --no-daemon --stacktrace` passed with 50 progression and 96 foundation assertions, including compressed saves, unknown/malformed data, reward overflow, and repeated completion. Jar metadata and definition inclusion/test exclusion were inspected.
- Dedicated `runServer` reached Done and stopped cleanly over three runs. Partial mission 2/3 and 7 supplies survived restart; the full chain ended at 250 points/55 supplies. A second restart kept all receipts, with no repeated rewards. Nether/End command contexts shared state. Missing/invalid arguments, prerequisites, and insufficient costs failed safely.
- An isolated datapack override applied through `/reload` (one-point reward/one-supply cost, final QA state 251/54). Cyclic and malformed JSON overrides refused commands without changing balances or crashing. Disabling the pack restored defaults and existing completion receipts; the ignored QA pack is valid and disabled, not deleted.
- M3 exit evidence is observed on the dedicated server. Client startup, final clean build, CI, and publication are being completed before the phase merge; no M3 visual client check is claimed yet.
- Client resource loading and mod discovery are observed; Liam has the optional integrated checks. The final clean before launch passed 146 assertions. A small atomic-start corner-case fix now passes `build` with 148 assertions; repeat clean after client exit before finishing publication.

## Next milestone

`Continue: M3 steps 3.1-3.4 (research definitions, supply/research transactions, persistent missions, and verification) on codex/m3-foundation.`
Implementation/automated/dedicated-server gates are complete; finishing client smoke proof and publication.
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
