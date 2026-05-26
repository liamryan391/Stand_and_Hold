# Release Checklist

Use this checklist before tagging or publishing a Stand and Hold release.

## Build Readiness

- [ ] Confirm Java 8 is active with `java -version`.
- [ ] Confirm `JAVA_HOME` points to a Java 8 JDK.
- [ ] Run the local helper: `.\scripts\build-java8.ps1`.
- [ ] Confirm `.\gradlew.bat clean build --no-daemon --stacktrace` passes.
- [ ] Confirm GitHub Actions passes on Windows with Java 8.
- [ ] Confirm the release jar exists in `build/libs/`.

## Smoke Tests

- [ ] Launch the client where possible.
- [ ] Launch a dedicated server where possible.
- [ ] Test without Scape and Run: Parasites installed.
- [ ] Test with Scape and Run: Parasites installed, if available.
- [ ] Run the wider alpha checklist in `docs/alpha-testing-checklist.md`.
- [ ] Confirm existing worlds save/reload after HumanWorldData migration to the current saved data version.
- [ ] Confirm loaded stale Field Command Post, Research Lab, and Main Base saved positions clean safely without force-loading chunks.

## Documentation

- [ ] Update `CHANGELOG.md`.
- [ ] Update `KNOWN_ISSUES.md`.
- [ ] Update README release notes, jar name, and planned next phase if needed.
- [ ] Confirm known Java 8 build requirements are still documented.

## Version Consistency

The mod version is currently duplicated. Keep these values in sync:

- `gradle.properties`: `mod_version`
- `src/main/java/com/liamryan/standandhold/StandAndHoldConstants.java`: `VERSION`

Do not tag a release if these values differ.

## Tagging

- [ ] Confirm `git status` is clean.
- [ ] Create an annotated release tag, for example `git tag -a v0.1.0-alpha.1 -m "Stand and Hold 0.1.0 alpha 1"`.
- [ ] Push the branch.
- [ ] Push the tag.
- [ ] Confirm the tag exists on GitHub.
