# Modern Port Phase Roadmap

This roadmap covers the NeoForge modern rewrite. The existing M0-M14 phase names remain stable; each phase now has numbered tasks and smaller subtasks where needed.

## How To Track Work

- Phase M1 uses steps `1.1`, `1.2`, and so on. A larger step uses `1.1.1`, `1.1.2`, and so on. Phase M0 uses `0.x`.
- Keep existing identifiers stable. Add a subtask only before work genuinely needs splitting, never retroactively or deeper than `x.y.z`; retain historical task IDs.
- `[x]` means completed and verified; `[ ]` means outstanding. A parent is complete only when all its subtasks and verification pass.
- Keep live state in [STATUS.md](STATUS.md) and append concise observed results to [Progress Log](progress-log.md), following [AGENTS.md](../../AGENTS.md). Do not mark a build or launch passed without observing it.
- Tick tasks as verification passes. Commit logical changes; Git history records hashes, and the session report records publication.
- Later phases describe planned scope. Expand large steps before implementation as APIs, dependencies, and testing needs become clearer.

Current position: **Phase M19 player/developer growth controls exit gate passed**; see [STATUS.md](STATUS.md).

## Priority Scale

- P0 - Preservation and decision gates: must happen before risky changes.
- P1 - Critical foundation: required for any playable modern build.
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
- [ ] **20.4 Verify the complete milestone and document usage.** Logic and native GameTests for role stats/AI/unlocks/retention and all deployment paths, real restart for identities/equipment/rosters, reviewed qaClient tier views, final clean build/jar isolation and green CI. Human feedback only for silhouette/combat feel; multiplayer/packaged claims stay separate.

Exit gate:

- New units are distinguishable, configurable and stage/research-gated for automated deployment; old NPCs, saves and common limits remain intact.
- No extra population budget, dependency/pin change, weapon system or support claim is smuggled into the tier milestone.

## Phase M21 - Useful Level-2 Outpost Layout

Priority: P2

Scope: improve usable space rather than repeat the cramped M18 outer ring. One original Level-2 layout/delta first; Level-3 second floors, functional sensors/wire, new storage mechanics, larger shared armies and Main Base expansion stay outside this milestone. M19 interaction controls are reused, not replaced with another command procedure.

- [ ] **21.1 Author the layout and upgrade contract.** Define a usable yard, protected command/lab/storage access, clear entrance and one modest watch position; distinguish new sites, already-apron-expanded sites and customised sites. Show exact local-level/footprint requirements and identify preserved buildings and any old-wall removals before mutation.
- [ ] **21.2 Implement bounded planning and protected application.** Extend provenance/layout revisions and expected-state deltas only as required; refuse unsupported/occupied/unknown changes and preserve inventories, labs, identities, receipts and reservations. No silent adoption, full-site regeneration or loot reseed.
- [ ] **21.3 Integrate manual/automatic growth and readable status.** The same menu policy selects the supported layout at the earned local level. Make failure/space requirements actionable; capacity remains the existing shared policy, not a side effect of decorative towers.
- [ ] **21.4 Verify usefulness and safety.** GameTests for new/apron/customised cases, terrain/entrance/NPC access, protected removal and fault/repeat behavior; two-JVM persistence; reviewed overhead/interior/menu captures; clean build/jar isolation and green CI. Liam judges space/navigation and visual payoff after automation.

Exit gate:

- One Level-2 upgrade provides visibly useful space with working building/defender access and no loss, duplication or unsafe replay of existing state.
- Larger multi-level growth and functional defensive systems remain explicitly separate, not implied by the Level-2 layout.

## Later Content Backlog

Not a second live roadmap or approval to implement everything at once; refine into bounded milestone steps before starting work. The [direction/reference review](gameplay-direction-and-references.md) records the larger level/identity vision.

- **P2:** Level-3 usable upper floor and dedicated zones; original additional layouts and properly budgeted storage upgrades.
- **P3:** Functional watch positions, guarded approaches, reachable patrols and clear defensive feedback; original soldier/weapon/armour animation polish, after a small pipeline evaluation and explicit approval if a runtime library is required.
- **P3:** Isolated optional horde/urban-world coexistence QA using verified candidate artifacts, only after installation/dependency approval; no external-mod support claim from listings alone.
- **P4:** Ground vehicles before aircraft, following the existing warfare/performance contract; broader combined-arms content and additional loaders after stable NeoForge gameplay. No copied art/code or automatic pin changes.
