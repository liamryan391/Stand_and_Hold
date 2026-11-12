# Modern Port Phase Roadmap

This is the single phase roadmap for the NeoForge rewrite: foundation work M0-M21, Roadmap 2 M22-M31 and the Roadmap 3 draft from M32. Existing identifiers stay stable; larger work is split prospectively, not renamed or silently added to a completed phase.

## How To Track Work

- Phase M1 uses steps `1.1`, `1.2`, and so on. A larger step uses `1.1.1`, `1.1.2`, and so on. Phase M0 uses `0.x`.
- Keep existing identifiers stable. Add a subtask only before work genuinely needs splitting, never retroactively or deeper than `x.y.z`; retain historical task IDs.
- `[x]` means completed and verified; `[ ]` means outstanding. A parent is complete only when all its subtasks and verification pass.
- Keep live state in [STATUS.md](STATUS.md) and append concise observed results to [Progress Log](progress-log.md), following [AGENTS.md](../../AGENTS.md). Do not mark a build or launch passed without observing it.
- Tick tasks as verification passes. Commit logical changes; Git history records hashes, and the session report records publication.
- Later phases describe planned scope. Expand large steps before implementation as APIs, dependencies, and testing needs become clearer.

Current position: maintained only in [STATUS.md](STATUS.md).

## Priority Scale

- P0 - Preservation and decision gates: must happen before risky changes.
- P1 - Critical foundation and alpha reliability: correctness, install/session safety and evidence needed to trust the playable build.
- P2 - Core gameplay: important systems that create the Stand and Hold loop.
- P3 - Differentiators: major quality and identity features that make the mod stand out.
- P4 - Later expansion: large or risky systems to defer until the foundation is stable.

## Phase M0 - Preserve And Decide

Priority: P0

- [x] **0.1 Verify preservation and repository access.**
  - [x] **0.1.1** Check the working tree, current branch, tracking branch, and GitHub remote.
  - [x] **0.1.2** Compare live `main` and `1.12` references with local commits; record the preserved commit.
  - [x] **0.1.3** Verify GitHub authentication and repository permission without modifying either remote branch.
- [x] **0.2 Establish durable project notes.**
  - [x] **0.2.1** Store roadmap, project memory, research, and progress notes in `docs/modern-port/`.
  - [x] **0.2.2** Record NeoForge first, Fabric later, and `main` as the eventual modern branch.
  - [x] **0.2.3** Define numbered work steps, priorities, verification gates, and next-step tracking.
- [x] **0.3 Select and record the exact modern toolchain.**
  - [x] **0.3.1** Check official Minecraft/NeoForge releases and MDK availability. Compare the proposed 26.3 track with stable alternatives; record release status and source URLs. Evidence: [Release Verification](release-verification.md), 2026-10-07.
  - [x] **0.3.2** Check matching Minecraft AND loader support for likely animation/AI libraries and at least one target threat mod. Separate required dependencies from optional future integrations. Evidence: [Ecosystem Compatibility](ecosystem-compatibility.md), 2026-10-07; published artifacts/manifests checked, no combined runtime test.
  - [x] **0.3.3** Select exact Minecraft, NeoForge, Java, Gradle, and build-plugin versions from a compatible official MDK. Record the rationale and local JDK selection; resolve the PATH/JAVA_HOME mismatch for the build process. Evidence: [Selected Modern Toolchain](toolchain.md), 2026-10-07; local selector verified, modern build deferred to M1.
    - [x] **0.3.3.1** Recheck official release/MDK metadata and Gradle Java runtime compatibility; select exact versions and record beta-loader risk.
    - [x] **0.3.3.2** Configure a reusable process-local Java 25 selector with an ignored machine-specific path; preserve global Java 8 settings.
    - [x] **0.3.3.3** Verify saved selection and invalid-input handling, record evidence, and update the current handoff without changing the legacy build.
  - [x] **0.3.4** Record Windows build commands, IDE Gradle JVM setup, and the rule for choosing a fallback if build verification fails. Evidence: [Windows Build And IDE Setup](building.md), 2026-10-13; documentation/preflight verified, actual modern build and IDE sync remain M1 gates.
    - [x] **0.3.4.1** Separate current legacy commands from future modern wrapper/build/run commands; guard against the legacy wrapper and document expected outputs.
    - [x] **0.3.4.2** Document project SDK, Gradle JVM/wrapper/user-home settings, and JVM override troubleshooting using official sources.
    - [x] **0.3.4.3** Define failure classification, minimal reproduction, maintainer approval, and full verification before adopting a coherent fallback toolchain.
    - [x] **0.3.4.4** Check PowerShell examples, legacy rejection, links, and roadmap consistency; record what was not run and advance the handoff.
- [x] **0.4 Finalize foundation scope and dependency policy.** Evidence: [Foundation Scope And Dependency Policy](foundation-scope.md), 2026-10-14. M1 is NeoForge-only with no added runtime libraries or approved exceptions; documentation and unchanged implementation checks passed.
  - [x] **0.4.1** Freeze the minimal M1 deliverable and map gameplay, art, vehicles, and multi-loader work to their later phases.
  - [x] **0.4.2** Define separate modern worlds, local-file preservation, no legacy world/config migration, and future modern-schema safety requirements.
  - [x] **0.4.3** Classify and constrain dependencies.
    - [x] **0.4.3.1** Separate platform/runtime dependencies, required MDK build tooling, and dev/test additions; record no extra M1 library exceptions.
    - [x] **0.4.3.2** Defer AI/animation/helper libraries and keep threat integrations optional, side-safe, and independently testable.
    - [x] **0.4.3.3** Require exact artifact/provenance, licence, transitive/side checks, runtime evidence, and maintainer approval for scope exceptions.
  - [x] **0.4.4** Check policy/roadmap consistency, links, unchanged implementation/toolchain, and publication evidence; record the exact handoff.
- [x] **0.5 Prepare the scaffold handoff.**
  - [x] **0.5.1** Review the consolidated M0 decisions (pins, setup, scope, dependency/save policy) and confirm their commit/push status separately from the scaffold. Evidence: [M0 Decision Review](archive/m0-decision-review.md), 2026-10-14; decisions required no correction and all four checkpoints are published on `origin/main`.
    - [x] **0.5.1.1** Review the canonical decisions and their scope/setup/verification boundaries for consistency.
    - [x] **0.5.1.2** Fetch GitHub and verify checkpoint ancestry, matching main refs, preserved `1.12`/alpha-tag refs, and unchanged legacy implementation.
    - [x] **0.5.1.3** Record review evidence, validate documents/task states, and publish the progress handoff to 0.5.2.
  - [x] **0.5.2** Recheck remote refs and user changes; prepare `codex/modern-neoforge-scaffold` from the agreed `main` baseline, with a merge back to `main` after verification. Evidence: [Scaffold Branch Handoff](archive/scaffold-handoff.md), 2026-10-14; reused checkout, preserved local files, and published M0 completion checkpoint.
    - [x] **0.5.2.1** Refresh GitHub refs, confirm a clean published baseline, and recheck preserved branch/tag and work-branch availability.
    - [x] **0.5.2.2** Inventory ignored/untracked local files and validate the saved JDK path without moving or deleting user content.
    - [x] **0.5.2.3** Create the requested local work branch from the published baseline and record the reused checkout path.
    - [x] **0.5.2.4** Validate and publish the M0 completion record, advance/switch the work branch to that checkpoint, push its upstream, and verify preserved refs/local files and clean status.

Exit gate:

- The old code is preserved locally and on GitHub, and planning work is committed.
- Exact toolchain versions, sources, dependency policy, and scaffold scope are recorded.
- Implementation proceeds to M1; build proof is recorded there, not assumed here.

## Phase M1 - Clean NeoForge Scaffold

Priority: P1

- [x] **1.1 Prepare the legacy-to-modern file transition on the work branch.**
  - [x] **1.1.1** Recheck the preserved `1.12` commit and inventory legacy build, source, resource, script, and CI files. Evidence: [Legacy Transition Inventory](legacy-inventory.md) and its 199-file manifest, 2026-10-14; source/build/CI remain unchanged.
    - [x] **1.1.1.1** Refresh refs and verify preserved branch/tag, published work-branch baseline, and unchanged legacy implementation.
    - [x] **1.1.1.2** Inspect build/wrapper/identity files, both scripts, and both workflows; record current values and transition ownership.
    - [x] **1.1.1.3** Inventory every Java/resource path and identify legacy startup/API/resource coupling and later-phase references.
    - [x] **1.1.1.4** Record exact paths/blob IDs/modes and proposed dispositions; validate completeness, protected local files, and current handoffs before publishing the work branch checkpoint.
  - [x] **1.1.2** Preserve modern planning notes and project identity; move legacy-only documentation references to the `1.12` branch in the modern README. Evidence: rewritten root README, labelled changelog history/current port issues, preserved modern notes, and verified legacy documentation paths, 2026-10-14.
    - [x] **1.1.2.1** Rewrite the root README for the selected modern direction, keeping name, mod ID, package/group, author, and honest preparation status.
    - [x] **1.1.2.2** Route all ten legacy guides and the legacy README/issue/changelog references to the preserved `1.12` branch.
    - [x] **1.1.2.3** Preserve changelog history with an explicit Forge 1.12.2 label and separate current port limitations from legacy issue reports.
    - [x] **1.1.2.4** Verify links, retained planning notes/identity/version, unchanged runtime/local files, and the work-branch publication handoff.
  - [x] **1.1.3** Replace the inventoried legacy implementation/build files with the chosen MDK layout; leave local worlds and unrelated user files alone.
- [x] **1.2 Configure the modern build.**
  - [x] **1.2.1** Pin the MDK wrapper, Java toolchain, build plugin, Minecraft, and NeoForge versions.
  - [x] **1.2.2** Set `standandhold`, `Stand and Hold`, and `com.liamryan.standandhold`; configure metadata, resource expansion, and run profiles.
  - [x] **1.2.3** Update build instructions/helper and CI to use the selected JDK; upload the correct normal mod jar.
- [x] **1.3 Add the minimal mod entry point.** Add constants, logger, config shell, and startup logging; keep client initialization separate from common/server code.
- [x] **1.4 Verify the scaffold.**
  - [x] **1.4.1** Run `./gradlew --version` and `./gradlew clean build --no-daemon --stacktrace` with the chosen JDK (Windows: `.\gradlew.bat`). Inspect jar metadata.
  - [x] **1.4.2** Run `runClient`; confirm main menu, Mods list, and a new test world without external threat mods.
  - [x] **1.4.3** Run `runServer` in an isolated test directory through readiness and clean shutdown. Record an EULA stop as incomplete launch verification, not a successful server test.
- [x] **1.5 Commit the scaffold, merge the verified work into `main`, and record local/CI results and push status.** Confirm `1.12` is unchanged.

Exit gate:

- The clean build passes and the expected jar exists.
- Client and dedicated server launch results are recorded; missing runtime checks remain outstanding.
- `main` contains the verified modern scaffold and `1.12` remains preserved.

## Phase M2 - Persistence And Progression Core

Priority: P1

- [x] **2.1 Rebuild progression definitions.** Port stage identities and configurable thresholds; define bounds and what setting a stage does to points.
- [x] **2.2 Implement server-owned SavedData.**
  - [x] **2.2.1** Define one global owner across dimensions, a versioned schema, and safe defaults for absent fields.
  - [x] **2.2.2** Add non-destructive migration hooks and dirty marking only when values change.
  - [x] **2.2.3** Add HumanPointManager operations with amount validation and overflow protection.
- [x] **2.3 Add Brigadier `status`, `addpoints`, and `setstage` commands.** Validate arguments and enforce admin permissions for mutations.
- [x] **2.4 Verify and document progression.** Build; test stage boundaries, invalid inputs, dimension changes, save/reload, and dedicated-server commands. Record results before committing.

Exit gate:

- Points/stage save and reload.
- Commands work on client-integrated and dedicated server.

## Phase M3 - Research, Supplies, And Missions Foundation

Priority: P1

- [x] **3.1 Define research data.** Add stable IDs, requirements, prerequisites, costs, and unlocks; safely reject duplicate/malformed definitions and cycles.
- [x] **3.2 Add supplies and research transactions.**
  - [x] **3.2.1** Persist global supply totals and validate all add/spend operations.
  - [x] **3.2.2** Check prerequisites and costs before completing research; apply costs, completion, and rewards once on the server.
  - [x] **3.2.3** Expose minimal status/list/admin commands; keep physical sample requirements ready for M5.
