[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
$GradleWrapper = Join-Path $RepoRoot "gradlew.bat"

Write-Host "Stand and Hold Java 8 build helper"
Write-Host "ForgeGradle 2.3 and Gradle 4.10.3 require Java 8 for this Minecraft Forge 1.12.2 project."
Write-Host ""

if (-not (Test-Path $GradleWrapper)) {
    throw "Could not find gradlew.bat at $GradleWrapper"
}

Write-Host "JAVA_HOME: $env:JAVA_HOME"
if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    Write-Warning "JAVA_HOME is not set. Set JAVA_HOME to a Java 8 JDK before running this build."
} else {
    $JavaHomeJava = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (-not (Test-Path $JavaHomeJava)) {
        Write-Warning "JAVA_HOME does not point to a JDK/JRE with bin\java.exe: $env:JAVA_HOME"
    }
}

Write-Host ""
Write-Host "java -version:"
$JavaVersionOutput = & cmd.exe /d /c "java -version 2>&1"
if ($LASTEXITCODE -ne 0) {
    throw "Could not run java -version. Confirm Java is installed and available on PATH."
}
$JavaVersionOutput | ForEach-Object { Write-Host $_ }
$JavaVersionText = ($JavaVersionOutput | Out-String)

if ($JavaVersionText -notmatch 'version "1\.8\.') {
    Write-Host ""
    Write-Warning "Java 8 is required. Modern Java can fail before compilation with: Unable to get mutable Windows environment variable map / InaccessibleObjectException."
    throw "Activate a Java 8 JDK, then rerun this helper."
}

Push-Location $RepoRoot
try {
    Write-Host ""
    Write-Host "Stopping Gradle daemons..."
    & $GradleWrapper --stop
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Gradle daemon stop returned exit code $LASTEXITCODE; continuing to the clean build."
    }

    Write-Host ""
    Write-Host "Running clean build..."
    & $GradleWrapper clean build --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
