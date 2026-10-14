# Status

Updated: 2026-10-14 · Branch: `main` (M1 merge handoff)

## Where we are

- M0 (preserve and decide) and M1 steps 1.1.1-1.1.2 (legacy inventory and modern documentation/identity) are complete.
- Liam resumed M1. The legacy compile/resource tree and obsolete local guides are retired on the work branch; the preserved branch and local user files are unchanged.
- M1 is complete: pinned MDK scaffold, identity, minimal logger/config shell, isolated runs, Java 25 CI, and observed local build/client/server verification. The scaffold is merged into `main`; the preserved `1.12` branch/tag are unchanged.
- GitHub [Build run 38632437020](https://github.com/liamryan391/Stand_and_Hold/actions/runs/38632437020) passed for `836c31197e83e6f809aa7859e59fb37d0f0605dd`, including the Windows Java 25 build and normal-jar upload. The new AGENTS.md batching policy is committed separately.
- Wrapper verification passed: Gradle 9.2.1, Temurin Java 25.0.4+7 on Windows 11. First `clean build --no-daemon --stacktrace` passed in 8m 43s; the rebuild after stdin forwarding passed in 34s. Jar metadata/content and Java 25 bytecode were checked. No unit test sources exist.
- `runClient` discovered/initialized the mod and generated `config/standandhold-local.toml`. Liam reported the main menu, Mods list/version, and fresh-world entry/save/reopen checks passed. Logs corroborate world entry, save, and normal exit; the task finished BUILD SUCCESSFUL in 3m 24s. Log: `run/modern-client/logs/latest.log`.
- `runServer` reached `Done`, reloaded the generated test world on retry, accepted `stop` after stdin forwarding was added, saved players/worlds, and exited BUILD SUCCESSFUL in 1m 16s. NeoForge dev mode skipped the EULA check; no EULA file or agreement was written. The retry used localhost only. Log: `run/modern-server/logs/latest.log`.

## Next milestone

`Continue: M2 steps 2.1-2.4 (progression definitions, versioned global SavedData, Brigadier commands, and verification) on codex/m2-progression, cut from main.`
Keep the pinned toolchain and version `0.1.0-alpha.1`; no new runtime libraries or legacy save conversion.

## Needs Liam

- No decision is needed for the completed local scaffold tests; Liam confirmed the visual client checks passed and quit normally.
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
