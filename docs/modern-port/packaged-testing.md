# Isolated Packaged Testing

This procedure tests the **normal jar outside Gradle**, not the separate QA mod. Use fresh modern worlds only. Minecraft 26.3, NeoForge 26.3.0.57-beta and Java 25 remain pinned; no extra mod is required. The identically named historical Forge 1.12.2 release is not this artifact.

For the isolated `codex/m30-neoforge-102-spike` worktree only, the pack helper
and manifest require `26.3.0.102` instead of the main pin. Verify the trial
version against that worktree's new manifest and never reuse the historical
M22 profile or world. The trial has not reached production `Done` or launcher
world entry; successful pack preparation does not establish either.

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

Only install once in that fresh server directory. For subsequent launches, select Java 25, enter the recorded server directory and run `run.bat nogui` without reinstalling. Keep `server-ip=127.0.0.1`, port `25586`, `online-mode=true`, and only the normal jar in `mods/`. The private console fixture sets `pause-when-empty-seconds=-1` so loaded blocks keep ticking without a player; this is not a mod gameplay default. Do not connect real players or expose this fixture to the network. Retain platform installation logs.

**By default, stop at the EULA request and hand it to Liam.** Do not copy an accepted file from another instance, use an acceptance property/flag, or interpret past development-server success as consent. The setup helper never writes acceptance. M22's one explicitly delegated acceptance is recorded below, not a reusable auto-accept policy. An EULA refusal is not Done/server proof.

After acceptance, reach `Done` within a bounded startup wait (normally allow up to 3 minutes). Through its console, record `standandhold status`, add 150 points, check Stage 1, add a small supply balance and create a loaded Command Post/Lab/Crate fixture with explicit coordinates. Allow loaded ticks for queued NeoForge block initialization before inspecting registration; freezing or vanilla empty-server pausing can otherwise delay it. Inspect building NBT through vanilla `data get block`. Save with `save-all flush`, then `stop`, observe clean exit, and restart the same world in a new JVM. Recheck points/stage/supplies and unchanged building storage/target/registration. Temporary fixture-only `forceload` tickets may keep these few chunks loaded; remove them before final stop. Never add production chunk-loading logic for the test. A missing Done, refused command, crash or timeout is a failed/blocked check, not a pass.

## Evidence Boundaries

The helper's CI checks are offline and never invoke a launcher, installer or EULA acceptance. CI GameTests and the two-JVM source fixture remain separate from real packaged proofs. Preserve local logs privately and redact account names/tokens before sharing anything; do not publish a whole launcher directory.

On 2026-11-02, the first M22 session was `m22-20261102-143650-32816cbd`. Its 409,863-byte normal jar had SHA256 `5dddffba3c052414dc29c8d7f665bd46542799b29801756314c227da007a119a`; build, installed client and installed server copies matched. The official installer hash was `9f3b8ee9309883c708b037255cb5ec5a762c138926422304b4825f0ebb77d797`, and server installation completed successfully. Initial Modrinth Microsoft sign-in failure and the first server's EULA refusal were retained, not promoted to launch passes. Liam subsequently signed in himself and explicitly authorized the agent to accept the EULA on his behalf for retesting. Only that session's existing `server/eula.txt` was changed to true; no credentials were read and no acceptance switch was added to scripts. The directory is under the checkout's `.local/packaged-qa/`, not `run/modern-server/`.

### Observed Normal-Jar Proof

- **Production server:** two separate Java 25 JVMs using the installed argument files reached `Done`, accepted console commands, saved and stopped with exit 0. `server-write.log` and `server-read.log` retain the evidence in the session directory. Both logged `DEDICATED_SERVER in PROD`, exact platform versions and only Minecraft/NeoForge/Stand and Hold. The second JVM retained 150 points/Stage 1, 37 global supplies, the post at `(2,-60,2)` with Level 2/23 supplies/2 samples, paused Lab at `(4,-60,2)` with 7 supplies/1 sample/target `standandhold:parasite_samples`/work 4, and Crate at `(6,-60,2)` with 13 supplies/3 samples. One loaded Command Post registered; the one fixture chunk ticket was removed and normal tick flow restored before final stop.
- **Real launcher client:** Modrinth loaded the same normal jar with Java 25 and logged `CLIENT in PROD`, exact pins and only those three mods. Mods showed Stand and Hold 0.1.0-alpha.1. A new Creative Superflat `M22 Packaged Smoke` world was entered, saved through the normal menu and reopened. Its generated checkpoint at `(8,-60,-9)` registered and all three original menus opened. The guide, original sample icon/name, supply import, sample Store and automatic deployment were exercised; the post showed 3/3 assigned defenders, not unlimited spawning.
- **Client transactions/reload:** a labelled 20-supply grant funded a 10-supply Lab import. Selection did not auto-start; Start, a pause at 8/12, resume and 12/12 Completed worked. Exactly one rendered completion message appeared. After reopening, the Lab still showed 24 points/Stage 0, global supplies 0 and Completed with local supplies 0. A read-only crate NBT query retained exactly 12 supplies and 10 stored sample items. The ten samples were explicitly granted for storage QA, not claimed as earned drops. `client-smoke-reload.log` and the private launcher world retain this proof.
- **Follow-on probe:** the local threat record showed one human/player-credited mapped kill after a low-health zombie was summoned under cover. A defender intercepted it; a separate player-only kill or random sample drop is not claimed from this probe. A later Creative test click broke the post; a manual replacement was placed only after the recorded reload checks. The retained world is an operator fixture, not a clean earned playthrough or a surviving generated-site provenance test. Exact kill/drop/reward and targeting rules remain covered separately by native GameTests.
- **Screenshots:** all ten original PNGs under `%APPDATA%/ModrinthApp/profiles/standandhold-packaged-qa/screenshots/` were opened and reviewed: `2026-11-02_15.32.00.png` (menu), `15.32.51` (Mods), `15.38.24` (site/Survivor), `15.42.14` (paused Lab), `15.45.15` (Crate), `15.47.47` (stored samples), `15.49.41` (post/cap), `15.51.35` (completed Lab), `15.54.12` (completed after reload) and `15.56.19` (reloaded site). The shortened names use the same date prefix and `.png`. The two attempted transient chat captures show the world after chat faded, not standalone guide/data evidence; logs retain their text. Vanilla's first-use tutorial overlapped part of the small-window header initially and disappeared on reload; mod body text, controls and original textures were readable.

Non-blocking startup diagnostics included Windows performance-counter/optional epoll messages and one unknown-passenger warning on client reload. Both production starts reached Done and normal client entry/reload completed; no OS settings, loader pins or mod behaviour were changed to suppress diagnostics. No real multi-client session, new earned-loop completion, external threat combination, other launcher/OS or public-release approval is inferred from this baseline.
