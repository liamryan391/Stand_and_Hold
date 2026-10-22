# Status

Updated: 2026-10-23 - Branch: `codex/m12-showcase`

## Where we are

- M1-M10 passed their original phase gates. M11's generic compatibility foundation is merged with Liam's explicit 2026-10-22 deferral of successful external-infection gameplay/modded-world removal. That external requirement is not passed; no working external infection combination is advertised.
- M11 adds opt-in presets, verified vanilla tags, safe mod/version probing and operator diagnostics. Manual priority/zombie-only defaults remain unchanged. No external class imports, added runtime dependency, per-tick probe or pin/version change.
- Exact Contagion 2.1.1 fails client/server bootstrap on the pinned loader (`NoSuchFieldError: ModConfig.Type.SERVER`). It is retained only as a disabled isolated reproduction fixture. See [compatibility results/deferral](compatibility.md) and [Known Issues](../../KNOWN_ISSUES.md). No other runtime mod is approved.
- M11 clean build passed 2,364 assertions; jar metadata/test/external-content exclusion inspected. Windows implementation CI 39856404656 and final branch-head CI 39857071341 passed. No-mod/generic client and dedicated baseline/recovery checks passed; client saved/exited normally and screenshots were reviewed.
- M12 is the active batched milestone: assemble/document the existing loop, provide bounded guidance, observe a roughly 15-minute usability session, prepare original community materials and a technical test candidate. This is not a survival-release or public-publishing approval.
- M12 read-only `/standandhold help` and `guide`, default-loop transaction regression, isolated showcase runs and tester/community/candidate documents are implemented. Clean build passed 2,427 assertions; normal-jar metadata and absence of bundled external/test classes inspected. Dedicated initial/restart launches reached Done, kept saved 2/3 mission progress, rejected invalid arguments, awarded once and saved/stopped cleanly. Client resources loaded; maintainer rehearsal is in progress. Implementation branch pushed; Windows CI pending.

## Next milestone

`Continue: M12 steps 12.1-12.4 (assemble the guided alpha loop, observe a new-tester session, prepare original community materials, and verify a technical test candidate) on codex/m12-showcase.`

Keep existing gameplay and toolchain pins. Do not imply the deferred external-mod test passed or bundle the failed fixture.

## Needs Liam

- Liam chose a maintainer rehearsal and agreed to record a real defence clip. Client is open in the isolated showcase instance; grouped checks and [play sheet](showcase-loop.md) supplied. Independent new-user evidence is still required by M12's exit gate, not waived by the M11 decision.
- Original F2 screenshots and a short real defence clip are community-material evidence; draft copy must not substitute for captured gameplay. No external publishing is authorized by preparation.
- Only Liam may accept a server EULA if requested. No new runtime dependency or pin change is approved.

## Open risks

- The pinned NeoForge is beta; metadata-compatible external artifacts can fail binary initialization. Generic/no-mod proof is not infection-effect or successful modded-world removal proof.
- Existing standalone tests do not establish a complete Survival acquisition/crafting economy, long-session multiplayer performance or packaged-launcher installation.
- Saved response quotas/history remain conservative; whole-world backups are required. Patrol/projectile reload, multi-outpost soak and real multiplayer remain separately unverified.
- Preserve all ignored worlds/configs, `.local/modern-jdk.json`, disabled QA jars/crash evidence and protected legacy refs. No legacy save conversion is supported.

Follow [AGENTS.md](../../AGENTS.md); the [roadmap](phase-roadmap.md) owns task checkboxes.
