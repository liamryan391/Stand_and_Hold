[CmdletBinding()]
param(
    [string]$Java8Home
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
$GradleWrapper = Join-Path $RepoRoot "gradlew.bat"

function Get-JavaVersionText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaExe
    )

    if ([string]::IsNullOrWhiteSpace($JavaExe) -or -not (Test-Path $JavaExe)) {
        return ""
    }

    try {
        # Java prints version information to stderr. Calling through cmd.exe avoids
        # PowerShell 5.1 converting native stderr into NativeCommandError records
        # when $ErrorActionPreference is Stop.
        $escapedJavaExe = '"' + $JavaExe + '"'
        $output = & cmd.exe /d /c "$escapedJavaExe -version 2>&1"
        return ($output | Out-String)
    } catch {
        return ""
    }
}

function Write-VersionText {
    param(
        [string]$VersionText
    )

    if ([string]::IsNullOrWhiteSpace($VersionText)) {
        Write-Host "<no version output captured>"
        return
    }

    $VersionText -split "`r?`n" |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        ForEach-Object { Write-Host $_ }
}

function Test-IsJava8 {
    param(
        [string]$VersionText
    )

    return $VersionText -match 'version "1\.8\.' -or $VersionText -match 'version "8[\.\+]'
}

function Get-JavaHomeFromJavaExe {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaExe
    )

    $javaFile = Get-Item $JavaExe -ErrorAction SilentlyContinue
    if ($null -eq $javaFile) {
        return $null
    }

    $binDir = Split-Path -Parent $javaFile.FullName
    return Split-Path -Parent $binDir
}

function Add-CandidateJavaHome {
    param(
        [System.Collections.Generic.List[string]]$Candidates,
        [string]$Path
    )

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }

    try {
        $resolved = (Resolve-Path $Path.Trim().Trim('"') -ErrorAction Stop).Path
    } catch {
        return
    }

    if (-not $Candidates.Contains($resolved)) {
        $Candidates.Add($resolved)
    }
}

function Add-CandidateChildren {
    param(
        [System.Collections.Generic.List[string]]$Candidates,
        [string]$Root,
        [string[]]$Patterns
    )

    if ([string]::IsNullOrWhiteSpace($Root) -or -not (Test-Path $Root)) {
        return
    }

    foreach ($pattern in $Patterns) {
        Get-ChildItem -Path $Root -Directory -Filter $pattern -ErrorAction SilentlyContinue |
            ForEach-Object { Add-CandidateJavaHome -Candidates $Candidates -Path $_.FullName }
    }
}

function Find-Java8Home {
    param(
        [string]$RequestedJava8Home
    )

    $candidates = New-Object 'System.Collections.Generic.List[string]'

    Add-CandidateJavaHome -Candidates $candidates -Path $RequestedJava8Home
    Add-CandidateJavaHome -Candidates $candidates -Path $env:JAVA8_HOME
    Add-CandidateJavaHome -Candidates $candidates -Path $env:JDK8_HOME
    Add-CandidateJavaHome -Candidates $candidates -Path $env:JAVA_HOME
    Add-CandidateJavaHome -Candidates $candidates -Path (Join-Path $RepoRoot ".jdks\jdk8u492-b09")

    Add-CandidateChildren -Candidates $candidates -Root (Join-Path $RepoRoot ".jdks") -Patterns @("jdk8*", "jdk-8*", "*8u*", "*1.8*")

    $programRoots = @(
        $env:ProgramFiles,
        ${env:ProgramFiles(x86)}
    )

    foreach ($programRoot in $programRoots) {
        if ([string]::IsNullOrWhiteSpace($programRoot)) {
            continue
        }

        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "Eclipse Adoptium") -Patterns @("jdk-8*", "jdk8*", "*8u*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "AdoptOpenJDK") -Patterns @("jdk-8*", "jdk8*", "*8u*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "Java") -Patterns @("jdk1.8*", "jre1.8*", "jdk8*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "Amazon Corretto") -Patterns @("jdk1.8*", "jdk8*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "BellSoft") -Patterns @("LibericaJDK-8*", "jdk8*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "Zulu") -Patterns @("zulu-8*", "zulu8*")
        Add-CandidateChildren -Candidates $candidates -Root (Join-Path $programRoot "Microsoft") -Patterns @("jdk-8*", "jdk8*")
    }

    foreach ($candidate in $candidates) {
        $javaExe = Join-Path $candidate "bin\java.exe"
        $versionText = Get-JavaVersionText -JavaExe $javaExe
        if (Test-IsJava8 -VersionText $versionText) {
            return $candidate
        }
    }

    return $null
}

Write-Host "Stand and Hold Java 8 build helper"
Write-Host "ForgeGradle 2.3 and Gradle 4.10.3 require Java 8 for this Minecraft Forge 1.12.2 project."
Write-Host "This helper may temporarily set JAVA_HOME and PATH for this PowerShell process only. It does not change your global Windows Java settings."
Write-Host ""

if (-not (Test-Path $GradleWrapper)) {
    throw "Could not find gradlew.bat at $GradleWrapper"
}

Write-Host "Initial JAVA_HOME: $env:JAVA_HOME"
Write-Host ""
Write-Host "Active PATH java -version:"
$activeJava = Get-Command java -ErrorAction SilentlyContinue
$activeJavaVersionText = ""
if ($null -ne $activeJava) {
    $activeJavaVersionText = Get-JavaVersionText -JavaExe $activeJava.Source
    Write-VersionText -VersionText $activeJavaVersionText
} else {
    Write-Warning "No java executable was found on PATH. The helper will try to locate Java 8 from known install paths."
}

$java8HomeToUse = $null
if (Test-IsJava8 -VersionText $activeJavaVersionText) {
    $java8HomeToUse = Get-JavaHomeFromJavaExe -JavaExe $activeJava.Source
    Write-Host ""
    Write-Host "Active PATH Java is already Java 8."
} else {
    Write-Host ""
    Write-Warning "Active Java is not Java 8. Searching for a Java 8 JDK to use only for this build..."
    $java8HomeToUse = Find-Java8Home -RequestedJava8Home $Java8Home
}

if ([string]::IsNullOrWhiteSpace($java8HomeToUse)) {
    Write-Host ""
    Write-Host "Java 8 could not be detected automatically."
    Write-Host "Install a Java 8 JDK, such as Eclipse Temurin 8, or rerun with an explicit path:"
    Write-Host 'powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-java8.ps1 -Java8Home "C:\Path\To\Java8\JDK"'
    throw "Java 8 is required. Modern Java can fail before compilation with: Unable to get mutable Windows environment variable map / InaccessibleObjectException."
}

$java8Exe = Join-Path $java8HomeToUse "bin\java.exe"
$java8VersionText = Get-JavaVersionText -JavaExe $java8Exe
if (-not (Test-IsJava8 -VersionText $java8VersionText)) {
    throw "Detected Java path is not Java 8: $java8HomeToUse"
}

$env:JAVA_HOME = $java8HomeToUse
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host ""
Write-Host "Using Java 8 for this build only: $env:JAVA_HOME"
Write-Host "Confirmed Java 8 version:"
Write-VersionText -VersionText $java8VersionText

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