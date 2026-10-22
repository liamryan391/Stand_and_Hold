# Status

Updated: 2026-10-23 - Branch: `codex/m12-showcase`

## Where we are

- M1-M10 passed their original phase gates. M11's generic compatibility foundation is merged with Liam's explicit 2026-10-22 deferral of successful external-infection gameplay/modded-world removal. That external requirement is not passed; no working external infection combination is advertised.
- M11 adds opt-in presets, verified vanilla tags, safe mod/version probing and operator diagnostics. Manual priority/zombie-only defaults remain unchanged. No external class imports, added runtime dependency, per-tick probe or pin/version change.
- Exact Contagion 2.1.1 fails client/server bootstrap on the pinned loader (`NoSuchFieldError: ModConfig.Type.SERVER`). It is retained only as a disabled isolated reproduction fixture. See [compatibility results/deferral](compatibility.md) and [Known Issues](../../KNOWN_ISSUES.md). No other runtime mod is approved.
- M11 clean build passed 2,364 assertions; jar metadata/test/external-content exclusion inspected. Windows implementation CI 39856404656 and final branch-head CI 39857071341 passed. No-mod/generic client and dedicated baseline/recovery checks passed; client saved/exited normally and screenshots were reviewed.
- M12 is the active batched milestone: assemble/document the existing loop, provide bounded guidance, observe a roughly 15-minute usability session, prepare original community materials and a technical test candidate. This is not a survival-release or public-publishing approval.
- M12 read-only `/standandhold help` and `guide`, default-loop transaction regression, isolated showcase runs and tester/community/candidate documents are implemented. Final `clean build --no-daemon --stacktrace` passed (5s, 2,429 assertions); Windows implementation CI 39869561568 and final-code CI 39870620661 passed, including normal-jar upload. Metadata/external/test exclusion inspected. Dedicated initial/restart/fixture/final-guide launches reached Done, retained mission progress/receipt, rejected invalid input and saved/stopped cleanly.
- Rehearsal screenshots/logs exposed long guide wrapping and an incomplete mission-start input. Compact click-to-fill (not auto-run) guidance and missing-ID feedback are fixed and server/regression verified. Refreshed client resource loading observed; short readability/click/reload check requested and pending Liam's report.
- First client ran/exited normally (22m32s). Logs/F2 show recovery and command-post mission completions, completed Lab, natural raid/reinforcement and an admin raid with real repeat cooldown refusals. Stockpile/first-research/stage mission sequence and earned sample acquisition are not confirmed; screenshot includes a 64-sample stack and Liam was asked its source. Two original rehearsal frames retained with qualified captions in community-draft.md; real clip path is pending.
- `/debug` summary: 995.08 seconds, 16,499 ticks, 16.58 TPS across a session with pause entries. No mod exception or repeated per-tick error observed; known OSHI startup warnings remain. This is not clean sustained-load proof; an unpaused controlled sample is still needed. Local logs retained under `.local/m12-qa/evidence/`.

## Local Candidate

- `build/libs/standandhold-0.1.0-alpha.1.jar`: 304,971 bytes; SHA-256 `3629320D3428F30E93D58BBE00ABAE04E78414CF919B752DEB71121B179B51D1`.
- Exact target unchanged: Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25. This source-run-verified candidate is not a published/tagged release or packaged-launcher/multiplayer pass.

## Next milestone

`Continue: M12 steps 12.1-12.4 (assemble the guided alpha loop, observe a new-tester session, prepare original community materials, and verify a technical test candidate) on codex/m12-showcase.`

Keep existing gameplay and toolchain pins. Do not imply the deferred external-mod test passed or bundle the failed fixture.

## Needs Liam

- Liam chose a maintainer rehearsal and agreed to record a real defence clip. Short-check client is open in the isolated showcase instance; pending readability/click/reload report, sample-source clarification and clip path. Independent new-user evidence is still required by M12's exit gate, not waived by the M11 decision.
- Original F2 screenshots and a short real defence clip are community-material evidence; draft copy must not substitute for captured gameplay. No external publishing is authorized by preparation.
- Only Liam may accept a server EULA if requested. No new runtime dependency or pin change is approved.

## Open risks

- The pinned NeoForge is beta; metadata-compatible external artifacts can fail binary initialization. Generic/no-mod proof is not infection-effect or successful modded-world removal proof.
- Existing standalone tests do not establish a complete Survival acquisition/crafting economy, long-session multiplayer performance or packaged-launcher installation.
- Saved response quotas/history remain conservative; whole-world backups are required. Patrol/projectile reload, multi-outpost soak and real multiplayer remain separately unverified.
- Preserve all ignored worlds/configs, `.local/modern-jdk.json`, disabled QA jars/crash evidence and protected legacy refs. No legacy save conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
