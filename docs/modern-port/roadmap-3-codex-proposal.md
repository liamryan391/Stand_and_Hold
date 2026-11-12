# Roadmap 3: Codex Planning Input

Advisory input for Claude, not the roadmap of record or approval to implement. Claude should validate, challenge, and narrow it; Liam chooses the plan. This proposal uses the M31 internal QA baseline. [STATUS](STATUS.md) owns live project state; [Handoff](../../handoff.md) owns the collaboration process.

## Product Bet

Stand and Hold should make a modest base feel like the beginning of a human resistance, not just a place to store loot. The first payoff is tangible: a post the player built, an analysis the player funded, and defenders who show up because the world progressed. That makes later patrols, stronger units, and base growth matter. It should work in vanilla-plus Survival without an infection mod, a rare world seed, or a wiki open.

| Priority | Use |
| --- | --- |
| P1 - Playability and trust | Remove an operator dependency, prove the critical path, preserve saves and permissions. |
| P2 - Core payoff | Give the established post a meaningful next decision, such as preparing for one attack. |
| P3 - Readability and identity | Improve a presentation or equipment role that players already interact with. |
| P4 - Expansion and ports | Bigger bases, vehicles, aircraft, new integration targets, and Fabric need independent admission gates. |

## Baseline And Gaps

| Observation in the current checkout | Planning implication |
| --- | --- |
| The Command Post, Lab, and Crate have block loot tables. The shipped mod recipes cover equipment, not those blocks. | Finding a checkpoint can skip work, but a player needs a reliable way to build the first two blocks without an operator or a lucky seed. |
| The guide is read-only; general mission start/progress/complete commands are operator tools. | Keep those commands. Give non-operators one narrowly defined gameplay action instead of elevating command permissions. |
| The catalog, mission receipts, research, and global supplies are world-shared. Labs and posts have local storage. | Do not introduce per-player rewards or imply that joining another post restarts the chain. Explain shared status in the UI. |
| The Lab separates selection and Start, takes local supplies, and consumes costs at completion. | Reuse it as-is. Improve the path into the existing screen rather than design a second research flow. |
| Generated checkpoints carry provenance that ordinary placed posts do not. | A recipe must not grant growth rights, initial crate stock, a site receipt, or any changes to structure registration rules. |
| M31 passed its technical baseline and received positive maintainer feedback. Its log does not prove a full grant-free run. | Build a new Survival acceptance test; do not reuse the operator-assisted showcase as evidence of this gate. |

