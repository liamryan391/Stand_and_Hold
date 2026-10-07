[CmdletBinding()]
param(
    [string]$JavaHome,
    [switch]$Save
)

$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path -Parent $PSScriptRoot
$LocalConfig = Join-Path $RepoRoot '.local/modern-jdk.json'

if (-not $PSBoundParameters.ContainsKey('JavaHome')) {
    if (-not (Test-Path -LiteralPath $LocalConfig -PathType Leaf)) {
        throw 'No modern JDK configured. Run this script with -JavaHome <JDK-25-directory> -Save first.'
    }
    $JavaHome = (Get-Content -LiteralPath $LocalConfig -Raw | ConvertFrom-Json).javaHome
}

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    throw 'JavaHome must name a Java 25 JDK directory.'
}

$JavaHome = (Resolve-Path -LiteralPath $JavaHome -ErrorAction Stop).ProviderPath
$JavaExe = Join-Path $JavaHome 'bin/java.exe'
$JavacExe = Join-Path $JavaHome 'bin/javac.exe'
$ReleaseFile = Join-Path $JavaHome 'release'
foreach ($file in @($JavaExe, $JavacExe, $ReleaseFile)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Not a complete JDK: missing $file"
    }
}

# Check metadata before executing --version, which legacy Java 8 does not support.
$Release = Get-Content -LiteralPath $ReleaseFile -Raw | ConvertFrom-StringData
if ($Release.JAVA_VERSION -notmatch '^"25(?:[.+-]|")') {
    throw "Modern builds require Java 25, found $($Release.JAVA_VERSION). Use build-java8.ps1 for the legacy project."
}
if ($Release.OS_ARCH -notin @('"x86_64"', '"amd64"', '"aarch64"')) {
    throw "A 64-bit JDK is required, found $($Release.OS_ARCH)."
}

$JavaVersion = & $JavaExe --version
if ($LASTEXITCODE -ne 0 -or ($JavaVersion -join "`n") -notmatch '(?m)^(?:openjdk|java) 25(?:[. +\r\n-]|$)') {
    throw 'The selected java.exe did not report Java 25 successfully.'
}
$JavacVersion = & $JavacExe --version
if ($LASTEXITCODE -ne 0 -or ($JavacVersion -join "`n") -notmatch '^javac 25(?:[. +\r\n-]|$)') {
    throw 'The selected javac.exe did not report Java 25 successfully.'
}

# Save only after validation; machine-specific paths never belong in tracked build files.
if ($Save) {
    $null = New-Item -ItemType Directory -Path (Split-Path -Parent $LocalConfig) -Force
    @{ javaHome = $JavaHome } | ConvertTo-Json | Set-Content -LiteralPath $LocalConfig -Encoding UTF8
    Write-Host "Saved local JDK selection: $LocalConfig"
}

$JavaBin = Join-Path $JavaHome 'bin'
$env:JAVA_HOME = $JavaHome
$env:PATH = (@($JavaBin) + @($env:PATH -split ';' | Where-Object { $_ -and $_ -ne $JavaBin })) -join ';'
Write-Host "Modern JAVA_HOME: $env:JAVA_HOME"
$JavaVersion | ForEach-Object { Write-Host $_ }
$JavacVersion | ForEach-Object { Write-Host $_ }
Write-Host 'Selected for this PowerShell process only; user/machine Java settings are unchanged.'
Write-Host 'This script does not run Gradle. Until M1 replaces the legacy build, use build-java8.ps1 to build it.'
