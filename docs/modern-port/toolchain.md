# Selected Modern Toolchain

Decision: Phase M0, step 0.3.3, verified 2026-10-07.

These are the exact pins for the upcoming M1 scaffold, not a claim that the current root project already uses them. `main` still builds the Forge 1.12.2 alpha with Java 8; `1.12` remains untouched. No modern build or game launch has passed yet.

## Pins

| Component | Selected version | Evidence |
| --- | --- | --- |
| Minecraft Java Edition | `26.3`, metadata range `[26.3]` | [Mojang release manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), released 2026-09-15; per-version metadata requires Java 25 |
| NeoForge | `26.3.0.57-beta` | [Published loader POM](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.57-beta/neoforge-26.3.0.57-beta.pom) |
| Java language, compiler, and build runtime | `25`; baseline Eclipse Temurin **`25.0.4+7`**, 64-bit HotSpot | Installed `java --version`, `javac --version`, and JDK `release` metadata checked locally |
| Gradle wrapper | `9.2.1`, binary distribution | [Pinned official MDK wrapper](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/4dacaffe4d5395c675a824beac2d1961b7d03bdb/gradle/wrapper/gradle-wrapper.properties) |
| Build plugin | `net.neoforged.moddev` **`2.0.148`** | [Pinned official MDK build](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/4dacaffe4d5395c675a824beac2d1961b7d03bdb/build.gradle) |
| MDK toolchain resolver plugin | `org.gradle.toolchains.foojay-resolver-convention` **`1.0.0`** | [Pinned official MDK settings](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/4dacaffe4d5395c675a824beac2d1961b7d03bdb/settings.gradle) |
| Scaffold source | `NeoForgeMDKs/MDK-26.3-ModDevGradle` at `4dacaffe4d5395c675a824beac2d1961b7d03bdb` | [Immutable template tree](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/tree/4dacaffe4d5395c675a824beac2d1961b7d03bdb) |

In M1, use `java.toolchain.languageVersion = JavaLanguageVersion.of(25)` and the explicit JDK for the wrapper process. A compiler toolchain alone does not fix a wrapper launched under the wrong Java. Gradle's [Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html#java_runtime) lists Java 25 runtime support from Gradle 9.1.0 onward, covering the selected 9.2.1.

Use the [official distribution checksum](https://services.gradle.org/distributions/gradle-9.2.1-bin.zip.sha256) as `distributionSha256Sum` when installing the M1 wrapper:

```text
72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
```

The checksum endpoint was checked; the Gradle archive has not been downloaded or build-tested in this step. Preserve these pins rather than taking moving `latest` values during scaffolding. Java patch/security updates can be reviewed later; the selector enforces major version 25 and prints the actual patch, rather than rejecting every other Java 25 security release.

## Rationale And Limits

- 26.3 meets the latest-release preference. It has official MDKs and matching optional animation/AI/threat artifacts documented in [Ecosystem Compatibility](ecosystem-compatibility.md).
- NeoForge remains **beta** on this track. Accept that risk for the initial scaffold, not as a promise of stable APIs or a release-ready modpack.
- Deliberately use published loader `26.3.0.57-beta` instead of the template's older `26.3.0.52-beta`. Keep the template's Gradle and plugin versions. The inspected library manifests allow the selected loader, but only M1 build/client/server testing can prove the actual combination.
- ModDevGradle fits a single-target project; there is no need for a multi-version build or Fabric layer now.
- No third-party animation/AI library or threat mod becomes a dependency in this step. No legacy gameplay files, version numbers, Gradle settings, or CI configuration are changed.
- Keep Minecraft 26.2 / NeoForge 26.2.0.88 as the nearest non-beta-loader fallback. The fallback decision procedure and IDE/run commands belong to step 0.3.4. Do not switch targets automatically after a download/network error.

## Local JDK Selection

Java 25 is already installed on this computer; no install or global environment change is needed. The machine's `JAVA_HOME` points to Java 8 while PATH finds Java 25. Use [use-modern-java.ps1](../../scripts/use-modern-java.ps1) to explicitly select the modern JDK for the current PowerShell process.

One-time configuration from the repository root, replacing the example directory with an installed Java 25 **JDK**:

```powershell
& .\scripts\use-modern-java.ps1 -JavaHome 'C:\path\to\jdk-25' -Save
```

For each new modern-development PowerShell session:

```powershell
& .\scripts\use-modern-java.ps1
$env:JAVA_HOME
java --version
javac --version
```

The saved path is in ignored `.local/modern-jdk.json`. It is local configuration, not a downloaded JDK, and is not pushed to GitHub. Other checkouts/machines must configure their own path. An explicit `-JavaHome` overrides the saved value for that invocation; add `-Save` only to remember it. Invalid paths, JRE-only installs, non-64-bit metadata, and wrong Java majors are rejected before environment changes or saving.

Run the selector in the shell that will run Gradle. Calling it through a separate `powershell.exe -File` process only configures that child, not the calling shell. The script never invokes Gradle, installs software, changes user/machine environment variables, or edits global Gradle properties. Existing IDE processes require their own Gradle JVM selection in 0.3.4.

**Do not run the current legacy wrapper with Java 25.** Until the M1 replacement, build the existing source using `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1`. That helper remains unchanged and selects Java 8 itself. Closing the modern shell discards its environment changes.

## Verification Boundary

Step 0.3.3 verifies published pins, the installed JDK, and local selector behavior. Modern wrapper `--version`, dependency resolution, `clean build`, client launch, and dedicated-server readiness remain M1 gates. No modern CI/build success is inferred from legacy results.

**Next: Phase M0, step 0.3.4 - document Windows build commands, IDE Gradle JVM setup, and the fallback decision rule.**