- [x] **3.3 Rebuild small mission definitions and state.**
  - [x] **3.3.1** Define a short objective chain that uses planned samples, buildings, research, supplies, and defence hooks.
  - [x] **3.3.2** Persist active progress, completion, and reward state; handle unknown IDs without deleting unrelated saves.
  - [x] **3.3.3** Add readable list/status/debug commands and meaningful progress feedback without repeated completion rewards or recursive callbacks.
- [x] **3.4 Verify foundation data.** Build; test save/reload, insufficient costs, repeated completion, and malformed/missing data. Gameplay hooks are verified when their owning phases land.

Exit gate:

- Complete first research by command.
- Supplies save/reload.
- Mission state saves/reloads and rewards only once.

## Phase M4 - Core Blocks And UI Shell

Priority: P1

- [x] **4.1 Register Field Command Post, Research Lab, and Supply Crate blocks/items.** Add simple original resources, names, creative entries, and basic access for testing.
- [x] **4.2 Implement building state and storage.**
  - [x] **4.2.1** Save command-post level/storage and dimension-aware registration; unregister on removal and ignore unloaded positions during cleanup.
  - [x] **4.2.2** Save lab target/progress/storage and supply crate state. Expose a bounded sample-consumption path for M5.
  - [x] **4.2.3** Add bounded passive points/supply generation and research work, respecting loaded state, intervals, and configurable caps.
  - [x] **4.2.4** Implement upgrade requirement checks and local/global supply transfer without duplication or partial spending.
- [x] **4.3 Add basic menus, screens, and server actions.**
  - [x] **4.3.1** Display progression, research requirements, storage, and blocked/completed states clearly.
  - [x] **4.3.2** Validate player distance, dimension, block entity, active menu, and action values; change state on the server thread and sync only needed data.
  - [x] **4.3.3** Keep client screen/render classes out of dedicated-server initialization.
- [x] **4.4 Verify buildings.** Build; test placement/removal, unload/reload, GUI reopen, two-player transfers, rejected remote actions, and research/mission hooks. Local/runtime and Liam's integrated exit checks passed; competing transactions and packet guards were checked, but actual two-client/network-fuzz coverage remains explicitly unverified in STATUS.md and the buildings QA recipe.

Exit gate:

- Blocks place, save, load, and open UI.
- UI reads server data correctly.

## Phase M5 - Samples And Threat Reward Hooks

Priority: P2

- [x] **5.1 Register Parasite Tissue Samples with original placeholder resources and lang entries.**
- [x] **5.2 Add optional threat identification and rewards.**
  - [x] **5.2.1** Define tags/config mappings with `minecraft:zombie` as a clearly documented test default.
  - [x] **5.2.2** Award points and sample drops once per qualifying server death; define kill attribution and ignore null, malformed, or absent registry entries safely.
  - [x] **5.2.3** Connect sample pickup missions and actual lab sample consumption, including inventory and full-storage edge cases.
- [x] **5.3 Verify the first resource loop.** Build; test configured/unconfigured kills, reward/drop boundaries, pickup progress, lab consumption, and operation without other mods. Automated rules and dedicated reward/drop/lab/save tests passed; Liam reported the integrated sample/mission/menu/full-inventory/reload checks passed. The reported missing lab chat feedback received a small follow-up fix; its visual retest and two-client/external-mod coverage are tracked in STATUS.md.
- [x] **5.4 Document mappings and missing-mod behavior.** Keep external IDs inactive until verified for the selected version/loader.

Exit gate:

- Default test mapping works.
- Invalid registry IDs/tags do not crash.

## Phase M6 - Human NPC Foundation

Priority: P2

- [x] **6.1 Register the base human NPC and initial survivor/army tiers.** Configure stats, attributes, stage requirements, spawn eggs, and temporary original visuals.
- [x] **6.2 Add simple, stable AI.**
  - [x] **6.2.1** Evaluate the chosen version's vanilla Goal/Brain APIs before selecting any helper library. Pinned platform sources support simple goals, bounded target queries and server-side melee; no additional library is needed.
  - [x] **6.2.2** Add bounded wander/look/target tasks; reuse threat mappings and avoid attacking players by default.
  - [x] **6.2.3** Handle removed targets, unreachable positions, unloaded regions, and server/client separation.
- [x] **6.3 Add controlled command-post deployment.** Enforce stages, per-post/area caps, intervals, and valid spawn locations; document test egg behavior separately.
  - [x] **6.3.1** Save bounded defender reservations so unloaded NPCs cannot free post quota; preserve existing modern data.
  - [x] **6.3.2** Select an unlocked tier and validate bounded loaded spawn candidates, area caps and cancellation.
  - [x] **6.3.3** Wire tile-local intervals and release reservations only on confirmed removal or loaded missing homes.
- [x] **6.4 Verify NPCs.** Build; test spawning, combat, stage gates, several nearby posts, unload/reload, missing mappings, and dedicated-server loading. Automated/server evidence and Liam's integrated NPC and requested lab-control retest passed; extended multiplayer/external-mod limits are recorded in STATUS.md.

Exit gate:

- Defender spawns, targets configured enemies, and does not attack players by default.
- No dedicated server renderer crashes.

## Phase M7 - Bases, Structures, And World Presence

Priority: P2

- [x] **7.1 Choose bounded structure placement and config.** Define allowed dimensions, spacing/chance, footprint, terrain limits, and debug command policy.
- [x] **7.2 Build a Small Army Checkpoint.** Add an original lightweight layout with an entrance, lighting, protected command post, and restrained supply access.
- [x] **7.3 Build a distinct Main Base foundation.** Add simple command/research/supply zones and stage-based activation without a large base expansion.
- [x] **7.4 Make placement and registration safe.**
  - [x] **7.4.1** Preflight relevant terrain, height, fluids, and occupied space; use the chosen worldgen context without triggering extra chunk loads.
  - [x] **7.4.2** Register dimension/position data only for successfully placed required blocks; report failed placement clearly.
  - [x] **7.4.3** Clean stale loaded command-post/main-base records; preserve unloaded records and completed mission history.
- [x] **7.5 Verify world presence.** Build; test debug placement and natural generation, unsuitable terrain, break/removal, save/reload, and discovery missions. Update structure documentation.

Exit gate:

- Structures generate safely.
- Important block positions register and survive reload.

## Phase M8 - Art Pipeline And Identity

Priority: P2/P3

- [x] **8.1 Write the visual style guide.** Define silhouettes, tier colours/markings, UI readability, and original asset ownership/source records.
- [x] **8.2 Establish the Blockbench pipeline.**
  - [x] **8.2.1** Verify animation library support for the pinned loader/version; select one only if required.
  - [x] **8.2.2** Validate one model's UV layout, texture dimensions, export, and in-game animation before making variants.
  - [x] **8.2.3** Keep editable source models and export instructions in the repository or a documented linked asset location.
- [x] **8.3 Create distinguishable visuals for implemented human tiers and core items/blocks.** Add further tier art when those units are implemented.
- [x] **8.4 Polish existing GUI labels and layout.** Test long research names, blocked states, and supported GUI scales.
- [x] **8.5 Verify art in game and build.** Check texture fit, missing resources, animation, held equipment, lighting, and dedicated-server isolation; capture reference screenshots. Observed evidence and remaining visual coverage are in STATUS.md; long-title limits have pure checks, while held-item/custom-title visual tests remain separate coverage.

Exit gate:

- Each unit tier is readable in screenshots.
- No missing textures in the first playable slice.

## Phase M9 - Combat Equipment

Priority: P2/P3

- [x] **9.1 Define a small equipment set and configurable stage/research gates.** Record costs and intended roles using Minecraft-scale combat.
- [x] **9.2 Add an anti-threat melee weapon and initial armour tiers.** Include attributes, recipes/test access, original models, and lang entries.
  - [x] **9.2.1** Register the blade, two four-piece armour tiers and a shared server-side equip/use gate with synced balancing config.
  - [x] **9.2.2** Add original item/worn armour resources, basic recipes and regression checks without a runtime art library.
- [x] **9.3 Add a simple ranged prototype.**
  - [x] **9.3.1** Implement server-owned projectile spawn, cooldown, hit detection, and damage attribution.
  - [x] **9.3.2** Add bounded visual/audio feedback and safe use by one existing NPC tier if practical.
  - [x] **9.3.3** Verify unlock checks and friendly-target behavior; defer advanced reload/ammo mechanics.
- [x] **9.4 Verify equipment.** Build; test player/NPC use, multiplayer damage, cooldowns, recipes, stage gates, and save/reload. Evaluate external combat integrations later only if useful.

Verification scope: local rule/resource tests, dedicated launch/restart and Liam's singleplayer equipment/gate/combat/crafting/reload checks passed, including the focused visual/hostile-targeting retest. Actual two-client damage, left-main-arm visuals, physical mid-flight reload and external-mod combinations remain separate unverified QA, not implied by the singleplayer pass. See [Equipment](equipment.md).

Exit gate:

- Equipment is usable, craftable or test-spawnable, and gated by stage/research.

## Phase M10 - Threat Response And Events

Priority: P2/P3

- [x] **10.1 Define bounded regional threat state.** Persist scores, losses, kill/attack counters, decay timestamps, and record limits.
- [x] **10.2 Add event hooks and decay.** Update from real server events; throttle decay and use loaded-region/tile-local work without global per-tick scans.
- [x] **10.3 Add outpost attacks and reinforcements.**
  - [x] **10.3.1** Enforce stage-based selection, cooldowns, per-post/area caps, and failed-spawn backoff.
  - [x] **10.3.2** Assign bounded patrol targets, connect defence missions, and persist event state across reload.
  - [x] **10.3.3** Add admin trigger/status commands and configurable player warnings without chat spam.
- [x] **10.4 Verify response behavior.** Clean build/jar and final Windows CI passed; three dedicated launches and Liam's combined/natural/debug/cooldown/reload/cap checks passed. Phase exit gates are observed. Long-session multi-outpost soak, physical patrol reload, multiplayer and the final issued-blade retention/reload retest remain explicitly unverified in STATUS.md.

Exit gate:

- Threat changes from real events.
- Reinforcements can spawn without mob spam.

## Phase M11 - Compatibility Layer

Priority: P3

- [x] **11.1 Recheck candidate mods against the pinned Minecraft and NeoForge versions.** Exact release records, hashes, jar requirements and licence evidence checked on 2026-10-22; see [Optional Threat Compatibility](compatibility.md). These are metadata-compatible candidates, not runtime-supported combinations.
- [x] **11.2 Add generic presets and optional adapters.** Bounded opt-in config resolution and diagnostics; defaults unchanged. The Contagion candidate is explicitly reproduction-only, not supported play.
  - [x] **11.2.1** Verify registry IDs/tags for rewards, samples, targeting, and missions before activating any preset. Shipped vanilla names checked against pinned game sources; Contagion's vanilla-zombie target annotation inspected without copying implementation.
  - [x] **11.2.2** Isolate integrations behind mod-loaded checks; avoid importing external classes in common initialization.
  - [x] **11.2.3** Document presets and their source versions for modpack authors. See [Optional Threat Compatibility](compatibility.md) for exact sources and runtime failures.
- [x] **11.3 Execute the compatibility verification checkpoint.** Build, no-mod client/server, unknown mappings and failed-jar removal recovery passed; approved Contagion failed bootstrap on both sides. Liam explicitly deferred successful external-infection gameplay/modded-world removal on 2026-10-22. Those tests are not passed and no external support is claimed.
- [x] **11.4 Record supported combinations and deferred integrations.** Exact unsupported artifact, crash/API evidence, generic baseline and explicit deferral recorded in [Optional Threat Compatibility](compatibility.md). A 1.12 SRP jar is not evidence of modern-loader compatibility.

Exit gate:

- Works with vanilla-only test threats.
- Original external-infection gameplay/removal gate: **explicitly deferred by Liam on 2026-10-22**, not passed, after the exact Contagion artifact failed bootstrap. Foundation merge is approved on observed no-mod/generic checks, build and green CI; compatible external integration remains unverified and separately gated.

## Phase M12 - Showcase Vertical Slice

Priority: P3

