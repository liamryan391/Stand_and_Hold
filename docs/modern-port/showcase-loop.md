# First Playable Test Loop

A roughly 15-minute **Creative/operator-assisted technical alpha rehearsal**, not a complete Survival campaign. Use a fresh modern world and no external mods. The legacy 1.12 alpha is a different build; the checked modern Contagion artifact is unsupported. See [Compatibility](compatibility.md).

## Alpha Tester Handoff

- Testing policy (Liam, 2026-10-27): Liam is the alpha tester while the mod develops. Independent unfamiliar testers and their feedback are deferred to beta after a stable alpha. Record alpha results as maintainer evidence, not independent usability proof; no new tester is required to complete the M12 alpha gate.
- Liam (or a coordinator): verify the exact [candidate target](alpha-test-candidate.md), shipped config and no external mods, then prepare only the infrastructure below in a fresh disposable world. Preserve all earlier worlds/configs; do not reuse the maintainer rehearsal save for an earned-only run.
- Use this play sheet and the in-game help/guide. Setup time is separate from the roughly 15-minute attempt; record actual duration if longer. Record pauses and any extra hints or commands; do not silently perform missions or grant resources. A separate observer is optional for maintainer QA.
- Allowed assistance is the documented fixture, vanilla weapon/zombie eggs, mission **starts**, inspection and one event trigger. Samples, progression, research completion and supplies must be earned. Do not add human spawn eggs or bypass real cooldowns/caps.
- At about 15 minutes, record the checkpoint reached and what you would do next. Incomplete/blocked results are useful; they are not passes and must not be hidden by debug grants. Continue the missing checks as needed, finish save/reload and return the scorecard below, with original logs/screenshots if available. The time target is an observation, not an alpha deadline or proof of first-time usability.

## Prepare The Disposable World

Launch `runShowcaseClient` using the pinned Java selector/user home from [Building](building.md). It uses `run/showcase-client`, separate from normal development and compatibility worlds. Create a Creative Superflat world with commands enabled; name it clearly, for example `M12 Showcase Rehearsal`. Keep default Stand and Hold balancing/config for this test.

Run these one-time fixture commands on clear grass:

```text
/difficulty normal
/gamerule minecraft:spawn_mobs false
/gamerule minecraft:advance_time false
/time set night
/standandhold structure checkpoint
```

The gamerules isolate the test from random vanilla spawning and daylight burning; they are saved only in this disposable world, not changed mod defaults. Eggs, manual summons and the mod's raid controller still work. A new checkpoint registers a Command Post and contains an empty Research Lab plus a Supply Crate with 2 LOCAL supplies. Use its included Lab; older existing checkpoints are not retrofitted, so place a Lab manually there if needed. This operator-provided infrastructure is intentional QA setup: Survival acquisition of every core building is not implemented.

Use only an ordinary Creative melee weapon and zombie eggs for the sample step. **Do not use Creative samples, `/give` samples, `addpoints`, `setstage`, `supplies add`, research completion or mission progress/completion commands** to bypass the loop. Enemy kills by defenders do not give the default player-only reward.

Start `/debug start` once the world is ready if available; finish with `/debug stop` before leaving. This is a local integrated-server sample, not a hardware-independent benchmark. Spend about 15 real loaded minutes, with no `/tick sprint`; note menu pauses and time away from the post.

## Play Sheet

