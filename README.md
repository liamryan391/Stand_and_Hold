# Stand and Hold

Stand and Hold is being rebuilt for modern Minecraft as a human military resistance mod for parasite, infection, and other hostile-world scenarios. The design centres on humanity responding to escalating threats through research, supplies, bases, and increasingly capable defenders.

## Development Status

See [STATUS.md](docs/modern-port/STATUS.md) for live port state and the active milestone. Contributor workflow is defined in [AGENTS.md](AGENTS.md).

**This is a modern NeoForge foundation, not a complete port or public release.** Progression, research, supplies and missions have command foundations; core buildings add storage and menus, physical samples connect the first resource loop, and two human NPC tiers provide bounded melee defence. The remaining gameplay systems are not ported from `1.12`. Build/launch results are recorded in STATUS.md; legacy alpha results do not establish modern feature availability.

| Branch | Purpose |
| --- | --- |
| `1.12` | Preserved Forge 1.12.2 alpha and its installation/gameplay documentation |
| `codex/modern-neoforge-scaffold` | Verified M1 scaffold history |
| `codex/m<N>-<short-name>` | One work branch per modern phase, cut from `main` |
| `main` | Verified modern NeoForge foundation and merged phase work |

The historical [v0.1.0-alpha.1 tag](https://github.com/liamryan391/Stand_and_Hold/tree/v0.1.0-alpha.1) belongs to the Forge 1.12.2 line. The existing version value remains `0.1.0-alpha.1` during preparation; no modern release or retag is announced here.

## Project Identity

| Field | Value |
| --- | --- |
| Name | Stand and Hold |
| Mod ID / resource namespace | `standandhold` |
| Java package / group | `com.liamryan.standandhold` |
| Author | Liam Ryan |

The human-response theme, original project identity, and design lessons carry forward. The port uses new modern test worlds; legacy world/config conversion is outside the foundation scope.

## Selected Modern Toolchain

The modern scaffold uses these selected pins:

| Component | Selected version |
| --- | --- |
| Minecraft | `26.3` |
| NeoForge | `26.3.0.57-beta` |
| Java | `25`; local baseline Temurin `25.0.4+7`, 64-bit |
| Gradle wrapper | `9.2.1` |
| ModDevGradle | `2.0.148` |
| Foojay toolchain resolver | `1.0.0` |

[Selected Modern Toolchain](docs/modern-port/toolchain.md) records the official sources, immutable MDK commit, checksum, and accepted beta-loader risk. Versions change through a reviewed decision, not a moving latest-version download.

## Development And Planned Scope

Follow the [Windows build and IDE guide](docs/modern-port/building.md) for Java 25 selection, wrapper commands, jar inspection, and separate client/server smoke tests. Java 8 build instructions apply only to the preserved `1.12` branch.

M1 delivers the build foundation, metadata, entry point, logger/config shell, and verification. It adds no gameplay systems or extra runtime libraries. The [foundation policy](docs/modern-port/foundation-scope.md) and [numbered roadmap](docs/modern-port/phase-roadmap.md) define the boundaries.

The [human progression foundation](docs/modern-port/progression.md) adds seven configurable stages, versioned world-global saves, and `/standandhold status`, `/standandhold addpoints <amount>`, and `/standandhold setstage <0..6>`. Mutations require operator level 2; setting a stage resets points to its configured minimum. Use fresh modern worlds, not legacy saves.

The [research/supply/mission foundation](docs/modern-port/foundation-data.md) provides three JSON research definitions, global supply balances, seven small missions, persistent once-only rewards, and server-authoritative inspection/admin commands. For a minimal test: `/standandhold supplies add 10`, then `/standandhold research complete parasite_samples`; expect 10 human points and zero supplies, with no extra reward on repetition. Defence hooks and a mission GUI remain deferred.

The [core buildings guide](docs/modern-port/buildings.md) covers Field Command Posts, Research Labs, and Supply Crates: creative access, original utility textures, local/global supply transfers, loaded-only point/supply generation, timed lab work, upgrades, safe registration cleanup, menus, configuration, and the manual test recipe.

The [structure foundations](docs/modern-port/structures.md) add a Small Army Checkpoint and distinct Main Base layout with conservative one-chunk terrain preflight, sparse configurable natural generation, admin placement/inspection commands, loaded-only cleanup and persistent discovery. Main Base command generation/deployment activates at Stage 5 by default; labs/crates remain usable while dormant. They are original small layouts, not complete faction bases.

[Samples and optional threat rewards](docs/modern-port/samples-and-threats.md) add the physical tissue item, configurable entity/tag mappings, server-owned kill/drop rewards, real sample storage/consumption and recovery mission hooks. The safe default test zombie gives 5 points and a 35% chance of one sample on a player-attributed kill. No external threat mod is required, and no external entity mappings are enabled by default.

The [human NPC foundation](docs/modern-port/human-npcs.md) adds Survivor Defenders and Army Riflemen with original uniforms/test eggs, configurable stats/stage gates, simple AI targeting hostile mobs plus optional threat mappings, and capped command-post deployment backed by saved quotas. Test eggs/summons bypass automatic stage gates. Survivors remain melee; Riflemen can use the unlocked equipment prototype with melee fallback.

[Combat equipment](docs/modern-port/equipment.md) adds an Anti-Threat Blade, Army/Elite armour sets and one server-owned cooldown-based Prototype Launcher. Ordinary recipes and original item/worn textures support creative and survival tests; normal equip/use is gated by world stage/research. Bolts damage hostile mobs or mapped threats, protect players/human NPCs, expire in bounded loaded-only flight and reuse existing kill attribution. Points/samples still require configured reward mappings. Ammo, reloads, advanced firearms and further armour tiers remain deferred.

The [art and identity guide](docs/modern-port/art-pipeline.md) documents original cap/pack and helmet/vest models, editable Blockbench sources, deterministic source-to-runtime exports, distinct utility-block faces and compact menu readability. These are alpha visuals using vanilla animation, with no external art or animation library bundled.

The partial modern loop can recover samples, establish a command post/lab, fund research/progression and obtain defenders. Outpost attacks, richer combat, larger bases, vehicles/aircraft and Fabric evaluation follow later phase gates; they are not features of an available modern release.

Compatibility with external threat mods remains optional. Registry/tag mappings and integrations must be verified for the exact modern target when implemented; legacy SRP compatibility does not imply modern SRP support.

## Project Notes

Progress and decisions are tracked in this repository:

- [Modern port index](docs/modern-port/README.md)
- [Phase roadmap and task checkboxes](docs/modern-port/phase-roadmap.md)
- [Status](docs/modern-port/STATUS.md)
- [Progress log and publication checkpoints](docs/modern-port/progress-log.md)
- [Technical plan](docs/modern-port/technical-port-plan.md)
- [Legacy transition inventory and exact file manifest](docs/modern-port/legacy-inventory.md)
- [Current port limitations](KNOWN_ISSUES.md)
- [Changelog with labelled legacy history](CHANGELOG.md)

## Legacy Forge 1.12.2 Documentation

Use the preserved `1.12` branch for the existing alpha, its Java 8 build, and its gameplay commands. These guides describe that legacy implementation; they are not modern-port installation or feature instructions.

| Reference | Preserved documentation |
| --- | --- |
| Installation, commands, config, progression, research, and buildings | [Legacy README](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/README.md) |
| Java 8 / ForgeGradle build | [Legacy build guide](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/building.md) |
| First playable alpha loop | [Legacy loop](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/first-playable-loop.md) |
| Mission chain | [Legacy missions](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/missions.md) |
| Checkpoints and Main Bases | [Legacy structures](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/structures.md) |
| Optional SRP mappings | [Legacy SRP guide](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/srp-compatibility.md) |
| SRP test cases | [Legacy SRP checklist](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/srp-test-checklist.md) |
| Alpha test cases | [Legacy testing checklist](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/alpha-testing-checklist.md) |
| QA result template | [Legacy QA template](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/alpha-qa-results-template.md) |
| Phase 31 QA session | [Legacy session 001](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/qa-sessions/phase-31-alpha-qa-session-001.md) |
| Release process | [Legacy release checklist](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/release-checklist.md) |
| Known issues | [Legacy issue notes](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/KNOWN_ISSUES.md) |
| Alpha changelog | [Legacy changelog](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/CHANGELOG.md) |
