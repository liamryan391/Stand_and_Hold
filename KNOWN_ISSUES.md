# Known Issues

Known issues for `0.1.0-alpha.1`.

## Build Toolchain

- Java 8 is required for building.
- Java 17+ or other modern Java versions can fail before compilation with Gradle 4.10.3 and ForgeGradle 2.3.
- A common modern-Java failure is `Unable to get mutable Windows environment variable map` caused by `java.lang.reflect.InaccessibleObjectException`.
- Forge 1.12.2 and ForgeGradle 2.3 are legacy tooling; this project intentionally does not update to a modern Gradle version.
- Restricted or offline environments can make ForgeGradle print network/version-check stack traces such as `UnknownHostException` or `Permission denied: connect` even when the cached build finishes with `BUILD SUCCESSFUL`.

## QA And CI

| Issue | Impact | Suggested Test | Blocks Wider Alpha? |
| --- | --- | --- | --- |
| GitHub Actions may not queue workflow runs due to a GitHub/account/repository Actions issue. | Remote CI proof may be unavailable even when the repository workflow files are present. | Use `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1` for local Java 8 build proof and confirm `build/libs/standandhold-0.1.0-alpha.1.jar` exists. | No, if local Java 8 build proof passes and manual launch testing continues. |

## Compatibility

- Scape and Run: Parasites entity registry IDs are not verified by default.
- The default `minecraft:zombie` mappings are test mappings only.
- SRP compatibility is currently config-driven and should be tested with the exact SRP build used by a pack.

## Gameplay Foundation

- The first playable vertical slice assumes a Creative/admin test world for placing core blocks and forcing deterministic event checks.
- Current worldgen structures are placeholder/simple.
- Scientist AI is placeholder/deferred.
- Advanced weapons, ammo, reload systems, and modern weapon mechanics are deferred.
- Art, item models, block models, and textures are placeholder quality.
- Human NPC models/textures are placeholder/WIP for alpha testing.
- Balance is testing-focused, not final.

## Saved Data

- Saved data versioning currently has a small non-destructive version 0 to 1 migration hook. Larger save migrations are still deferred until the data model stabilizes.
- Stale registered Field Command Post, Research Lab, or Main Base positions should be tested if blocks are broken manually, structures are removed, or worlds are edited externally.
- Cleanup only checks loaded chunks and intentionally does not force-load distant saved positions.
- Command/list/status systems should tolerate missing loaded tiles, but wider alpha testing should keep an eye on stale saved positions and active Main Base deactivation.
