# Foundation Scope And Dependency Policy

Decision: Phase M0, step 0.4, 2026-10-14. This is the accepted starting scope for the modern port, not implementation or runtime proof. It resolves the earlier provisional "NeoForge-only unless essential" recommendation: **M1 has no additional third-party gameplay/library dependencies and no approved exceptions.**

The ambition remains a larger human-resistance mod. Small verified phases control implementation order, not the eventual size of the project. This policy owns scope and dependency admission; [Toolchain](toolchain.md) owns exact version pins, [Building](building.md) owns commands and fallback rules, and the [Roadmap](phase-roadmap.md) owns task IDs and completion.

## M1 Deliverable

A clean NeoForge development project that builds and launches on the selected target, before any old gameplay systems are rebuilt.

| Included in M1 | Boundary |
| --- | --- |
| Official MDK-based build | Use the pinned Minecraft/NeoForge/JDK/Gradle/plugins and wrapper checksum; preserve required template notices and verify its licence during import |
| Project identity | `standandhold`, `Stand and Hold`, `com.liamryan.standandhold`; correct expanded metadata and normal mod jar |
| Minimal entry point | Startup logger and a small config shell; no progression, commands, blocks, mobs, or sample MDK demo content left registered |
| Side-safe initialization | Common/server startup must not load client-only classes; add client setup only when needed, not an empty framework |
| Development support | Java selector/build instructions, Windows CI build/artifact configuration, and separate modern client/server run directories |
| Verification and handoff | Actual clean build, jar inspection, client/new-world check, dedicated-server readiness/clean shutdown, and recorded local/CI results |

Do not mechanically port the legacy classes or pre-create large service layers, module hierarchies, empty package trees, networking, or registries for future features. Preserve existing design lessons and identity, then implement each system in its owning phase. Keep the current `0.1.0-alpha.1` value unchanged during M0; this decision does not retag the legacy alpha or authorize a release/version bump.

## Deferred Work

| Work | Owning phase; not M1 scope |
| --- | --- |
| Versioned progression data and admin commands | M2 |
| Research, global supplies, and missions | M3 |
| Command post, lab, crate, building upgrades, and basic UI/network actions | M4 |
| Samples, configurable threat rewards, and safe missing-ID handling | M5 |
| Initial human NPCs and bounded defender AI | M6, starting with vanilla goals/Brain APIs |
| Checkpoint and Main Base foundations | M7 |
| Original model/texture workflow and optional animation library | M8 |
| Small equipment set and ranged prototype | M9 |
| Threat response, raids, reinforcements, and patrols | M10 |
| Verified external-mod presets/adapters | M11; an optional coexistence smoke test may follow a working M1 baseline |
| Integrated showcase loop and public test preparation | M12 |
| Vehicles, aircraft, and larger warfare | M13, after a separate design/performance gate |
| Fabric and other loader evaluation | M14, after a stable NeoForge foundation |

The first showcase assembles existing phase work: recover samples, establish a command post/lab, complete research, obtain a defender, survive one attack, and save/reload. It is not promised by the empty M1 scaffold. Full campaigns, advanced reload/ammo systems, huge bases, and additional loaders do not enter M1 through dependency selection.

## Dependency Classification

"NeoForge-only" means no added runtime library mods beyond the Minecraft/NeoForge platform and its normal dependencies. It does not mean a dependency-free build or removal of libraries provided by the platform.

| Category | Decision |
| --- | --- |
| Platform/runtime | Minecraft + NeoForge and their ordinary resolved dependencies; use platform APIs and the JDK before adding libraries |
| Build-time tooling | The selected wrapper, ModDevGradle, Foojay resolver, and necessary official MDK build plugins/transitives are allowed; these are not extra player-installed mods |
| First-party Stand and Hold code | One NeoForge project initially; no common/Fabric module split or new abstraction just to anticipate another loader |
| AI library | SmartBrainLib is an evaluation candidate in M6, not a current dependency; add it only if concrete behavior complexity justifies it |
| Animation library | Evaluate one library in M8; GeckoLib is the recorded prototype preference, AzureLib the alternative. Do not add both or import their APIs during M1 |
| Information/UI/accessory helpers | JEI/EMI, Jade/WTHIT, accessory APIs, and guidebook libraries are deferred candidates, not supported integrations or promised compatible dependencies |
| Threat mods | Optional test-instance content and later mappings/adapters; never required to launch or play the core mod |
| Test/development-only additions | Add only for a stated test need; do not leak them into published runtime requirements or the release jar |

Keep exact pins and source provenance for direct additions; no dynamic `+`, moving `latest`, unreviewed snapshots, or copied jars in `libs/`. Use the existing [ecosystem snapshot](ecosystem-compatibility.md) as dated research, not permission to install a dependency or proof of a tested combination. No library or threat-mod version is newly selected by this policy.

## Admission And Exceptions

