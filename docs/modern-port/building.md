# Windows Build And IDE Setup

The modern scaffold uses the exact pins in [toolchain.md](toolchain.md).
For live verification results use [STATUS.md](STATUS.md); this guide describes commands, not passed tests.
The preserved [1.12 build guide](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/docs/building.md)
and Java 8 helper apply only on that branch. Never run the legacy wrapper with Java 25.

## Select Java 25

Open PowerShell in the modern repository root. Configure your own installed 64-bit Java 25 JDK once:

```powershell
& .\scripts\use-modern-java.ps1 -JavaHome 'C:\path\to\jdk-25' -Save
```

Then prepare each build/run shell:

```powershell
$ErrorActionPreference = 'Stop'
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat --version
if ($LASTEXITCODE -ne 0) { throw 'Gradle version check failed.' }
```

Expect Gradle **9.2.1** and Java **25** for the launcher and daemon.
The local baseline is Temurin 25.0.4+7; the selector prints your actual patch.
It validates the JDK and changes only this shell, not global Java settings or a running IDE.
The ignored `.local/modern-jdk.json` stores only a local JDK path. Do not commit it.
If script policy blocks a reviewed local script, use a dedicated
`powershell.exe -NoProfile -ExecutionPolicy Bypass` shell, respecting managed organizational policy.

## Clean Build

In that same prepared shell:

```powershell
.\gradlew.bat clean build --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'Modern clean build failed.' }
Get-ChildItem .\build\libs\*.jar | Select-Object Name, Length, LastWriteTime
```

Use `build/libs/standandhold-0.1.0-alpha.1.jar`, not a sources/javadoc jar.
Inspect its `META-INF/neoforge.mods.toml`: Stand and Hold, `standandhold`, version
`0.1.0-alpha.1`, Minecraft `[26.3]`, and NeoForge `[26.3.0.57-beta,26.4)`.
The version/filename is intentionally retained; legacy jars with the same filename are **not** compatible.
Version is expanded from `gradle.properties`, not duplicated in Java constants.
No gameplay systems or MDK demo blocks/items are registered. The local config is
`config/standandhold-local.toml` inside each run directory; it only controls startup logging.

The Windows CI job uses Java 25 and the wrapper, runs the clean build, and uploads only the normal jar.
Its run result must be observed separately from a local build.
The wrapper scripts/jar come from the pinned official MDK; `TEMPLATE_LICENSE.txt` retains its MIT notice
and is included in the mod jar. This does not relicense Stand and Hold; its metadata retains the default
All Rights Reserved until the maintainer chooses a project licence.

## Client And Server

Run each separately from a prepared Java 25 shell:

```powershell
.\gradlew.bat runClient --no-daemon --stacktrace
```

Check main menu without errors, Mods list showing Stand and Hold / 0.1.0-alpha.1,
then create a fresh disposable world, enter it, save/quit, and exit the client.
No Stand and Hold items, units, commands, or structures are expected in M1.

```powershell
.\gradlew.bat runServer --no-daemon --stacktrace
```

Client and server use separate ignored `run/modern-client` and `run/modern-server` directories.
An EULA prompt is an incomplete test. **Liam must review and accept it himself**
in `run/modern-server/eula.txt`; the agent must stop, not set it to true.
After acceptance, rerun, wait for `Done`, type `stop`, and check world save and clean shutdown.
The pinned NeoForge development environment may skip the EULA file entirely; that is not an
installed-server test or permission to accept a later prompt. The agent never writes acceptance.
The Gradle `runServer` task forwards console input so `stop` can shut it down normally.
Development servers may use offline mode: keep them private, set `server-ip=127.0.0.1` in the
ignored test `server.properties`, and never expose them to the Internet.
Logs are `run/modern-client/logs/latest.log` and `run/modern-server/logs/latest.log`.
Never open 1.12 worlds/configs here. Test without external mods first.

## IntelliJ IDEA Setup

1. Open this root as a Gradle project in an IDE supporting Java 25.
2. Select JDK 25 for the Project SDK, language level 25 without preview.
3. Choose the checked-in Gradle wrapper; set Gradle JVM explicitly to JDK 25.
4. Use Gradle for builds/tests, and this checkout's absolute `.gradle-home/modern` as Gradle user home.
5. Reload Gradle and run its build task, then generated client/server run configurations.

Terminal success does not verify IDE sync. See [JetBrains settings](https://www.jetbrains.com/help/idea/gradle-settings.html).
On JVM mismatch, inspect the checkout, `JAVA_HOME`, IDE settings, project/user Gradle properties,
`JAVA_OPTS`/`GRADLE_OPTS`, and any `gradle/gradle-daemon-jvm.properties`.
Daemon criteria take precedence over `JAVA_HOME`; see [Gradle's build environment](https://docs.gradle.org/current/userguide/build_environment.html).
Correct the cause, run `.\gradlew.bat --stop` in the prepared modern shell, then recheck `--version`.
Do not delete caches/worlds, install global Java replacements, or pin personal paths in tracked files.

## Fallback Decision Rule

The selected target remains 26.3. **A failed command alone is not a reason to downgrade or upgrade.**

1. Record the commit, exact pins, OS/JDK, `--version` output, failed command, first relevant exception, and full log location in [Progress Log](progress-log.md). Record the result as failed or blocked, not passed. Redact credentials/personal details before publishing logs.
2. Fix environment failures first: wrong JVM/checkout, stale IDE settings, proxy/TLS/download failures, rate limits, permissions, disk space, and memory. Retry after addressing the cause; do not disable certificate/checksum verification, delete whole caches/worlds, or change pins to hide it.
3. For a build or baseline launch failure, reproduce using the recorded official MDK snapshot and selected pins in an isolated scratch project with no optional mods. Preserve the main checkout and logs. If the clean template works, fix Stand and Hold's scaffold/configuration. If only an optional library/mod fails, defer that integration before reconsidering Minecraft.
4. Consider a target fallback only for a reproducible toolchain/loader blocker in the minimal template that cannot be resolved by a small verified fix, or an explicitly essential dependency unavailable on 26.3. Check official issue/release evidence. A newly published version is a separate reviewed pin update, not an automatic fix.
5. Propose the reason and test evidence to the maintainer before changing target. First candidate: **Minecraft 26.2 / NeoForge 26.2.0.88**, Java 25, Gradle 9.2.1, ModDevGradle 2.0.148, from the [verified 26.2 MDK snapshot](release-verification.md). Recheck artifact availability and dependency ranges when invoking the fallback; keep the whole toolchain coherent. Reconsider 1.21.1/Java 21 only if a specific requirement such as Spore makes it necessary, not as the default fallback.
6. After approval, update [toolchain.md](toolchain.md), STATUS.md, logs, wrapper/checksum, metadata ranges, build/CI pins, and test instructions together on the scaffold work branch. Repeat M1 clean build, client, and dedicated-server gates. Do not modify `1.12` or claim that a non-beta label proves runtime compatibility.
