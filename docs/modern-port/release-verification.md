# Official Release And MDK Verification

Phase M0, step 0.3.1. Checked on 2026-10-07 using official release metadata and the official NeoForge MDK repositories.

Status: **complete for availability research**. No project toolchain has been selected or installed. No modern build or Minecraft launch was attempted.

## Findings

Mojang's [Java version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) reports `26.3` as its latest release and `26.4-snapshot-3` as its latest snapshot. Minecraft Java 26.3 was released on 2026-09-15; 26.4 is not a release target for this port. See the [official 26.3 announcement](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).

The [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml) reports `26.3.0.57-beta` as latest, with metadata timestamp `20261007202120`. All published builds in the 26.3 family at this check have a beta suffix. Its XML `<release>` field also contains that beta version: that field alone does not mean a non-beta build.

Minecraft 26.2 is the newest released Minecraft line with a non-beta NeoForge artifact at this check. Here, "non-beta" describes the published version label, not a guarantee of a bug-free runtime or third-party mod support.

## Available Candidates

| Minecraft | Latest published NeoForge in that family | Loader label | Official ModDevGradle MDK default | Java major |
| --- | --- | --- | --- | --- |
| 26.3 | `26.3.0.57-beta` | Beta | `26.3.0.52-beta` | 25 |
| 26.2 | `26.2.0.88` | Non-beta | `26.2.0.88` | 25 |
| 26.1.2 | `26.1.2.114` | Non-beta | `26.1.2.114` | 25 |
| 1.21.11 | `21.11.45` | Non-beta | `21.11.45` | 21 |
| 1.21.1 | `21.1.256` | Non-beta | `21.1.256` | 21 |

NeoForge versions come from the Maven metadata, MDK defaults from each template's `gradle.properties`, and Java majors from both its `build.gradle` and Mojang's per-version metadata. No assumption was made that a mod for one Minecraft version will work on another.

The 26.3 MDK default and newest published loader differ. Both artifacts have matching published POMs. Step 0.3.3 must deliberately choose a version; do not silently treat the template default as the newest release or update it without subsequent build verification.

## MDK Availability And Build Files

The [NeoForge homepage](https://neoforged.net/) links to its [mod generator](https://neoforged.net/mod-generator/) and the [official MDK organization](https://github.com/NeoForgeMDKs). Its repositories provide both ModDevGradle and NeoGradle templates for all five candidates above.

For this check, the ModDevGradle variants were inspected in detail. The organization describes ModDevGradle as the simpler build-script option and NeoGradle as supporting multiple Minecraft/NeoForge versions in one project. ModDevGradle is a reasonable initial candidate for this single-target scaffold; the plugin decision is still recorded in step 0.3.3.

All five inspected templates contain:

- `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`.
- A wrapper configured for **Gradle 9.2.1**.
- The **`net.neoforged.moddev` plugin, version 2.0.148**.
- The Java toolchain major listed in the candidate table.
- Mod metadata at `src/main/templates/META-INF/neoforge.mods.toml`.

Immutable template snapshots inspected:

| Minecraft | Official MDK snapshot | Files checked |
| --- | --- | --- |
| 26.3 | [MDK at 4dacaffe](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/tree/4dacaffe4d5395c675a824beac2d1961b7d03bdb) | `gradle.properties`, `build.gradle`, wrapper properties, repository tree |
| 26.2 | [MDK at bc9b0583](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle/tree/bc9b0583c08e952fcaba27f721ca2f7c282fcc8a) | Same |
| 26.1.2 | [MDK at 6c484f87](https://github.com/NeoForgeMDKs/MDK-26.1.2-ModDevGradle/tree/6c484f87207386aafd9c57533ae87bb14fde4239) | Same |
| 1.21.11 | [MDK at f3a174f3](https://github.com/NeoForgeMDKs/MDK-1.21.11-ModDevGradle/tree/f3a174f32f2372a4bc97af6cb3a733bc67dd3677) | Same |
| 1.21.1 | [MDK at ad911709](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/tree/ad911709f06d3bdd5b1a500023a9156999216351) | Same |

These are upstream source snapshots, not installed project dependencies. MDK `main` branches may change; use these links to reproduce this inspection, then recheck when selecting the scaffold.

## Published Artifact Checks

Each following POM was retrieved successfully and its artifact ID/version checked:

- [NeoForge 26.3.0.57-beta](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.57-beta/neoforge-26.3.0.57-beta.pom).
- [NeoForge 26.3.0.52-beta](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.52-beta/neoforge-26.3.0.52-beta.pom), the inspected MDK default.
- [NeoForge 26.2.0.88](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88.pom).
- [NeoForge 26.1.2.114](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.1.2.114/neoforge-26.1.2.114.pom).
- [NeoForge 21.11.45](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.11.45/neoforge-21.11.45.pom).
- [NeoForge 21.1.256](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.256/neoforge-21.1.256.pom).
- [ModDevGradle 2.0.148 plugin marker](https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/2.0.148/net.neoforged.moddev.gradle.plugin-2.0.148.pom).

Gradle publishes the [9.2.1 binary distribution checksum](https://services.gradle.org/distributions/gradle-9.2.1-bin.zip.sha256):

```text
72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
```

The checksum text was fetched and validated as a SHA-256 value. The distribution archive and dependency jars were not downloaded or executed; full dependency resolution remains part of the scaffold build check.

## Recommendation At Completion Of Step 0.3.1

Carry **26.3** and **26.2** into step 0.3.2 as the primary comparison. This is a planning recommendation, not a toolchain selection:

1. 26.3 satisfies the preference for the newest released Minecraft and has an official MDK, but currently requires accepting a beta loader.
2. 26.2 is the closest non-beta-loader alternative. Prefer it if the needed ecosystem is available there and avoiding beta API changes matters more than the newest Minecraft release.
3. Keep 26.1.2 and the Java 21 alternatives available if the actual libraries/threat mods point to them. Their inclusion here does not assert stronger mod support; verify published files in step 0.3.2.

Check animation libraries, the AI approach, and at least one compatible threat mod before committing to a target. NeoForge-only remains the initial dependency policy; future optional libraries should inform the choice without automatically becoming hard dependencies.

## Scope And Handoff

- Completed: released/snapshot distinction, NeoForge beta/non-beta distinction, official template availability, template file inspection, Java requirements, published POM checks, and stable alternatives.
- At completion of 0.3.1, third-party ecosystem compatibility was still outstanding. It is now documented in [Ecosystem Compatibility](ecosystem-compatibility.md). Final pinning (0.3.3), JDK/IDE/build setup (0.3.3-0.3.4), and scaffold build/runtime proof (M1) remain outstanding.
- Existing Forge 1.12.2 source, Gradle files, mod version, Java settings, and Git branches were not changed.
- The Java 8/25 PATH/JAVA_HOME mismatch in [Preflight Checks](preflight-checks.md) still needs explicit build-process selection once a target is chosen. A Java 21 target would require arranging a Java 21 toolchain; none was found in the earlier installed-JDK check.
- Planning changes were local/uncommitted at the end of 0.3.1. Subsequent publication is tracked in Git history and the [progress log](progress-log.md).

**Next: Phase M0, step 0.3.3 - select and record exact toolchain versions using this release snapshot and the completed ecosystem check.**
