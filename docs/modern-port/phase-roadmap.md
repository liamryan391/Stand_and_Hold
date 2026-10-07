# Modern Port Phase Roadmap

This roadmap covers the NeoForge modern rewrite. The existing M0-M14 phase names remain stable; each phase now has numbered tasks and smaller subtasks where needed.

## How To Track Work

- Phase M1 uses steps `1.1`, `1.2`, and so on. A larger step uses `1.1.1`, `1.1.2`, and so on. Phase M0 uses `0.x`.
- Keep identifiers stable. Append new subtasks when work grows and record scope changes in the progress log.
- `[x]` means completed and verified; `[ ]` means outstanding. A parent is complete only when all its subtasks and verification pass.
- Record active work, blockers, actual test results, and the next exact step in [Progress Log](progress-log.md). Do not mark a build or launch passed without observing it.
- Update the checklist after each completed step. Commit logical changes and record their commit IDs when committed; record push status separately.
- Later phases describe planned scope. Expand large steps before implementation as APIs, dependencies, and testing needs become clearer.

Current position: **Phase M0, next step 0.3.3**. Preservation, access, planning, official release/MDK availability, and matching library/threat-mod artifacts are verified. The exact modern toolchain is not pinned and no modern scaffold has been created. Evidence: [Release Verification](release-verification.md) and [Ecosystem Compatibility](ecosystem-compatibility.md). Runtime compatibility is still untested.

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
- [ ] **0.3 Select and record the exact modern toolchain.**
  - [x] **0.3.1** Check official Minecraft/NeoForge releases and MDK availability. Compare the proposed 26.3 track with stable alternatives; record release status and source URLs. Evidence: [Release Verification](release-verification.md), 2026-10-07.
  - [x] **0.3.2** Check matching Minecraft AND loader support for likely animation/AI libraries and at least one target threat mod. Separate required dependencies from optional future integrations. Evidence: [Ecosystem Compatibility](ecosystem-compatibility.md), 2026-10-07; published artifacts/manifests checked, no combined runtime test.
  - [ ] **0.3.3** Select exact Minecraft, NeoForge, Java, Gradle, and build-plugin versions from a compatible official MDK. Record the rationale and local JDK selection; resolve the PATH/JAVA_HOME mismatch for the build process.
  - [ ] **0.3.4** Record Windows build commands, IDE Gradle JVM setup, and the rule for choosing a fallback if build verification fails.
- [ ] **0.4 Finalize foundation scope and dependency policy.** Start NeoForge-only unless an essential dependency is justified; defer animation/AI libraries until their phase. Record that legacy world conversion is not part of scaffolding and use separate test worlds.
- [ ] **0.5 Prepare the scaffold handoff.**
  - [ ] **0.5.1** Commit the final M0 decisions, including pinned versions and setup notes, separately from the scaffold and record push status. The earlier research checkpoint published at the end of 0.3.2 does not replace this final decision handoff.
  - [ ] **0.5.2** Recheck remote refs and user changes; prepare `codex/modern-neoforge-scaffold` from the agreed `main` baseline, with a merge back to `main` after verification.

Exit gate:

- The old code is preserved locally and on GitHub, and planning work is committed.
- Exact toolchain versions, sources, dependency policy, and scaffold scope are recorded.
- Implementation proceeds to M1; build proof is recorded there, not assumed here.

## Phase M1 - Clean NeoForge Scaffold

Priority: P1

- [ ] **1.1 Prepare the legacy-to-modern file transition on the work branch.**
  - [ ] **1.1.1** Recheck the preserved `1.12` commit and inventory legacy build, source, resource, script, and CI files.
  - [ ] **1.1.2** Preserve modern planning notes and project identity; move legacy-only documentation references to the `1.12` branch in the modern README.
  - [ ] **1.1.3** Replace the inventoried legacy implementation/build files with the chosen MDK layout; leave local worlds and unrelated user files alone.
