# Status

Updated: 2026-10-15 · Branch: `codex/m4-buildings`

## Where we are

- M1-M3 are verified and merged; [M3 Windows CI](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38646896892) and post-merge run 38647618098 passed. Legacy refs, pins, version and ignored user files are unchanged.
- M4 implementation adds three original utility blocks/items/creative entries, shared saved storage, schema 3 post registration/loaded-only cleanup, bounded point/supply/lab work, checked upgrades/transfers, and vanilla-packet server-owned menus. No new libraries, sample items, structures or NPCs. See [Buildings](buildings.md).
- Final local `clean build --no-daemon --stacktrace` passed after both runtimes exited (6s): 50 progression + 98 foundation + 71 building assertions. Checked jar metadata, assets, loot tables and exclusion of regression classes. Coverage includes signed-short long sync, competing transactions, sample consumption, upgrade gates, overflow, compressed saves and dimension keys.
- Two dedicated `runServer` runs reached Done/stopped cleanly. All blocks placed; registration completed its active mission once, passive generation ran, lab work spent 10 local supplies/completed once, and an upgraded level-2 post/storage/target/completed receipt survived restart (62 points, 20 global supplies before subsequent QA edits).
- Four dedicated runs reached Done/stopped cleanly. Normal crate destruction recovered 30 supplies once; normal post destruction unregistered immediately, while list cleaned exactly one stale admin-replaced post. A restart loaded zero persistent chunks: list retained the unloaded post and `/data get block` still refused its unloaded position. Explicit QA forceload then showed level 2/20 supplies, lab target/work 5/20 supplies, and preserved newer/malformed compounds. No forced loading is done by mod cleanup; the QA force ticket was removed.
- A valid resource override/reload granted the probe reward once; an unsupported-format override refused commands without spending/crashing. Disabling it restored defaults and receipts. Final isolated QA balances 171 points/49 global supplies; the retained probe is valid and disabled. Review fixed removed-target menu recovery/unknown upgrade gates and removed unnecessary sorting from building mission observation.
- Client discovery/resource loading/world entry, save/reopen and normal exit were observed (BUILD SUCCESSFUL, 9m 4s). Liam reported the combined textures/names/menus, lab completion once, transfers, upgrade and persistence checks passed. The removed-definition recovery refinement was source-reviewed/built, not separately visually retested; actual two-client/network-fuzz and packaged-installation checks remain unobserved.
- [Windows CI 38718922314](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38718922314) passed for initial runtime commit `9fdfc53`. Final QA refinements need their own green CI before merge/publication; no M5 implementation has started.

## Next milestone

`Continue: M4 steps 4.1-4.4 (core blocks, building state/storage, bounded generation and upgrades, server-authoritative menus/actions, and verification) on codex/m4-buildings.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No blocking decision or outstanding integrated visual gate: Liam reported passed and the client exited cleanly. Optional follow-up coverage: actual two-player simultaneous transfers/upgrades, stale remote/unknown packets, GUI scales and removed-definition recovery, and packaged installation; follow the buildings QA recipe. These are not reported as runtime passes.
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
