# Alpha QA Results Template

Use one copy of this template per test session. Attach logs, crash reports, screenshots, and config files when possible.

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
| Build | Java 8 build completed | Not tested | |
| Launch | Main menu opens | Not tested | |
| Launch | Mod appears in Mods list | Not tested | |
| World | New world loads | Not tested | |
| World | Existing world reloads | Not tested | |
| Saved Data | Existing pre-Phase 32 world migrates to saved data version 1 | Not tested | |
| Saved Data | Broken loaded Field Command Post stale record cleans safely | Not tested | |
| Saved Data | Broken loaded Research Lab stale record cleans safely | Not tested | |
| Saved Data | Broken loaded Main Base command post cleans/deactivates safely | Not tested | |
| Commands | `/standandhold status` works | Not tested | |
| Commands | `/standandhold help` gives first-loop guidance | Not tested | |
| Commands | Admin-only commands are permission-gated | Not tested | |
| Progression | Points save and reload | Not tested | |
| Research | Research command flow works | Not tested | |
| Buildings | Field Command Post GUI opens | Not tested | |
| Buildings | Research Lab GUI opens | Not tested | |
| Vertical Slice | First playable loop guide can be followed in Creative/admin test world | Not tested | |
| Missions | `recover_parasite_sample` can track sample recovery when active | Not tested | |
| Supplies | Supply crate deposit works | Not tested | |
| Entities | Human spawn eggs work | Not tested | |
| Entities | Stage 1 early army defenders can spawn near a loaded Field Command Post | Not tested | |
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
