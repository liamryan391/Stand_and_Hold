# Phase 31 Alpha QA Session 001

Use this file for the first Phase 31 alpha QA pass. Keep it as a practical test record, not a release announcement.

## Phase 31 Build Proof Note

- Local Java 8 clean build passed before this QA session.
- Confirm the normal jar exists at `build/libs/standandhold-0.1.0-alpha.1.jar`.
- Do not test with `build/libs/standandhold-0.1.0-alpha.1-sources.jar`.
- GitHub Actions queueing is currently pending due to a GitHub/account/repository Actions issue. This should not block local alpha QA while local Java 8 build proof is passing.

## Session Details

- Date:
- Tester name:
- OS:
- Java version:
- Minecraft version:
- Forge version:
- Stand and Hold jar filename:
- Scape and Run: Parasites installed: Yes / No
- SRP version, if installed:

## Mod List

```text
Paste the tested mod list here.
```

## Config Changes

```text
List changes made to config/standandhold.cfg, or write "None".
```

## Test Result Summary

- Overall result: Pass / Fail / Partial / Blocked
- Summary:
- Blocker count:
- Non-blocking issue count:

## Pass/Fail Table

| Area | Test | Result | Notes |
| --- | --- | --- | --- |
| Build | Java 8 build completed | Passed locally before session | Confirm path and timestamp |
| Launch | Main menu opens | Not tested | |
| Launch | Mod appears in Mods list | Not tested | |
| World | New world loads | Not tested | |
| World | Existing world reloads | Not tested | |
| Saved Data | Existing pre-Phase 32 world migrates to saved data version 1 | Not tested | |
| Saved Data | Broken loaded Field Command Post stale record cleans safely | Not tested | |
| Saved Data | Broken loaded Research Lab stale record cleans safely | Not tested | |
| Saved Data | Broken loaded Main Base command post cleans/deactivates safely | Not tested | |
| Commands | `/standandhold status` works | Not tested | |
| Commands | Admin-only commands are permission-gated | Not tested | |
| Progression | Points save and reload | Not tested | |
| Research | Research command flow works | Not tested | |
| Buildings | Field Command Post GUI opens | Not tested | |
| Buildings | Research Lab GUI opens | Not tested | |
| Supplies | Supply crate deposit works | Not tested | |
| Entities | Human spawn eggs work | Not tested | |
| Entities | Higher-tier units respect stage/research gates | Not tested | |
| Visuals | Human NPC visuals are placeholder/WIP but do not crash | Not tested | |
| Events | Debug event commands work | Not tested | |
| Structures | Checkpoint/Main Base reload safely | Not tested | |
| Logs | No repeated tick spam | Not tested | |

## Crash Reports

- Crash report links/paths:

```text
Paste crash report paths or links here.
```

## Logs

- latest.log path:
- debug.log path, if available:

```text
Paste short relevant log excerpts here. Do not paste enormous full logs into this file.
```

## Screenshots

- Screenshot links/paths:

## Known Issues Found

| ID | Issue | Impact | Reproduction Steps | Blocks Wider Alpha? |
| --- | --- | --- | --- | --- |
| QA-001 | | | | No |

## Notes

```text
Add tester notes here.
```