1. Run `/standandhold help`, `/standandhold guide` and `/standandhold status`. Expect 0 points/Stage 0 initially, apart from a loaded Command Post's slow generation. The guide is read-only; it does not start missions, refresh rewards or grant anything. With chat open, its coloured Start line can fill the command; review it and press Enter yourself. No command executes just from reading/clicking the guide.
2. Start `/standandhold mission start recover_parasite_samples`. Kill spawned **zombies** normally, not with `/kill`. Keep test zombies away from defenders until this step is finished so they do not take the killing blow. Each player-credited death gives 5 points; sample chance is 35%, not guaranteed. Carry 3 samples simultaneously. Expect the mission's once-only +25 points/+10 GLOBAL supplies. Re-picking the same item is not cumulative progress.
3. Run the guide again. Start `/standandhold mission start establish_field_command`. The already registered checkpoint can satisfy it immediately: +25 points/+10 global supplies. Check `/standandhold commandpost nearest` and open the Command Post menu.
4. Before spending/importing supplies, start `/standandhold mission start stockpile_supplies`. The 20 GLOBAL supply balance satisfies it and gives +25 points. LOCAL storage does not count. Start `/standandhold mission start complete_first_research` before working in the Lab.
5. Open the Lab. In Supplies mode, **Import once** to transfer the default 10-supply batch from global to local storage. Use Next research until **Initial Sample Analysis** is selected, then Start research. Selection alone never starts work. Stay within loaded range for about 60 running seconds: 12 work units at 5 seconds each. Pause research preserves work; Clear target resets local work. Closing the menu does not pause the lab, but the singleplayer pause menu can pause the world.
6. Expect Completed/full bar and exactly one completion chat message. The first analysis spends 10 LOCAL supplies, costs **0 samples**, and gives +10 points. Its active mission gives +25 points/+10 global supplies. After the recovery mission is complete, test Samples mode Store/Take with the real recovered items; this first research does not consume them. A later research uses samples.
7. Start `/standandhold mission start early_army_response`. Reach Stage 1 at 150 points through kills, mission/research rewards and loaded-post generation. With the shipped chain, eight player zombie kills plus those four mission rewards and the first research reach 150 before passive generation. Lucky early drops can require a few more kills. Use mission status to refresh an already-satisfied active stage objective; its reward is +25 points. Do not force the stage with a command.
8. Observe deployment near the operational post: about one attempt per 60 loaded seconds, at most 3 assigned defenders at this post and the existing area limit of 8. New available deployments use the stage-appropriate tier; existing Survivors do not turn into Riflemen. Survivors may receive a blade at Stage 1 + Initial Sample Analysis. No spawn eggs are needed for the deployment check. Avoid player friendly fire.
9. Start `/standandhold mission start defend_outpost`. Check `/standandhold event status`, prepare video recording, then use `/standandhold event raid` if a natural raid has not already occurred. At Stage 1 the default debug wave is one zombie, subject to terrain/area limits. Repeating the command must respect the real cooldown/reservations. Survive/observe one raid and record human combat; only player/human kills of reserved raiders count toward this mission. The mission needs 5 kills across waves, so **one wave need not finish it within this session**. Natural raids may already have set a cooldown; that is not a failure or permission to reset it.
10. Optional storage check: after stockpiling/research, import/export a 10-supply batch through the Crate and confirm its original 2 local supplies are not duplicated/refilled. Optional upgrade: after 150 points and Initial Sample Analysis, import 20 supplies into the Command Post and upgrade to Level 2; it spends local supplies, not the minimum point requirement.
11. Finish `/debug stop`, note points/stage/supplies, active mission and local storage. Save/quit to title, reopen the world, and check the same values/Completed research/structures/defenders without a refill or spawn flood. Finally **Quit Game**, not just Quit to Title.

Main Base discovery, five-wave defence completion, Stage 5 activation and all higher research are outside this short slice. The player should nevertheless know what remains from the guide/mission list.

## If Something Blocks Progress

| Symptom | Check |
| --- | --- |
| No points/drops | Default mapping is zombie-only, not all hostile mobs. Kill attribution must be a player; drops are random and respect mob drops. Inspect threat config/operator compat status. |
| Recovery stuck | Mission must be started and 3 samples carried at once. Samples already banked locally do not count as carried. |
| Stockpile stuck | Check GLOBAL supplies before importing into buildings. If research costs were spent too early, a loaded Command Post generates local supplies slowly; export full batches back to global, then start/refresh stockpiling. Do not grant/reset rewards to conceal the extra wait. A default export needs at least 10 local supplies; the checkpoint crate's initial 2 cannot be exported alone. |
| Research blocked | Correct target, Stage/prerequisites, LOCAL costs and explicit Start. Global balance alone does not fund a lab. Initial analysis uses 10 local supplies and no samples. |
| Research seems frozen | Stay in loaded range; close pause menus. Next research/Clear target is not a pause/resume control. Read Work/Left/bar and server status. |
| Research finished but the next mission is blocked | Research does not automatically start its mission. Start `complete_first_research` normally; existing completed research is recognised without another cost/reward. Then start `early_army_response`. No mission completion/progress grant is needed. |
| No new Rifleman | Post/area quota may already be filled with Survivors. Existing units do not upgrade tier; do not delete saved quotas to force spawns. |
| Raid refused | Read the specific quota or terrain refusal. The default area cap counts 8 hostile mobs in the post's 32-block box, including ambient slimes; a bounded inspection limit also refuses dense sites. Turning natural spawning off does not remove mobs already present. Clear ambient monsters normally, keep natural spawning off, and wait the reported failed-spawn backoff (default 400 unpaused ticks/20 seconds). Use Normal difficulty and open dry loaded ground 8-12 blocks from the post. Do not raise caps/reset threat to bypass safeguards. |
| Mission list differs | Datapack/config changes alter the catalog. The in-game guide reads loaded definitions; this sheet describes shipped defaults. |

In a **disposable QA world only**, with **zero reserved raiders**, briefly using `/difficulty peaceful` removes existing ambient monsters; restore `/difficulty normal`, keep natural spawning off and let the failed-spawn backoff expire before the raid command. Do not do this during an active/reserved raid or in a valuable world. It is test-fixture cleanup, not an earned defence, quota reset or cooldown bypass. Never turn natural spawning on just to enable a mod raid: the raid controller does not require it.

## Optional Admin Research Shortcut

Outside the earned-resource test, the existing operator command completes eligible sample-free research immediately, without waiting for lab work:

