# AGENTS.md — Stand and Hold

Stand and Hold is a Minecraft mod (human military response to infection-style threats).
The Forge 1.12.2 alpha is preserved; the active work is a clean NeoForge rewrite.

## Read first, and only this

1. `docs/modern-port/STATUS.md` — current position, next milestone, open risks. **The only file that says what's next.**
2. `docs/modern-port/phase-roadmap.md` — the section for the current phase only.
3. Reference docs (`toolchain.md`, `building.md`, `foundation-scope.md`, etc.) only when the task needs them.

Do not read the whole `docs/` folder or the legacy root README at the start of a session.

## Branches and things you must never touch

- `1.12` branch and tag `v0.1.0-alpha.1`: preserved legacy. Never commit to, rebase, delete or force-push them.
- One work branch per phase, cut from `main`: `codex/m<N>-<short-name>` (e.g. `codex/m2-progression`).
  Merge into `main` when the phase exit gate passes with observed evidence and CI is green, then start
  the next phase's branch from the updated `main` in the same session.
- Never force-push, rewrite published history, or delete untracked/ignored user files (worlds, `.local/`, run dirs).

## Toolchain (pinned — see docs/modern-port/toolchain.md)

Minecraft 26.3 · NeoForge 26.3.0.57-beta · Java 25 · Gradle 9.2.1 · ModDevGradle 2.0.148.
Windows / PowerShell. Before any modern Gradle command, in the same shell:

```powershell
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
```

## How to work

**Unit of work = a milestone, not a sub-step.** A milestone is **a whole phase** (e.g. all of M2:
2.1–2.4). If only a small remainder of the current phase is left (2 steps or fewer, or just
merge/CI/admin work), the milestone is that remainder **plus the whole next phase**.

When asked to "continue" or given a milestone, work through every consecutive roadmap step until
one of these happens:

1. The milestone's exit gate is reached (build/run evidence observed), or
2. A real blocker you cannot fix after a reasonable attempt (record it, stop), or
3. A decision in the "Ask Liam first" list below.

Do not stop just because one numbered step finished. Do not ask "shall I continue?".

**In-game checks don't end the run.** If a step needs Liam to look at something in game, do
everything else that doesn't depend on it, then list the checks together in the final report.
Only wait mid-run if later steps genuinely depend on the result.

**Session start (once, keep it cheap):** `git status`, `git fetch`, and
`git rev-parse origin/1.12` (expect `62bb07b84eb614b5967ef318ccb98aef1f22a5d8`).
Do not re-verify gh auth, JDK metadata, the alpha tag, `git fsck`, or remote permissions
unless something actually looks wrong.

## Ask Liam first (stop and say why)

- Changing any toolchain pin, or falling back to 26.2 / another target (follow the fallback rule in `building.md`).
- Adding any runtime dependency or library (policy: `foundation-scope.md`).
- Accepting the Minecraft EULA — Liam does this himself in `run/modern-server/eula.txt`.
- Anything that would touch `1.12`, the alpha tag, or delete user files.

Everything else on the roadmap is pre-approved: make the change, verify it, commit it.

## Verification — what counts

- Code or build change → `.\gradlew.bat clean build --no-daemon --stacktrace` must pass. Inspect the jar's `neoforge.mods.toml`.
- Server proof → `runServer` reaches "Done", accepts `stop`, exits cleanly.
- Client proof → `runClient` loads to the main menu with the mod in the Mods list (check `latest.log`).
- Docs-only change → `git diff --check`. No link counting, task-ID counting or handoff counting.
- Never report a build or launch as passed without observing it. Say plainly what was not run.

### Test it yourself first — Liam is the last check, not the first

Use the cheapest level that can prove the behaviour, and do it yourself before asking Liam:

1. **Logic** → `progressionTest` checks (run by `build`).
2. **Commands, saves, restarts** → drive `runServer` through its console: type the commands, read the
   log, `stop`, restart, confirm state persisted.
3. **In-world behaviour** (blocks, block entities, NPC targeting/combat, drops, raids/spawns,
   structures, vehicles, chunk/restart edge cases) → write a **GameTest** and run
   `.\gradlew.bat runGameTestServer`. A non-zero exit means failed required tests. Every new
   in-world feature gets at least one GameTest; CI runs them too.
4. **Screens, textures, text fit, models** → the `qaClient` run: it auto-loads a disposable test
   world, runs the scripted QA steps, saves screenshots to `run/qa-client/screenshots/` and quits.
   Open the PNGs yourself and judge them; quote the file names in the report.

GameTests and QA hooks live outside the normal jar and add no dependency (Minecraft/NeoForge
built-ins only). Never weaken a failing test to make it pass; fix the code or report it.

**Only hand Liam what automation can't judge:** feel/fun/balance, a full earned playthrough,
animation in motion, real multiplayer, packaged-launcher installs. Never ask Liam to type a command
sequence and read back numbers: that is level 2 or 3, so do it yourself.

**Liam's check comes last, and it never blocks you:**
- Finish everything first: final clean build, GameTests, CI and push. Then open one client for him
  and leave it running. Never ask him to quit the game so you can build.
- Make it short: at most about 5 numbered checks, each saying which automated level already
  covered the mechanics. End with: "Play for about 5 minutes. Does anything feel off or annoying?"
  His design feedback is the most valuable part of a manual check.
- If he reports a bug, first reproduce it in a GameTest or server fixture, then fix it. Only
  ask him to retest what automation still can't see.
- If a final test question remains unresolved after automation and reasonable investigation,
  ask Liam one focused question as the last step. Say what was checked and what remains unknown;
  include the exact commands/actions, target, range and prerequisites needed to answer it.
  This is a last resort, not a replacement for automatable checks or routine command readbacks.

## Documentation rules (keep overhead low)

- `STATUS.md` holds the current position and next milestone. **No other file repeats it.**
  Reference docs never get "Current position / Next step" lines. **Keep it under ~25 short lines**:
  replace old bullets rather than adding to them. History goes in `progress-log.md`.
- Roadmap: tick boxes. Add a sub-task only *before* doing work that genuinely needs splitting —
  never retroactively, never deeper than `x.y.z`.
- `progress-log.md`: append one entry per session, **max 8 lines**: what changed, what was
  verified (commands actually run + result), what wasn't, blockers, next milestone.
- Don't create a new evidence document per step. A doc earns a place only if a future developer needs it.
- Don't record a commit's own hash inside that commit; Git history is the record.
- A typical step should touch the files it changes plus `STATUS.md`, the roadmap and the log — not 11–15 files.

## Commits

- Conventional prefixes: `build:`, `feat:`, `fix:`, `docs:`, `ci:`, `chore:`.
- One commit per logical change; push the work branch at least at the end of each milestone.

## Hard design rules (unchanged)

- Never copy code, assets, models or textures from other mods.
- Threat mods stay optional (tags / registry IDs / config); never a hard dependency.
- Server-authoritative state; client classes never load on a dedicated server.
- No legacy world/config/NBT conversion; test in fresh modern worlds only.
- Keep the build compiling before adding systems; bounded ticking, no global scans.

## Final report (every session, max ~10 lines)

Done · Verified (commands + result) · Not verified · In-game checks for Liam · Blockers / decisions for Liam · Next.

**The "Next" line is always a milestone (step range), never a single step**, and is written as a
ready-to-paste prompt, for example:

> Next: `Continue: M2 steps 2.1–2.4 (progression definitions, SavedData, commands, verification).`

Use the same range in `STATUS.md`. The only exceptions: the next item is blocked on Liam, or it
is the final step of the whole roadmap.
