# Status

Updated: 2026-10-27 - Branch: `codex/m12-showcase`

## Where we are

- M1-M10 passed their original phase gates. M11's generic compatibility foundation is merged with Liam's explicit 2026-10-22 deferral of successful external-infection gameplay/modded-world removal. That external requirement is not passed; no working external infection combination is advertised.
- M11 adds opt-in presets, verified vanilla tags, safe mod/version probing and operator diagnostics. Manual priority/zombie-only defaults remain unchanged. No external class imports, added runtime dependency, per-tick probe or pin/version change.
- Exact Contagion 2.1.1 fails client/server bootstrap on the pinned loader (`NoSuchFieldError: ModConfig.Type.SERVER`). It is retained only as a disabled isolated reproduction fixture. See [compatibility results/deferral](compatibility.md) and [Known Issues](../../KNOWN_ISSUES.md). No other runtime mod is approved.
- M11 clean build passed 2,364 assertions; jar metadata/test/external-content exclusion inspected. Windows implementation CI 39856404656 and final branch-head CI 39857071341 passed. No-mod/generic client and dedicated baseline/recovery checks passed; client saved/exited normally and screenshots were reviewed.
- M12's qualified alpha exit gate is accepted by Liam on 2026-10-27: his earned-progression pass plus controlled dedicated raid/combat/restart proof. The client raid attempts at unsuitable sites remain blocked, not passed. Final documentation CI/merge is pending; M13 has not started and no public release/tag is authorized. Independent unfamiliar-tester feedback remains deferred to beta, not passed.
- Read-only `/standandhold help` and `guide`, default-loop transaction regression and isolated showcase runs are implemented. Latest `clean build --no-daemon --stacktrace` passed (5s, 2,442 assertions); metadata/external/test exclusion inspected. Windows final-code CI 39874147169 passed, including normal-jar upload; prior implementation/capture CI also passed.
- Rehearsal exposed long guide wrapping, missing-ID feedback and the old word parser rejecting namespaced IDs. Fixed all three without costs/permission/save changes. Vanilla identifier parsing retains the exact token for short-ID defaults; 13 parser regressions pass. Dedicated namespaced start/progress/complete/research, short-alias repeats, unknown namespace/save/stop passed (28s). Liam's final click retest passed; log shows the exact suggested command starts Stockpile Supplies 0/20, with normal save/exit (2m18s). Guide screenshot fits its first/last lines.
- First client ran/exited normally (22m32s); Liam reports a maintainer functional pass, save/reload and real zombie sample drops. Logs/F2 show recovery and command-post missions, completed Lab, natural raid/reinforcement and admin raid/repeat cooldown refusals. Subsequent reopen/save/exit checks also completed cleanly. Creative-assisted footage, including a 64-sample stack and later unfinished stockpiling, is not strict earned-only mission-chain or independent-new-user proof.
- Reviewed the provided 253.637s Radeon recording; retained three qualified gameplay frames and prepared a 13s local contiguous human/zombie combat excerpt under `.local/m12-qa/media/`. Exact source/excerpt paths, hashes and captions are in community-draft.md; no video upload. Stage-scaled defender budgets and locally expanding bases recorded as future proposals in structures.md, with no balance/code change.
- `/debug` summary: 995.08 seconds, 16,499 ticks, 16.58 TPS across a session with pause entries. No mod exception or repeated per-tick error observed; known OSHI startup warnings remain. This is not clean sustained-load proof; an unpaused controlled sample is still needed. Local logs retained under `.local/m12-qa/evidence/`.
- 2026-10-26: Liam confirmed no unfamiliar tester is available and requested handoff preparation only. Added coordinator boundaries and a fill-in gate scorecard to the existing play sheet; no game launch, code/config change or merge. Repository was clean/synced at resumption; final documentation-head CI 39874542031 passed, and local candidate size/hash still match the verified artifact. No new local build was run.
- 2026-10-27: Liam will be the alpha tester while the mod develops; independent testers/feedback are planned for beta after a stable alpha. Revised M12's tester requirement accordingly, retaining real-drop/ordered-mission, save/reload and technical checks. This is a testing-policy decision, not an earned-loop pass, dependency approval or public release. No gameplay, balance, toolchain or version change.
- 2026-10-27 verification: fresh pinned `clean build --no-daemon --stacktrace` passed (6s, 2,442 assertions); candidate size/hash and normal-jar metadata/external/test exclusion match. Branch-head Windows CI 40573254612 passed. Two isolated dedicated runs reached Done, accepted diagnostics/save/stop and exited cleanly (24s/32s); restart retained 60 points, 10 global supplies, completed analysis and Stockpile Supplies 10/20 in the existing admin-test world. Invalid commands rejected without point changes.
- Liam reports the fresh-world earned checks passed. Client log shows recovery, command-post and stockpile receipts before lab completion, then the first-research and Stage 1 receipts; a missed first-research mission start was safely reconciled after lab completion. Liam confirms the later +600,000 points/+6,000,000,000 supplies were extra admin exploration; 189 points were already earned before the point grant. This is not an entirely grant-free final save. Client exited normally (19m36s). Added guidance for the existing instant sample-free research command; no new gameplay/bypass command.
- Two focused client follow-ups exited cleanly (8m26s/5m24s); raids safely refused at ambient-crowded/unsupported hillside sites, not client raid passes. Split the ambiguous refusal into quota/terrain reasons and unpaused backoff guidance without changing spawn rules/timers/caps. Latest clean build passed (8s, 2,442 assertions), updated jar inspected; final-code Windows CI 40579806350 passed. No completed new profiler sample: the last client started profiling without a recorded stop/summary. Source launches are not packaged/multiplayer proof.
- Controlled disposable dedicated fixture verified quota refusal with 8 slimes, terrain refusal, tagged spawning after safe conditions were restored, and reservations/cooldowns across restart. Real combat reduced a tagged zombie to 17.06 health and a defender from 20 to 2.36; the raider then died, its reservation cleared and a human-credited kill in adjacent region 1,2 persisted. Final restart kept 2 raids, 0 reserved raiders and a 4,436-tick cooldown, refusing a new wave. Six successful dedicated runs this session saved/stopped/exited cleanly; the initial CLI override failed before bootstrap and was corrected. QA-only chunk tickets were removed; sprinted fixtures/manual test NPCs are not earned-loop, automatic-quota or performance proof. Liam explicitly accepted this combined evidence for the alpha merge, retaining broad-terrain/client raid coverage as a known limitation.

