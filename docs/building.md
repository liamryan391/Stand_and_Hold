# Building Stand and Hold

Stand and Hold targets Minecraft Forge 1.12.2 and uses ForgeGradle 2.3 with Gradle 4.10.3. This legacy toolchain must run under Java 8.

## Java Requirement

Use a Java 8 JDK to run Gradle. Newer Java versions can fail before the project even compiles.

Known modern-Java failure:

```text
Unable to get mutable Windows environment variable map
Caused by:
java.lang.reflect.InaccessibleObjectException:
module java.base does not "opens java.lang" to unnamed module
```

This usually means Gradle 4.10.3 is being launched with Java 9+ or newer. Java 17+ and Java 24 are especially likely to trigger this class of failure.

## Check Your Java Version

In PowerShell:

```powershell
java -version
echo $env:JAVA_HOME
```

The version should start with `1.8.0`.

## Temporarily Set JAVA_HOME

Set `JAVA_HOME` to your own Java 8 JDK path. Do not copy these paths blindly; adjust them for your machine.

Example using Eclipse Temurin 8:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.x-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Example using a workspace-local JDK:

```powershell
$env:JAVA_HOME = (Resolve-Path ".jdks\jdk8u492-b09").Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

## Recommended Local Build

Use the helper script:

```powershell
.\scripts\build-java8.ps1
```

If your PowerShell execution policy blocks local scripts, use a one-run bypass that does not change the global policy:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1
```

The helper prints `java -version`, checks `JAVA_HOME`, explains the Java 8 requirement, stops Gradle daemons, and runs:

```powershell
.\gradlew.bat clean build --no-daemon --stacktrace
```

## Direct Gradle Build

If Java 8 is already active:

```powershell
.\gradlew.bat clean build --no-daemon --stacktrace
```

## Build Output

Successful builds place jars in:

```text
build/libs/
```

For `0.1.0-alpha.1`, the expected alpha test jar is:

```text
build/libs/standandhold-0.1.0-alpha.1.jar
```

The `-sources.jar` is for source reference and is not the normal mod jar to install.

## Legacy ForgeGradle Network Warnings

ForgeGradle 2.3 can try old metadata/version-check URLs during configuration. In restricted or offline environments it may print stack traces such as `UnknownHostException` or `Permission denied: connect`. If the Gradle output still ends with `BUILD SUCCESSFUL` and the jar exists in `build/libs/`, treat those as legacy tooling warnings rather than a Stand and Hold compile failure.