References: [showcase loop](showcase-loop.md), [candidate evidence](alpha-test-candidate.md), and the [deferred admission rules](phase-roadmap.md#deferred-expansion-admission).

## P1 Candidate: First Survival Outpost

**Player promise:** Build or find a post, bring back real samples, complete Initial Sample Analysis, and see the first automatic defenders. The expected path uses a fresh cheats-off Survival world with shipped defaults, ordinary tools, and naturally spawned zombies. No grants, spawn eggs, required commands, prepared structures, or external mods. A found checkpoint is a welcome shortcut, not a dependency. Do not require a specific first-night duration, a raid, an upgrade, or Stage 2.

### 32.1 - Make A Post And Lab Obtainable

- Prefer two vanilla crafting recipes, using existing item/block IDs and the existing resource layout under `data/standandhold/recipe/`. Starting materials to price: planks or stone, iron, and optionally glass for the Lab. Avoid redstone depth, boss drops, sample costs, or a new component item on this path. Compare a low-iron recipe against a more expensive variant in an actual Survival rehearsal; these are ingredient ideas, not approved costs.
- Use vanilla recipe-book discovery through an inventory-trigger advancement. The existing equipment recipe advancement is a local format example. Optionally add one non-reward discovery advancement for the first placed post, only if it solves a verified signposting problem.
- Prefer two separate blocks over a starter kit or a portable structure. It is easier to balance and does not need a new inventory transaction, footprint check, or provenance model. Leave the Supply Crate recipe out unless the loop cannot work without it; the Lab and Post already have local storage.
- Treat built and generated Command Posts consistently for ordinary registration and deployment. Keep the distinction visible for growth: hand-built posts stay useful without becoming generated, and recipes do not add a refund or refill on re-placement.

### 32.2 - Put Early Objectives At The Post

- Add one compact objective row in the existing Command Post screen: world-shared label, current objective, a short condition, and a clear action when it can start. Show active, blocked, already completed, or unavailable rather than bouncing the player into chat. Use the existing screen, translations, and synchronized menu state; keep the guide read-only and update it to point non-operators to the post instead of an operator.
- Prefer explicit acceptance to auto-start or auto-claim on opening. Scope the player action to the five existing early IDs: `recover_parasite_samples`, `establish_field_command`, `stockpile_supplies`, `complete_first_research`, and `early_army_response`. The action identifies the **offered** objective, not an arbitrary ID or a generic "whatever is next now." If another player starts or completes it, resync without starting a different mission from the same click.
- Reuse `BuildingMenu` validation (active menu, owner, distance, same dimension/tile, loaded chunk, valid definitions, server thread, and supported saved data), then call the shared mission transaction. An implementation candidate is a finite button action per eligible ID with a server-side offer check, rather than a general mission packet. Check capacity for synced menu fields before choosing an encoding. The server computes the eligible offer; the client never supplies reward values, progress, or coordinates.
- Resolve through the loaded catalog and enforce prerequisites; missing or incompatible overridden definitions must fail visibly and safely, not be replaced with hidden defaults. Preserve completed receipts, current admin actions, and the existing non-duplication rules. Do not turn this into a new ownership/claims layer.
- Reuse the actor inventory observation after accepting sample recovery. A player who collected three real samples before opening the menu should not be told to drop and re-pick them. Do not count cumulative pickups or automatically consume samples for a mission that currently just observes them. Avoid inventory polling and any new global tick loop.

### 32.3 - Make The First Payoff Clear

The defaults below are from the shipped catalog/config and describe the path to verify, not hardcoded new UI or balancing targets:

| Step | Current rule | What the player needs to see |
| --- | --- | --- |
| Recover samples | Three carried tissue samples; the mission grants 25 points and 10 global supplies once. Default zombies grant 5 points per credited kill and have a 35% sample chance. | Obtainable from actual drops; allies taking the kill may not award player-only credit. Do not set a fixed kill-count promise. |
| Establish and stockpile | Existing post satisfies Establish after Recovery; another 10 global supplies. Stockpile observes 20 global supplies before Lab import. | The difference between global and local, what to do next, and why the Lab might be unfunded. |
| Fund analysis | Existing Lab imports a default 10-supply batch. Initial Sample Analysis needs 10 local supplies and zero samples, then about 60 loaded seconds at defaults. | Explicit Start, a visible work bar, and one completion notice. The sample mission teaches collection; this first analysis does not consume those items. |
| Reach Local Army Response | Stage 1 at 150 points by default. Kills, mission rewards, and loaded generation contribute. | Automatic deployment and unchanged shared caps. Stage 0 may already deploy Survivors; Stage 1 changes the type available to new deployments, not existing units or full reservations. Do not imply a guaranteed immediate Rifleman. |

**Spend-order decision.** The minimum change is to keep the mission economy and teach "stockpile before import." It must also recover: export a full batch from a funded building or build one via existing loaded-post generation. At default +1 local per 1,200 loaded ticks and a 10-unit export batch, waiting for a new batch alone can take about ten loaded minutes. Measure the real early-spend case. If the return path is too opaque or slow, propose a small, testable change to the objective or supply transfer; do not silently add free supplies, double-credit global/local balances, or change costs across the mod. Treat deliberate server overrides (disabled generation or an impossible transfer capacity) as clear diagnostics, not as default tuning to override.

**Payoff definition.** Analysis completed once, Stage 1 legitimately earned, and at least one automatic defender deployed. A stage-appropriate new deployment can be verified in a controlled empty-cap fixture; do not remove existing Survivors during the ordinary playthrough to force it. Growth, first raid timing, and later research are out of scope. Keep an optional discovered-checkpoint route in verification so it is still a useful reward for exploring.

### 32.4 - Evidence Before Calling It Playable

| Gate | Required evidence |
| --- | --- |
| Logic and GameTests | Recipes and discovery resolve on the pinned target; a crafted post registers exactly once without generated provenance. From a valid menu, non-op starts only an eligible early mission. Wrong menu/site, spectator, distance, stale offer, repeated click, missing prerequisites, and reloaded catalog reject safely. Two clients with the same offer get one reward without unintentionally accepting the next objective. Pre-carried samples are recognised once; pickup repetition is not a currency source. |
| Commands and persistence | Existing operator debug commands are unchanged. `runGameTestServer` and `test-restart.ps1` cover mission receipts, local/global supply conservation, partially worked Lab, exactly-once completion, and unchanged deployment caps after restart. Include an existing-world fixture with an active or completed starter mission. No old ID or world format migration just for onboarding. |
| Client and artifact | `runQaClient` renders the post objective states (ready, active, blocked, completed) and the Lab at normal/large GUI scale. Review actual captures for text fit and stockpile/import clarity. Java 25 clean build, normal-jar metadata/QA exclusion, and Windows CI pass. If testing a packaged install, distinguish the exact normal jar from a dev run. |
| Maintainer playthrough | One fresh Survival attempt with cheats off: craft or acquire legitimate materials, collect real drops, fund the Lab, earn Stage 1, observe defenders, then save/reopen. Record duration by checkpoint, unclear steps, and the first reward that felt useful. A fixed-seed or automated fixture is separate mechanical proof. Report omissions as omissions, not a full pass. |

## Suggested Follow-On Lanes (Not Assigned Milestones)

| Priority | Small candidate | Reuse and boundary |
| --- | --- | --- |
| P2 | **Defend an outpost that matters.** One readable warning and one optional defend-or-reinforce decision; investigate why natural defence is hard to observe in ordinary play. | Existing threat, raid reservations, duty modes, support costs, and cooldowns. Verify hostile quota, blocked terrain, pause/unload/restart, and no repeated warnings. No forced waves or new global director. |
| P2 | **Make growth and recruitment useful.** Pick either one mixed-role roster choice or one additional bounded outpost upgrade, based on what players miss first. | Existing reservations, stage/research gates, local supply costs, layout provenance, and receipts. Keep automatic growth opt-in; test obstruction, recovery, and shared caps. |
| P3 | **Improve one visible equipment or defence role.** Options: a clearer original launcher animation, distinct armour detail, or a watch alert with an actual observable purpose. | Existing editable models, vanilla rendering, and client screenshot fixtures. Validate readability and server authority separately. No copied assets, new weapon catalogue, or library assumed necessary. |
| P1 for a distribution claim | **Verify an exact candidate in its target install.** Fresh normal-jar client and dedicated server, authenticated multiplayer where claimed, and save/restart. | Existing package and two-client fixtures. Keep remote auth, launcher setup, and public release as separate decisions; a CI artifact is not a release. |
| P4 | **Optional compatibility, transport, and ports.** Admit one precise artifact or one small ground utility vehicle at a time only after the core payoff is proven. | Tag/registry integration first, originals only, bounded vehicle tests, separate pin/library approval. Aircraft, large armies, and Fabric do not enter on speculation. |

## Questions For The Planning Review

1. Is the no-operator path achievable with the two recipes and one menu action, or does a concrete blocker require a different first slice?
2. Can the player recover after spending supplies early without an unreasonable wait? What observation would justify changing the economy?
3. Which existing test proves each claim, which new test is needed, and which one observation genuinely needs Liam?
4. What will be explicitly deferred so the first implementation stays small, with every admin tool still available?