- [ ] **1.2 Configure the modern build.**
  - [ ] **1.2.1** Pin the MDK wrapper, Java toolchain, build plugin, Minecraft, and NeoForge versions.
  - [ ] **1.2.2** Set `standandhold`, `Stand and Hold`, and `com.liamryan.standandhold`; configure metadata, resource expansion, and run profiles.
  - [ ] **1.2.3** Update build instructions/helper and CI to use the selected JDK; upload the correct normal mod jar.
- [ ] **1.3 Add the minimal mod entry point.** Add constants, logger, config shell, and startup logging; keep client initialization separate from common/server code.
- [ ] **1.4 Verify the scaffold.**
  - [ ] **1.4.1** Run `./gradlew --version` and `./gradlew clean build --no-daemon --stacktrace` with the chosen JDK (Windows: `.\gradlew.bat`). Inspect jar metadata.
  - [ ] **1.4.2** Run `runClient`; confirm main menu, Mods list, and a new test world without external threat mods.
  - [ ] **1.4.3** Run `runServer` in an isolated test directory through readiness and clean shutdown. Record an EULA stop as incomplete launch verification, not a successful server test.
- [ ] **1.5 Commit the scaffold, merge the verified work into `main`, and record local/CI results and push status.** Confirm `1.12` is unchanged.

Exit gate:

- The clean build passes and the expected jar exists.
- Client and dedicated server launch results are recorded; missing runtime checks remain outstanding.
- `main` contains the verified modern scaffold and `1.12` remains preserved.

## Phase M2 - Persistence And Progression Core

Priority: P1

- [ ] **2.1 Rebuild progression definitions.** Port stage identities and configurable thresholds; define bounds and what setting a stage does to points.
- [ ] **2.2 Implement server-owned SavedData.**
  - [ ] **2.2.1** Define one global owner across dimensions, a versioned schema, and safe defaults for absent fields.
  - [ ] **2.2.2** Add non-destructive migration hooks and dirty marking only when values change.
  - [ ] **2.2.3** Add HumanPointManager operations with amount validation and overflow protection.
- [ ] **2.3 Add Brigadier `status`, `addpoints`, and `setstage` commands.** Validate arguments and enforce admin permissions for mutations.
- [ ] **2.4 Verify and document progression.** Build; test stage boundaries, invalid inputs, dimension changes, save/reload, and dedicated-server commands. Record results before committing.

Exit gate:

- Points/stage save and reload.
- Commands work on client-integrated and dedicated server.

## Phase M3 - Research, Supplies, And Missions Foundation

Priority: P1

- [ ] **3.1 Define research data.** Add stable IDs, requirements, prerequisites, costs, and unlocks; safely reject duplicate/malformed definitions and cycles.
- [ ] **3.2 Add supplies and research transactions.**
  - [ ] **3.2.1** Persist global supply totals and validate all add/spend operations.
  - [ ] **3.2.2** Check prerequisites and costs before completing research; apply costs, completion, and rewards once on the server.
  - [ ] **3.2.3** Expose minimal status/list/admin commands; keep physical sample requirements ready for M5.
- [ ] **3.3 Rebuild small mission definitions and state.**
  - [ ] **3.3.1** Define a short objective chain that uses planned samples, buildings, research, supplies, and defence hooks.
  - [ ] **3.3.2** Persist active progress, completion, and reward state; handle unknown IDs without deleting unrelated saves.
  - [ ] **3.3.3** Add readable list/status/debug commands and meaningful progress feedback without repeated completion rewards or recursive callbacks.
- [ ] **3.4 Verify foundation data.** Build; test save/reload, insufficient costs, repeated completion, and malformed/missing data. Gameplay hooks are verified when their owning phases land.

Exit gate:

- Complete first research by command.
- Supplies save/reload.
- Mission state saves/reloads and rewards only once.

## Phase M4 - Core Blocks And UI Shell

Priority: P1

