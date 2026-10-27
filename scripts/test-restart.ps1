$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & .\scripts\use-modern-java.ps1
    $env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
    $world = 'qa-' + [guid]::NewGuid().ToString()
    foreach ($phase in @('write', 'read')) {
        & .\gradlew.bat runQaPersistenceServer "-PqaWorld=$world" "-PqaPhase=$phase" --no-daemon --stacktrace --console=plain
        if ($LASTEXITCODE -ne 0) { throw "QA restart $phase failed ($LASTEXITCODE). See run/qa-server/logs/latest.log." }
    }
    Write-Host "Two-process restart passed: run/qa-server/$world (preserved)."
} finally {
    Pop-Location
}
