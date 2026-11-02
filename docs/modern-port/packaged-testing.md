# Isolated Packaged Testing

This procedure tests the **normal jar outside Gradle**, not the separate QA mod. Use fresh modern worlds only. Minecraft 26.3, NeoForge 26.3.0.57-beta and Java 25 remain pinned; no extra mod is required. The identically named historical Forge 1.12.2 release is not this artifact.

## Prepare A Fresh Session

From the repository root:

```powershell
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat clean build --no-daemon --stacktrace
if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-packaged-qa.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\prepare-packaged-qa.ps1 -DownloadInstaller
```

The helper creates a uniquely named `.local/packaged-qa/m22-.../` directory, never reuses one, and retains failures. Its `manifest.json` records exact versions, source commit, normal-jar SHA256/size, private pack checksum, installer URL/checksum, profile name and server directory. An optional `-Artifact <path> -ExpectedArtifactSHA256 <sha256>` selects a separately verified CI artifact. The helper checks the entire expanded mod metadata template, entry point and absence of QA classes, bundled jars and authoring models. It does not launch/install software, handle credentials, write an EULA or publish a pack.

The generated `.mrpack` contains only its format index, a session marker and our normal jar. Each new pack filename includes its session ID to avoid a launcher's filename-derived profile collision. Inspect the pack and manifest yourself before import. The first M22 session predates that filename improvement and retains its original `standandhold-packaged-qa.mrpack`; do not overwrite or reimport it over an existing instance.

## Client Profile

1. In an existing launcher, import the private pack as a **new** instance; confirm its unique name and a new game directory. Never import over a valued profile. Inspect any trust warning personally; do not disable warnings. The pack uses the official [Modrinth format](https://support.modrinth.com/en/articles/8802351-modrinth-modpack-format-mrpack).
2. Confirm Minecraft **26.3**, NeoForge **26.3.0.57-beta**, Java **25**, exactly one `standandhold-0.1.0-alpha.1.jar` in `mods/`, and no QA/test mod. Compare the imported jar's SHA256 with the manifest using `Get-FileHash -Algorithm SHA256`. Do not copy account files, development configs, saves or EULA flags.
3. Launch using the normal launcher. Account/authentication and security prompts belong to Liam. Do not bypass login, update the launcher to hide a failure, or inspect credentials. The [official NeoForge client guide](https://docs.neoforged.net/user/docs/client/) also describes separate client profiles/installations.
4. Inspect Mods for Stand and Hold 0.1.0-alpha.1. Enter a new Creative Superflat world with commands enabled. Run `/standandhold guide` and `status`; generate one checkpoint on clear grass. Open its Command Post, Lab and Crate. Check sample and defender assets, funded Lab Start/Pause/completion and one real zombie kill/drop/defender interaction. Operator fixture grants are technical smoke tests, not an earned playthrough.
5. Save/quit, reopen **the same** world, and confirm progression, research, storage and registration. Capture F2 views of Mods, guide, the three menus, site and defenders, and inspect the PNGs. Fully quit normally; retain logs/screenshots/world. Record actual outcomes rather than inferring success from an import or source QA run.

## Production Dedicated Server

Use the generated manifest, not a guessed path. The installer download is checked against the official Maven SHA256 before execution. The official [NeoForge server guide](https://docs.neoforged.net/user/docs/server/) describes `--installServer`, Windows `run.bat`, initial EULA handling and the `mods` directory.

```powershell
& .\scripts\use-modern-java.ps1
$session = '<absolute fresh session directory printed by the helper>'
$manifest = Get-Content -LiteralPath (Join-Path $session 'manifest.json') -Raw | ConvertFrom-Json
if (-not $manifest.installerSHA256) { throw 'Prepare a checksum-verified installer first' }
if ((Get-FileHash -LiteralPath $manifest.installer -Algorithm SHA256).Hash.ToLowerInvariant() -ne $manifest.installerSHA256) { throw 'Installer changed' }
Push-Location $manifest.serverDirectory
try {
    & "$env:JAVA_HOME/bin/java.exe" -jar $manifest.installer --installServer
    if ($LASTEXITCODE -ne 0) { throw 'Installation failed; retain logs' }
    .\run.bat nogui
} finally { Pop-Location }
```

Only install once in that fresh server directory. For subsequent launches, select Java 25, enter the recorded server directory and run `run.bat nogui` without reinstalling. Keep `server-ip=127.0.0.1`, port `25586`, `online-mode=true`, and only the normal jar in `mods/`. Do not connect real players or expose this fixture to the network. Retain platform installation logs.

**If startup requests the EULA, stop. Only Liam reviews it and edits that new server's `eula.txt` himself.** Do not copy an accepted file from another instance, use an acceptance property/flag, or interpret past development-server success as consent. An EULA refusal is not Done/server proof.

After acceptance, reach `Done` within a bounded startup wait (normally allow up to 3 minutes). Through its console, record `standandhold status`, add 150 points, check Stage 1, add a small supply balance and create a loaded Command Post/Lab/Crate fixture with explicit coordinates. Inspect building NBT through vanilla `data get block`. Save with `save-all flush`, then `stop`, observe clean exit, and restart the same world in a new JVM. Recheck points/stage/supplies and unchanged building storage/target/registration. Temporary fixture-only `forceload` tickets may keep these few chunks loaded; remove them before final stop. Never add production chunk-loading logic for the test. A missing Done, refused command, crash or timeout is a failed/blocked check, not a pass.

## Evidence Boundaries

The helper's CI checks are offline and never invoke a launcher, installer or EULA acceptance. CI GameTests and the two-JVM source fixture remain separate from real packaged proofs. Preserve local logs privately and redact account names/tokens before sharing anything; do not publish a whole launcher directory.

On 2026-11-02, the first M22 session was `m22-20261102-143650-32816cbd`. Its 409,863-byte normal jar had SHA256 `5dddffba3c052414dc29c8d7f665bd46542799b29801756314c227da007a119a`; the installed client copy matched. The official installer hash was `9f3b8ee9309883c708b037255cb5ec5a762c138926422304b4825f0ebb77d797`, and server installation completed successfully. Liam imported the new Modrinth profile and approved its launch; Modrinth then displayed **Sign in Failed / Unknown error before Minecraft opened**. No account interaction or packaged client entry/reload pass is claimed.

First server boot used Java 25 and the two argument files from the installed `run.bat`, without its trailing interactive `pause`. `server-first-boot.log` records **DEDICATED_SERVER in PROD**, exactly Minecraft/NeoForge/Stand and Hold in the mod list, and the new EULA request. It exited 1 before Done and generated `server/eula.txt` with `eula=false`, left untouched. The loader then cancelled background scanning and reported that the server thread never started; this is retained as part of the EULA-aborted startup, not claimed as an independently reproduced gameplay crash. No packaged world/restart proof exists yet. Authentication and EULA are explicit environment prerequisites, not evidence of a mod defect or permission to merge an incomplete milestone.
