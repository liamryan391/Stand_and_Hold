# Library And Threat Mod Compatibility

Phase M0, step 0.3.2. Checked on 2026-10-07.

Status: **complete for published-release and dependency-metadata verification**. This is not a combined client/server playtest. Stand and Hold still contains the legacy implementation; no dependency was installed or added to its build.

This records the 0.3.2 comparison. Step 0.3.3 subsequently selected the [exact 26.3 toolchain](toolchain.md); the recommendations below explain that decision's ecosystem evidence.

## Result And Recommendation

Both **Minecraft 26.3 and 26.2 on NeoForge** have matching published animation libraries, an AI library, and useful external threat mods.

Recommend carrying **26.3** forward as the preferred candidate for step 0.3.3, consistent with the project's latest-release direction, with **26.2** as the nearest non-beta-loader fallback. The beta NeoForge risk recorded in [Release Verification](release-verification.md) still applies. Exact versions are not pinned by this recommendation.

Use **Undead Nights** as the first optional external threat test and **Contagion** as a later infection/coexistence test. Neither reproduces SRP's parasite evolution. If immediate compatibility with **Fungal Infection: Spore** becomes the deciding requirement, **1.21.1 NeoForge** is the verified candidate among the versions checked here.

## Method And Limits

- Read the authors' Modrinth project and version records, relevant project documentation, and selected published jar metadata.
- Required an individual release to list both the exact Minecraft version and `neoforge`. Project-wide loader/version labels alone were not treated as evidence.
- Selected the newest matching published file for each candidate; every positive entry below was marked `release` by its author.
- Queried five Minecraft candidates: 26.3, 26.2, 26.1.2, 1.21.11, and 1.21.1.
- Cross-checked eight small jars in memory against their published SHA-512 hashes and read only `META-INF/neoforge.mods.toml`. No assets/code were extracted into the repository and no jar was executed.
- A missing result means no matching release found in the named sources on this date, not a claim that no such mod exists anywhere. Binary compatibility, performance, interactions, and registry mappings still need runtime tests.

Reproducible API pattern: `GET https://api.modrinth.com/v2/project/<slug>/version`, filtering each record's `game_versions` and `loaders`. For an exact artifact use `GET https://api.modrinth.com/v2/version/<id>`; the IDs are in the linked release URLs below. API dependency lists can omit built-in loader requirements or contain errors, so inspect the actual manifest before installation.

## Primary Candidates

Every cell links to the author's exact release, not a generic project search.

