[CmdletBinding()]
param(
    [ValidatePattern('^m22-[a-z0-9-]{8,64}$')]
    [string]$SessionId = ('m22-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8)),
    [string]$Artifact,
    [ValidatePattern('^[a-fA-F0-9]{64}$')]
    [string]$ExpectedArtifactSHA256,
    [switch]$DownloadInstaller
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$pins = Get-Content -LiteralPath (Join-Path $repo 'gradle.properties') -Raw | ConvertFrom-StringData
if ($pins.minecraft_version -ne '26.3' -or $pins.neo_version -ne '26.3.0.102' -or $pins.mod_version -ne '0.1.0-alpha.1') {
    throw 'This M22 helper needs review if the approved Minecraft, NeoForge or mod version changes; no fallback is selected.'
}
if (-not $Artifact) { $Artifact = Join-Path $repo "build/libs/standandhold-$($pins.mod_version).jar" }
$Artifact = (Resolve-Path -LiteralPath $Artifact).ProviderPath
$hash = (Get-FileHash -LiteralPath $Artifact -Algorithm SHA256).Hash.ToLowerInvariant()
if ($ExpectedArtifactSHA256 -and $hash -ne $ExpectedArtifactSHA256.ToLowerInvariant()) { throw 'Normal artifact checksum differs from the expected build/CI artifact.' }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = [IO.Compression.ZipFile]::OpenRead($Artifact)
try {
    $metadata = $jar.GetEntry('META-INF/neoforge.mods.toml')
    if (-not $metadata) { throw 'Not the modern NeoForge mod jar; the same-named legacy release is not accepted.' }
    $reader = [IO.StreamReader]::new($metadata.Open())
    try { $actual = $reader.ReadToEnd() } finally { $reader.Dispose() }
    $expected = Get-Content -LiteralPath (Join-Path $repo 'src/main/templates/META-INF/neoforge.mods.toml') -Raw
    foreach ($key in $pins.Keys) { $expected = $expected.Replace(('${' + $key + '}'), [string]$pins[$key]) }
    # Validate the complete known template, not a partial TOML parse that could miss a second mod.
    if ($actual.Replace("`r`n", "`n").Trim() -ne $expected.Replace("`r`n", "`n").Trim()) { throw 'Normal-jar metadata does not match the approved expanded template.' }
    if (@($jar.Entries | Where-Object { $_.FullName -match '(^|/)(qa|standandhold_qa|progressionTest)/|^standandhold-qa-tests\.txt$|\.bbmodel$|\.jar$' }).Count) {
        throw 'QA, authoring or bundled-jar content found in the normal artifact.'
    }
    if (-not $jar.GetEntry('com/liamryan/standandhold/StandAndHold.class')) { throw 'Normal mod entry point missing.' }
} finally { $jar.Dispose() }

$base = Join-Path $repo '.local/packaged-qa'
$session = [IO.Path]::GetFullPath((Join-Path $base $SessionId))
if (-not $session.StartsWith(([IO.Path]::GetFullPath($base) + [IO.Path]::DirectorySeparatorChar), [StringComparison]::OrdinalIgnoreCase)) { throw 'Session must stay inside .local/packaged-qa.' }
if (Test-Path -LiteralPath $session) { throw "Refusing an existing session/world/profile directory: $session" }
$null = New-Item -ItemType Directory -Path $base -Force
$null = New-Item -ItemType Directory -Path $session
$server = Join-Path $session 'server'
$null = New-Item -ItemType Directory -Path (Join-Path $server 'mods')
$modName = "standandhold-$($pins.mod_version).jar"
Copy-Item -LiteralPath $Artifact -Destination (Join-Path $server "mods/$modName")

$utf8 = [Text.UTF8Encoding]::new($false)
$profileName = 'Stand and Hold ' + $SessionId
$marker = [ordered]@{ type = 'standandhold-packaged-qa'; version = 1; sessionId = $SessionId; artifactSHA256 = $hash }
$index = [ordered]@{
    formatVersion = 1; game = 'minecraft'; versionId = $SessionId; name = $profileName
    summary = 'Private normal-jar QA fixture; not a published modpack or earned playthrough.'
    files = @(); dependencies = [ordered]@{ minecraft = $pins.minecraft_version; neoforge = $pins.neo_version }
}
# A unique filename also avoids reusing a launcher's filename-derived profile identifier.
$pack = Join-Path $session ("standandhold-$SessionId.mrpack")
$archive = [IO.Compression.ZipFile]::Open($pack, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($entry in @(
        @{ path = 'modrinth.index.json'; value = ($index | ConvertTo-Json -Depth 6) },
        @{ path = 'overrides/m22-packaged-qa.json'; value = ($marker | ConvertTo-Json) }
    )) {
        $writer = [IO.StreamWriter]::new($archive.CreateEntry($entry.path).Open(), $utf8)
        try { $writer.Write($entry.value) } finally { $writer.Dispose() }
    }
    $null = [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $Artifact, "overrides/mods/$modName")
} finally { $archive.Dispose() }

# Generated only in this fresh, private QA server. No account data or EULA flag is copied.
$properties = @(
    'server-ip=127.0.0.1', 'server-port=25586', 'online-mode=true', 'max-players=2',
    'level-name=m22-packaged', 'level-type=minecraft:flat', 'generate-structures=false',
    'view-distance=2', 'simulation-distance=2', 'gamemode=creative', 'pause-when-empty-seconds=-1',
    'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}'
) -join "`n"
[IO.File]::WriteAllText((Join-Path $server 'server.properties'), $properties + "`n", $utf8)
[IO.File]::WriteAllText((Join-Path $server 'm22-packaged-qa.json'), ($marker | ConvertTo-Json), $utf8)

$installerURL = 'https://maven.neoforged.net/releases/net/neoforged/neoforge/' + $pins.neo_version + '/neoforge-' + $pins.neo_version + '-installer.jar'
$installer = Join-Path $session ('neoforge-' + $pins.neo_version + '-installer.jar')
$installerHash = $null
if ($DownloadInstaller) {
    $checksum = (Invoke-WebRequest -UseBasicParsing -Uri ($installerURL + '.sha256') -TimeoutSec 60).Content
    if ($checksum -is [byte[]]) { $checksum = [Text.Encoding]::UTF8.GetString($checksum) }
    $checksum = ([string]$checksum).Trim()
    if ($checksum -notmatch '^[a-fA-F0-9]{64}$') { throw 'Official installer SHA256 response is malformed; do not execute the download.' }
    Invoke-WebRequest -UseBasicParsing -Uri $installerURL -OutFile $installer -TimeoutSec 120
    $installerHash = (Get-FileHash -LiteralPath $installer -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($installerHash -ne $checksum.ToLowerInvariant()) { throw 'Installer checksum mismatch; do not execute it. Failed session retained.' }
}
$commit = (& git -C $repo rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) { throw 'Cannot identify the source commit for the local manifest.' }
$manifest = [ordered]@{
    type = $marker.type; version = 1; sessionId = $SessionId; createdUTC = [DateTime]::UtcNow.ToString('o')
    sourceCommit = $commit; minecraft = $pins.minecraft_version; neoForge = $pins.neo_version; javaMajor = 25
    modVersion = $pins.mod_version; artifact = $Artifact; artifactSHA256 = $hash; artifactBytes = (Get-Item -LiteralPath $Artifact).Length
    profileName = $profileName; clientPack = $pack; clientPackSHA256 = (Get-FileHash -LiteralPath $pack -Algorithm SHA256).Hash.ToLowerInvariant()
    serverDirectory = $server; serverPort = 25586; installer = $installer; installerURL = $installerURL; installerSHA256 = $installerHash
    eulaPolicy = 'Only Liam accepts the EULA; this helper never writes eula.txt or supplies an acceptance flag.'
}
$manifestFile = Join-Path $session 'manifest.json'
[IO.File]::WriteAllText($manifestFile, ($manifest | ConvertTo-Json -Depth 6), $utf8)
Write-Host "Prepared private packaged QA session: $session"
[pscustomobject]@{ SessionDirectory = $session; Manifest = $manifestFile; ClientPack = $pack; ServerDirectory = $server; Installer = $installer }
