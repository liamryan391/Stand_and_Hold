# Status

Updated: 2026-10-23 - Branch: `codex/m12-showcase`

## Where we are

- M1-M10 passed their original phase gates. M11's generic compatibility foundation is merged with Liam's explicit 2026-10-22 deferral of successful external-infection gameplay/modded-world removal. That external requirement is not passed; no working external infection combination is advertised.
- M11 adds opt-in presets, verified vanilla tags, safe mod/version probing and operator diagnostics. Manual priority/zombie-only defaults remain unchanged. No external class imports, added runtime dependency, per-tick probe or pin/version change.
- Exact Contagion 2.1.1 fails client/server bootstrap on the pinned loader (`NoSuchFieldError: ModConfig.Type.SERVER`). It is retained only as a disabled isolated reproduction fixture. See [compatibility results/deferral](compatibility.md) and [Known Issues](../../KNOWN_ISSUES.md). No other runtime mod is approved.
- M11 clean build passed 2,364 assertions; jar metadata/test/external-content exclusion inspected. Windows implementation CI 39856404656 and final branch-head CI 39857071341 passed. No-mod/generic client and dedicated baseline/recovery checks passed; client saved/exited normally and screenshots were reviewed.
- M12 implementation, maintainer rehearsal, original community-material preparation and technical candidate verification are done. The independent new-tester/earned-only exit gate remains open: M12 is not merged, M13 is not started, and no public release/tag is authorized.
- Read-only `/standandhold help` and `guide`, default-loop transaction regression and isolated showcase runs are implemented. Latest `clean build --no-daemon --stacktrace` passed (5s, 2,442 assertions); metadata/external/test exclusion inspected. Windows final-code CI 39874147169 passed, including normal-jar upload; prior implementation/capture CI also passed.
- Rehearsal exposed long guide wrapping, missing-ID feedback and the old word parser rejecting namespaced IDs. Fixed all three without costs/permission/save changes. Vanilla identifier parsing retains the exact token for short-ID defaults; 13 parser regressions pass. Dedicated namespaced start/progress/complete/research, short-alias repeats, unknown namespace/save/stop passed (28s). Liam's final click retest passed; log shows the exact suggested command starts Stockpile Supplies 0/20, with normal save/exit (2m18s). Guide screenshot fits its first/last lines.
- First client ran/exited normally (22m32s); Liam reports a maintainer functional pass, save/reload and real zombie sample drops. Logs/F2 show recovery and command-post missions, completed Lab, natural raid/reinforcement and admin raid/repeat cooldown refusals. Subsequent reopen/save/exit checks also completed cleanly. Creative-assisted footage, including a 64-sample stack and later unfinished stockpiling, is not strict earned-only mission-chain or independent-new-user proof.
- Reviewed the provided 253.637s Radeon recording; retained three qualified gameplay frames and prepared a 13s local contiguous human/zombie combat excerpt under `.local/m12-qa/media/`. Exact source/excerpt paths, hashes and captions are in community-draft.md; no video upload. Stage-scaled defender budgets and locally expanding bases recorded as future proposals in structures.md, with no balance/code change.
- `/debug` summary: 995.08 seconds, 16,499 ticks, 16.58 TPS across a session with pause entries. No mod exception or repeated per-tick error observed; known OSHI startup warnings remain. This is not clean sustained-load proof; an unpaused controlled sample is still needed. Local logs retained under `.local/m12-qa/evidence/`.

## Local Candidate

- `build/libs/standandhold-0.1.0-alpha.1.jar`: 305,318 bytes; SHA-256 `94DB0AE6AC87E6D40D47F5139E4F3C77B93C4C466375C46849E334C60D718FD9`. Rebuilt after fixing the namespaced-ID bug; supersedes earlier rehearsal previews.
- Exact target unchanged: Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25. This source-run-verified candidate is not a published/tagged release or packaged-launcher/multiplayer pass.

## Next milestone

`Continue: M12 steps 12.1-12.4 (finish independent new-tester and earned-loop verification, resolve any usability findings, and merge only after the exit gate passes) on codex/m12-showcase.`

Keep existing gameplay and toolchain pins. Do not imply the deferred external-mod test passed or bundle the failed fixture.

## Needs Liam

- No immediate repeated maintainer check remains: client is closed and the final click fix passed. Arrange an unfamiliar tester for about 15 minutes using the play sheet, real dropped samples, ordered supply/research missions and no progression/resource grants. This exit gate was not waived by the M11 decision.
- Original F2 frames and reviewed real combat footage are retained with qualified captions; publishing still requires separate authorization. Larger stage-scaled defender caps/local base expansion are proposals only.
- Only Liam may accept a server EULA if requested. No new runtime dependency or pin change is approved.

## Open risks

- The pinned NeoForge is beta; metadata-compatible external artifacts can fail binary initialization. Generic/no-mod proof is not infection-effect or successful modded-world removal proof.
- Existing standalone tests do not establish a complete Survival acquisition/crafting economy, long-session multiplayer performance or packaged-launcher installation.
- Saved response quotas/history remain conservative; whole-world backups are required. Patrol/projectile reload, multi-outpost soak and real multiplayer remain separately unverified.
- Preserve all ignored worlds/configs, `.local/modern-jdk.json`, disabled QA jars/crash evidence and protected legacy refs. No legacy save conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
