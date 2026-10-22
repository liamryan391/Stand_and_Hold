# Optional Threat Compatibility

Artifact metadata and isolated runtime checks on 2026-10-22 target Minecraft 26.3, NeoForge 26.3.0.57-beta and Java 25. **Contagion 2.1.1 fails initialization with this pinned loader on both client and server. Do not install this combination for play.** The dated [M0 ecosystem review](ecosystem-compatibility.md) establishes only historical metadata, not runtime support.

## Boundary

- Stand and Hold must launch and retain its core progression, missions, samples and defence without any external threat mod.
- Integrations use verified registry IDs/tags and configuration, not imports of another mod's classes, copied code/assets or a required threat-mod dependency.
- `minecraft:zombie` remains the safe default test mapping. An infection-themed mod title does not establish new entity IDs, and infected players must not become NPC targets.
- Keep approved external test mods in separate disposable client/server instances, outside normal development runs, build dependencies and the release jar. Never install a legacy SRP jar on the modern loader.

## Exact Artifact Candidates

Author-controlled release records were filtered for both `26.3` and `neoforge`. During the initial metadata review, both artifacts were inspected in memory and matched their release SHA-512, without installation. Liam subsequently approved the exact Contagion artifact for isolated QA only; Undead Nights was not installed or executed.

