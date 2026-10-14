# Known Issues

These notes describe the modern NeoForge foundation. The retained `0.1.0-alpha.1` value comes from the legacy alpha; it does not announce a modern release. Live build/launch results are in [STATUS.md](docs/modern-port/STATUS.md).

## Build And Test Limits

- Use Java 25 and Gradle 9.2.1 from the modern wrapper, not the legacy Java 8 helper. See [Building](docs/modern-port/building.md).
- The normal jar retains the legacy filename; inspect `META-INF/neoforge.mods.toml` to avoid confusing incompatible artifacts.
- Build, client, server, and remote CI evidence are separate gates. A server EULA stop is not a passed launch; Liam must accept it himself.
- The [selected NeoForge loader](docs/modern-port/toolchain.md) is a beta build. M0 accepted that choice subject to actual build and launch verification.
- This Windows development launch logged OSHI/Perflib performance-counter warnings and system-report exceptions, then continued through mod initialization and graphics/audio resource loading. Recheck on the installed client/server; these messages alone are not a Stand and Hold crash. No Windows registry changes are made by the project.

## Foundation Limits

- Human progression, supplies, research, and missions have command/persistence foundations only. Physical point sources, buildings, samples, NPCs, equipment, structures, and events remain planned. Their presence in legacy source/docs is not modern feature completion.
- Physical mission objectives have definitions/debug progress only. Supply/research/stage objectives are checked by explicit commands, not continuously. Nonzero research sample costs block safely until M5; no sample items or virtual sample currency are added. See [Foundation Data](docs/modern-port/foundation-data.md) for commands and limits.
- Schema 2 preserves unknown research/mission IDs and receipts. Malformed or missing current-schema receipt maps refuse access instead of silently resetting completion; keep backups, since lost/corrupt reward history cannot safely be reconstructed.
- Progression schema hooks normalize missing/malformed fields and preserve unknown fields, but cannot recover a physically corrupt save file. Newer schemas refuse progression access; restore a compatible mod/backup. See [save contract and tests](docs/modern-port/progression.md).
- Legacy worlds, saved NBT, and configs have no supported conversion path in the foundation. Use separate modern test worlds and retain valuable legacy saves outside the checkout.
- External threat mods remain optional. Legacy SRP mapping/test results do not prove support on the modern Minecraft/NeoForge target. Exact artifacts and IDs need later verification.
- The modern scaffold starts with the platform/build tooling only; animation, AI libraries, art exports, and optional integrations remain later work.

These are foundation limits, not reports of observed modern runtime crashes.

## Legacy Forge 1.12.2 Issues

Use the [preserved legacy known issues](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/KNOWN_ISSUES.md) for Java 8/ForgeGradle errors, historical Actions queue notes, sample/research/mission behavior, NPC art, and save cleanup tests.

The [legacy README](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/README.md) links to the matching installation and gameplay guidance. Legacy guides have been retired from the modern checkout; use their preserved branch copies for legacy testing.

## Verification Records

- [Current roadmap and task states](docs/modern-port/phase-roadmap.md)
- [Progress and publication evidence](docs/modern-port/progress-log.md)
- [Legacy transition inventory](docs/modern-port/legacy-inventory.md)
- [Modern build and launch instructions](docs/modern-port/building.md)