- [ ] **4.1 Register Field Command Post, Research Lab, and Supply Crate blocks/items.** Add simple original resources, names, creative entries, and basic access for testing.
- [ ] **4.2 Implement building state and storage.**
  - [ ] **4.2.1** Save command-post level/storage and dimension-aware registration; unregister on removal and ignore unloaded positions during cleanup.
  - [ ] **4.2.2** Save lab target/progress/storage and supply crate state. Expose a bounded sample-consumption path for M5.
  - [ ] **4.2.3** Add bounded passive points/supply generation and research work, respecting loaded state, intervals, and configurable caps.
  - [ ] **4.2.4** Implement upgrade requirement checks and local/global supply transfer without duplication or partial spending.
- [ ] **4.3 Add basic menus, screens, and server actions.**
  - [ ] **4.3.1** Display progression, research requirements, storage, and blocked/completed states clearly.
  - [ ] **4.3.2** Validate player distance, dimension, block entity, active menu, and action values; change state on the server thread and sync only needed data.
  - [ ] **4.3.3** Keep client screen/render classes out of dedicated-server initialization.
- [ ] **4.4 Verify buildings.** Build; test placement/removal, unload/reload, GUI reopen, two-player transfers, rejected remote actions, and research/mission hooks.

Exit gate:

- Blocks place, save, load, and open UI.
- UI reads server data correctly.

## Phase M5 - Samples And Threat Reward Hooks

Priority: P2

- [ ] **5.1 Register Parasite Tissue Samples with original placeholder resources and lang entries.**
- [ ] **5.2 Add optional threat identification and rewards.**
  - [ ] **5.2.1** Define tags/config mappings with `minecraft:zombie` as a clearly documented test default.
  - [ ] **5.2.2** Award points and sample drops once per qualifying server death; define kill attribution and ignore null, malformed, or absent registry entries safely.
  - [ ] **5.2.3** Connect sample pickup missions and actual lab sample consumption, including inventory and full-storage edge cases.
- [ ] **5.3 Verify the first resource loop.** Build; test configured/unconfigured kills, reward/drop boundaries, pickup progress, lab consumption, and operation without other mods.
- [ ] **5.4 Document mappings and missing-mod behavior.** Keep external IDs inactive until verified for the selected version/loader.

Exit gate:

- Default test mapping works.
- Invalid registry IDs/tags do not crash.

## Phase M6 - Human NPC Foundation

Priority: P2

- [ ] **6.1 Register the base human NPC and initial survivor/army tiers.** Configure stats, attributes, stage requirements, spawn eggs, and temporary original visuals.
- [ ] **6.2 Add simple, stable AI.**
  - [ ] **6.2.1** Evaluate the chosen version's vanilla Goal/Brain APIs before selecting any helper library.
  - [ ] **6.2.2** Add bounded wander/look/target tasks; reuse threat mappings and avoid attacking players by default.
  - [ ] **6.2.3** Handle removed targets, unreachable positions, unloaded regions, and server/client separation.
- [ ] **6.3 Add controlled command-post deployment.** Enforce stages, per-post/area caps, intervals, and valid spawn locations; document test egg behavior separately.
- [ ] **6.4 Verify NPCs.** Build; test spawning, combat, stage gates, several nearby posts, unload/reload, missing mappings, and dedicated-server loading.

Exit gate:

- Defender spawns, targets configured enemies, and does not attack players by default.
- No dedicated server renderer crashes.

## Phase M7 - Bases, Structures, And World Presence

Priority: P2

- [ ] **7.1 Choose bounded structure placement and config.** Define allowed dimensions, spacing/chance, footprint, terrain limits, and debug command policy.
- [ ] **7.2 Build a Small Army Checkpoint.** Add an original lightweight layout with an entrance, lighting, protected command post, and restrained supply access.
- [ ] **7.3 Build a distinct Main Base foundation.** Add simple command/research/supply zones and stage-based activation without a large base expansion.
- [ ] **7.4 Make placement and registration safe.**
  - [ ] **7.4.1** Preflight relevant terrain, height, fluids, and occupied space; use the chosen worldgen context without triggering extra chunk loads.
  - [ ] **7.4.2** Register dimension/position data only for successfully placed required blocks; report failed placement clearly.
  - [ ] **7.4.3** Clean stale loaded command-post/main-base records; preserve unloaded records and completed mission history.
