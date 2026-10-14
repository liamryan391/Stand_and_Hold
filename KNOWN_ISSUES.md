# Known Issues

These notes describe the modern NeoForge port during Phase M1 preparation. The retained `0.1.0-alpha.1` value comes from the legacy alpha; it does not identify a verified modern release.

## Current Port Status

- The source, wrapper, helper, and CI transition is still pending. The current files use Forge 1.12.2, Gradle 4.10.3, and Java 8; follow the [build guide's checkout-state instructions](docs/modern-port/building.md) before running anything.
- The selected NeoForge MDK layout and Java 25 build have not been installed or verified yet. Modern jar metadata, client launch, and dedicated-server readiness checks remain M1 / 1.4 tasks.
- The existing GitHub build workflow still selects Java 8. Its output is legacy build evidence; modern CI configuration belongs to 1.2.3.
- The [selected NeoForge loader](docs/modern-port/toolchain.md) is a beta build. M0 accepted that choice subject to actual build and launch verification.

## Foundation Limits

- Modern progression, research, buildings, NPCs, equipment, structures, and events will be rebuilt in their roadmap phases. Their presence in legacy source/docs is not modern feature completion.
- Legacy worlds, saved NBT, and configs have no supported conversion path in the foundation. Use separate modern test worlds and retain valuable legacy saves outside the checkout.
- External threat mods remain optional. Legacy SRP mapping/test results do not prove support on the modern Minecraft/NeoForge target. Exact artifacts and IDs need later verification.
- The modern scaffold starts with the platform/build tooling only; animation, AI libraries, art exports, and optional integrations remain later work.

These are preparation limits and verification gaps, not reports of observed modern runtime crashes.

## Legacy Forge 1.12.2 Issues

Use the [preserved legacy known issues](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/KNOWN_ISSUES.md) for Java 8/ForgeGradle errors, historical Actions queue notes, sample/research/mission behavior, NPC art, and save cleanup tests.

The [legacy README](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/README.md) links to the matching installation and gameplay guidance. Local legacy guides remain during staging and are scheduled for retirement from the modern checkout during the file transition; use their preserved branch copies for legacy testing.

## Verification Records

- [Current roadmap and task states](docs/modern-port/phase-roadmap.md)
- [Progress and publication evidence](docs/modern-port/progress-log.md)
- [Legacy transition inventory](docs/modern-port/legacy-inventory.md)
- [Modern build and launch instructions](docs/modern-port/building.md)