| Component | Role | NeoForge / Minecraft 26.3 | NeoForge / Minecraft 26.2 |
| --- | --- | --- | --- |
| GeckoLib | Animation library candidate | [5.5.7](https://modrinth.com/mod/geckolib/version/2s1O42FX) | [5.5.6](https://modrinth.com/mod/geckolib/version/IEGPh4CJ) |
| AzureLib | Alternative animation library | [4.0.8](https://modrinth.com/mod/azurelib/version/wl4AQ7Fi) | [4.0.0](https://modrinth.com/mod/azurelib/version/5Put70yw) |
| SmartBrainLib | Optional AI helper candidate | [2.0.3](https://modrinth.com/mod/smartbrainlib/version/7Lf18LcC) | [2.0.3](https://modrinth.com/mod/smartbrainlib/version/sBdm4rDL) |
| Undead Nights | First external horde/defence test | [2.2.6](https://modrinth.com/mod/undead-nights/version/hXnSFPmW) | [2.2.5](https://modrinth.com/mod/undead-nights/version/dUVxg7wz) |
| Contagion | Optional infection coexistence test | [2.1.1](https://modrinth.com/mod/contagion/version/4tdqhokG) | [2.1.1](https://modrinth.com/mod/contagion/version/xqQr0gg8) |

Exact filenames for the primary test combinations:

| Component | Minecraft 26.3 file | Minecraft 26.2 file |
| --- | --- | --- |
| GeckoLib | `geckolib-neoforge-26.3-5.5.7.jar` | `geckolib-neoforge-26.2-5.5.6.jar` |
| AzureLib | `azurelib-neo-26.3-4.0.8.jar` | `azurelib-neo-26.2-4.0.0.jar` |
| SmartBrainLib | `smartbrainlib-neoforge-26.3-2.0.3.jar` | `smartbrainlib-neoforge-26.2-2.0.3.jar` |
| Undead Nights | `UndeadNights-2.2.6-NeoForge-mc26.3.jar` | `UndeadNights-2.2.5-NeoForge-mc26.2.jar` |
| Contagion | `Contagion-2.1.1-NeoForge-mc26.3.jar` | `Contagion-2.1.1-NeoForge-mc26.2.jar` |

The same library version number on different Minecraft lines does not make its jars interchangeable. Use the exact Minecraft/loader artifact. The records for these ten releases list no extra required mod dependency; actual loader requirements are checked separately below.

## Manifest Cross-Checks

These are declared ranges read from the published jars, not promises of runtime compatibility. Broad ranges must not be used to infer support for additional Minecraft versions.

| Inspected artifact | NeoForge range declared | Minecraft range declared |
| --- | --- | --- |
| GeckoLib 5.5.7 for 26.3 | `[26.3.0.7-beta,)` | `[26.3,)` |
| AzureLib 4.0.8 for 26.3 | `[21,)` | `[26.3, 26.4)` |
| SmartBrainLib 2.0.3 for 26.3 | `[26.3.0.0-beta,)` | `[26.3,)` |
| Undead Nights 2.2.6 for 26.3 | `[26.2.0.7-beta,)` | `[26.3, 27.1)` |
| Contagion 2.1.1 for 26.3 | `[26.3.0.16-beta,)` | `[26.3, 27.1)` |
| Undead Nights 2.2.5 for 26.2 | `[26.2.0.1-beta,)` | `[26.2, 27.1)` |
| Contagion 2.1.1 for 26.2 | `[26.2.0.6-beta,)` | `[26.2, 27.1)` |

The candidate NeoForge builds from step 0.3.1, 26.3.0.52-beta/26.3.0.57-beta and 26.2.0.88, meet these declared lower bounds on their respective Minecraft lines. All listed requirements apply to both sides. The eighth inspected jar was The Hordes; its separate metadata issue is recorded below. The three 26.2 library entries were verified from release records, not inspected jar manifests in this pass.

## Other Verified Version Options

| Component | NeoForge / 26.1.2 | NeoForge / 1.21.11 | NeoForge / 1.21.1 |
| --- | --- | --- | --- |
| GeckoLib | [5.5.2](https://modrinth.com/mod/geckolib/version/xfVfPcoC) | [5.4.5](https://modrinth.com/mod/geckolib/version/FpONDAt3) | [4.9.3](https://modrinth.com/mod/geckolib/version/Grwn5rUB) |
| AzureLib | No match found | No match found | [3.1.17](https://modrinth.com/mod/azurelib/version/InpTe6Sy) |
| SmartBrainLib | [2.0.3](https://modrinth.com/mod/smartbrainlib/version/qcqMdYOK) | [1.16.11](https://modrinth.com/mod/smartbrainlib/version/5wOE1uXk) | [1.16.11](https://modrinth.com/mod/smartbrainlib/version/O5EpeqI3) |
| Undead Nights | [2.2.5](https://modrinth.com/mod/undead-nights/version/a0Lh4LC0) | [2.1.3](https://modrinth.com/mod/undead-nights/version/dzN1PTfL) | [2.3.1](https://modrinth.com/mod/undead-nights/version/3HitqfUv) |
| Contagion | [2.1.1](https://modrinth.com/mod/contagion/version/7tDxP9A5) | [1.5.7](https://modrinth.com/mod/contagion/version/A4KeuqOW) | [2.1.1](https://modrinth.com/mod/contagion/version/qFSWDBoO) |
| Fungal Infection: Spore | No match found | No match found | [2.2.0j](https://modrinth.com/mod/fungal-infectionspore/version/DwE3w8IX) |

Spore's verified file is `spore_1.21.1_2.2.0j_neo.jar`. The author's [CurseForge listing](https://www.curseforge.com/minecraft/mc-mods/fungal-infection-spore) independently identifies it as a 1.21.1 NeoForge release. Its roughly 119 MB jar exceeded this pass's 6 MB metadata-inspection limit and was not downloaded; dependency/runtime verification remains outstanding.

Additional candidates reviewed:

- [The Hordes 1.21.1-1.6.3f](https://modrinth.com/mod/the-hordes/version/gdHByusK) is published for NeoForge 1.21.1. Its Modrinth record incorrectly points its required Atlas Lib dependency at [a Forge 1.12.2 artifact](https://modrinth.com/mod/atlas-lib/version/UekJfJ7l). The inspected jar instead declares `atlaslib` version `1.21.0-1.1.14`; a [matching NeoForge Atlas Lib release](https://modrinth.com/mod/atlas-lib/version/bpOrAZJe) exists. Treat this as an installer-metadata caveat; do not use the 1.12.2 dependency or claim a tested combination. No 26.x/1.21.11 match was found.
- [Sculk Horde](https://modrinth.com/mod/sculk-horde/versions) and [From Another World](https://modrinth.com/mod/from-another-world/versions) had no exact NeoForge match for the five candidate Minecraft versions in their checked Modrinth records.
- [The Flesh That Hates](https://www.curseforge.com/minecraft/mc-mods/the-flesh-that-hates) lists Forge builds through 1.20.1; [Scape and Run: Parasites](https://www.curseforge.com/minecraft/mc-mods/scape-and-run-parasites) remains a Forge 1.12.2 option. Neither is evidence of NeoForge compatibility.
- [Creeper Parasites 1.0+mod](https://modrinth.com/mod/creeper-parasites/version/F91y2NF9) lists NeoForge 26.2/26.1.2/1.21.11. Its small, multi-loader listing was not investigated beyond metadata, so it is not the primary external test recommendation.

## Dependency Policy For Stand And Hold

| Category | Recommendation | Earliest evaluation |
| --- | --- | --- |
| Scaffold requirements | NeoForge and its selected build toolchain only | M1 |
| First human AI | Start with bounded vanilla goals; evaluate SmartBrainLib if behavior/memory complexity warrants it | M6 |
| Animation | Prefer a small GeckoLib prototype when animation is needed; keep AzureLib as an alternative, not a second animation stack | M8, or earlier if M6 rendering requires it |
| External threat mods | Optional test-instance mods and config/tag adapters, never unconditional Stand and Hold dependencies | Coexistence smoke test after M1; gameplay checks as M5/M6/M10 become available |

The animation preference is an engineering recommendation: GeckoLib has verified artifacts across every candidate and an [official documentation path](https://wiki.geckolib.com/docs/geckolib5/) for setup and models. [SmartBrainLib's author](https://modrinth.com/mod/smartbrainlib) positions it around more complex Brain behavior; the first soldier does not need that complexity. Library availability is not a reason to add it to the empty scaffold.

GeckoLib and AzureLib project metadata list MIT; SmartBrainLib lists MPL-2.0. Keep original Stand and Hold models/textures and review actual dependency licences when packaging. Threat-mod assets are not a reusable art library. Contagion's project page and jar licence field differ, so no asset/code reuse assumption should be made from its project badge.

## Future Runtime Verification

1. Build and launch Stand and Hold alone on the chosen exact NeoForge version; check client and dedicated server separately.
2. In a separate test instance, add only the matching Undead Nights release and its actual declared dependencies. Confirm both launches before using gameplay hooks.
3. After M5/M6, verify actual entity registry IDs, configure targeting/rewards, observe defender combat, and test save/reload. Do not invent IDs from the mod title or assume all its mobs are vanilla zombies.
4. Add Contagion as a separate coexistence test. Preserve friendly-player targeting rules even when players can be infected; no special infection mechanic integration is promised.
5. When both projects' raids are available, test spawn caps, cooldowns, and combined event pressure. Begin with one raid scheduler enabled to isolate failures.
6. Test removal of the optional mod in a backed-up test world, unknown mappings, and server performance. If choosing 1.21.1 for Spore, repeat the same checks with its exact release and manifest dependencies.

None of these runtime checks has been run in this step. Stand and Hold's modern APIs and content must exist first.

**Current next step: Phase M0, step 0.3.4 - document Windows build commands, IDE Gradle JVM setup, and the fallback decision rule.** Pins and local JDK selection are recorded in [Selected Modern Toolchain](toolchain.md).