- [ ] **7.5 Verify world presence.** Build; test debug placement and natural generation, unsuitable terrain, break/removal, save/reload, and discovery missions. Update structure documentation.

Exit gate:

- Structures generate safely.
- Important block positions register and survive reload.

## Phase M8 - Art Pipeline And Identity

Priority: P2/P3

- [ ] **8.1 Write the visual style guide.** Define silhouettes, tier colours/markings, UI readability, and original asset ownership/source records.
- [ ] **8.2 Establish the Blockbench pipeline.**
  - [ ] **8.2.1** Verify animation library support for the pinned loader/version; select one only if required.
  - [ ] **8.2.2** Validate one model's UV layout, texture dimensions, export, and in-game animation before making variants.
  - [ ] **8.2.3** Keep editable source models and export instructions in the repository or a documented linked asset location.
- [ ] **8.3 Create distinguishable visuals for implemented human tiers and core items/blocks.** Add further tier art when those units are implemented.
- [ ] **8.4 Polish existing GUI labels and layout.** Test long research names, blocked states, and supported GUI scales.
- [ ] **8.5 Verify art in game and build.** Check texture fit, missing resources, animation, held equipment, lighting, and dedicated-server isolation; capture reference screenshots.

Exit gate:

- Each unit tier is readable in screenshots.
- No missing textures in the first playable slice.

## Phase M9 - Combat Equipment

Priority: P2/P3

- [ ] **9.1 Define a small equipment set and configurable stage/research gates.** Record costs and intended roles using Minecraft-scale combat.
- [ ] **9.2 Add an anti-threat melee weapon and initial armour tiers.** Include attributes, recipes/test access, original models, and lang entries.
- [ ] **9.3 Add a simple ranged prototype.**
  - [ ] **9.3.1** Implement server-owned projectile spawn, cooldown, hit detection, and damage attribution.
  - [ ] **9.3.2** Add bounded visual/audio feedback and safe use by one existing NPC tier if practical.
  - [ ] **9.3.3** Verify unlock checks and friendly-target behavior; defer advanced reload/ammo mechanics.
- [ ] **9.4 Verify equipment.** Build; test player/NPC use, multiplayer damage, cooldowns, recipes, stage gates, and save/reload. Evaluate external combat integrations later only if useful.

Exit gate:

- Equipment is usable, craftable or test-spawnable, and gated by stage/research.

## Phase M10 - Threat Response And Events

Priority: P2/P3

- [ ] **10.1 Define bounded regional threat state.** Persist scores, losses, kill/attack counters, decay timestamps, and record limits.
- [ ] **10.2 Add event hooks and decay.** Update from real server events; throttle decay and use loaded-region/tile-local work without global per-tick scans.
- [ ] **10.3 Add outpost attacks and reinforcements.**
  - [ ] **10.3.1** Enforce stage-based selection, cooldowns, per-post/area caps, and failed-spawn backoff.
  - [ ] **10.3.2** Assign bounded patrol targets, connect defence missions, and persist event state across reload.
  - [ ] **10.3.3** Add admin trigger/status commands and configurable player warnings without chat spam.
- [ ] **10.4 Verify response behavior.** Build; test natural/debug events, cooldown reload, multiple outposts, invalid targets, and long-session entity counts.

Exit gate:

- Threat changes from real events.
- Reinforcements can spawn without mob spam.

## Phase M11 - Compatibility Layer

Priority: P3

- [ ] **11.1 Recheck candidate mods against the pinned Minecraft and NeoForge versions.** Record exact compatible artifacts and licences; do not infer compatibility from similar version numbers.
- [ ] **11.2 Add generic presets and optional adapters.**
  - [ ] **11.2.1** Verify registry IDs/tags for rewards, samples, targeting, and missions before activating any preset.
  - [ ] **11.2.2** Isolate integrations behind mod-loaded checks; avoid importing external classes in common initialization.
  - [ ] **11.2.3** Document presets and their source versions for modpack authors.
