# Status

Updated: 2026-10-14 · Branch: `codex/modern-neoforge-scaffold`

## Where we are

- M0 (preserve and decide) and M1 steps 1.1.1-1.1.2 (legacy inventory and modern documentation/identity) are complete.
- Liam resumed M1. The legacy compile/resource tree and obsolete local guides are retired on the work branch; the preserved branch and local user files are unchanged.
- M1 steps 1.1.3-1.4.3 are complete: pinned MDK scaffold, identity, minimal logger/config shell, separate run directories, build instructions, Java 25 CI configuration, and local build/client/server verification. Step 1.5 publication/merge handoff remains separate.
- Wrapper verification passed: Gradle 9.2.1, Temurin Java 25.0.4+7 on Windows 11. First `clean build --no-daemon --stacktrace` passed in 8m 43s; the rebuild after stdin forwarding passed in 34s. Jar metadata/content and Java 25 bytecode were checked. No unit test sources exist.
- `runClient` discovered/initialized the mod and generated `config/standandhold-local.toml`. Liam reported the main menu, Mods list/version, and fresh-world entry/save/reopen checks passed. Logs corroborate world entry, save, and normal exit; the task finished BUILD SUCCESSFUL in 3m 24s. Log: `run/modern-client/logs/latest.log`.
- `runServer` reached `Done`, reloaded the generated test world on retry, accepted `stop` after stdin forwarding was added, saved players/worlds, and exited BUILD SUCCESSFUL in 1m 16s. NeoForge dev mode skipped the EULA check; no EULA file or agreement was written. The retry used localhost only. Log: `run/modern-server/logs/latest.log`.

## Next milestone

**M1 / 1.5:** review the published work branch and actual Java 25 CI result, then merge the verified scaffold into `main` and record publication. Do not change the preserved branch/tag or version `0.1.0-alpha.1`.
This session publishes the scaffold and local verification on the work branch only; it does not merge `main` or create a release.

## Needs Liam

- No decision is needed for the completed local scaffold tests; Liam confirmed the visual client checks passed and quit normally.
- If a future installed-server test requests EULA acceptance, only Liam can agree. This NeoForge development run skipped that check and did not create `eula.txt`.

## Open risks

- NeoForge 26.3.0.57-beta is a beta loader. If the build fails, follow the fallback rule in
  [building.md](building.md) and ask before changing pins.
- Local client startup logged Windows OSHI/Perflib system-report warnings but continued loading; details are in [KNOWN_ISSUES.md](../../KNOWN_ISSUES.md). Remote Java 25 CI results have not been observed.
- Recheck untracked/ignored local content before replacing files. Keep `.local/modern-jdk.json`;
  back up valuable worlds/configs outside the checkout. Git branches do not back up ignored files.
- Threat-mod/library release metadata is not combined runtime proof. M1 adds no runtime libraries;
  later additions need Liam's approval and the [dependency policy](foundation-scope.md).

Follow [AGENTS.md](../../AGENTS.md) for session workflow. The [roadmap](phase-roadmap.md) owns task checkboxes;
[technical-port-plan.md](technical-port-plan.md) retains design rules, and [archive/](archive/) holds historical notes.