| Candidate | Exact release/file | Declared required mods | Physical sides | Licence evidence |
| --- | --- | --- | --- | --- |
| Contagion | [2.1.1-NeoForge-mc26.3](https://modrinth.com/mod/contagion/version/4tdqhokG), `Contagion-2.1.1-NeoForge-mc26.3.jar` (141,723 bytes; published 2026-09-24) | `neoforge [26.3.0.16-beta,)`, `minecraft [26.3, 27.1)` | Client and server | Project metadata: MIT; this jar's `neoforge.mods.toml`: All Rights Reserved. Treat redistribution/reuse permission as unresolved; do not copy or bundle. |
| Undead Nights | [2.2.6-NeoForge-mc26.3](https://modrinth.com/mod/undead-nights/version/hXnSFPmW), `UndeadNights-2.2.6-NeoForge-mc26.3.jar` (402,078 bytes; published 2026-09-23) | `neoforge [26.2.0.7-beta,)`, `minecraft [26.3, 27.1)` | Client and server | Project and jar metadata: All Rights Reserved; do not copy or bundle. |

Both records are author-labelled releases, list no additional Modrinth dependencies, and their inspected manifests declare only Minecraft/NeoForge as required. The pinned platform satisfies those declared ranges; that is not binary/runtime proof. The first inspected class in each jar has major version 69 (Java 25); this is not an audit of every class or mixin.

Exact SHA-512 values:

```text
Contagion-2.1.1-NeoForge-mc26.3.jar
b0c7f75437cbd960e9d15eecee7e0297f3ea6dac48d52e129b17ce8282f29b9f165d0584858aea68f9df15c7864a0085113fa009dfff43fc2ac066edef666bd1

UndeadNights-2.2.6-NeoForge-mc26.3.jar
9ac855ebfc5558f5ffc1b3f5780951fc901cc1f27232ee792304d37f11f3a24102e6481f4e7b861c13b8e66c6f2bee2e83f2f69156d65dd49e27975b35393b8a
```

Primary metadata: [Contagion project](https://api.modrinth.com/v2/project/contagion), [exact release](https://api.modrinth.com/v2/version/4tdqhokG), [author source repository](https://github.com/MC-Mods-Pete/Contagion); [Undead Nights project](https://api.modrinth.com/v2/project/undead-nights), [exact release](https://api.modrinth.com/v2/version/hXnSFPmW), [author source repository](https://github.com/MC-Mods-Pete/UndeadNights).

## Proposed Test Admission

| Decision field | Proposal |
| --- | --- |
| Need | M11 optional infection coexistence and mapping/removal tests; platform-only tests cannot demonstrate coexistence with another mod. |
| Selection | Contagion only, at the exact release/hash above. Its [author description](https://modrinth.com/mod/contagion) identifies zombie-driven infection; Undead Nights is a horde scheduler, not sufficient infection-mod evidence by itself. |
| Runtime contract | Liam approved this exact Contagion artifact on 2026-10-22 for optional QA-instance content on both sides. No required Stand and Hold dependency, extra library or default-run modification. No other external runtime mod is approved by that decision. |
| Verification | Baseline without it passed; both launches with it failed during Contagion construction. Recovery after moving it out of `mods` passed on the backed-up server. External-mod gameplay and a successful modded-world removal test are not established. |
| Maintenance/removal | No infection-effect API, immunity or infected-player targeting promised. Test one external mod at a time, preserve exact mod lists/configs and keep a full disposable-world backup before removal. An external mod's saved content is not guaranteed to survive its removal. |
| Alternative | Keep vanilla-only QA and explicitly leave the external-mod exit gate unmet. No fallback/pin change is proposed. |

Use the [dependency policy](foundation-scope.md) for admission and [STATUS.md](STATUS.md) for the live decision. Do not infer verified registry mappings or activate an external preset from this artifact table alone.

## Opt-In Presets And Diagnostics

`config/standandhold-threats.toml` keeps the original manual zombie test rule and no presets by default. To test the generic tag instead, in a disposable instance:

```toml
requirePlayerKill = true
mappings = []
presets = ["vanilla_undead"]
```

| Preset | Shipped selector | Boundary |
| --- | --- | --- |
| `vanilla_undead` | `#standandhold:vanilla_undead` | Original tag containing `minecraft:zombie`, `minecraft:husk`, `minecraft:drowned`, `minecraft:zombie_villager`; IDs checked against the pinned Minecraft `EntityTypeIds`/`EntityTypes` sources. No external mod needed. |
| `contagion_2_1_1` | `#standandhold:test_threats` | **Reproduction candidate only, not supported play.** Requires loaded `contagion` with exact version `2.1.1-NeoForge-mc26.3`. The shipped tag contains only `minecraft:zombie`; this jar fails before the game can use the rule on our pins. |

Both presets use the existing test reward (5 points, 35% chance of one sample); neither changes defaults, adds an infection mechanic, requires a dependency or identifies infected players. The exact Contagion jar's zombie mixin annotation targets vanilla `Zombie`; its loot metadata also references the vanilla zombie loot table. Registry IDs were cross-checked with the pinned game, not invented from the mod title. No external implementation or asset was copied.

Manual mappings precede preset mappings. Exact IDs beat tags; otherwise the first matching tag wins, with no stacking. Duplicate selectors keep the first entry. At most 16 preset selections and 256 combined entries are processed; full manual lists leave no room for presets. Unknown names, absent mods and untested versions are skipped with config-load warnings. Mod-list changes require a process restart. Custom datapacks can extend tags, but their extra members are not verified by these shipped presets.

All consumers continue to use the same resolved table: death rewards, sample drops, mapped threat tracking and NPC targets. Sample pickup drives the existing recovery mission; defence missions retain their reserved-raid rules. NPC player/human exclusions remain enforced even if a datapack adds them to a tag. There is no per-tick external-mod probe, registry scan or external class import.

`/standandhold compat status` is operator-only. It shows the installed Contagion version, active presets, resolved-rule count and at most eight issues (the complete issue list is in the config-load log). A resolved rule for an unknown ID/tag is not proof that such an entity exists. Empty **both** `mappings` and `presets` to disable mapping-driven rewards/targets; ordinary hostile targeting is independently controlled by `targetHostileMobs`.

## Runtime Results And Blocker

| Combination/check | Observed result |
| --- | --- |
| No external mods, default config, isolated dedicated server | Reached Done (1.279s); compat/status/points/supplies/research/save/stop succeeded; clean process exit (30s). |
| Approved Contagion jar, isolated client | Bootstrap failed before the main menu: `NoSuchFieldError` for `ModConfig.Type.SERVER`; user closed the error window and process exited with code 1. |
| Same approved jar, isolated dedicated server | Same failure at `net.petemc.contagion.Contagion.<init>(Contagion.java:60)` before Done; exit code 1 (8s). |
| Failed jar moved out of `mods`, optional preset plus missing/invalid entries left configured | Server reached Done (0.177s), skipped absent/unknown presets and malformed entries, retained 27 points/completed Initial Sample Analysis, refused repeat reward, saved/stopped/exited cleanly (15s). |
| No-Contagion client, generic preset and skipped optional candidate | Liam passed gameplay/targeting/mission/save-reload checks. Reviewed frames show active generic/skipped optional mapping, mission reward and human/zombie combat; client saved and fully exited normally (5m9s). This is baseline proof, not an external-mod pass. |
| Undead Nights or another external mod | No runtime test or support claim. |

The pinned FML 12.0.9 enum exposes `LOCAL`, `CLIENT`, `SYNCED`, `STARTUP`, not `SERVER` (verified with the installed loader's `javap` output). The crash is an external binary/API incompatibility despite declared version ranges. Mapping guards cannot make an incompatible mod constructor load; this is not fixable by changing a target ID or catching an exception in Stand and Hold. No external jar patch, copied source, compatibility shim or pin change was made.

Retained local evidence:

- `run/m11-compat-client/crash-reports/crash-2026-10-22_22.07.34-client.txt`
- `run/m11-compat-server/crash-reports/crash-2026-10-22_22.09.40-fml.txt`
- `.local/m11-qa/evidence/` copies of both failed-run logs/reports and `.local/m11-qa/server-before-contagion/` baseline-world backup.
- Hash-checked jars retained in `.local/m11-qa/artifacts/` and each QA run's `mods-disabled/`, not deleted or loaded by normal runs.

Recovery after an early bootstrap failure does **not** prove removal from a world that successfully ran Contagion. No external infection gameplay pass is claimed, and the M11 external-mod exit gate remains unmet.

Read-only replacement check on the same date: Modrinth search filtered to `project_type:mod`, `versions:26.3`, `categories:neoforge` returned only Contagion for `infection`, and no hits for `parasite` or `virus`. The [Spore version records](https://api.modrinth.com/v2/project/fungal-infectionspore/version) and [Creeper Parasites version records](https://api.modrinth.com/v2/project/creeper-parasites/version) had no exact 26.3/NeoForge match. The author's [Contagion 26.3 file list](https://www.curseforge.com/minecraft/mc-mods/contagion/files/all?page=1&pageSize=20&showAlphaFiles=hide&version=26.3) still lists NeoForge 2.1.1 and a separate Fabric 2.1.2; the Fabric file is not a NeoForge substitute. This is a limited catalog check, not proof that no suitable mod exists anywhere. No replacement runtime mod was installed or approved.

## Reproducible QA Commands

Use the pinned Java selector and isolated Gradle user home before each wrapper invocation, as described in [Building](building.md):

```powershell
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat clean build --no-daemon --stacktrace
.\gradlew.bat runCompatServer --no-daemon --stacktrace
.\gradlew.bat runCompatClient --no-daemon --stacktrace
```

Run the launches separately; server console must accept `stop`, and client must fully Quit Game before another clean build. These tasks use `run/m11-compat-server` and `run/m11-compat-client`; normal `runServer`/`runClient` directories are unchanged. The local dedicated fixture binds only `127.0.0.1:25576` with offline authentication for console QA; it is not a public-server configuration. Only Liam may accept a server EULA if one is requested.

Baseline/generic checks: fresh modern disposable world; compat/status; normal player kills (not `/kill`) of mapped zombies/husks; +5 points and occasional samples; carry 3 samples for the active recovery mission; mapping-only NPC targeting (`targetHostileMobs=false`); save/reload; no repeat mission reward. Then test missing IDs/tags and malformed selections without fallback entities or log spam. Preserve valuable data outside the checkout.

A successful external combination must still demonstrate client menu/new-world/reload, dedicated Done/stop, real reward/sample/mission/defender behavior and removal from a full backed-up disposable modded world. Metadata, headless checks and baseline recovery are not substitutes. SRP 1.12 and untested variants/other artifacts remain unsupported; no infection-effect, infected-player targeting, immunity, variant balance or third-party raid-scheduler integration is provided.
