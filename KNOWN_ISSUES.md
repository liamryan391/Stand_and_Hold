# Known Issues

Known issues for `0.1.0-alpha.1`.

## Build Toolchain

- Java 8 is required for building.
- Java 17+ or other modern Java versions can fail before compilation with Gradle 4.10.3 and ForgeGradle 2.3.
- A common modern-Java failure is `Unable to get mutable Windows environment variable map` caused by `java.lang.reflect.InaccessibleObjectException`.
- Forge 1.12.2 and ForgeGradle 2.3 are legacy tooling; this project intentionally does not update to a modern Gradle version.
- Restricted or offline environments can make ForgeGradle print network/version-check stack traces such as `UnknownHostException` or `Permission denied: connect` even when the cached build finishes with `BUILD SUCCESSFUL`.

## Compatibility

- Scape and Run: Parasites entity registry IDs are not verified by default.
- The default `minecraft:zombie` mappings are test mappings only.
- SRP compatibility is currently config-driven and should be tested with the exact SRP build used by a pack.

## Gameplay Foundation

- Current worldgen structures are placeholder/simple.
- Scientist AI is placeholder/deferred.
- Advanced weapons, ammo, reload systems, and modern weapon mechanics are deferred.
- Art, item models, block models, and textures are placeholder quality.
- Balance is testing-focused, not final.

## Saved Data

- Major save migration is not implemented yet.
- Stale registered Field Command Post or Main Base positions should be tested if blocks are broken manually, structures are removed, or worlds are edited externally.
- Command/list/status systems should tolerate missing loaded tiles, but wider alpha testing should keep an eye on stale saved positions.
