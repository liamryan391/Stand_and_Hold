# Status

Updated: 2026-10-22 - Branch: `codex/m11-compatibility`

## Where we are

- M1-M10 remain merged into main. M11 11.1-11.2 are implemented on the phase branch; 11.3-11.4 and the external-infection exit gate are incomplete. Do not merge M11 or prepare M12 while that gate is unmet.
- Added bounded opt-in presets, original vanilla-undead tag, safe exact mod/version probe and operator `/standandhold compat status`. Manual priority and zombie-only defaults are unchanged; no external imports, runtime dependency, per-tick probe or global scan. The Contagion candidate is reproduction-only, explicitly not supported play.
- Liam approved exact Contagion 2.1.1 for isolated QA only. Both client and dedicated launch fail before gameplay with `NoSuchFieldError: ModConfig.Type.SERVER` in Contagion's constructor. The pinned FML 12.0.9 enum has no such field. Artifact metadata matches the pins, but binary compatibility does not. See [Optional Threat Compatibility](compatibility.md) and [Known Issues](../../KNOWN_ISSUES.md).
- Latest clean build passed (6s, 2,364 assertions including 27 compatibility checks). Normal jar metadata/pins inspected; no nested jars, Contagion classes/assets or test classes packaged. Branch CI for the new implementation is pending publication.
- No-mod dedicated baseline reached Done, accepted progression/research/save/stop and exited cleanly (30s). The failed with-mod server run exited 1 (8s); failed client exited 1 after Liam closed its error window (2m14s). These are failed compatibility results, not passed launches.
- After moving only our installed jar copies to each isolated run's `mods-disabled/`, the backed-up server reached Done again, retained 27 points/completed Initial Sample Analysis, refused repeat rewards, skipped missing/invalid rules and saved/stopped/exited cleanly (15s). All crash logs/reports, artifact copies and baseline backup are retained under the QA directories/`.local/m11-qa/`; no user files were deleted.
- Liam passed recovery/generic client gameplay, mapping-only targeting, mission and save/reload checks; client saved and fully exited normally (5m9s). Reviewed F2 frames show the active generic preset/skipped absent candidate, mission reward and human/zombie combat. Normal run folders/configs are untouched; no pins/version/EULA acceptance changed.

## Next milestone

Blocked on a compatible external runtime candidate/admission decision:

`Continue: M11 steps 11.3-11.4 (approved compatible infection-mod runtime/removal tests and supported-combination documentation) on codex/m11-compatibility.`

The preferred constraint is to keep the existing pins. A corrected upstream Contagion artifact or a separately verified/approved alternative is needed; do not patch the external jar, silently substitute a release or treat Undead Nights metadata as infection-mod proof.

## Needs Liam

- No further baseline client check is needed for this checkpoint. The reported pass and reviewed frames establish generic/no-mod behavior only, not Contagion compatibility.
- Decide the external-test path after the recorded failure: retain pins and wait for a compatible upstream artifact, or evaluate another pinned-target infection candidate before separately approving its runtime use. No other external mod or pin change is approved.
- Only Liam may accept a server EULA if requested. The development launches did not prompt for one; Codex performed no EULA acceptance.
- Optional M10 retention retest remains separate: previously issued Survivor blade stays usable at Stage 0 after reload; a new unarmed Survivor still needs Stage 1 + Initial Sample Analysis.

## Open risks

- Config guards cannot stop another installed mod's constructor from crashing before game initialization. Safe missing-mod behavior is not a promise to load incompatible binaries.
- Removal recovery followed an early bootstrap failure, not a successfully played external-modded world. Infection effects, external gameplay and full modded-world removal remain unverified.
- Response storage retains unresolved raiders and removed-post cooldown records. Whole-world backups are required. Long-session multi-outpost counts, physical patrol/projectile reload and multiplayer remain unverified.
- Player weapons can hurt human/passive mobs; NPC player/human exclusions and team protections remain separate. Art and structure limits are unchanged; see Known Issues for platform warnings.
- Preserve ignored worlds/configs, `.local/modern-jdk.json`, the protected legacy branch/tag and all retained QA evidence. No legacy save conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