Before adding any dependency in its owning phase, record this compact decision alongside that phase's work:

| Required record | Evidence to provide |
| --- | --- |
| Need and scope | Feature/test that requires it, owning task ID, why existing platform APIs are insufficient, and the no-dependency alternative |
| Exact artifact | Author-controlled source/repository, coordinate or release file, exact Minecraft AND NeoForge versions, Java requirements, and release status |
| Runtime contract | Build-only, dev/test-only, required library, or optional integration; physical sides needed and all additional required/transitive mods |
| Licence/provenance | Licence and required notices for the exact artifact; redistribution/bundling permission and original asset provenance. Unknown permission means do not copy or bundle |
| Verification | Clean build and client/dedicated-server results for the selected combination, startup without optional mods, and a removal/failure test where relevant |
| Maintenance cost | Known conflicts/performance impact, reason for choosing it over alternatives, and a removal/replacement plan |

Do not call a library optional if core code directly requires its classes or its models. If an AI/animation library becomes essential later, declare and document the required runtime dependency accurately. That does not make any threat mod mandatory.

Normal phase-local additions must pass this record and review. **Adding a runtime library in M1, moving a later feature into the scaffold, changing the loader/version, or altering save-conversion scope requires an explicit maintainer-approved scope change first.** Update this policy, the roadmap, memory, and test expectations before implementation; use the [fallback procedure](building.md#fallback-decision-rule) for toolchain blockers. No exception is approved by this step.

Do not shade, bundle, download at runtime, or redistribute another mod by default. Availability of source or a jar is not permission to reuse its code or art. Referencing external registry IDs/tags for compatibility does not require copying those implementations. Stand and Hold models/textures remain original; official MDK imports and any later authorised reuse must retain applicable notices.

## Optional Compatibility Boundary

- Core progression, research, missions, and defence must work without SRP or any other infection mod. When M5 exists, use the clearly labelled `minecraft:zombie` test mapping for baseline QA.
- Prefer data/config tags and registry IDs. Missing mods, invalid IDs, and absent tags must disable/skip only the affected mapping safely; do not automatically install missing mods or substitute unchecked IDs.
- M1 has no threat-mod imports, adapters, presets, or automatic registry scanning. Later adapters must isolate external classes from common initialization and load only when the matching mod is present on the appropriate side.
- Keep optional test mods out of the default development run and release artifact. A baseline without them must pass before a separate coexistence test. Record exact test-instance mod lists; never equate an author-listed matching version with tested compatibility.
- An optional mod's removal test uses a disposable backed-up copy. Stand and Hold handling missing mappings does not promise another mod's saved blocks/entities survive removal, nor that a mod can be removed from a running server.
- The old SRP 1.12.2 jar and legacy compatibility IDs do not become modern dependencies. Verify modern target artifacts and IDs afresh when implementing an integration.

## Saves And Local Files

- `1.12` and its existing release tags remain the legacy reference. Replacing `main` happens only through the verified scaffold workflow, not during this policy step.
- Back up valuable worlds and configurations outside the checkout before M1 file replacement. A Git branch preserves committed source, **not ignored worlds, caches, `.local` settings, or test jars**. Inventory those files before editing; never delete them as part of a broad scaffold cleanup.
- Use new modern worlds in separate `run/modern-client` and `run/modern-server` directories. Do not open/convert 1.12 saves or copy old `standandhold.cfg`/saved NBT into them. Legacy world, config, and saved-data migration are explicitly out of scope; any future importer needs its own design and opt-in backup/validation path.
- From M2 onward, version modern saved data and add small migrations between **modern** schemas. Reusing the mod ID, an NBT field name, or schema number does not make legacy data compatible. Persistence ownership, dimensions, validation, and once-only rewards must be tested in their relevant phases.
- Keep state authoritative on the server; clients receive only needed view data. Changes mark data dirty only when needed. Do not force-load chunks for cleanup, add unbounded global scans, or create uncapped spawning. Concrete intervals/caps belong to the feature that uses them, not a speculative M1 framework.

## Verification And Handoff

M1 cannot be declared verified solely because dependency metadata exists or a legacy build passes. Its exit gates require the pinned wrapper/JDK output, successful clean build, correct normal jar metadata, client Mods list/new-world entry, dedicated-server readiness/save/clean stop, and honest local/CI status. A blocked launch or CI run remains explicitly outstanding. Run commands and result expectations are in [Building](building.md); persistence/reload tests expand as M2 onward adds state.

This step changes documentation only. It installs no dependencies, changes no pins or gameplay, creates no work branch, and runs no modern build/game. Step 0.5.1 will review the consolidated M0 decisions and their published commits; it need not manufacture another commit if the record is already complete. Step 0.5.2 then checks preservation/local files and prepares the scaffold branch before M1.

**Next: Phase M0, step 0.5.1 - review the consolidated M0 decisions and confirm their commit/push status.**
