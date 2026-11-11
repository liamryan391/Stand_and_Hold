[CmdletBinding(DefaultParameterSetName = 'Run')]
param(
    [Parameter(ParameterSetName = 'Prepare')][switch]$PrepareNetwork,
    [Parameter(Mandatory, ParameterSetName = 'Review')][ValidatePattern('^m31-[a-f0-9]{12}$')][string]$ReviewSession
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot

function Read-Results([string]$Path) {
    $content = [IO.File]::ReadAllText($Path)
    foreach ($count in 0, 1, 2, 4) {
        $sample = [regex]::Match($content, "(?s)M31_SAMPLE_BEGIN_$count\b(.*?)M31_SAMPLE_END_$count\b").Groups[1].Value
        $averages = @([regex]::Matches($sample, 'Average time per tick: ([\d.]+)ms') | ForEach-Object { [double]$_.Groups[1].Value })
        $p95 = @([regex]::Matches($sample, 'P95: ([\d.]+)ms') | ForEach-Object { [double]$_.Groups[1].Value })
        $p99 = @([regex]::Matches($sample, 'P99: ([\d.]+)ms') | ForEach-Object { [double]$_.Groups[1].Value })
        $defenders = @([regex]::Matches($sample, 'm31_(?:rifles|survivors) has (\d+)') | ForEach-Object { [int]$_.Groups[1].Value })
        $hostiles = [regex]::Match($sample, 'm31_zombies has (\d+)')
        $reservations = @([regex]::Matches($sample, '\| reserved (\d+)') | ForEach-Object { [int]$_.Groups[1].Value })
        $raiders = @([regex]::Matches($sample, 'reserved raiders (\d+)') | ForEach-Object { [int]$_.Groups[1].Value })
        if ($averages.Count -ne 5 -or $p95.Count -ne 5 -or $p99.Count -ne 5 -or $defenders.Count -ne 2 `
                -or !$hostiles.Success -or $reservations.Count -ne $count -or $raiders.Count -ne $count `
                -or $sample -notmatch "Registered Field Command Posts: ${count};") {
            throw "Incomplete sample for $count posts; inspect $Path"
        }
        $meanMax = ($averages | Measure-Object -Maximum).Maximum
        $alive = ($defenders | Measure-Object -Sum).Sum
        $zombies = [int]$hostiles.Groups[1].Value
        if ($meanMax -gt 25 -or @($p95 | Where-Object { $_ -le 35 }).Count -lt 4 `
                -or $alive -gt [math]::Min(3 * $count, 8) -or $zombies -gt 8 `
                -or @($reservations | Where-Object { $_ -gt 3 }).Count -or @($raiders | Where-Object { $_ -gt 4 }).Count) {
            throw "M31 budget exceeded at $count posts; inspect $Path"
        }
        [pscustomobject]@{ Posts = $count; Defenders = $alive; Reserved = ($reservations -join '/'); Zombies = $zombies;
            RaidReserved = ($raiders -join '/'); MaxMeanMs = $meanMax; MaxP95Ms = ($p95 | Measure-Object -Maximum).Maximum;
            MaxP99Ms = ($p99 | Measure-Object -Maximum).Maximum }
    }
}
if ($ReviewSession) {
    Read-Results (Join-Path $repo "run/integration/$ReviewSession/server/logs/latest.log")
    return
}
$id = 'm31-' + [Guid]::NewGuid().ToString('N').Substring(0, 12)
$server = Join-Path $repo "run/integration/$id/server"
if (Test-Path -LiteralPath $server) { throw "Refusing to reuse $server" }
$null = New-Item -ItemType Directory -Path $server
$online = if ($PrepareNetwork) { 'false' } else { 'true' }
[IO.File]::WriteAllLines((Join-Path $server 'server.properties'), @(
    'server-ip=127.0.0.1', 'server-port=25579', "online-mode=$online", "enforce-secure-profile=$online",
    'level-name=fixture', 'level-type=minecraft\:flat', 'level-seed=310031',
    'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}',
    'generate-structures=false', 'pause-when-empty-seconds=-1',
    'view-distance=4', 'simulation-distance=4', 'difficulty=normal', 'spawn-protection=0', 'enable-rcon=false', 'white-list=true'
))
Write-Host "Integration session $id in $server"
if ($PrepareNetwork) {
    $seats = @('m31seata', 'm31seatb') | ForEach-Object {
        # Minecraft's offline identity is the Java name-based UUID, not an authenticated account.
        $digest = [Security.Cryptography.MD5]::HashData([Text.Encoding]::UTF8.GetBytes("OfflinePlayer:$_"))
        $digest[6] = ($digest[6] -band 0x0f) -bor 0x30
        $digest[8] = ($digest[8] -band 0x3f) -bor 0x80
        @{ name = $_; uuid = [Guid]::ParseExact([Convert]::ToHexString($digest), 'N').ToString() }
    }
    [IO.File]::WriteAllText((Join-Path $server 'whitelist.json'), (ConvertTo-Json -InputObject @($seats)))
    return
}

& (Join-Path $PSScriptRoot 'use-modern-java.ps1')
$env:GRADLE_USER_HOME = Join-Path $repo '.gradle-home/modern'
$start = [Diagnostics.ProcessStartInfo]::new($env:ComSpec)
$start.WorkingDirectory = $repo
$start.UseShellExecute = $false
$start.CreateNoWindow = $true
$start.RedirectStandardInput = $true
$start.RedirectStandardOutput = $true
$start.RedirectStandardError = $true
foreach ($argument in '/d', '/c', ".\gradlew.bat runIntegrationServer -PintegrationId=$id --no-daemon --console=plain --stacktrace") {
    $start.ArgumentList.Add($argument)
}
$process = [Diagnostics.Process]::Start($start)
$stdout = $process.StandardOutput.ReadToEndAsync()
$stderr = $process.StandardError.ReadToEndAsync()
$log = Join-Path $server 'logs/latest.log'
$posts = @(@(8,8), @(24,8), @(8,24), @(24,24))

function Read-GameLog {
    if (Test-Path -LiteralPath $log) {
        $stream = [IO.File]::Open($log, 'Open', 'Read', 'ReadWrite')
        $reader = [IO.StreamReader]::new($stream)
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    }
    return ''
}
function Send-Console([string[]]$Lines) {
    foreach ($line in $Lines) { $process.StandardInput.WriteLine($line) }
    $process.StandardInput.Flush()
}
function Wait-Log([string]$Pattern, [int]$Seconds = 20) {
    $until = [DateTime]::UtcNow.AddSeconds($Seconds)
    do {
        if ((Read-GameLog) -match $Pattern) { return }
        if ($process.HasExited) { throw "Integration server stopped while waiting for $Pattern" }
        Start-Sleep -Milliseconds 250
    } while ([DateTime]::UtcNow -lt $until)
    throw "Timed out waiting for $Pattern in $log"
}

try {
    Wait-Log 'Done \([\d.]+s\)' 180
    Start-Sleep -Seconds 2
    Send-Console @('gamerule minecraft:spawn_mobs false', 'forceload add -16 -16 47 47',
        'difficulty peaceful', 'scoreboard objectives add m31 dummy', 'standandhold setstage 2')
    Start-Sleep -Seconds 5
    Send-Console @('difficulty normal', 'time set night', 'gamerule minecraft:advance_time false', 'jfr start')
    $placed = 0
    foreach ($count in 0, 1, 2, 4) {
        while ($placed -lt $count) {
            $x, $z = $posts[$placed]
            Send-Console "execute positioned $x -60 $z run standandhold structure checkpoint"
            Wait-Log "Placed CHECKPOINT at minecraft:overworld@$x,-60,$($z - 1)"
            Send-Console "execute positioned $x -60 $z run standandhold event reinforcement"
            $placed++
        }
        Write-Host "Warming $count posts"
        Start-Sleep -Seconds 65
        Send-Console "say M31_SAMPLE_BEGIN_$count"
        if ($count) {
            $x, $z = $posts[$count - 1]
            Send-Console "execute positioned $x -60 $z run standandhold event raid"
        }
        for ($sample = 1; $sample -le 5; $sample++) {
            Start-Sleep -Seconds 5
            Send-Console 'tick query'
        }
        Send-Console @('execute store result score m31_rifles m31 if entity @e[type=standandhold:army_rifleman]',
            'execute store result score m31_survivors m31 if entity @e[type=standandhold:survivor_defender]',
            'execute store result score m31_zombies m31 if entity @e[type=minecraft:zombie]',
            'scoreboard players get m31_rifles m31', 'scoreboard players get m31_survivors m31',
            'scoreboard players get m31_zombies m31', 'standandhold commandpost list')
        for ($index = 0; $index -lt $count; $index++) {
            $x, $z = $posts[$index]
            Send-Console @("execute positioned $x -60 $z run standandhold commandpost duty status",
                "execute positioned $x -60 $z run standandhold event status")
        }
        Send-Console "say M31_SAMPLE_END_$count"
        Wait-Log "M31_SAMPLE_END_$count"
        Write-Host "Captured $count posts"
    }
    Send-Console @('jfr stop', 'forceload remove -16 -16 47 47', 'save-all flush')
    Wait-Log 'Saved the game'
} finally {
    if (!$process.HasExited) {
        try { Send-Console 'stop' } catch { Write-Warning $_ }
        if (!$process.WaitForExit(90000)) {
            Write-Warning 'Disposable server did not stop on console request; stopping its process tree.'
            $process.Kill($true)
            $process.WaitForExit()
        }
    }
    [IO.File]::WriteAllText((Join-Path $server 'gradle.stdout.log'), $stdout.GetAwaiter().GetResult())
    [IO.File]::WriteAllText((Join-Path $server 'gradle.stderr.log'), $stderr.GetAwaiter().GetResult())
}
if ($process.ExitCode -ne 0) { throw "Integration server exit $($process.ExitCode); inspect $server" }
Read-Results $log
Write-Host "Load fixture stopped normally. Review $log and its JFR profile against the documented budgets."
