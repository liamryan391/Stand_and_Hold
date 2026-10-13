# Windows Build And IDE Setup

Phase M0, step 0.3.4. Documented 2026-10-13. This is a setup/runbook decision, not modern build or IDE launch proof.

## Which Project Are You Building?

| Checkout state | Java and build | Instructions |
| --- | --- | --- |
| Current `main`, before M1 replaces the legacy files | Java 8, Forge 1.12.2, Gradle 4.10.3 | Use the existing [legacy build guide](../building.md) and Java 8 helper |
| Preserved `1.12` branch | Same legacy toolchain | Keep Java 8; do not apply modern Gradle/IDE settings to it |
| Modern scaffold, after M1 installs the selected MDK | Java 25, NeoForge 26.3.0.57-beta, Gradle 9.2.1, ModDevGradle 2.0.148 | Use the modern steps below |

[Selected Modern Toolchain](toolchain.md) owns all exact pins, the MDK commit, Java patch baseline, and wrapper checksum. This step does not update those pins or replace build files. Do not run the current legacy wrapper under Java 25.

## Legacy Build Now

Open PowerShell in the repository root and run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1
if ($LASTEXITCODE -ne 0) { throw 'Legacy Java 8 build failed.' }
```

This helper chooses Java 8 inside its child process and runs the legacy clean build. A successful legacy build says nothing about the modern port. Close a modern-development shell before returning to legacy work so its process-local Gradle cache/JDK settings are not inherited accidentally.

## Select Java For Modern Work

Use a dedicated PowerShell window opened in the intended checkout root. A new checkout needs its own ignored local JDK configuration. Replace this example path with an installed 64-bit Java 25 JDK:

```powershell
& .\scripts\use-modern-java.ps1 -JavaHome 'C:\path\to\jdk-25' -Save
```

In each later shell, the saved choice is reused:

```powershell
& .\scripts\use-modern-java.ps1
$env:JAVA_HOME
Get-Command java.exe, javac.exe | Select-Object Name, Source
java --version
javac --version
```

Expect Java **25** for both tools; the verified local baseline is Temurin **25.0.4+7**. The selector changes only this shell and its future child processes. Do not launch it with a separate `powershell.exe -File` and expect the parent shell to change. It does not configure a running IDE. If local policy blocks the reviewed script, open a dedicated `powershell.exe -NoProfile -ExecutionPolicy Bypass` shell and run the selector there; do not change machine-wide policy or bypass managed organizational policy.

## Modern Build After M1 Only

Run this preparation block from the modern checkout root. The guard intentionally rejects today's legacy checkout before selecting Java or executing Gradle:

```powershell
$ErrorActionPreference = 'Stop'
$wrapper = Get-Content .\gradle\wrapper\gradle-wrapper.properties -Raw
$build = Get-Content .\build.gradle -Raw
if ($wrapper -notmatch '(?m)^distributionUrl=.*gradle-9\.2\.1-bin\.zip\s*$' -or
    $build -notmatch 'net\.neoforged\.moddev') {
    throw 'Modern scaffold is not installed. Use the Java 8 helper until M1.'
}
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat --version
if ($LASTEXITCODE -ne 0) { throw 'Gradle version check failed.' }
```

Inspect the output before continuing: Gradle must be **9.2.1**, and the launcher JVM and daemon JVM selection must resolve to **Java 25**. A daemon entry may show a JDK path or criteria instead of a version: resolve it and confirm it matches the intended JDK. Stop on a mismatch. Also check the M1 build's Minecraft, NeoForge, plugin, and Java toolchain pins against [toolchain.md](toolchain.md).

In that same prepared shell:

```powershell
.\gradlew.bat --stop
if ($LASTEXITCODE -ne 0) { throw 'Could not stop the modern Gradle daemons.' }
.\gradlew.bat clean build --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'Modern clean build failed.' }
Get-ChildItem .\build\libs\*.jar | Select-Object Name, Length, LastWriteTime
```

Use the checked-in wrapper, not a system `gradle` installation or an IDE-offered upgrade. The first dependency/source preparation can take time. `BUILD SUCCESSFUL` and exit code zero are required; an old jar in `build/libs` is not build proof. Select the normal mod jar, not `-sources` or `-javadoc`, and inspect its `META-INF/neoforge.mods.toml` and Minecraft/NeoForge requirements before installing it in a modern test instance. The filename alone cannot distinguish the legacy and modern artifacts if the mod version is retained.

The ignored `.gradle-home/modern` directory isolates modern caches, daemon logs, and user-level Gradle properties from other projects. It can require fresh downloads. Set the same location in the IDE. Do not copy global init scripts/properties into it blindly or commit caches, absolute JDK paths, logs, or worlds. Gradle documents `GRADLE_USER_HOME` and JDK selection in its [build environment reference](https://docs.gradle.org/current/userguide/build_environment.html).

## IntelliJ IDEA Setup

Apply this to the **modern scaffold only**, after M1 has replaced the legacy build. The current legacy checkout must keep its Java 8 settings.

1. Open the modern checkout's root as a Gradle project. Use an IntelliJ version that supports Java 25; do not change the IDE's own boot runtime just to configure this project.
2. Under **File > Project Structure > Project**, select the installed JDK 25 as Project SDK. Use language level 25 without preview features, following the Gradle toolchain on reimport.
3. Under **Settings > Build, Execution, Deployment > Build Tools > Gradle**, select this linked project. Choose the distribution from `gradle-wrapper.properties`, not a locally installed Gradle.
4. Set **Gradle JVM** explicitly to that JDK 25 installation, not an inherited Java 8 `JAVA_HOME`. Set **Build and run using** and **Run tests using** to Gradle. Set **Gradle user home** to this checkout's absolute `.gradle-home\modern` path.
5. If the IDE displays **Gradle JVM Criteria** instead, inspect `gradle/gradle-daemon-jvm.properties` as described below. Keep it consistent with Java 25; review any file changes rather than silently accepting a new toolchain.
6. Reload the Gradle project. Run the `build` task from the Gradle tool window and check its JVM/configuration. Success in the embedded terminal alone does not verify IDE Gradle sync or task execution. Then test the generated client/server run configurations in M1.

Labels vary by IDE release. Sources: [JetBrains Gradle settings](https://www.jetbrains.com/help/idea/gradle-settings.html) and [Gradle JVM selection](https://www.jetbrains.com/help/idea/gradle-jvm-selection.html). Other IDEs must separately align their project JDK and Gradle runtime with Java 25 and use the wrapper; this guide does not claim those IDEs were tested.

## JVM Override Troubleshooting

If the terminal, IDE, or build disagree, inspect these before changing versions:

- The selected checkout and wrapper, the selector's saved path, `JAVA_HOME`, and `Get-Command java.exe` output.
- The IDE's Gradle JVM and Gradle user-home settings. A terminal environment change does not update an already-running IDE's Gradle settings.
- `org.gradle.java.home` in project and selected Gradle-user-home `gradle.properties`, plus `JAVA_OPTS`/`GRADLE_OPTS` overrides. A build JVM property does not change the wrapper launcher's JVM.
- `gradle/gradle-daemon-jvm.properties`, if present. Daemon criteria take precedence over `JAVA_HOME` and `org.gradle.java.home`. Do not add or delete criteria as a blind workaround; align reviewed criteria with the pinned target. See the [Gradle daemon documentation](https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria).

After correcting the cause, stop the modern daemons from the prepared modern shell, reload IDE Gradle, and inspect `--version` again. `--no-daemon` can still start a single-use build JVM; it does not bypass wrong JVM settings. Never fix this by globally replacing the legacy Java 8 installation or adding personal paths to tracked build files.

## M1 Runtime Checks

These commands are **not run in M0**. After a successful modern build, use the same prepared Java 25 shell and run each separately:

```powershell
.\gradlew.bat runClient --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'Client task failed; inspect its log.' }
```

```powershell
.\gradlew.bat runServer --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'Server task failed; inspect its log.' }
```

Before launching, M1 must set distinct ModDevGradle `gameDirectory` values under ignored `run/modern-client` and `run/modern-server`; these are intended directories, not configured by this documentation step. The pinned MDK defines client/server runs and gives the server `--nogui`. ModDevGradle otherwise defaults runs to `run`; see its [run configuration documentation](https://github.com/neoforged/ModDevGradle).

Use new disposable modern worlds with no external threat mods first. Client proof means Mods list, main menu, and new-world entry, not merely task startup. Server proof means readiness, world save, `stop` in its console, and a clean exit. An EULA stop is incomplete verification: review the EULA and accept only if agreed, then rerun. Never open a 1.12 save in the modern test instance. Optional threat mods are tested separately after the baseline works.

## Fallback Decision Rule

The selected target remains 26.3. **A failed command alone is not a reason to downgrade or upgrade.**

1. Record the commit, exact pins, OS/JDK, `--version` output, failed command, first relevant exception, and full log location in [Progress Log](progress-log.md). Record the result as failed or blocked, not passed. Redact credentials/personal details before publishing logs.
2. Fix environment failures first: wrong JVM/checkout, stale IDE settings, proxy/TLS/download failures, rate limits, permissions, disk space, and memory. Retry after addressing the cause; do not disable certificate/checksum verification, delete whole caches/worlds, or change pins to hide it.
3. For a build or baseline launch failure, reproduce using the recorded official MDK snapshot and selected pins in an isolated scratch project with no optional mods. Preserve the main checkout and logs. If the clean template works, fix Stand and Hold's scaffold/configuration. If only an optional library/mod fails, defer that integration before reconsidering Minecraft.
4. Consider a target fallback only for a reproducible toolchain/loader blocker in the minimal template that cannot be resolved by a small verified fix, or an explicitly essential dependency unavailable on 26.3. Check official issue/release evidence. A newly published version is a separate reviewed pin update, not an automatic fix.
5. Propose the reason and test evidence to the maintainer before changing target. First candidate: **Minecraft 26.2 / NeoForge 26.2.0.88**, Java 25, Gradle 9.2.1, ModDevGradle 2.0.148, from the [verified 26.2 MDK snapshot](release-verification.md). Recheck artifact availability and dependency ranges when invoking the fallback; keep the whole toolchain coherent. Reconsider 1.21.1/Java 21 only if a specific requirement such as Spore makes it necessary, not as the default fallback.
6. After approval, update [toolchain.md](toolchain.md), memory, logs, wrapper/checksum, metadata ranges, build/CI pins, and test instructions together on the scaffold work branch. Repeat M1 clean build, client, and dedicated-server gates. Do not modify `1.12` or claim that a non-beta label proves runtime compatibility.

## Evidence And Handoff

Modern Gradle execution, artifact inspection, IDE sync/build, client, and server tests remain **not run** at this step. M0 verifies the instructions, local selector, and safe rejection of the legacy checkout, not a modern mod build.

Verification passed for all seven PowerShell examples' syntax under PowerShell 7.6.5 and Windows PowerShell 5.1. The preparation guard was executed against the legacy checkout and rejected it before changing Java/cache settings or running Gradle. The saved Java 25 selection and documentation links/task consistency were also checked. Build/run examples were not executed past that guard.

The accepted [foundation scope and dependency policy](foundation-scope.md) now closes step 0.4. These commands still wait for the actual M1 scaffold.

**Next: Phase M1, step 1.1.2 - preserve modern planning notes and project identity, and move legacy-only documentation references to the 1.12 branch in the modern README.** The [inventory](legacy-inventory.md) is complete; modern command execution follows the actual M1 build replacement.
