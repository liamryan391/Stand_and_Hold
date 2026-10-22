# Status

Updated: 2026-10-22 - Branch: `codex/m11-compatibility`

## Where we are

- M1-M10 are merged into `main`; M11 is on its prepared phase branch. The worktree started clean, GitHub refs were fetched and `origin/1.12` still matches the preserved commit. No implementation, build, toolchain, version or dependency changes in this checkpoint.
- M11 11.1 is complete for artifact metadata: Contagion 2.1.1 and Undead Nights 2.2.6 have exact author-labelled Minecraft 26.3/NeoForge releases. SHA-512 checks and in-memory jar manifests confirm declared platform ranges include our pins and no additional required mod is declared. See [Optional Threat Compatibility](compatibility.md) for exact files, sources, licence caveats and proposed admission.
- Contagion is the proposed isolated infection-mod QA fixture; Undead Nights is a separate horde-scheduler candidate. Neither was installed, saved into the checkout or executed. Contagion's project MIT badge conflicts with its jar's All Rights Reserved metadata; no code/assets/jars will be copied into or bundled with the mod.
- Presets/adapters and M11 with/without/removal runtime checks are not implemented or passed. M11 must not be merged until its actual exit gate is observed. The existing safe zombie mapping and optional-mod behavior are unchanged.
- M10's final code clean build passed 2,337 assertions; main CI 39824129254 passed. That earlier proof is not M11 runtime proof. This docs-only checkpoint uses `git diff --check`; no new build/game launch was needed or claimed.

## Next milestone

After the runtime-test admission decision:

`Continue: M11 steps 11.2-11.4 (optional mapping presets/adapters, isolated with/without-mod and removal testing, and supported-combination documentation) on codex/m11-compatibility.`

Do not install or execute the external test mod before Liam approves its exact isolated QA use.

## Needs Liam

- Approve or decline Contagion `Contagion-2.1.1-NeoForge-mc26.3.jar` (Modrinth release `4tdqhokG`) for a separate disposable client/server QA instance. No default development runs, build dependencies, release contents or toolchain pins would change. This is the explicit runtime-admission gate in AGENTS.md, not a proposal to make Contagion required.
- Only Liam may accept an installed-server EULA if requested. No EULA acceptance file was written.
- Optional M10 retention retest: issue a Survivor blade at Stage 1 + Initial Sample Analysis, reset to Stage 0, spawn a new Survivor and save/reopen. The old unit should retain its usable blade; the new unit should stay unarmed.

## Open risks

- Published version/range/hash checks do not establish mixin, binary, gameplay, performance or removal compatibility. Exact external registry IDs/tags must be verified before any preset is activated. Infection effects must not broaden NPC targets to players.
- Response storage remains conservative: unresolved unloaded raiders and removed-post cooldown records are retained. Full/malformed storage pauses new sites; whole-world backups remain required.
- Long-session multi-outpost counts, physical patrol/projectile reload and real multiplayer remain unverified. Player weapons can hurt human/passive mobs; players/allied teams stay protected.
- Art remains alpha quality; fixed building fronts, conservative one-chunk placement and loaded-only cleanup are unchanged. See [Known Issues](../../KNOWN_ISSUES.md) for retained platform/render warnings.
- Preserve `.local/modern-jdk.json`, ignored worlds/configs and the protected legacy branch/tag. No legacy world/config conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