```text
/standandhold research list
/standandhold research complete parasite_samples
/standandhold research complete standandhold:field_communications
```

This requires operator permission and still checks stage, prerequisites and **GLOBAL** supply costs. Repeating a completed ID never repeats its cost or reward. It completes shared world research, not a building upgrade, and does not spend supplies stored in a lab. Entries with physical sample costs (such as Special Division Training) deliberately refuse this command: use a funded Research Lab so samples are consumed safely. There is no new force/bypass command. Command-based completion is useful for separate admin exploration, not proof that the timed lab/earned loop passed.

## Record The Rehearsal

Record tester familiarity, real duration, command dependence, confusing messages, unintended grants and exact failures. A familiar-maintainer pass can satisfy the revised alpha gate but is **not** independent new-user evidence. Capture clean F2 views of the site/units, completed Lab and real defence; record a short real clip before triggering the raid and provide its path. Preserve original files and avoid private chat/desktop content.

### Fill-In Gate Scorecard

Tester/name or alias: [record]. Prior familiarity: [record]. Date: [record]. OS/Java/Minecraft/NeoForge: [record]. Jar filename/hash and mod list/config deviations: [record]. World name: [record]. Setup excluded: [record]. Attempt start/end, pauses and active duration: [record].

Replace each `Not run` with `Pass`, `Fail` or `Blocked` only from observed results. Record values/details, not just an overall pass. Liam may self-report the alpha run; a coordinator, if present, must record every extra hint/command and any Creative resource use.

| Check | Result | Values or evidence |
| --- | --- | --- |
| Fresh baseline; help/guide understandable; blue Start fills chat and needs Enter | Not run | Starting points/stage/global supplies; hints needed |
| Normal player zombie kills award points and drop samples; carry 3 real drops for recovery | Not run | Kill count, carried count, reward/status frame; no Creative samples |
| Establish Field Command, then Stockpile Supplies **before** lab import | Not run | Both missions completed; 20 global supplies before import |
| Start Complete First Research mission; import 10 local supplies; select/Start/Pause/resume analysis | Not run | Local/global balances, 12/12, one completion/reward |
| Earn Stage 1 at 150+ points and complete Early Army Response without grants | Not run | Points/stage, mission status; note legitimate passive generation |
| Automatic defenders and an actual outpost attack observed | Not run | Defender count (post cap 3), event type/wave; a cooldown refusal alone is not an attack |
| Save/reopen keeps points, missions, research and storage without reset/duplicate rewards | Not run | Before/after values, latest.log and screenshots |

Tester explanation: what would you do next, where do lab supplies come from, and how do you start research? [record]. Confusing steps, extra hints/commands, failures and skipped checks: [record]. Alpha gate assessment, actual duration and unresolved blockers: [record]. A completed maintainer scorecard still does not become independent evidence; use a separate unfamiliar-tester attempt for beta usability feedback.

Observed rehearsal results are recorded in [STATUS.md](STATUS.md); durable limitations belong in [Known Issues](../../KNOWN_ISSUES.md). Independent usability is deferred to beta, not passed by this alpha checklist. Do not call the slice public-ready solely because a familiar developer completed the checklist; publication requires separate approval and release checks.

For capture planning, the default raid cooldown is 6,000 ticks: five minutes of advancing world time at 20 TPS. Singleplayer pause stops that clock; unloading a post prevents new events but does not freeze its saved cooldown timestamp. A natural raid can consume the cooldown before an admin trigger. Start recording before the first trigger and check event status; repeated cooldown refusals are expected safety evidence, not a need to complete five waves. A guarded QA cooldown tool was suggested in rehearsal feedback; no bypass is implemented by this guide.

## Qualified Alpha Verification - 2026-10-27

Liam reported the earned core passed in a fresh Superflat world: real sample recovery, command-post/stockpile missions, funded lab analysis and earned Stage 1. Logs show the first five mission receipts before later admin grants; the point balance was already 189 before the +600,000 grant. Starting the first-research mission after completing the lab safely recognised existing research. The 19m36s client duration includes setup, pauses and later admin exploration, not a timed independent-usability result or entirely grant-free final save.

The two focused client raid attempts safely refused at crowded/unsupported sites (8m26s/5m24s), not passed attacks. Separate controlled dedicated tests reproduced both refusal reasons and successful tagged raids under unchanged caps with vanilla natural spawning disabled. Real NPC/raider damage, a human-credited kill, reservation release and saved cooldown/counters were observed across restart. Those fixtures used manual QA NPCs, temporary vanilla chunk tickets and tick sprinting: they are functional server evidence, not earned progression, automatic-cap, performance or client-visual proof.

Liam explicitly accepted this combined evidence for the M12 alpha merge, keeping broad-terrain/client raid QA as a known limitation. No complete new profiler result, packaged installation, real multiplayer or independent tester pass is claimed. Later independent feedback belongs to beta; this acceptance is not permission to publish a release.
