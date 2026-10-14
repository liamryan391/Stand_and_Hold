# Legacy Transition Inventory

Phase M1, step 1.1.1. Inspected 2026-10-14 on `codex/modern-neoforge-scaffold` at **`6df4c3fea5a6fa31f22589339a4c978b4e6e6d89`**. This step records the files and transition plan; no legacy implementation is replaced here.

## Preservation And Baseline

- GitHub fetch/live refs confirmed the work branch and `main` both at the baseline above, with a clean working tree and the work branch's own upstream.
- Local/remote `1.12` match **`62bb07b84eb614b5967ef318ccb98aef1f22a5d8`**. That commit is an ancestor of the work branch. Local/remote alpha-tag refs match `28fd40a81a50ea2f2382489e60d9015b4725cc92`.
- `git diff 1.12 HEAD` is empty for Java/resources, build/settings/properties/wrapper, both workflows, and the Java 8 helper. Legacy design/code/art remain available on the [preserved branch](https://github.com/liamryan391/Stand_and_Hold/tree/1.12).
- Only ignored `.local/modern-jdk.json` is present outside tracked files; no non-ignored untracked files exist. Its content hash is retained for post-publication verification. No runtime/world, output/cache, or external-jar directories exist in this checkout. Recheck local additions before the actual replacement.

## Exact Manifest

[legacy-file-inventory.tsv](legacy-file-inventory.tsv) records **all 199 tracked files at the baseline**, once each, including their Git blob ID, tracked mode, category, proposed disposition, and first owning step. It includes the pre-existing planning files so later cleanup has an explicit preserve set. This inventory document and its manifest are subsequent additions, not baseline files.

The manifest is a dated snapshot, not an executable removal script. Paths/blob IDs must be compared with the current tree before a transition; retain any new planning or user changes. The scope approved in [Foundation Scope](foundation-scope.md) governs implementation, not a blanket directory deletion.

| Baseline group | Files | Handling |
| --- | ---: | --- |
| Java implementation | 73 | Replace as M1 compile inputs; use legacy source as reference when the owning gameplay phase arrives |
| Runtime resources | 85 | Replace the legacy runtime resource tree; retain original assets on `1.12` for later schema/art review |
| Root/build/repository files | 12 | Seven MDK build/wrapper replacements; retain/adapt ignore rules, attributes, identity/docs/history |
| Modern planning documents | 15 | Retain and maintain |
| Legacy docs under `docs/` | 10 | Move current-reader references to the preserved branch in 1.1.2; retire obsolete modern-checkout guides during transition |
| PowerShell scripts | 2 | Retire the legacy-only helper from the modern path; retain/adapt the Java 25 selector |
| GitHub workflows | 2 | Replace the Java 8 build job in 1.2.3; keep the version-independent Actions probe as a repository diagnostic |

Disposition tokens: `replace-m1` (legacy Java/resources), `replace-mdk` (build/wrapper), `archive-docs` (legacy guides), `legacy-helper`, `replace-ci`, `rewrite-docs`, `retain-history`, `retain-adapt`, and `retain`. They describe later work only. The legacy changelog describes the artwork as original alpha placeholders; reuse still needs the M8 provenance/format review and is not M1 runtime scope. Source links below point to the immutable inventory baseline so this record remains readable after replacement.

## Build And Identity

| Files | Observed legacy state | Transition owner |
| --- | --- | --- |
| [build.gradle](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/build.gradle) | `com.anatawa12.forge:ForgeGradle:2.3-1.0.8`, Forge plugin, Java 8, Forge Maven/Maven Central/JCenter, old resource expansion, wrapper task fixed to 4.10.3 | Replace with MDK layout in 1.1.3; pin/configure in 1.2.1 |
| [settings.gradle](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/settings.gradle) | `rootProject.name = 'Stand_and_Hold'`; no modern plugin management/resolver | 1.1.3 / 1.2.1-1.2.2 |
| [gradle.properties](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/gradle.properties) | Minecraft 1.12.2, Forge 14.23.5.2847, stable mappings 39, mod version 0.1.0-alpha.1 | Replace legacy target properties; preserve identity and review modern metadata in 1.2 |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties` | Wrapper distribution 4.10.3; preserve tracked modes/line-ending conventions during replacement | Use the pinned official MDK wrapper/scripts/jar and checksum in 1.1.3 / 1.2.1 |
| `.gitignore`, `.gitattributes` | Local selection/world/build/cache/IDE ignores; LF shell wrapper, CRLF batch scripts, binary jars | Retain and reconcile MDK additions; keep `.local/` and existing local protection |

Identity to retain: `standandhold`, `Stand and Hold`, `com.liamryan.standandhold`, and author Liam Ryan. `StandAndHoldConstants.java` duplicates version `0.1.0-alpha.1` and also hardcodes legacy Minecraft/Forge requirements; retaining identity does not mean carrying those old requirements into modern metadata. No version value or release tag changes in this inventory step.

No project licence file is tracked at this baseline. Inspect and retain applicable official MDK licence/notices during 1.1.3; do not invent a project licence or copy external source/assets as part of this inventory.

## Java Implementation

All 73 files are under `src/main/java/com/liamryan/standandhold/`: two entry/constants files, seven client files, 63 common files, and one config file. There are no tracked `src/test/` files. Every Java path/blob is listed in the manifest.

The old `StandAndHold` entry point uses Forge FML lifecycle events and `@SidedProxy`; startup registers entities, tile entities, world generators, networking, GUI handlers, progression events, and commands. It is coupled to the full alpha. The clean M1 entry point/config shell must be recreated independently; simply changing imports or retaining old managers in the source set is not the approved scaffold transition.

| Legacy area / examples | Rebuild reference |
| --- | --- |
| `StandAndHold`, `StandAndHoldConstants`, `StandAndHoldConfig`, common/client proxies | Minimal logger/config/metadata in M1; Forge `@Config`/`ConfigManager` and lifecycle APIs are legacy |
| `HumanWorldData`, `HumanPointManager`, `HumanStage`, `CommandStandAndHold` | M2 progression foundation; current `WorldSavedData`/`MapStorage` and `CommandBase` are reference only |
| Research, mission, supplies, and logistics managers | Owning M3/M4 and later phase tasks; M1 has none of these services |
| Blocks/items, tiles, infrastructure managers, common containers/GUI handler, client screens | M4/M7; legacy `TileEntity`/`ITickable`, registration events, and GUI wiring leave the M1 runtime |
| Progression/sample event hooks and `SRPCompat` | M5/M11; keep optional identification design, verify modern IDs/APIs later |
| NPC/projectile entities, target AI, tier stats, rendering/model registration, equipment | M6/M8/M9; retain original tier/role design, not old entity/rendering classes |
| Threat/dynamic managers and checkpoint/Main Base `IWorldGenerator` implementations | M7/M10; bounded registration/generation lessons carry forward in their owning phases |
| `StandAndHoldNetwork` and three packet classes | Modern server-authoritative UI actions in M4; current `SimpleNetworkWrapper` implementation is legacy |

## Runtime Resources

| Resource family | Files | Existing format / next handling |
| --- | ---: | --- |
| `src/main/resources/mcmod.info`, `pack.mcmeta` | 2 | Expanded Forge JSON metadata plus pack format 3; replace with the selected MDK's metadata/resources and validate target formats in 1.2 |
| `assets/standandhold/lang/en_us.lang` | 1 | Legacy key/value translations; select useful identity strings and rebuild translations when modern content is added |
| `assets/standandhold/blockstates/` | 3 | Command post, lab, crate; future M4 resource definitions |
| `assets/standandhold/models/` | 21 | Legacy block/item models; revalidate modern schemas and registry names in M4/M8/M9 |
| `assets/standandhold/recipes/` | 28 | Legacy crafting/repair JSON and location; rebuild at the target's datapack recipe location/schema with the future items |
| `assets/standandhold/textures/` | 30 | Original alpha block/item/human/armour PNGs; preserve legacy art as an M8 reference and review before any reuse |

M1 contains no custom blocks, weapons, units, or recipes. Old resources are not evidence of modern registrations or features; do not package the old gameplay tree and advertise it as a port. No resource is removed or modified in this inventory step.

## Scripts, CI, And Documentation

- [build-java8.ps1](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/scripts/build-java8.ps1) discovers a JDK 8, sets process Java variables, stops old daemons, and runs the legacy clean build. Keep it available through the `1.12` branch; retire its modern-checkout entry after legacy build links are moved. Do not invoke it on the future Java 25 scaffold.
- [use-modern-java.ps1](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/scripts/use-modern-java.ps1) is an M0 addition to retain. It validates Java 25, uses ignored `.local/modern-jdk.json`, and changes the calling process only. Its final legacy-build guidance needs updating in 1.2.3 after the modern wrapper exists.
- [build.yml](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/.github/workflows/build.yml) runs Windows/Temurin 8 and the legacy wrapper, uploading `build/libs/*.jar`. Convert build JVM/tasks/artifact selection in 1.2.3; current workflow presence does not prove the future scaffold builds.
- [test-actions.yml](https://github.com/liamryan391/Stand_and_Hold/blob/6df4c3fea5a6fa31f22589339a4c978b4e6e6d89/.github/workflows/test-actions.yml) is an Ubuntu push/manual job that only prints an availability message. It can remain as a repository probe; it does not compile or test the mod.
- Rewrite the root README for the modern scaffold and point old installation/gameplay instructions at `1.12` in 1.1.2. Keep changelog history labelled as legacy; rewrite current known issues for the actual modern state. The ten old QA/build/mission/SRP/structure guides are historical references, not tests of features available in M1.
- Preserve every `docs/modern-port/` file, including new inventory/handoff records. This inventory supplies the next documentation/file transition; it does not perform that transition or change the existing CI.

## Result And Handoff

Preservation, baseline coverage, proposed dispositions, local-file protection, and document consistency are the verification gates for this step. No build or game launch was run because the runtime/build remains unchanged; modern build/launch verification is scheduled in M1 / 1.4.

Inventory checkpoint `4cac6b9` was published on `codex/modern-neoforge-scaffold`. Step 1.1.2 subsequently updated the root README/current port issues and labelled changelog history, preserving modern notes/identity and linking legacy guides on `1.12`. The exact manifest remains the original baseline snapshot; runtime/build replacement has not started. `main`, `1.12`, and the alpha tag retain their recorded refs.

**Next: Phase M1, step 1.1.3 - replace the inventoried legacy implementation/build files with the selected MDK layout, preserving modern notes, project identity, and local files.**