- [ ] **11.3 Verify with and without external mods.** Build; test at least one available modern threat mod, removal in a backed-up test environment, and unknown mappings.
- [ ] **11.4 Record supported combinations and deferred integrations.** A 1.12 SRP jar is not evidence of modern-loader compatibility.

Exit gate:

- Works with vanilla-only test threats.
- Works with at least one modern infection mod through config/tags.

## Phase M12 - Showcase Vertical Slice

Priority: P3

- [ ] **12.1 Assemble the existing loop.** Sample recovery, command post, lab research, supplies, defender, one attack, and save/reload should be playable with clear guidance.
- [ ] **12.2 Run a new-tester session.**
  - [ ] **12.2.1** Provide a guidebook or command-based tutorial with expected results and blocked-state explanations.
  - [ ] **12.2.2** Observe a tester trying the loop for about 15 minutes; record confusion, failures, and command dependence.
  - [ ] **12.2.3** Fix issues within the implemented loop and retest before expanding scope.
- [ ] **12.3 Prepare community materials.** Capture original gameplay screenshots, a short defence clip, page drafts, and an honest done/next/later roadmap.
- [ ] **12.4 Prepare a test release.** Run build/client/server and performance checks; update changelog, known issues, supported versions, artifact details, and tester instructions. Record publishing separately.

Exit gate:

- New tester can understand the loop in 15 minutes.

## Phase M13 - Vehicles, Planes, And Larger Warfare

Priority: P4

- [ ] **13.1 Evaluate the need and integration options after the core slice is stable.** Check compatible vehicle mods and decide which gameplay role Stand and Hold must own.
- [ ] **13.2 Write a separate technical design and performance budget.**
  - [ ] **13.2.1** Define call-ins, deployables, transport markers, supply costs, and spawn caps as the first scope candidate.
  - [ ] **13.2.2** Define ownership, networking, collision, persistence, unloading, and recovery behavior for any proposed physical vehicle.
  - [ ] **13.2.3** Plan aircraft support/set pieces before considering free flight; resolve animation and dedicated-server requirements.
- [ ] **13.3 Implement one selected prototype only after the design gate.** Keep vehicle/aircraft expansion as separately scoped follow-up work.
- [ ] **13.4 Verify the prototype under multiplayer load.** Build; test chunk boundaries, restart, passengers if applicable, and entity/tick budgets before expansion.

Exit gate:

- Vehicle work has a separate technical design and performance budget.

## Phase M14 - Multi-Loader Evaluation

Priority: P4

- [ ] **14.1 Confirm the NeoForge release baseline is stable and maintained.** Assess Fabric audience, matching dependencies, and support cost.
- [ ] **14.2 Audit platform-specific code and shared data.** Record registries, networking, config, persistence, rendering, and test differences.
- [ ] **14.3 Choose a port strategy.** Evaluate a common/platform module split against separate ports; document the cost before restructuring.
- [ ] **14.4 Prototype Fabric in isolation.** Verify the scaffold, persistence, and one existing interaction before attempting parity.
- [ ] **14.5 Add parity tests, CI, and support documentation only after the prototype succeeds.** Keep the NeoForge release working throughout.

Exit gate:

- NeoForge build is stable enough that multi-loader work will not slow core gameplay.
- The evaluation has a recorded proceed/defer decision; a Fabric release additionally requires working parity checks.

## Immediate Next Step

**Phase M0 - Preserve And Decide, step 0.3.3: select and record exact Minecraft, NeoForge, Java, Gradle, and build-plugin versions and resolve local build JDK selection.** The availability/compatibility research recommends 26.3 with 26.2 as the non-beta-loader fallback; 1.21.1 remains the verified Spore option. Complete the remaining M0 gates before Phase M1, step 1.1.1 begins scaffold implementation.
