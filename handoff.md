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
   `gradle.properties`. Do not upgrade pins or add a library to fix a setup issue.
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

## Alternating Ownership

- One writer per milestone branch. Liam assigns the next writer. The other
  agent can review a specific commit read-only while implementation is underway;
  use a separate worktree if it needs to run builds. Never share a live run
  directory, edit the same branch concurrently, or publish a second competing fix.
- Hand over a pushed branch/commit, observed commands/results and CI link,
  unresolved decisions, and ignored evidence locations. Put durable evidence in
  the existing progress log, update STATUS, and point the next agent there.
  Do not copy credentials, private logs, worlds or local profiles into Git.
- The receiving agent checks the actual diff and evidence, then takes the
  agreed milestone. Findings should name a file, reproduction and expected
  behavior. If the agents disagree on scope or a gate, Liam decides. Do not
  declare a green merge before the roadmap exit gate and required CI pass.

## Proposed Work Lanes

Use the numbered roadmap and STATUS to assign ownership. A useful split is one
agent owning a bounded implementation milestone while the other reviews saved
state, server authority, and test gaps, then swap for the following milestone.
Roadmap 2 connects specialist recruitment, an inhabited Main Base layout,
optional threat coexistence or explicit deferral, and an integrated alpha
candidate. Beyond that gate, propose one original bounded slice at a time
from Deferred Expansion Admission. Fabric, dependencies and release decisions
remain separate approvals.

[Codex's Roadmap 3 proposal](docs/modern-port/roadmap-3-codex-proposal.md)
is advisory input for Claude's independent planning pass, not the official roadmap.

## Future Experiment: Agent Meeting

Separate project, not a mod dependency or an active assignment. Start with a
human-mediated review: share a commit, the question and redacted evidence; record
each agent's recommendation, disagreement and Liam's decision. Only consider a
website/API after that workflow proves useful. A small coordinator could hand
each model the same read-only packet, run a bounded review round and save a
decision record. Keep credentials server-side, require approval for edits or
publishing, and make stopping the session explicit. A later `SKILL.md` can
capture the proven procedure. This file does not connect or call another model.
