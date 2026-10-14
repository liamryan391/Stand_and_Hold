# Stand and Hold

Stand and Hold is being rebuilt for modern Minecraft as a human military resistance mod for parasite, infection, and other hostile-world scenarios. The design centres on humanity responding to escalating threats through research, supplies, bases, and increasingly capable defenders.

## Development Status

See [STATUS.md](docs/modern-port/STATUS.md) for live port state and the active milestone. Contributor workflow is defined in [AGENTS.md](AGENTS.md).

**A modern NeoForge build is not ready to install yet.** The source, Gradle wrapper, scripts, and CI still contain the Forge 1.12.2 implementation until the upcoming file/build transition. Legacy alpha features and test results do not establish modern feature availability or build/launch success.

| Branch | Purpose |
| --- | --- |
| `1.12` | Preserved Forge 1.12.2 alpha and its installation/gameplay documentation |
| `codex/modern-neoforge-scaffold` | Modern port work and preparation; current checkout for M1 |
| `main` | M0 handoff baseline, to receive the verified modern scaffold after M1 |

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

These are the M0-selected pins for the upcoming scaffold, not the current legacy wrapper configuration:

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

Follow the [Windows build and IDE guide](docs/modern-port/building.md). It distinguishes the current Java 8 legacy build from the future Java 25 build and guards modern commands until the new wrapper/layout exists. Build and launch proof is recorded in M1 after that transition.

M1 delivers the build foundation, metadata, entry point, logger/config shell, and verification. It adds no gameplay systems or extra runtime libraries. The [foundation policy](docs/modern-port/foundation-scope.md) and [numbered roadmap](docs/modern-port/phase-roadmap.md) define the boundaries.

Later phases rebuild the playable loop: recover threat samples, establish a command post and lab, complete research, improve supplies and human progression, deploy defenders, and respond to an outpost attack. Original models, richer combat, larger bases, vehicles/aircraft, and Fabric evaluation follow their own phase gates. These are planned systems, not features of an available modern release.

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