- [x] **12.1 Assemble the existing loop.** Sample recovery, command post, lab research, supplies, defender, one attack, and save/reload connected by the play sheet/guide. Default earned-cost sequence regression passes; Liam reports a maintainer functional pass. Creative-assisted rehearsal is not the earned-only evidence required by 12.2.
- [x] **12.2 Run Liam-led alpha verification.** Independent new-tester feedback is deferred to beta after a stable alpha, per Liam's 2026-10-27 decision; familiarity is recorded honestly, not treated as independent proof.
  - [x] **12.2.1** Provide a guidebook or command-based tutorial with expected results and blocked-state explanations. Read-only help/guide use loaded definitions; [play sheet](showcase-loop.md) covers costs, mission order and cooldowns without grants.
  - [x] **12.2.2** Liam passed the earned sample/mission/research/Stage 1 core (19m36s client including pauses/exploration); later grants are qualified separately. Client raid attempts safely refused at crowded/unsupported sites. Liam explicitly accepted combined proof on 2026-10-27: earned core plus controlled dedicated tagged-raid combat, kill credit, reservation release and restart/cooldown persistence. This is not a complete unassisted client run, broad-terrain pass or independent usability proof.
  - [x] **12.2.3** Fix issues within the implemented loop and retest before expanding scope. Compact guide/missing-ID feedback and namespaced parsing passed prior checks. Raid refusal now distinguishes quota/terrain and explains unpaused backoff; live dedicated tests cover both refusals, successful spawning and persistence. Documented mission-start reconciliation and existing instant-research syntax. No balance change or new gameplay system.
- [x] **12.3 Prepare community materials.** Original F2/video-derived frames, a local contiguous 13s staged vanilla-hostile combat excerpt, page drafts and honest available/later scope prepared. Source/capture qualifications recorded in [community draft](community-draft.md); no external publishing or reserved-wave/independent-test claim.
- [x] **12.4 Prepare a test release.** Run build/client/server and performance checks; update changelog, known issues, supported versions, artifact details, and tester instructions. [Candidate](alpha-test-candidate.md) prepared, not published/tagged: clean build, Windows CI, dedicated restart and source client observed. Paused-session profiler result is inconclusive for sustained load; packaged/multiplayer testing remains separate. Qualified alpha evidence is accepted, not an independent/public-ready claim.

Exit gate:

- Liam completes the documented earned-resource loop with observed results and any blocking usability findings resolved; build/client/server evidence is retained and CI is green before merge. Creative infrastructure is allowed, progression/resource grants are not.
- Independent first-time usability testing is deferred to beta after a stable alpha; an alpha maintainer pass must not be advertised as independent feedback. Packaged/multiplayer proof and any public beta release remain separate checks/approval, not implied by this gate change.

## Phase M13 - Vehicles, Planes, And Larger Warfare

Priority: P4

- [x] **13.0 Automated QA prerequisite.** Minecraft/NeoForge built-ins only; no QA code/resources in the normal jar.
  - [x] **13.0.1** Add isolated GameTest registration/run and regressions for structures, raids, NPC targeting/loadouts, lab receipts/messages and Main Base notices; use documented server fixtures only if the pinned GameTest runtime blocks this.
  - [x] **13.0.2** Add a real-process saved-state restart fixture and a self-running screenshot/review `qaClient`.
  - [x] **13.0.3** Run the ladder locally, add server tests to CI, inspect jar isolation and record observed results before new gameplay. Local baseline: 15 required GameTests, two JVMs, nine reviewed screenshots; CI result remains separate.
- [x] **13.1 Evaluate the need and integration options after the core slice is stable.** Check compatible vehicle mods and decide which gameplay role Stand and Hold must own. Author release listings do not establish a matching pinned-target NeoForge vehicle artifact; no dependency selected.
- [x] **13.2 Write a separate technical design and performance budget.** [Warfare Support Design](warfare-support-design.md); one supply-funded reinforcement call-in selected before implementation, not physical vehicles.
  - [x] **13.2.1** Define call-ins, deployables, transport markers, supply costs, and spawn caps as the first scope candidate.
  - [x] **13.2.2** Define ownership, networking, collision, persistence, unloading, and recovery behavior for any proposed physical vehicle.
  - [x] **13.2.3** Plan aircraft support/set pieces before considering free flight; resolve animation and dedicated-server requirements.
- [x] **13.3 Implement one selected prototype only after the design gate.** Supply-funded player reinforcement command reuses existing units, saved cooldowns and caps; no physical vehicle/aircraft.
- [x] **13.4 Verify the prototype under multiplayer load.** Scoped technical proof: four synthetic server players/four posts/80 serialized requests, shared-bank contention, exact units/debits/caps and unchanged loaded chunks; actual two-JVM support persistence and real client command/screens passed. No passengers apply. Real multi-client latency/sustained-load proof remains unverified and required before vehicle expansion; this is not a production multiplayer benchmark.

Exit gate:

- Vehicle work has a separate technical design and performance budget.

## Phase M14 - Multi-Loader Evaluation

Priority: P4

- [x] **14.1 Confirm the NeoForge release baseline is stable and maintained.** Assessed technical alpha evidence versus remaining release/multiplayer gaps, available Fabric tooling and maintenance cost; project is not ready to expand support. Newer non-beta NeoForge artifacts exist but are not adopted.
- [x] **14.2 Audit platform-specific code and shared data.** [Multi-Loader Evaluation](multi-loader-evaluation.md) records actual registries/events, injected methods, config, menus, persistence, rendering, worldgen and QA boundaries.
- [x] **14.3 Choose a port strategy.** **DEFER** implementation; retain the single NeoForge project. Conditional future isolated spike before gradual shared-rule/platform extraction; no library or build restructuring approved.
- [ ] **14.4 Prototype Fabric in isolation.** Deferred, not run: requires acceptance of the stable-alpha gate and explicit exact toolchain/runtime-dependency approval. Verify scaffold, persistence and one existing interaction before parity.
- [ ] **14.5 Add parity tests, CI, and support documentation only after the prototype succeeds.** Deferred with 14.4; no Fabric support claim or parity result.

Exit gate:

- NeoForge build is stable enough that multi-loader work will not slow core gameplay.
- The evaluation has a recorded proceed/defer decision; a Fabric release additionally requires working parity checks.

Evaluation verdict (2026-10-28): **DEFER**. The decision/audit is complete; the proceed gate for 14.4-14.5 is not passed. No pins/dependencies changed and no Fabric prototype was attempted.

Liam confirmed NeoForge-first on 2026-10-28. Fabric implementation/parity remain deferred; this is not approval for a new loader, plugin, Gradle version or runtime library.

## Phase M15 - Outpost Progression And Defender Capacity

Priority: P2

Scope: make existing Command Post upgrades useful for local defence, following Liam's earlier outpost-growth feedback. World human stage continues to unlock unit tiers; each post's saved building level controls its own capacity. This milestone adds no physical structure expansion, units, weapons, vehicles, libraries or toolchain changes.