## Local Candidate

- `build/libs/standandhold-0.1.0-alpha.1.jar`: 305,591 bytes; SHA-256 `C18A7CE9C2F52A7DDF6AA345AC9720E69CB9E4614A2E273D1A96CE5461970036`. Rebuilt after clarifying raid refusal diagnostics; supersedes earlier rehearsal previews.
- Exact target unchanged: Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25. This source-run-verified candidate is not a published/tagged release or packaged-launcher/multiplayer pass.

## Next milestone

`Continue: M13 steps 13.1-13.4 (integration review, bounded support/vehicle design, one scoped prototype after the design gate, and verification) on codex/m13-vehicles.`

Keep existing gameplay and toolchain pins. Do not imply the deferred external-mod test passed or bundle the failed fixture.

## Needs Liam

- No further M12 manual repeat requested: Liam accepted the earned-core plus controlled server proof for this alpha merge. Keep client terrain refusals and later admin grants qualified, not relabelled as an unassisted full client loop. Broad-terrain/client raid, sustained-load, packaged and real multiplayer checks remain separate coverage; independent first-time feedback belongs to beta.
- Original F2 frames and reviewed real combat footage are retained with qualified captions; publishing still requires separate authorization. Larger stage-scaled defender caps/local base expansion are proposals only.
- Only Liam may accept a server EULA if requested. No new runtime dependency or pin change is approved.

## Open risks

- The pinned NeoForge is beta; metadata-compatible external artifacts can fail binary initialization. Generic/no-mod proof is not infection-effect or successful modded-world removal proof.
- Existing standalone tests do not establish a complete Survival acquisition/crafting economy, long-session multiplayer performance or packaged-launcher installation.
- Saved response quotas/history remain conservative; whole-world backups are required. Patrol/projectile reload, multi-outpost soak and real multiplayer remain separately unverified.
- Structure footprint validation does not guarantee clear raid spawn terrain 8-12 blocks away. Ambient hostiles can fill the area cap; vanilla natural spawning being disabled does not remove existing mobs or disable the mod's raid controller. See the play-sheet troubleshooting and Known Issues.
- Preserve all ignored worlds/configs, `.local/modern-jdk.json`, disabled QA jars/crash evidence and protected legacy refs. No legacy save conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
