# Status

Updated: 2026-10-14 · Branch: `codex/modern-neoforge-scaffold`

## Where we are

- M0 (preserve and decide) and M1 steps 1.1.1-1.1.2 (legacy inventory and modern documentation/identity) are complete.
- **Step 1.1.3 is on hold at Liam's request.** This session is a one-time documentation cleanup only.
- No modern code yet. Source, wrapper, helpers, and CI still use the Forge 1.12.2 build.
- The 26.3 / NeoForge beta pins are selected but **not build-proven**. Legacy build results are not modern proof.

## Next milestone

**After Liam resumes: M1 complete, 1.1.3 through 1.4.3.** Retire the inventoried legacy files,
install the pinned MDK, preserve identity (`Stand and Hold`, `standandhold`,
`com.liamryan.standandhold`, Liam Ryan), add the minimal entry point/config shell and Java 25 CI,
then observe a passing clean build, jar metadata, `runServer` readiness/clean stop, and
`runClient` main menu/Mods list/new world. Keep the version at `0.1.0-alpha.1`.
Merge to `main` (1.5) only after the exit gate passes. Do not start the scaffold during this cleanup.

## Needs Liam

- Accept the Minecraft EULA in `run/modern-server/eula.txt` when server testing reaches it (only Liam can agree to it).
- 2-minute in-game check after `runClient`: main menu → Mods list shows Stand and Hold → create a fresh world.

## Open risks

- NeoForge 26.3.0.57-beta is a beta loader. If the build fails, follow the fallback rule in
  [building.md](building.md) and ask before changing pins.
- Recheck untracked/ignored local content before replacing files. Keep `.local/modern-jdk.json`;
  back up valuable worlds/configs outside the checkout. Git branches do not back up ignored files.
- Threat-mod/library release metadata is not combined runtime proof. M1 adds no runtime libraries;
  later additions need Liam's approval and the [dependency policy](foundation-scope.md).

Follow [AGENTS.md](../../AGENTS.md) for session workflow. The [roadmap](phase-roadmap.md) owns task checkboxes;
[technical-port-plan.md](technical-port-plan.md) retains design rules, and [archive/](archive/) holds historical notes.
