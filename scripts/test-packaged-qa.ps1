$ErrorActionPreference = 'Stop'
$prepare = Join-Path $PSScriptRoot 'prepare-packaged-qa.ps1'
$repo = Split-Path $PSScriptRoot -Parent
$id = 'm22-check-' + [guid]::NewGuid().ToString('N')
$result = & $prepare -SessionId $id
$manifest = Get-Content -LiteralPath $result.Manifest -Raw | ConvertFrom-Json
function Assert($condition, $message) { if (-not $condition) { throw $message } }
function Refuses([scriptblock]$action) {
    $refused = $false
    try { & $action | Out-Null } catch { $refused = $true }
    Assert $refused 'Expected preparation to refuse before overwriting anything.'
}
Assert ($manifest.minecraft -eq '26.3' -and $manifest.neoForge -eq '26.3.0.102') 'Exact platform pins retained.'
Assert ([IO.Path]::GetFileName($result.ClientPack).Contains($id)) 'Each pack filename must identify its fresh session.'
Assert ($manifest.artifactSHA256 -eq (Get-FileHash -LiteralPath (Join-Path $result.ServerDirectory 'mods/standandhold-0.1.0-alpha.1.jar') -Algorithm SHA256).Hash.ToLowerInvariant()) 'Normal server jar checksum matches.'
Assert (-not (Test-Path -LiteralPath (Join-Path $result.ServerDirectory 'eula.txt'))) 'Preparation must not write an EULA.'
$properties = Get-Content -LiteralPath (Join-Path $result.ServerDirectory 'server.properties') -Raw | ConvertFrom-StringData
Assert ($properties.'server-ip' -eq '127.0.0.1' -and $properties.'server-port' -eq '25586' -and $properties.'online-mode' -eq 'true') 'Loopback-only server with normal authentication.'
Assert ($properties.'pause-when-empty-seconds' -eq '-1') 'Private console fixture must keep loaded chunks ticking without players.'
$pack = [IO.Compression.ZipFile]::OpenRead($result.ClientPack)
try {
    Assert ($pack.Entries.Count -eq 3) 'Pack must contain only index, marker and our mod.'
    $reader = [IO.StreamReader]::new($pack.GetEntry('modrinth.index.json').Open())
    try { $index = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    Assert ($index.dependencies.minecraft -eq $manifest.minecraft -and $index.dependencies.neoforge -eq $manifest.neoForge -and @($index.files).Count -eq 0) 'No unpinned dependencies or extra mod downloads.'
    $reader = [IO.StreamReader]::new($pack.GetEntry('overrides/m22-packaged-qa.json').Open())
    try { $marker = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    Assert ($marker.sessionId -eq $id -and $marker.artifactSHA256 -eq $manifest.artifactSHA256) 'Imported client marker identifies the same session and artifact.'
    $stream = $pack.GetEntry('overrides/mods/standandhold-0.1.0-alpha.1.jar').Open()
    $sha = [Security.Cryptography.SHA256]::Create()
    try { $packedHash = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '').ToLowerInvariant() } finally { $stream.Dispose(); $sha.Dispose() }
    Assert ($packedHash -eq $manifest.artifactSHA256) 'Pack contains the exact normal jar, not sources/QA.'
} finally { $pack.Dispose() }
$before = (Get-FileHash -LiteralPath $result.Manifest -Algorithm SHA256).Hash
Refuses { & $prepare -SessionId $id }
Assert ((Get-FileHash -LiteralPath $result.Manifest -Algorithm SHA256).Hash -eq $before) 'Existing manifest retained byte-for-byte.'
Refuses { & $prepare -SessionId '../outside' }
Refuses { & $prepare -SessionId ($id + '-badjar') -Artifact (Join-Path $repo 'gradle/wrapper/gradle-wrapper.jar') }
Refuses { & $prepare -SessionId ($id + '-badhash') -ExpectedArtifactSHA256 ('0' * 64) }
Assert (-not (Test-Path -LiteralPath (Join-Path $repo ('.local/packaged-qa/' + $id + '-badjar')))) 'Invalid jar must not create a session.'
Assert (-not (Test-Path -LiteralPath (Join-Path $repo ('.local/packaged-qa/' + $id + '-badhash')))) 'Invalid hash must not create a session.'
Write-Host 'Packaged preparation checks passed; test outputs retained. No installer, EULA or launcher invoked.'
