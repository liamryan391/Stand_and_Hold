# Status

Updated: 2026-10-15 · Branch: `codex/m4-buildings`

## Where we are

- M1-M3 are verified and merged; [M3 Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38646896892) and post-merge run 38647618098 passed. Legacy refs, pins, version and ignored user files are unchanged.
- M4 implementation adds three original utility blocks/items/creative entries, shared saved storage, schema 3 post registration/loaded-only cleanup, bounded point/supply/lab work, checked upgrades/transfers, and vanilla-packet server-owned menus. No new libraries, sample items, structures or NPCs. See [Buildings](buildings.md).
- Local `clean build --no-daemon --stacktrace` passed (6s): 50 progression + 98 foundation + 63 building assertions. Checked jar metadata, assets, loot tables and exclusion of regression classes. Tests caught and fixed empty dimension parsing; the old migration assertion now follows the current schema.
- Two dedicated `runServer` runs reached Done/stopped cleanly. All blocks placed; registration completed its active mission once, passive generation ran, lab work spent 10 local supplies/completed once, and an upgraded level-2 post/storage/target/completed receipt survived restart (62 points, 20 global supplies before subsequent QA edits).
- Normal crate destruction recovered 30 supplies once; admin replacement suppressed side effects as vanilla intends. List cleaned exactly one resulting stale loaded post without a scan/chunk load. Partial-work and future-schema reload checks are underway in the disposable M4 world.
- Client discovery/resource loading/new-world entry were observed with no missing mod textures/models in the log. Liam's visual/menu/transfer/upgrade/reload checks are pending; do not treat startup as UI proof. Final clean/remote CI/publication remain pending.

## Next milestone

`Continue: M4 steps 4.1-4.4 (core blocks, building state/storage, bounded generation and upgrades, server-authoritative menus/actions, and verification) on codex/m4-buildings.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No pin/dependency decision. The M4 client is open for Liam's combined block/menu/storage/research/upgrade/save-reload checks; fully quit afterwards for the final clean build. Actual two-player/network-fuzz and packaged-installation checks remain unobserved; follow the buildings QA recipe.
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
