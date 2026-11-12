# Stand and Hold: Codex and Claude Handoff

Use this as a durable entry point, not a second roadmap. Read [AGENTS.md](AGENTS.md),
[STATUS.md](docs/modern-port/STATUS.md), then the relevant section of the
[phase roadmap](docs/modern-port/phase-roadmap.md). STATUS alone names the active
milestone and branch. Use Git and observed test reports to resolve stale notes.

## What Exists

- A modern NeoForge rewrite of the human resistance mod. The Forge 1.12.2 branch
  and legacy alpha tag are preserved, not migration targets.
- World progression, supplies, research, missions, Command Post/Lab/Crate menus,
  bounded defender deployments, patrols, raids, support call-ins, and saved state.
- Checkpoints and Main Bases, guarded opt-in checkpoint growth through the
  Level-3 deck, refusal/recovery receipts and loaded-only checks.
- Six human NPC tiers with original editable rigs, a shared Blade/Launcher
  equipment foundation, and native carry/aim/shot presentation. Super Elites
  are armoured melee defenders; Division operatives are mobile ranged
  specialists capped at one reservation per eligible Main Base.
  See [human NPCs](docs/modern-port/human-npcs.md) and the [art pipeline](docs/modern-port/art-pipeline.md).
- Optional threat rules use tags/registry IDs; vanilla zombies are the baseline.
  Do not infer external-mod support from a listing or an old build.

## First Run

1. Check `git status --short --branch`, `git fetch origin`, and
   `git rev-parse origin/1.12`. Expect
   `62bb07b84eb614b5967ef318ccb98aef1f22a5d8` for the preserved branch. Compare
   the assigned branch with its upstream and `origin/main`. Preserve any work
   already in progress; do not reset, rebase or clean another agent's files.
2. Check the active pins in [toolchain](docs/modern-port/toolchain.md) and
   `gradle.properties` (currently Minecraft 26.3, NeoForge 26.3.0.102, Java 25,
   Gradle 9.2.1, ModDevGradle 2.0.148). Do not upgrade pins or add a library to
   fix a setup issue.
3. From the repo root, use the Windows PowerShell build/test commands below.
   Run sequentially; stop on a failure and keep its log. The clean build includes
   logic tests. Inspect the jar's `META-INF/neoforge.mods.toml`; do not distribute
   a QA jar or the identically named legacy artifact.

```powershell
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-art.ps1 -Check
.\gradlew.bat clean build --no-daemon --stacktrace
.\gradlew.bat runGameTestServer --no-daemon --stacktrace --no-parallel
& .\scripts\test-restart.ps1
.\gradlew.bat runQaClient --no-daemon --stacktrace
```

4. Follow [automated testing](docs/modern-port/automated-testing.md) for reports,
   disposable client/server runs and screenshot review. Look for required test
   failures, bad side loading, wrong Java/metadata, missing models or textures,
   and unexpected save/config resets. Use [packaged testing](docs/modern-port/packaged-testing.md)
   when the gate needs the normal jar outside development runs. Do not accept an
   EULA, change a valuable world, or install a test mod without approval.
5. If a failure is real, reproduce it at the cheapest appropriate test level.
   Fix only the proven defect; do not weaken a test or erase data to get green.
   Record failures, skipped checks and limits separately from observed passes.

## Who Owns What

Claude and Codex are partners on this project; neither needs the other's
permission to work. Liam approves scope, numbered decisions, libraries, pins and
releases, and decides any disagreement.

- **Roadmaps:** Claude owns Roadmap 3 and the provisional future roadmaps in the
  [phase roadmap](docs/modern-port/phase-roadmap.md). Codex reviews roadmap
  changes and may propose alternatives on its own branch, as
  `codex/roadmap-3-input` did; the writer merges what holds up.
- **Milestones:** one writer per milestone branch, named
  `<agent>/m<N>-<short-name>` (`claude/` or `codex/`). Liam assigns the writer;
  by default the agents alternate. M32 is the worked example: Codex found a
  same-tick spacing gap and stale-record credit, Claude fixed both with GameTests.

## Pull Requests

Every change reaches `main` through a GitHub pull request, including docs-only
fixes; nobody pushes to `main` directly. Small docs PRs still get a review
comment, but it can be short.

1. **Open:** the writer opens a PR from its milestone branch once the
   automated part of the exit gate is green. The description lists what
   changed, the observed commands and results, what was not run, and anything
   Liam still has to check. Open it as a draft while Liam's check is pending.
2. **Review:** the other agent reviews the PR diff read-only and posts its
   findings as a PR comment, each with a file/line, a reproduction and the
   expected behaviour, ending with a clear verdict (merge, or fix first). Both
   agents push as Liam's account, so GitHub's "Approve" button is not
   available; the comment verdict is the approval.
3. **Fix:** the writer fixes real findings on the same branch with a test for
   each and replies on the PR. CI re-runs on every push.
4. **Merge:** Liam merges when the review verdict is "merge", CI is green and
   any check he asked for is done. He can tell the writer to merge instead.
   Use a merge commit to keep the milestone's history and evidence.
5. **Record:** the writer updates STATUS and the progress log in the PR itself,
   so `main` never carries an unreviewed status claim.

A useful optional setting for Liam: protect `main` in the GitHub repository
settings so it requires a pull request and the green "Build" check.

## Audit: Local Working Folders

Recorded so evidence can be traced; the PR is the coordination point. Never
share a live run directory. Codex runs local builds in
`C:\Users\liamr\Documents\ChatGPT\Stand and Hold`; Claude runs them in
`C:\Users\liamr\Documents\GitHub\Stand_and_Hold` through a small job runner in
the ignored `.local/claude/` folder (`start-runner.cmd`, fixed build/test tasks
only; `play-m32.cmd` opens a development client). Windows CI runs the build,
GameTests and two-JVM restart on every push and PR.

## Next Work

STATUS names the active milestone. After the M32 PR merges, Roadmap 3
continues with M33-M37, then the provisional Roadmaps 4-7. Fabric, dependencies and release
decisions remain separate approvals.

## Future Experiment: Agent Meeting

Separate project, not a mod dependency or an active assignment. Today Liam relays
reviews between the agents, which works. Keep that human-mediated review: share a commit, the question and redacted evidence; record
each agent's recommendation, disagreement and Liam's decision. Only consider a
website/API after that workflow proves useful. A small coordinator could hand
each model the same read-only packet, run a bounded review round and save a
decision record. Keep credentials server-side, require approval for edits or
publishing, and make stopping the session explicit. A later `SKILL.md` can
capture the proven procedure. This file does not connect or call another model.