- [x] **15.1 Define the capacity and compatibility contract before implementation.**
  - [x] **15.1.1** Separate world stage, post level, assigned reservations and loaded nearby population. Keep the Level-1 default of 3, shared area default of 8, existing stage/research gates and the 16-entry roster hard limit.
  - [x] **15.1.2** Chosen profile 3/5/6/7/8, clamped by the unchanged absolute meaning of `maxDefendersPerPost`, shared area cap and roster hard limit. Fresh ceiling defaults to 16; existing explicit 3/0 ceilings remain authoritative. Invalid profiles reset via NeoForge validation; [capacity contract](buildings.md#defender-capacity-contract) records rollout before implementation.
- [x] **15.2 Apply one capacity policy across all deployment paths.**
  - [x] **15.2.1** Added validated profile/minimum policy; saved levels/UUID reservations and schemas unchanged.
  - [x] **15.2.2** Automatic deployment, natural/admin reinforcements and paid call-ins share the policy. Existing cooldowns, refunds, stage/dormancy gates and bounded attempts retained; excess/unloaded reservations are not discarded.
- [x] **15.3 Show actionable Command Post defence status.** Server-synced assigned/reserved count and effective cap; full/disabled/dormant/stage-locked/unavailable feedback. No nearby population scan in menus; upgrade costs/research unchanged. Compact layout retains the original 300x230 dimensions.
- [x] **15.4 Verify the whole milestone with the automated ladder.** Clean build: 7,910 assertions; all 29 required GameTests; upgraded five-UUID two-JVM restart; all 14 final qaClient captures opened/reviewed. Normal jar metadata/QA isolation checked. Windows CI 40722055999 passed build, GameTests, restart and artifact upload before merge; existing Level-1 regressions retained.

Exit gate:

- A higher-level post can use its configured extra slots without bypassing area/stage/cooldown limits or losing existing units/storage on reload.
- Command Post status explains the effective capacity; existing mechanics remain regression-tested and QA stays outside the normal jar.
- Physical footprint growth/terrain-safe expansion and broader real multiplayer/packaged testing remain separate work, not claimed by this milestone.

## Phase M16 - Outpost Expansion Design

Priority: P2

Scope: expansion design only, following Liam's request for bases that can physically grow with local upgrades. The separately requested addition of an empty Research Lab to newly placed checkpoints is authorized; no existing site retrofit or expansion mutation, new units, population-cap increase, library or toolchain change is included. Select a small first implementation candidate, not a full faction base system.

- [x] **16.1 Define the expansion contract.** [Outpost Expansion Design](outpost-expansion-design.md) separates stage/local level/layout/provenance/footprint and restricts future eligibility; no implicit terrain ownership or old-site adoption.
  - [x] **16.1.1** Empty lab added to all new checkpoint sizes. Logic and 33 required GameTests cover supported sizes, real lab use and obstruction safety; real restart preserves generated lab storage/work and crate non-refill. Existing sites are not retrofitted.
  - [x] **16.1.2** Explicit provenance/eligibility contract; old/manual/unsupported sites and Main Bases refuse the first proposed expansion path.
- [x] **16.2 Specify bounded placement and player-build protection.** One-chunk 9x9-to-13x13 outer apron candidate, original footprint unchanged; <=169 columns/2048 reads/256 writes, loaded-only preflight, ground-replacement disclosure and explicit operator consent. No physical expansion implemented.
- [x] **16.3 Design persistent application and recovery.** Separate site revision/operation receipt, no extra expansion debit/reward, fail-closed partial-state review; no claimed durable rollback or power-loss guarantee. Building storage/defender UUIDs/cooldowns stay untouched.
- [x] **16.4 Prepare an implementation-ready verification plan.** Logic, GameTest, restart and qaClient contracts distinguish future expansion proof from current lab tests. Verdict: PROCEED to provenance/read-only preview; DEFER writes until fault/recovery proof. All 16 final lab/regression screenshots opened/reviewed.

Exit gate:

- A small documented design has explicit terrain/ownership/persistence budgets and testable failure semantics.
- No live base is resized by this phase; runtime dependencies/pins remain subject to Liam's approval.

## Phase M17 - Site Provenance And Read-Only Expansion Preview

Priority: P2

Scope: implement the admitted read-only foundation from the M16 design. No expansion/confirm command, token cache, block writes from preview, adoption/retrofit of existing sites, new costs/rewards, capacity increase or dependencies.

- [x] **17.1 Persist bounded new-site provenance.** Additive site-v1 receipt after validated checkpoint placement; old/manual posts stay unproven, unsupported/raw future data retained. Building/world schemas and worker/server registration boundary unchanged; NBT and real restart checks passed.
- [x] **17.2 Implement the pure bounded expansion planner.** Exact 88-floor/45-wall/4-light delta; 169 columns/616 explicit terrain reads, no original-footprint cells or writes. Loaded/border/terrain/container/entity guards and 128-entity saturation are native-tested.
- [x] **17.3 Expose an operator-only read-only preview.** `/standandhold outpost preview` reuses the loaded six-block look ray; native tests cover real operator/non-op dispatch and unchanged blocks/NBT/dirty flags/global state/loaded chunks. No confirm/mutation command, tokens, spending or markers.
- [x] **17.4 Verify provenance and zero-mutation behavior.** Final clean build passed 15,178 assertions and all 40 required GameTests, including unchanged blocks/NBT/dirty flags/balances/rosters/loaded chunks and operator permissions. Two real JVMs retained site identity/storage and identical preview; all 19 qaClient captures opened/reviewed. Normal jar metadata/QA isolation checked; Windows implementation CI 40757351989 passed build, GameTests, restart and artifact upload. No physical expansion/recovery proof claimed.

Exit gate:

- Supported new checkpoints retain provenance and expose a useful bounded preview with no world mutation or resource effect.
- Expansion writes/confirmation remain disabled/unimplemented until a separate fault/recovery milestone.

## Phase M18 - Confirmed Apron Application And Recovery

Priority: P2

Scope: one explicitly confirmed, default-disabled application of the M17 radius-4 checkpoint apron, under the [expansion contract](outpost-expansion-design.md). No automatic resizing, old-site adoption, Main Base growth, extra cost/reward, capacity increase, library or pin change. Failure/restart proof is a prerequisite to admitting writes, not evidence supplied by M17.

- [x] **18.1 Define and test bounded operation receipts/recovery classification before writes.** Additive exact 137-cell operation-v1 intent with protected-footprint digest and prepared/review/applied phases; unknown raw data retained, no on-load scan/replay/rollback. Pure classification/token checks and native failure gate passed before command admission.
  - [x] **18.1.1** Defined strict site-bound expected-state receipts, protected-footprint digest and read-only recovery observations.
  - [x] **18.1.2** 231 dependency-free assertions cover malformed/future data, exact layout bounds, every partial prefix and token lifecycle. Pinned `id`/`properties` block-state NBT verified from platform source after native tests rejected the old field spelling.
- [x] **18.2 Implement explicit server-authoritative confirmation policy.** Separate operator prepare/expand/status/recover; application off by default, 200-tick single-use player/dimension/site/level/stage/policy/state-bound tokens, cache <=64, disconnect/stop cleanup. Native actual command dispatch verifies permissions, safe click-to-fill, late entities and stale/replayed/foreign consent without chunk loading/global ticks.
- [x] **18.3 Admit the single-chunk applicator only after fault/recovery fixtures pass.** Native fault gate passed before command wiring. Exact 137-cell intent, expected-state checks, original-footprint digest, receipt-mutation epoch and fixed metadata budgets; first/middle/last failures and callback/container/protected-state changes stop for review without rollback/reseed. Final 55 required tests retain the original preview/AI/structure/research regressions. No power-loss claim or automatic repair.
- [x] **18.4 Verify the complete milestone before merge.** Final clean build passed 15,409 assertions/all 55 required tests; exact XML cases/no failures/skips checked. Two real JVMs retained all four operation states and prior storage/UUID/cooldown receipts; old tokens refused and explicit all-after sealing passed. All 25 final client captures opened/reviewed. Normal jar metadata/QA isolation checked; Windows implementation CI 40772187791 passed the full ladder/artifacts. Defaults remain off; no power-loss guarantee.

Exit gate:

- A supported site's explicit application either completes once with protected state intact or stops with a persisted bounded diagnostic; unsupported/ambiguous cases do not mutate.
- Fresh confirmation is required after restart or any relevant change; defaults remain off, with no automatic repair, extra progression/population effect or durable-transaction claim.

## Phase M19 - Player-Friendly Outpost Growth Controls

Priority: P1

Scope: replace the M18 operator-only interaction as the normal way to use its one supported apron, without expanding the physical write set. Liam's 2026-10-28 feedback explicitly prioritises usable manual/automatic base growth. The previously unstarted Heavy/Elite scope moves prospectively to M20; no completed task ID or historical result is renumbered. [Interaction contract](outpost-expansion-design.md#player-facing-growth-direction) and [reference review](gameplay-direction-and-references.md) distinguish this plan from implemented behavior.

- [x] **19.1 Define the player interaction and saved policy before code.** [Contract](outpost-expansion-design.md#player-facing-growth-direction) fixes additive growth-v1 Manual/Automatic policy, explicit retry after a saved pause, existing loaded-post cadence, cheap menu state and shared immediate preflight/applicator. Existing commands retained; old/future receipts are not adopted/replayed, prices/caps/pins unchanged. Implementation/verification remain below.
- [x] **19.2 Add a simple server-authoritative Command Post growth action alongside commands.** Status/Growth tabs, manual Expand and explicit Manual/Automatic selection use the shared immediate preflight/applicator, with actor/menu/identity/range checks. Existing operator commands retained; grow/mode/help and literal completion added. Native tests and real client packets cover non-op menu use, mode selection and once-only expansion; no token required in normal menus.
- [x] **19.3 Add bounded automatic application for opted-in eligible sites.** Additive growth-v1 mode/pause defaults Manual and retains unknown raw data. The existing loaded-post cadence applies the same 137 cells once after earned level 2; obstruction/interrupted state pauses. Native real chunk unload/reload and two JVMs verify saved choice, no on-load replay and no stale-reference chunk loading. No automatic purchases, refill, cap increase or larger layout.
- [x] **19.4 Verify and document both interaction modes and retained command tools.** Final clean build passed 15,437 assertions/all 65 required GameTests, including real chunk unload/reload and newer-save refusal. Two real JVMs retained five growth states plus prior receipts; normal dedicated server reached Done/status/help/stop/clean exit. All 35 final e2ca06a4-prefix client captures opened/reviewed; actual menu/mode packets passed. Jar metadata/QA exclusion inspected; Windows implementation CI 40793286116 passed build, GameTests, restart and artifacts. Human feel, real multiplayer/packaged and power-loss proof remain separate.

Exit gate:

- An eligible site can expand manually through its menu, or once automatically after explicit opt-in and a normal paid level upgrade, without typed tokens or routine recovery commands.
- Existing protection/write budgets, operation receipts, resources, capacities, legacy/local files and pin/dependency rules remain intact. A simpler interface is not permission to overwrite builds or pretend atomic crash recovery exists.

## Phase M20 - Heavy And Elite Human Unit Tiers

Priority: P2

Scope: two additional readable alpha roles using the existing human entity/vanilla-AI, equipment, stage/research and shared deployment foundations. No new weapon mechanics, animation library, population increase, structures, automatic transformation of existing units or toolchain change. Super Elite/Special Division unit expansion remains separately scoped.

- [x] **20.1 Define the role/unlock/retention contract before implementation.** Distinguish Heavy and Elite roles, configurable stats and stage/research requirements; keep global stage separate from post level, existing issued-kit retention and all post/area/roster limits.
- [x] **20.2 Register the two units, test eggs and original visual variants.** Reuse the supported human model/art export pipeline with distinguishable silhouettes/uniforms, translated names and complete textures; no copied assets or runtime library. Eggs remain deliberate admin test tools, not progression bypasses for automated deployment.
- [x] **20.3 Apply stage/research selection through shared deployment paths.** Automatic, event and paid reinforcements choose an eligible role without bypassing cooldowns/caps or changing existing NPC identities/tiers. Use existing equipment/target rules; no new gun/ammo/complex-AI system.
- [x] **20.4 Verify the complete milestone and document usage.** Logic and native GameTests for role stats/AI/unlocks/retention and all deployment paths, real restart for identities/equipment/rosters, reviewed qaClient tier views, final clean build/jar isolation and green CI. Human feedback only for silhouette/combat feel; multiplayer/packaged claims stay separate.

Exit gate:

- New units are distinguishable, configurable and stage/research-gated for automated deployment; old NPCs, saves and common limits remain intact.
- No extra population budget, dependency/pin change, weapon system or support claim is smuggled into the tier milestone.

## Phase M21 - Useful Level-2 Outpost Layout

Priority: P2

Scope: improve usable space rather than repeat the cramped M18 outer ring. One original Level-2 layout/delta first; Level-3 second floors, functional sensors/wire, new storage mechanics, larger shared armies and Main Base expansion stay outside this milestone. M19 interaction controls are reused, not replaced with another command procedure.

- [x] **21.1 Author the layout and upgrade contract.** Define a usable yard, protected command/lab/storage access, clear entrance and one modest watch position; distinguish new sites, already-apron-expanded sites and customised sites. Show exact local-level/footprint requirements and identify preserved buildings and any old-wall removals before mutation.
- [x] **21.2 Implement bounded planning and protected application.** Extend provenance/layout revisions and expected-state deltas only as required; refuse unsupported/occupied/unknown changes and preserve inventories, labs, identities, receipts and reservations. No silent adoption, full-site regeneration or loot reseed.
  - [x] **21.2.1** Add additive revision-2 receipts while retaining sealed apron-v1 intent and diagnostics.
  - [x] **21.2.2** Author the bounded yard/watch delta, exact-state protection and shared guarded application.
- [x] **21.3 Integrate manual/automatic growth and readable status.** The same menu policy selects the supported layout at the earned local level. Make failure/space requirements actionable; capacity remains the existing shared policy, not a side effect of decorative towers.
- [x] **21.4 Verify usefulness and safety.** GameTests for new/apron/customised cases, terrain/entrance/NPC access, protected removal and fault/repeat behavior; two-JVM persistence; reviewed overhead/interior/menu captures; clean build/jar isolation and green CI. Liam judges space/navigation and visual payoff after automation.

Exit gate:

- One Level-2 upgrade provides visibly useful space with working building/defender access and no loss, duplication or unsafe replay of existing state.
- Larger multi-level growth and functional defensive systems remain explicitly separate, not implied by the Level-2 layout.

## Roadmap 2 - Useful Defence And A Testable Alpha

The product goal is **recover samples -> research -> improve a local outpost -> put its defenders to useful work -> defend that investment**. Prioritise a readable, dependable version of that loop over a large catalogue of unfinished guns, soldiers or bases. The [direction/reference review](gameplay-direction-and-references.md) records the identity and design hypotheses; it is dated research, not a fresh compatibility audit or asset licence.

This planning pass changes no gameplay, balance, version, toolchain, dependency or public release status. M22-M31 are bounded implementation milestones, not a promise to deliver them all in one run. A named library/mod remains a candidate until its owning phase rechecks the exact artifact and Liam approves installation. P1-P4 express importance/risk; prerequisite phases still run before their dependants.

### Sequence And Dependencies

| Phase / work branch | Priority | Prerequisites | Maximum feature slice |
| --- | --- | --- | --- |
| M22 `codex/m22-packaged-alpha` | P1 | Verified M21 baseline | One isolated normal-jar client/server installation, not another dev-run claim |
| M23 `codex/m23-defender-duties` | P2 | M22 | Guard/Patrol choice and one reachable watch assignment at a supported outpost |
| M24 `codex/m24-local-storage` | P2 | M22-M23 | Level-aware Command Post supply capacity; no new supply network |
| M25 `codex/m25-level3-preview` | P2 | M23-M24 | One Level-3 upper-deck contract and read-only planner; no placement |
| M26 `codex/m26-level3-application` | P2 | M25 gate | That one guarded delta through existing growth controls |
| M27 `codex/m27-motion-polish` | P3 | M23, M26 | One original Rifleman/Launcher presentation slice; library admission only if necessary |
| M28 `codex/m28-specialist-tiers` | P2 | M23, M27 | Super Elite and Special Division using existing weapon/deployment foundations |
| M29 `codex/m29-mainbase-layout` | P2 | M26, M28 | One original new-site Main Base layout; no old-base retrofit or multi-chunk engine |
| M30 `codex/m30-threat-coexistence` | P3 | M22, M28-M29; installation approval | One exact optional threat/horde combination, baseline and removal checks |
| M31 `codex/m31-alpha-integration` | P1 | M22-M29; explicit M30 disposition | Bounded load/network testing, earned-loop review and an internal alpha candidate |

### Shared Exit Rules

- Keep the approved pins, preserved legacy branch/tag and original-code/art policy. No 1.12 world/config import, mass AI rewrite, new loader split or dependency shopping list. Any runtime library, optional test-mod installation, pin change or EULA acceptance follows [AGENTS.md](../../AGENTS.md); a roadmap checkbox is not that approval.
- Keep player actions in the existing local menus. Retain admin commands/help/diagnostics and add a small useful command where needed, but never make players copy tokens or read receipts for normal progression. Global stage, local level, physical layout revision and shared defender capacity remain different concepts.
- Specify numeric write/read/path/spawn/packet budgets before implementation; prefer the existing bounds. No global tick scans, force-loaded production chunks, offline catch-up, unbounded retries or new population budget hidden inside a building/visual phase. A blocked path/action backs off and explains why.
- Extend the [automated ladder](automated-testing.md), not Liam's command checklist: logic/codec/transaction checks; native GameTests for every in-world feature and refusal; real two-JVM persistence; `qaClient` captures opened and judged by the agent. New tests add to the existing required set; do not freeze the old test count or weaken failures. Use bounded readiness conditions, not assumed entity/chunk availability after a fixed sleep.
- Code/build phases require `clean build`, correct normal-jar metadata/QA exclusion, relevant client/dedicated proof and green CI before merge. Authored layout and UI phases also require reviewed before/after and normal/large-scale captures. All saved changes need missing/malformed/future-data retention, no repeated debit/reward, and save/restart coverage. None promises cross-file power-loss atomicity.
- Packaged runs must use the normal jar without `standandhold_qa` or Gradle output directories. Synthetic server players prove authority/transactions, not real network multiplayer. Record the exact profile/mod list, config, artifact and observed results; unavailable tests stay unavailable. A blocked exit gate needs a fix or Liam's explicit revised gate, not a renamed pass.
- Liam remains the alpha tester; independent beta feedback is deferred, not fabricated. Automate mechanics first, leave only motion/feel/earned play or inaccessible real-install/network checks to him at the end. Feel-check availability never blocks a technical merge; unrun checks remain unverified. Do not publish, retag, change the version, add telemetry or promise mod support during planning. Real clips/screenshots and honest limitations take precedence over marketing claims.

## Phase M22 - Packaged Alpha Baseline

Priority: P1. Prerequisite: M21's verified normal jar and unchanged pins.

Scope: establish that a tester can run the normal mod outside Gradle. Keep all profiles, scripts, credentials and worlds isolated; no content, balance or installer-framework rewrite.

- [x] **22.1 Define and prepare the isolated install contract.** Reuse approved platform tooling/an existing launcher with exact pins; identify a fresh disposable client profile and loopback-only server directory, timeouts, expected logs, artifact checksum and backup boundaries. Never overwrite an existing profile or accept an EULA. No source/same-ID duplicate jar or QA classpath in either production profile.
  - [x] **22.1.1** Audit the verified modern build/CI artifact, required platform files and existing launcher/server facilities; document an explicit per-profile manifest rather than a moving latest download or copied user profile. The same filename/version on the preserved 1.12 release is not the modern artifact.
  - [x] **22.1.2** Prepare bounded, repeatable setup/console-driving helpers where needed, with explicit target paths, refusal to reuse valuable worlds and no credentials/EULA acceptance in scripts. Authentication/EULA handoff is explicit if actually encountered.
- [x] **22.2 Prove the packaged dedicated server and restart.** Reach Done, execute existing progression/status/save commands, stop cleanly and restart the same disposable world using only the normal mod jar. Verify saved progression and a small loaded building fixture, no duplicate initialization, no client-class loading and no QA mod. Test-only chunk tickets must stay in the fixture, not production logic.
- [x] **22.3 Prove the packaged client.** Launch the exact profile, inspect Mods, enter/save/reopen a fresh world and exercise the existing menus/guide/sample/defender loop with automation where available. Preserve screenshots/logs; actual normal-profile entry is required, not a successful `runQaClient` relabelled as packaged proof. Do all independent work before asking Liam for an inaccessible launcher/account step.
- [x] **22.4 Close the install baseline.** Re-run the existing automated regression ladder, inspect jar isolation, record exact client/server outcomes in the existing candidate/build guidance, and pass CI. Fix only demonstrated packaging/stability blockers; keep missing multiplayer/external-mod proof separate.

Exit gate: observed normal-jar client entry/reload and dedicated Done/stop/restart on the exact target, with retained evidence and no new gameplay/dependency. If a necessary environment or EULA prerequisite is blocked, record it and request the specific action; do not mark installation passed.

## Phase M23 - Useful Defender Duties

Priority: P2. Prerequisite: M22; reuse M21's reachable yard/watch geometry and the existing human goals/roster.

Scope: one local Guard/Patrol policy and one modest watch assignment, not squads, commanders, a new AI engine or more spawns. Missing duty data retains the existing guard/home behaviour.

- [x] **23.1 Define bounded duties and ownership.** Distinguish idle guard, local patrol and temporary threat-response orders; define return-to-post, blocked/unloaded target backoff, maximum waypoints/path requests and one watch-slot reservation. Only already-assigned eligible defenders participate; eggs do not silently become a free garrison.
- [x] **23.2 Implement reachable local orders and persistence.** Reuse vanilla navigation/goals, existing home/pursuit radius and optional threat matching. Preserve entity UUID/type/health/issued gear, home and quota; resolve watch-slot contention without stacking or teleporting units. Persist supported intent, validate on reload, abandon stale targets safely and never infer death from an unloaded lookup.
- [x] **23.3 Expose simple server-authoritative controls.** Add a small Command Post duty choice/status and matching admin inspection/debug tools. No remote order network or mandatory command ritual. Combat interrupts duty, then returns to valid work; existing support/event patrol ownership must not fight the new idle duty.
- [x] **23.4 Verify actual movement and authority.** GameTests must observe arrival/return and bounded stuck-path attempts, zombie/skeleton/creeper combat, player/animal exclusions, shared slots, unload and missing-post cases. Restart intent/UUIDs and review duty/menu/watch captures; ask Liam only about responsiveness and movement in motion after the mechanical checks.

Exit gate: defenders visibly perform a reachable useful duty and return after combat without extra population, chunk loads, path spam or loss of existing state. A watch deck is not advertised as a sensor, turret or staffed facility until its tested assignment exists.

## Phase M24 - Local Storage Progression

Priority: P2. Prerequisites: M22-M23.

Scope: earned local Command Post levels can provide real supply capacity. Labs/crates retain their existing independent storage behaviour; no new inventories, automatic transfers, convoy routes or passive-generation buff.

- [x] **24.1 Define the capacity/config rollout.** Choose a bounded per-level Command Post profile and explain the payoff in the existing menu. Preserve deliberate existing flat-cap settings; document opt-in/default handling before choosing values. Points remain a gate, level purchase uses the existing transaction, and planning grants no new prices or balance defaults.
- [x] **24.2 Apply capacity through one authoritative rule.** Deposits, generation, upgrades and menu status share it. Changing config/level never clamps away stored supplies, refills a bank or changes lab/crate balances; above-cap export stays available. Keep long-value/overflow and unknown-modern-data protections.
- [x] **24.3 Make the payoff readable.** Show effective capacity and the next level's storage benefit beside existing upgrade requirements, without a second purchase procedure. Retain transfers/debug commands; do not silently spend the shared global bank or scan nearby containers.
- [x] **24.4 Verify conservation.** Logic and native tests cover exact-cap batches, over-cap retention/export, config disable/legacy settings, upgrade repeats and competing actions. Restart balances/levels, review normal/large GUI views and pass the full ladder/CI without changing defender limits or generation rates.

Exit gate: a paid local level has an understandable storage benefit, all relevant paths use the same limit, and no saved supplies are destroyed, minted or charged twice.

## Phase M25 - Level-3 Contract And Read-only Preview

Priority: P2. Prerequisites: M23-M24.

Scope: author one usable upper-deck/second-floor slice over a proven revision-2 checkpoint. This milestone cannot place blocks or charge resources; no new building/profession, multi-chunk base or claim integration.

- [x] **25.1 Author the Level-3 layout and preservation contract.** Specify exact supports, stairs, headroom, entrance/building access and bounds. Keep one chunk and the existing 256-write/2,048-cached-preflight ceilings; declare the separate fixed protected-verification envelope. Reduce the design if it does not fit, rather than silently increasing budgets. Local level 3 is not global Stage 3.
  - [x] **25.1.1** Define revision-2 eligibility and unsupported/customised cases, exact old-wall/roof removals and preserved lab/command/crate/NPC positions; do not infer ownership from matching player blocks.
  - [x] **25.1.2** Define source/target receipts and the Manual/Automatic progression contract before implementation, including sites whose level already exceeds their physical revision. At most one supported delta per action/cadence; no free level purchase, regeneration or repair.
- [x] **25.2 Implement the bounded pure planner and world adapter.** Capture expected before states, protect containers/fluids/supporting builds/entities, use only loaded cells and fail without mutations. Keep M18 apron and M21 yard previews/receipts readable and unchanged.
- [x] **25.3 Expose a readable preview through existing tools.** Describe the next available layout, bounds and specific blocker in menu/help/operator preview; no new mandatory token flow. Add original planned views/fixture captures only where clearly labelled preview, not a built gameplay feature.
- [x] **25.4 Verify read-only behaviour.** Pure and native tests cover supported/customised sites, every planned removal, headroom/terrain/world border, missing chunks, unknown receipts and strict no-change assertions for resources, tile NBT, entity state and dirty flags. Review the preview views and pass CI.

Exit gate: one precisely bounded, useful Level-3 plan can be inspected safely; no blocks, currency, rewards or layout receipts are mutated. Placement remains outside this milestone.

## Phase M26 - Guarded Level-3 Application

Priority: P2. Prerequisite: the M25 planner/contract exit gate.

Scope: apply only that authored revision-2-to-Level-3 delta through existing growth controls. No full reconstruction, automatic repair, Main Base adoption or new army/storage bonus hidden in geometry.

- [x] **26.1 Extend receipts additively.** Retain old apron/yard intent, unknown fields and site identity; persist only the new exact bounded operation/progress. Missing/malformed/future or mixed state pauses instead of replaying, and historical digests are not confused with ordinary later storage changes.
- [x] **26.2 Apply through shared expected-state guards.** Validate actors/config/level and expected state before each write, plus protected data at completion. Refuse altered supports, occupied stairs/decks and lost anchors; preserve inventory/work, health, rosters and cooldowns. No rollback over a newly placed build and no claim of crash-atomic saves or arbitrary third-party callback safety.
- [x] **26.3 Reuse Manual/Automatic and operator tools.** One Expand/Retry applies the next eligible supported layout; opted-in Automatic uses the existing bounded cadence and saved pause. Keep old admin commands, clear preview/status and explicit all-after metadata recovery; a cosmetic level downgrade does not demolish an existing floor.
- [x] **26.4 Verify failure and restart.** Native tests inject first/middle/last-write failure, late state/entity obstruction where supported, changed protected data, missing anchor and repeated/concurrent actions. Test real navigation to both floors/buildings, exact all-after recovery and two-JVM no-replay; review before/after/interior/menu captures and pass CI.

Exit gate: one Level-3 site grows usefully with reachable access and preserved old state, while partial/unknown operations fail safely. No routine recovery command or repeated resource grant is needed to enjoy the upgrade.

## Phase M27 - Original Motion And Equipment Readability

Priority: P3. Prerequisites: M23 and M26.

Scope: polish one Rifleman and the existing Prototype Launcher first. No new firing/ammo/reload mechanics, combat rebalance, mass asset replacement or copied animations/models.

- [x] **27.1 Evaluate the smallest viable original pipeline.** Prototype walk/aim/attack transitions using the current native model/export stack. If that cannot meet the defined presentation need, prepare one exact animation-library proposal with a no-dependency fallback, licence/transitive/side contract and maintenance cost; stop for Liam's approval before installation. A previously listed GeckoLib artifact is not automatic approval or proof it works here.
- [x] **27.2 Author the bounded art slice.** Keep editable original source, rig/UV conventions, visible face/silhouette and correct hand/muzzle orientation in first/third person. Validate exported textures/models deterministically; do not import another mod's weapon, uniform or sound asset.
- [x] **27.3 Add restrained presentation only.** Aim/attack feedback must agree with server-authoritative firing and melee/cooldown rules. Cover walking, idle, damaged/dead and missing optional presentation cases; avoid camera discomfort or an every-tick packet/effect stream. Other armour/units use the established pipeline later, not an implicit full-art overhaul.
- [x] **27.4 Verify side safety and regression.** Dedicated/packaged launches must not load client classes; native combat/target/cooldown checks stay required. Review front/back/side/held/lighting/menu captures, resource reload and supported fallback, with motion/visual feel left to Liam after automation. If a required library was approved, declare it honestly and retest the normal installation contract.

Exit gate: the selected unit/weapon reads and animates better without changing gameplay or breaking side/install safety. No unapproved library or unreviewed art enters the normal jar.

## Phase M28 - Specialist Human Tiers

Priority: P2. Prerequisites: M23 and the M27 pipeline disposition.

Scope: two original roles at most, Super Elite and Special Parasite Division, using existing melee/projectile equipment. No exotic weapons, vehicles, new research engine or separate deployment director.

- [x] **28.1 Define roles, gates and rarity.** Specify configurable stats/research/stage requirements and useful role differences before implementation: Super Elite at Stage 4 and Special Division at Stage 5 plus Special Division Training as the initial contract. Bound Special Division recruitment to active Main Bases with at most one reserved specialist per base initially, inside existing shared quotas; unloaded specialists still reserve that slot. No deployment at every checkpoint or global threat scan.
- [x] **28.2 Register entities/eggs and original variants.** Reuse human save/AI/equipment contracts and the supported art pipeline, preserving old registry IDs and all four existing roles. Test eggs remain admin tools, not a survival recruitment bypass; player equipment access remains separate from NPC issue/use gates.
- [x] **28.3 Integrate shared recruitment and retention.** Automatic/event/paid paths share eligible selection, rarity, post/area reservations and cooldowns. Missing research/mappings/config fails closed for the affected tier with a valid lower-role fallback; no promotion/deletion of existing units or loss of issued kit after stage drops. No new population budget.
- [x] **28.4 Verify the complete role slice.** Logic/native tests cover stats, stage/research/Main Base gates, fallback, rarity/contention, combat/friendly exclusions, all deployment entry points and no gear drops. Restart mixed UUIDs/health/equipment/rosters; review distinguishing views/eggs and pass CI without a threat mod.

Exit gate: both roles are identifiable and useful, specialist recruitment is genuinely limited, and existing identities, optional threat rules and quotas survive intact.

## Phase M29 - Inhabited Main Base Layout

Priority: P2. Prerequisites: M26 and M28.

Scope: one original new-site Main Base arrangement within the existing one-chunk placement budget, using existing Command Post/Lab/Crate blocks and duties. No multichunk generator, retrofit of old bases, new scientist/barracks profession or automatic outpost-to-Main-Base conversion.

- [x] **29.1 Author distinct usable zones.** Specify command, research, supply and a modest barracks/guard area, protected movement/lighting and clear dormant/active presentation. A bench/zone marker remains decoration, not a promised production building. Keep global activation stage separate from local level and specialist research.
- [x] **29.2 Place only new eligible sites safely.** Reuse dimension/terrain/fluid/open-space/border checks and unchanged rarity unless a separately documented change is justified. Existing generated bases retain their blocks/state; no chunk-load regeneration, extra seeded loot or offline garrison refill. Version any new provenance without adopting unsupported old sites.
- [x] **29.3 Integrate existing systems without side effects.** Register the correct command anchor, validate its Lab/Crate tiles without a new global registry, preserve one-time notices, and use shared bounded duty/deployment services. Status/list/nearest and loaded-only cleanup must identify the site without scanning or loading the world.
- [x] **29.4 Verify placement and use.** Native tests cover clear and unsuitable terrain, preserved custom blocks, occupied refusal, registration/removal, dormancy/activation and capped specialist deployment. Save/restart both old and new layouts without refill or reannouncements; review overhead/interior/night/menu views and pass CI.

Exit gate: a new Main Base is visually distinct and usable as a command/research/supply site, while old sites and bounded safety rules remain intact. Full Main Base growth is not implied.

## Phase M30 - Optional Threat Coexistence

Priority: P3. Prerequisites: M22, M28-M29 and explicit installation approval for the exact isolated combination.

Scope: one threat/horde artifact first, not a large modpack or new infection mechanics. The October reference listings are dated candidates; this plan makes no fresh availability/support claim. Urban-world QA is a separate deferred combination.

- [x] **30.1 Recheck and propose one exact artifact.** Verify author-controlled manifest/source, pinned Minecraft/NeoForge/Java fit, required/transitive mods, licence, sides and known conflicts; record source/file/hash and request Liam's approval before installation. Undead Nights is a candidate horde test, not proof of infection compatibility. Retest Contagion only with newly verified compatible evidence and approval; its known failed jar stays unsupported.
- [x] **30.2 Add only verified optional mappings/adapters.** Prefer registry IDs/tags, retain the zombie baseline and safe missing/invalid mappings, and keep external classes out of common initialization. Test-only files live in isolated profiles, never normal development runs, build requirements or release jars.
- [x] **30.3 Exercise with/without/removal safely.** Test actual mapped kills/drops, defender targeting, bounded raids/duties and outpost refusal around changed terrain on client and dedicated server. Repeat the clean baseline without the mod; removal uses a backed-up disposable copy and does not promise the other mod's blocks/entities survive removal.
- [x] **30.4 Record the exact support disposition.** Extend first-party regression tests/fixtures for mapping logic, retain combination evidence and inspect artifact isolation. Label passed, blocked or unsupported combinations explicitly; no automatic target fallback or invented M11 external-infection pass. A blocked gate requires a fix or Liam's explicit deferral before advancing its claimed coverage.

Exit gate: the selected optional combination has observed client/server/gameplay/removal-boundary evidence, or an explicit deferred/unsupported disposition approved by Liam. Stand and Hold still launches and plays alone.

[Disposition](compatibility.md#m30-closeout): the exact Undead Nights combination passed limited checks on the isolated loader trial. Liam later approved the tested loader for M31 entry; the integration baseline is still standalone and no external dependency was added.

## Phase M31 - Integrated Alpha Candidate

Priority: P1. Prerequisites: M22-M29 and an explicit M30 disposition; no public-beta claim or new dependency is implied.

Scope: integrate and assess the built slices, fix demonstrated defects and prepare an internal NeoForge alpha candidate. Not a feature/balance overhaul, publishing permission, version/tag change or a substitute for later independent beta testers.

- [x] **31.1 Define and run a bounded load fixture.** Compare a recorded baseline with 1/2/4 loaded posts, reporting actual units/reservations under unchanged shared caps, named hardware/config, warm-up/duration and profiler/tick/packet evidence. Define acceptable budgets before running; do not infer percentiles from an average or universal performance from one machine. Extend automated regressions for fixes, not a blanket rewrite.
- [x] **31.2 Verify real client/server sessions.** Use at least two actual network clients where available; observe competing menu/upgrade/research/support actions, reconnect, death/dimension transitions, chunk edges and server restart. Automate the mechanics and read logs/screens yourself. Synthetic players, integrated-server screens and one real client are not two-client evidence; if access/accounts block this, record the gap and ask the one necessary question rather than fabricate a pass.
- [ ] **31.3 Rehearse the earned loop and resolve friction.** Liam tests resource acquisition, local upgrades/growth, useful duties, research and one real defence without progression grants in a fresh modern world. Automated tests cover numeric correctness; record confusing steps, time to useful payoff and combat/navigation feel. Debug exploration is labelled separately. Independent unfamiliar beta feedback stays deferred until its actual release gate.
- [x] **31.4 Prepare the honest candidate and showcase.** Run the full automated and relevant packaged ladder, inspect the normal jar, and update existing build/candidate/known-issue/command guidance. Prepare real before/after base views and a short genuine defence clip with accurate requirements/support claims; no public posts/uploads, fabricated footage or unsupported popularity promises. A release/version/tag decision requires separate explicit approval, never reuse the preserved legacy tag.

Exit gate: observed regression, install, persistence and bounded session/load evidence; remaining gaps are explicit rather than promoted to supported claims. Record supplied alpha feedback or its explicit deferral without blocking the technical merge on a feel check. A public multiplayer/stable/beta or earned-playthrough claim cannot be made without its corresponding evidence and release approval.

The internal closeout recorded positive maintainer feedback without claiming a completed 31.3 playthrough. The full grant-free run remains unchecked and does not block internal QA. See the [M31 fixture record](alpha-test-candidate.md#m31-integration-fixtures) for the observed two-client loopback, load and playtest evidence. Authenticated remote multiplayer remains a separate check.

## Roadmap 3 - Earned Survival Start, Then A Living Army

Claude owns this roadmap; Codex reviews it and alternates implementation (see the [handoff](../../handoff.md)); Liam approves scope and the numbered decisions. Roadmap 3 has two goals, in order:

1. **A new player in a fresh, cheats-off Survival world reaches a working Command Post, completes first research and sees automatic defenders without an operator, a lucky seed or a wiki** (M32).
2. **The army starts to look, sound and behave like a living force with a headquarters at its heart**: non-repetitive soldiers with original sounds (and, if a tested experiment earns it, a better animation pipeline), a readable attack, a "Phase 0" world of scattered survivors and abandoned or active sites, and the first HQ intelligence (M33-M37).

Everything larger (divisions, facilities, arsenal, vehicles, missiles, jets) is placed in the [future roadmaps](#future-roadmaps-provisional) so Liam can see when it lands. Roadmap 2's Shared Exit Rules still apply unchanged.

### Verified Starting Point

Checked against `main` at the M31 internal baseline (NeoForge 26.3.0.102). These findings decide the M32 scope.

| Finding | Where | Consequence |
| --- | --- | --- |
| No recipe produces a Command Post, Research Lab or Supply Crate; only equipment recipes exist. Natural checkpoints are one deterministic candidate per 12x12-chunk region, 1 in 2, then strict terrain checks with no retry. Their real density is unmeasured. | `data/standandhold/recipe/`, `StructureConfig`, `ResistanceSites` | **The biggest blocker.** Without a found site there is no Survival route to the loop. |
| Every mission needs `/standandhold mission start`, which is operator-only. The guide tells non-operators to "ask an operator". | `CommandFoundation`, `CommandGuide` | Not a hard stop: research, kill points and Stage 1 work without missions. But non-operators lose 100 of the 150 Stage-1 points, plus 30 supplies and all signposting. |
| A placed post registers on load, earns +1 point and +1 local supply per loaded minute and deploys at Stage 0. There is no spacing rule, only a 4,096-post registry cap. | `BuildingBlockEntity`, `HumanWorldData` | Recipes alone would let players stack crafted posts as a point and supply farm. M32 needs a spacing rule. |
| Mission progress from kills, building ticks and research is computed but its messages are discarded. Only sample pickups and research completion reach a player. | `ThreatEvents`, `BuildingBlockEntity.refreshMissions` | Objectives complete silently in ordinary play. |
| Default rewards map only exact `minecraft:zombie`. Husks, drowned and zombie villagers need the opt-in `vanilla_undead` preset. | `ThreatRules.DEFAULTS`, `ThreatConfig` | A desert or ocean start can be sample-locked. |
| Level-1 posts hold 3 defenders, and Stage 0 fills them with Survivors. Riflemen (Stage 1) only appear when a slot frees or the post reaches Level 2. Level 2 needs 150 points, Initial Sample Analysis and 20 local supplies. | `DefenderCapacity`, `BuildingConfig` | The visible Stage-1 payoff is the Level-2 upgrade, and the post never says so. |
| `stockpile_supplies` needs 20 global supplies, pays no supplies and precedes first research. Any building can Export a 10-batch back to global. Breaking a building returns its local stock to global. | `definitions.json`, `BuildingMenu` | Importing early is recoverable at once if unspent. If already spent, it takes about 10 loaded minutes of post generation. |
| Starting a mission reads 0 for sample recovery; only the operator command then observes the actor's inventory. | `MissionManager.start`, `CommandFoundation.missionChange` | Samples carried before a mission starts need one explicit observation. |

Expected default pacing for the M32 path (to be measured, not promised): about 9 zombie kills for 3 samples (+~45 points). Chain rewards of 25 x 4 plus 10 research points bring Stage 1 to roughly the moment first research completes. Survivors deploy one per loaded minute up to 3. Level 2, and with it the first Riflemen, is about 20-30 loaded minutes after the post is placed.

### Choosing The First Slice

**Blocker 1: getting a post (chosen: A).**

| Option | For | Against |
| --- | --- | --- |
| A. Two vanilla crafting recipes (Post, Lab) unlocked through the recipe book | Small, data-driven, works in every world and seed, reuses loot/registration. Found checkpoints stay a richer shortcut. | Loses the "found the army" moment for players who craft first. Opens post stacking unless spacing is added. |
| B. Guaranteed starter site near world spawn ("Phase 0") | Closest to the long-term vision. | Needs a spawn-area terrain search, which breaks the bounded no-retry worldgen rule. Does nothing for existing worlds. One shared site per multiplayer world. Larger test surface. |
| C. First-join field kit item | Instant start. | Per-player state on world-shared progression. Rejoin/duplication edge cases. Skips the crafting signpost. |

A wins because it is the only option that is small, works on any seed and is fully testable at logic and GameTest level. B returns in M35 as an optional discovery route, never as something the earned playthrough depends on.

**Blocker 2: missions need an operator (chosen: B, disagreeing with Codex's input).**

| Option | For | Against |
| --- | --- | --- |
| A. Accept button per offered objective in the Command Post (Codex input) | Explicit consent. Keeps the M3 "start explicitly" model. | The chain starts only after a post exists. Five clicks that each complete instantly. Needs per-ID actions, stale-offer and click-race handling. Accepting Stockpile after an early import still hits the trap. |
| B. The five onboarding missions auto-activate in catalog order once prerequisites complete (`auto_start` catalog flag) | No client action, so no stale offers or click races. Sample recovery is live from the first credited kill, so the mod introduces itself before a post exists. Stockpile is evaluated the instant Establish pays out. Proof is mostly logic-level. | Changes the explicit-start model for flagged IDs. Existing worlds can pay out on first refresh (internal alpha only, documented). Rewards need a visible notice. |
| C. Let non-operators run `mission start` | Smallest code change. | Players must discover and type a command. Weak onboarding. |

B removes the race and staleness problems instead of testing around them, and the Orders view gives the visible "moment" that Codex wanted from Accept. Operator start/progress/complete commands stay exactly as they are. A datapack can clear the flag to restore operator-started missions.

### Sequence

| Phase / branch | Priority | Prerequisites | Status | Maximum slice |
| --- | --- | --- | --- | --- |
| M32 `claude/m32-earned-outpost` | P1 | M31 baseline; D1-D4 approved 2026-11-12 | On `main` (PR #4); earned run outstanding | Recipes, spacing, auto-started onboarding, Orders view, ordinary undead rewards |
| M33 `claude/m33-army-look-sound` | P2 | M32; D5 approved 2026-11-12 for 33.4 | Codex reviewed (no blocker); PR to `main` after Liam's play check; 33.4 experiment in progress | Native per-soldier variants and a first original sound set; the animation library is a separate, test-gated experiment (D5) |
| M34 `<agent>/m34-defend-the-post` | P2 | M32 | Provisional | One readable attack warning; `defend_outpost` observable in ordinary play |
| M35 `<agent>/m35-phase-zero-world` | P2 | M32, M34; D6 | Provisional | Promote an outpost to the world's HQ; stage-aware abandoned/damaged/manned sites and survivor camps; optional ruined-bunker discovery |
| M36 `<agent>/m36-hq-intelligence` | P2 | M35 | Provisional | Usable HQ: threat profile from vanilla mobs, recommended research started at the HQ, reinforcement dispatch; no autonomous spending |
| M37 `<agent>/m37-roadmap3-candidate` | P1 | M32-M36 | Provisional | Earned playthrough from spawn to an active HQ with no external mods, packaged install, bounded load; internal candidate 2 |

M33 can run in parallel with M34 on a separate branch because it touches only client art and sound plus NPC appearance data. **If Liam's earned run shows players reach Stage 1 with nothing meaningful to do, M34 moves ahead of M33.** Order is provisional and evidence-driven. Later milestones are re-planned against the code before they start. A fresh packaged-install check (P1) is scheduled in M37, or earlier if a distribution claim is wanted.

### Phase M32 - First Earned Outpost

Priority: P1. Prerequisites: M31 internal baseline on `main`; Liam approved D1-D4 as suggested on 2026-11-12. No new dependency, pin, mission ID or world-format migration.

- [x] **32.1 Measure before changing.** In a logic check, list checkpoint candidates within 512 blocks of spawn for three fixed seeds. Terrain pass rates need real seeds; a superflat GameTest cannot represent them, so they stay unmeasured until a real-seed survey. Add a logic check that the default catalog's onboarding rewards and stage thresholds still produce the pacing above, so catalog edits that break it fail loudly. Report the numbers; tune nothing in M32. *Measured 2026-11-12 (logic, before terrain checks): 15 / 9 / 12 checkpoint candidates within 512 blocks of 0,0 for seeds 170310 / 918273 / -4172144997902289642; nearest candidate about 80 / 148 / 248 blocks. Default pacing: 110 onboarding/research points leave 8 credited kills for Stage 1; about 8.6 kills yield 3 samples.*
- [x] **32.2 Make the Post and Lab obtainable, and bound them.**
  - [x] **32.2.1 Recipes.** Add two shaped recipes using vanilla items/tags and the existing item IDs, at D1 costs. Add one `inventory_changed` (iron ingot) recipe advancement in the existing format. The Supply Crate stays recipe-free. A crafted post gets no site provenance, crate stock or growth rights.
  - [x] **32.2.2 Spacing.** A player-placed Command Post is refused with a message giving the distance to the nearest registered post or Main Base in the same dimension when it is within D2 blocks. The block is not consumed. The check runs once, at placement, over the existing bounded registry. Generated sites and admin placement are unaffected. A config of 0 disables it. Labs and crates are not restricted.
- [x] **32.3 Run onboarding without an operator.** Add an optional `auto_start` boolean to mission definitions (format stays 1; absent means false; a wrong type fails catalog validation as other fields do). Flag the five early IDs in the default catalog. In a new bounded `MissionManager.sync` used by every gameplay refresh path, start each flagged mission in catalog order when its prerequisites are complete and it has no saved state, then evaluate it through the existing start/update path. Observe the player's carried samples once when they open any building or log in. Deliver each start and completion once to everyone online, like research completion. Progress lines go to the actor only. Leave `defend_outpost`, `establish_main_base` and every operator command unchanged.
- [x] **32.4 Show the objective and what blocks it.** Add an Orders view to the existing Command Post screen. It shows the current world-shared objective with its title, progress, a one-line condition (the blocked reason plus its recovery action) and a state in words: Active, Waiting for an operator, All complete, or Unavailable (definitions changed or unreadable: reopen). Required condition texts:
  - Samples: "one player carries 3 at once (you: N)".
  - Stockpile: "global X/20; supplies inside buildings do not count; Export 10 from a building holding 10 (this post: L, +1/min)".
  - Research: "Lab: select, Import 10, Start".
  - Stage: "P/150 points; credited kills and loaded posts add points".

  When Stage 1 is reached and the post is full, the deployment line suggests the Level-2 upgrade. The server computes every value; mission titles travel with menu data like research titles. The guide points non-operators to the post instead of an operator. Operator help text is unchanged.
- [x] **32.5 Make ordinary undead count.** Per D3, ship `presets = ["vanilla_undead"]` as the default for new configs. Manual entries still precede presets, so a zombie is credited once. Existing config files keep their saved values; say so in the changelog and known issues.
- [ ] **32.6 Prove it, then hand Liam the earned run.** Complete the evidence below, update existing build/known-issue/guide docs, then give Liam one cheats-off Survival run. *Automated evidence passed 2026-11-12 (see the progress log); the earned run is outstanding.*

**Systems reused:** `BuildingMenu` validation and sync, `MissionManager` receipts and exactly-once rewards, `SampleEvents` observation, `BuildingTransactions` import/export, the recipe-advancement format, the `vanilla_undead` preset, the QA client, the restart fixture and the packaged-QA scripts.

**Non-goals:** No new missions, rewards, costs or stage thresholds beyond D1-D3. No starter kit, spawn-guaranteed site or checkpoint density change. No raid, defence, growth, duty or tier changes. No per-player progress or claims layer. No models, animation, sounds or library. No change to operator/debug commands. No legacy or world migration, Fabric, release or tag.

**Exit gate (observable):**

| Claim | Proof |
| --- | --- |
| Catalog flag parses absent/false/true/malformed; the chain auto-starts in order; each reward is paid once; repeated refresh is idempotent; the early-import path recovers through Export; guide text differs correctly for operators and non-operators | `progressionTest` logic checks (`FoundationChecks`, a mission check set, `GuideChecks`) |
| Recipes resolve on the pinned target. A crafted post registers once with no provenance and deploys a Survivor at Stage 0. Spacing refuses a close post and leaves generated sites alone. | New GameTests |
| Pre-carried samples count once on menu open; drop/re-pick adds nothing. Two server players finishing the same objective produce one reward and one completion notice. Husk/drowned credit once; zombie is not doubled. A reloaded catalog shows Unavailable and grants nothing. | New GameTests |
| Mid-chain state (samples done, Stockpile at 10/20, Lab partly worked) survives a real restart with no repeat reward and no lost progress | `test-restart.ps1` extension (two-JVM) |
| The Orders view is readable in all four states, the recipe book shows both blocks, and the deployment hint fits | `runQaClient` captures at normal and large GUI scale, opened and judged by the agent |
| The normal jar contains the recipes and advancement, excludes QA classes and carries correct metadata | `clean build`, packaged-QA script, Windows CI |
| A real player can earn it | Liam's cheats-off run below. M31's operator-assisted fixtures and grants never count here, and 31.3 stays unchecked unless its own scope is played. |

**Liam's earned run (last, about 45-60 minutes, never blocks the technical merge):** fresh Survival world, cheats off, default config, no wiki.
1. Note the clock when you first kill a zombie, place the post, see the first Survivor, finish research and reach Stage 1.
2. Did the first sample message and the post's Orders view tell you what to do next?
3. Did the recipe costs feel fair for day one?
4. Was there any stretch of waiting with nothing useful to do, especially for supplies?
5. Play about 5 more minutes. Does anything feel off or annoying?

### Phase M33 - Army Look And Sound I

Priority: P2. Prerequisites: M32 (the branch is stacked on `claude/m32-earned-outpost` and merges after it). No new dependency, pin, saved data, packet, entity ID or AI change. Started early on Liam's choice to play M32 later.

- [x] **33.1 Per-soldier variants on the existing rigs.** Each soldier picks one of 12 appearance variants and a render-only build (height 0.95-1.05, width 0.96-1.04) from its entity UUID, so every client agrees with no saved data or packets and hitboxes, reach and AI are unchanged. Variant 0 is the authored atlas; 1-11 vary skin tone (six tones), eye colour, hair at the sideburns and hairline, facial hair or scars, a headgear pattern (plain, blotch or band), 0-3 rank stripes and boot wear. A pure-JDK generator (`gradlew generateNpcVariants`) derives all 72 atlases from the Blockbench exports and only repaints pixels that are already opaque, so silhouettes and cutouts never change. *Packs need geometry per tier and move to M38 with the unit art rollout.*
- [x] **33.2 One original sound set.** Three sound events replace the vanilla stand-ins: `npc.gear` (webbing and buckles on about every other soldier step), `launcher.fire` (launch thump, air release and the mechanism cycling; player and soldier launchers) and `post.radio` (garbled radio traffic, client-only ambience at a Command Post, about once a minute nearby). Every sample is synthesised by `scripts/generate-sounds.py` from sine partials, filtered noise and formant pulses: mono 44.1 kHz Vorbis, subtitled, no recordings or other mods' audio.
- [ ] **33.3 Prove it, then Liam's look-and-listen check.** Automated evidence below on CI, then Liam plays a few minutes near a post with a squad. *Automated evidence passed 2026-11-12: Windows CI 43275994632 (180/180 GameTests, 1,533 art and 62 sound assertions, restart) and a 103-capture QA client run on Liam's PC. The first close-up showed a full-width moustache reading as an open mouth and overlapping arms; fixed with a goatee and wider spacing. Look-and-listen outstanding.*
- [ ] **33.4 Separate animation experiment (D5).** Approved by Liam on 2026-11-12 with the recommended library, GeckoLib 5.5.7 (MIT; latest release for 26.3, re-checked on its source repository the same day). Branch `claude/m33-d5-geckolib`. Only the Army Rifleman uses it; every other tier keeps the native rig. It merges only when the D5 gate passes and Liam approves the motion. M33 does not wait for it. *Implemented 2026-11-12. Windows CI 43292247436: build, 1,838 art assertions, 180/180 GameTests with GeckoLib loaded on the dedicated server, packaged-QA metadata and restart passed. Outstanding: QA client captures and the 8/32 frame-time comparison on Liam's PC, a packaged install that includes GeckoLib, and Liam's motion check.*

**Exit gate (observable):**

| Claim | Proof |
| --- | --- |
| The shipped atlases are exactly what the generator makes from the current exports; alpha is unchanged; every tier's 12 variants differ; UUIDs spread evenly over the 12 variants (12,000 samples, each 800-1,200); build scales stay in bounds | `ArtChecks` in `progressionTest` |
| `sounds.json` declares exactly the registered events; every file exists, decodes with Minecraft's own Vorbis decoder, is mono 44.1 kHz, audible, unclipped and within its duration range; every subtitle is translated; no unreferenced sound ships | `SoundChecks` in `progressionTest` |
| The events are registered; a survival player firing the launcher makes the server play `launcher.fire`, not the old snowball throw | GameTests `presentation_sounds_registered`, `presentation_launcher_sound` |
| Variants read well at distance, close up, across tiers and by torchlight; the client resolves the expected variant for each fixture UUID | `runQaClient` captures `variants-riflemen-day`, `variants-tiers-day`, `variants-closeup`, `variants-torchlight` |
| It looks and sounds right in play | Liam's look-and-listen check |

### Decisions Before M32 Starts

Approved by Liam on 2026-11-12: D1 suggested costs, D2 96 blocks, D3 enable `vanilla_undead`, D4 the five onboarding missions. D5 approved later the same day (GeckoLib experiment). D6 stays open.

- **D1 Recipe costs.** Suggested starting point: Post = 4 iron ingots, 1 copper ingot, 4 planks; Lab = 3 glass, 2 iron ingots, 3 stone. No samples, redstone or new components. Adjust after the earned run.
- **D2 Post spacing.** Suggested 96 blocks: twice the 48-block defender area, so post areas never overlap. Configurable; 0 disables.
- **D3 Ordinary undead.** Turn on `vanilla_undead` by default for new configs (recommended), or keep zombie-only.
- **D4 Auto-start scope.** The five onboarding missions (recommended), or all seven. M34 owns the defence mission otherwise.
- **D5 (for the M33 experiment) Animation library.** Native `HumanoidModel` animation stays the default. A library is tried only as a separate, test-gated experiment: GeckoLib (5.5.7 recorded for 26.3) or AzureLib (4.0.14 recorded for 26.3), both MIT, at most one. Recommended: GeckoLib, as the more widely used Blockbench keyframe pipeline. It merges only if the tests prove it works (normal jar, dedicated server loads without client classes, frame time with 8 and 32 soldiers) and Liam approves how it moves.
- **D6 (for M35) How the HQ exists.** Recommended, following Codex's review: **promotion is the reliable path.** An **Establish HQ** order lets the player promote one existing Command Post (suggested: local Level 2 or higher after Field Communications; tune after the earned run) into the world's single HQ, at a real supplies-and-samples cost. A ruined **Command Bunker** near spawn is an optional discovery route that can also become the HQ; the playthrough never depends on finding it, and it can slip to a later milestone without blocking M37. Alternative: HQ active from world start (strongest identity, skips the earned moment).

**Active HQ, defined.** A building the player can use, not just a status: one per world, persisted across restart; its screen shows the world threat profile, the current order and the recommended research; the player can start that research from the HQ using its own local supplies; and the HQ can dispatch one paid reinforcement to any loaded post linked to it, using the existing support call-in rules. Losing the HQ block returns the world to the promotion path without losing research or order history.

### Provisional Later Milestones

Each one becomes active only when the stated evidence justifies it, after re-checking the code.

- **M33 Army look and sound I (P2).** Justified now by Liam's feedback that soldiers look basic and repetitive. *Steps 1-2 implemented; see [Phase M33](#phase-m33---army-look-and-sound-i).*
  1. Native per-soldier variants from the entity UUID: faces, skin tones, headgear, packs and insignia on the existing rigs. No saved data, no library.
  2. One original sound set: footsteps on gear, radio chatter at posts, launcher fire and reload.
  3. Separate experiment on its own branch (D5): re-author the Army Rifleman in Blockbench with walk, aim, fire, reload, hurt and death keyframes on one admitted library, and measure it against the native rig. M33 does not wait for it; it merges later only when its tests pass and Liam approves the motion.

  Other mods are reference for ideas and proportions only; no copied geometry, textures, animations or sounds. *Exit evidence:* QA captures of variants at several distances and lighting, sound playback in a GameTest or client check, and Liam's look-and-listen check.
- **M34 Defend the post (P2).** One readable warning before a raid reaches a post; `defend_outpost` joins the Orders view and becomes observable in ordinary play. Reuses raid reservations, support costs and cooldowns; no new global director. *Moves forward if* the earned run reports "after Stage 1 nothing happened" or "never saw a real attack".
- **M35 Phase 0 world and the HQ (P2).** The world starts scattered, as Liam described. First, the reliable HQ path: the **Establish HQ** order promotes an existing Command Post (D6). Then new chunks generate checkpoints that are **abandoned, damaged or manned** by world stage (mostly abandoned at Phase 0; manned sites become more common as humanity's stage rises) and small **survivor camps** with Survivor-class defenders. New Main Bases generate ruined or dormant until humanity's research and stage earn an active one. The ruined Command Bunker is an optional discovery route. Deterministic per chunk, bounded, original layouts; existing sites are not retrofitted and no chunk is force-loaded.
- **M36 HQ intelligence v1 (P2).** The HQ becomes the usable "beating heart" defined above. It keeps a bounded per-world ledger of credited kills by threat category. Vanilla mobs fill every category it needs on their own (undead, arthropods, explosive, ranged, illagers, other hostile), so the standalone game passes; external infection, alien and horror mods map into the same categories through tags later (M43). The HQ recommends research that counters what the world actually faces; v1 never spends on its own.
- **M37 Roadmap 3 candidate (P1).** Earned cheats-off playthrough from spawn to an active HQ **with no external mods installed**, fresh packaged install, bounded multi-post load with the new art, and fixes for demonstrated defects. Internal candidate 2; no public release without a separate decision.

### Automated Proof Versus Player Feedback

Automation owns correctness: numbers, exactly-once rewards, permissions, persistence, packaging, text fit and server authority. Liam owns what automation cannot judge: whether the start is understandable without a wiki, whether pacing and costs feel fair, how combat and motion feel, and whether the army looks and sounds right. A dev run, synthetic player or operator fixture is never reported as an earned playthrough. Unrun checks stay unverified.

## Future Roadmaps (Provisional)

These roadmaps record where the rest of the vision lands. They are not approved milestones, their order is not locked and no dates are estimated: Roadmap 2's pace is no guide for systems this large. Each roadmap is planned in detail only after the previous one's candidate passes, and every library, pin or release still needs Liam's approval. Numbers after M37 are reserved slots and may be renumbered when planned.

### Where Liam's Vision Lands

| Vision item | First lands | Grows in |
| --- | --- | --- |
| Earned Survival start, craftable posts, orders | M32 | M34, M35 |
| Soldiers that don't look repetitive (variants) | M33 | M38 |
| Better 3D models and animation (Blockbench + library) | M33 experiment (one unit, test-gated) | M38 all units, M44+ weapons, M49+ vehicles |
| Army sounds | M33 first set | M38 per unit, M44+ per weapon |
| Phase 0: scattered survivors, abandoned/destroyed/active outposts, Main Bases appear as research grows | M35 | M42 |
| HQ as the heart of the army | M35 (promote an outpost; ruined bunker as a discovery) | M36 usable HQ, M39 HQ orders, M43 HQ v2 |
| HQ predicts what to research and fields the right troops | M36 (recommends, from vanilla threats) | M43 (opt-in auto-research and roster bias) |
| Classes, variants, divisions, command structure | M39 | M40, M45 |
| Smarter AI (cover, suppression, retreat to medics, priorities per threat) | M40 | M45, M50 |
| Barracks, med camp, armoury, research complex, motor pool | M41 | M42, M49 |
| Self-upgrading Main Base and outpost network | M42 | M43 |
| Infection, zombie, alien and horror mod support | M43 (tag adapters + categories) | Exact-build tests whenever a mod ships for our Minecraft version |
| Guns and a real arsenal (ammo, reload, recoil) | M44 | M45 |
| Grenades | M46 | |
| Flamethrowers, minigun, rocket and missile launchers (several missile types) | M47 | M53 |
| Turrets, mortars, searchlights, wire that actually works | M48 | |
| Vehicles | M49 utility vehicle | M50 armour, M51 helicopters |
| Jets | M52 | |
| Missile silo and ICBM | M53 | |
| Fabric or other loaders | M57 at the earliest | |

Infection mods named by Liam are design targets, not current dependencies. As of 2026-11-12, The Flesh That Hates' newest build targets Forge 1.20.1 and Scape and Run: Parasites targets 1.12.2; neither runs on the 26.3 pin. Support is built through entity tags and threat categories (M43) and tested only against an exact compatible build once one exists. The hard design rules still apply: optional, never a hard dependency, nothing copied.

### Roadmap 4 - The Army Takes Shape (M38-M43)

Goal: a structured force with roles, leaders and facilities, run from the HQ. Starts after the M37 candidate.

| Slot | Priority | Slice |
| --- | --- | --- |
| M38 Unit art rollout | P2 | Every tier on whichever pipeline M33 proves: models, animation sets and sounds per role; variants everywhere |
| M39 Command structure | P2 | Squads with a leader, ranks and divisions as rosters bound to the HQ or a Main Base; HQ orders a squad to defend, patrol or reinforce a post |
| M40 Combat AI v2 | P2 | Cover, suppression, retreat to a medic, per-threat target priority; numeric path, target and spawn budgets defined before work |
| M41 Facilities I | P2 | Barracks (capacity), Field Hospital / med camp (healing), Armoury (equipment tiers), Research Complex (parallel research); one tested function each; original schematics |
| M42 Self-upgrading bases | P2 | Main Base and outposts grow on their own under a player-set policy and paid supplies, extending the guarded growth system; outposts report to the HQ |
| M43 Threats and HQ v2 | P2 | Tag-based adapters and threat categories for infection, zombie, alien and horror mods; HQ v2 can queue research and bias recruitment when the player opts in; D7 decides whether a small in-house threat faction is needed |

Candidate gate: earned playthrough to a running division, multiplayer session, load with several squads. **D8:** whether this candidate becomes the first public alpha.

### Roadmap 5 - Arsenal (M44-M48)

Goal: a weapons framework players and soldiers share, then weapon families one at a time.

| Slot | Priority | Slice |
| --- | --- | --- |
| M44 Weapon framework | P2 | Ammunition, magazines and reload, fire modes, recoil and spread, server-authoritative hits, animation and sound hooks, NPC loadouts |
| M45 Small arms | P3 | Service rifle, marksman rifle, shotgun, light machine gun; class loadouts |
| M46 Grenades and explosives | P3 | Frag, smoke and incendiary grenades, with block-damage and griefing controls |
| M47 Heavy weapons | P3 | Flamethrower (anti-infection), minigun, rocket launcher, guided missiles (anti-armour, incendiary, cluster) |
| M48 Emplacements | P3 | Sentry turret, mortar, functional searchlight and wire |

### Roadmap 6 - Vehicles And Strategic Warfare (M49-M54)

Goal: mobility, air power and strategic strikes, each behind the existing [vehicle admission gate](warfare-support-design.md#physical-vehicles-deferred-admission-gate).

| Slot | Priority | Slice |
| --- | --- | --- |
| M49 Vehicle foundation | P3 | **D9:** vehicle library or in-house physics; one ground utility vehicle with passengers, storage, unload and reconnect recovery; motor pool building |
| M50 Armour | P3 | Troop truck/APC and a tank; soldier drivers and gunners |
| M51 Helicopters | P4 | Transport and gunship; landing pad |
| M52 Jets | P4 | Strike jet and airfield; bounded flight AI |
| M53 Missile silo and ICBM | P4 | Off by default; operator and claim controls, bounded blast budget, no chunk force-loading |
| M54 Strategic candidate | P1 | Load with vehicles and strikes; multiplayer; performance on named hardware |

### Roadmap 7 - The Long War (M55+)

Goal: a full campaign and wider reach. HQ v3 counter-offensives and a world-scale campaign, M57 Fabric/other-loader evaluation (reopening M14), public beta and release decisions. Planned only when Roadmap 6 is stable.

## Deferred Expansion Admission

These are prioritised options, not numbered implementation milestones or an extra live roadmap. Roadmap 3 admits items from here one bounded slice at a time and the [future roadmaps](#future-roadmaps-provisional) give each one a provisional slot; anything not yet planned stays deferred. Scope them prospectively after the relevant alpha evidence; no installation, dependency or large-system approval is implied.

- **P2:** Additional Level-4/5 outpost and Main Base growth, a mixed-role recruitment policy, richer supply/research facilities and missions that use the finished duties. Each needs a separate bounded layout/transaction/saved-state contract, not a single full-faction rewrite.
- **P3:** Carry the proven motion pipeline to other units/armour and improve the prototype's original model/sounds; then scope one ammo/reload or weapon-role experiment at a time. No mass gun catalogue or combat rebalance hidden in art work.
- **P3:** One original functional defence device at a time (for example a watch alert), with truthful range/effects, targeting, power/resource costs if any, disable/comfort settings and bounded tests. Wire/fences/searchlights stay decorative unless their tested function exists.
- **P3:** Separately approved Lost Cities urban-terrain or SecurityCraft coexistence/claim API tests, and a new exact-target infection combination if available. Listed files are not support; base growth never assumes third-party land-claim permission.
- **P4:** One small ground utility transport before combat vehicles or aircraft, using the [vehicle admission contract](warfare-support-design.md#physical-vehicles-deferred-admission-gate): authority, collision/passengers, inventory/UUID, unload/reconnect recovery, original art and measured 1/4/8-vehicle budgets. A library needs exact-artifact approval first; no custom physics/engine commitment in this roadmap.
- **P4:** Aircraft/free flight, large combined-arms forces, full scientist/profession AI and independent convoys after smaller systems prove useful and affordable. No long-distance force-loading or unbounded army director.
- **P4:** Fabric/other loaders remain deferred under the M14 parity/admission decision until the NeoForge alpha is stable enough to maintain. No pin change, common-module split or extra library solely to anticipate a later port.
