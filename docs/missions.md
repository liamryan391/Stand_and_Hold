# Stand and Hold Missions

Phase 37 expands the mission foundation into a small alpha objective chain. Missions are still config-backed, server-side, and persistent in `HumanWorldData`; this is not a full campaign system yet.

## Commands

```text
/standandhold mission list
/standandhold mission status [id]
/standandhold mission active
/standandhold mission start <id>
/standandhold mission progress <id> <amount>
/standandhold mission complete <id> [force]
/standandhold mission reset <id>
```

`list`, `status`, and `active` are safe inspection commands. `start`, `progress`, `complete`, and `reset` are admin/debug commands.

## Default Alpha Mission Chain

| Mission ID | Objective | Progress Source | Reward |
| --- | --- | --- | --- |
| `recover_parasite_sample` | Recover one Parasite Tissue Sample. | Parasite Tissue Sample pickup events. | 20 human points, 8 supplies |
| `establish_field_command` | Establish a Field Command Post. | Field Command Post placement, loading, or generated registration. | 25 human points, 12 supplies |
| `stockpile_supplies` | Reach 16 stored/global supplies. | Supply Crate claims, building deposits, GUI supply transfers, and passive local generation. | 20 human points |
| `complete_first_research` | Complete one research entry. | Research command completion, Research Lab completion, or research rewards. | 40 human points, 10 supplies |
| `reach_local_response` | Reach Human Stage 1. | Any point source that advances the saved human stage. | 25 supplies |
| `defend_outpost` | Trigger or survive one outpost attack. | Dynamic outpost attack events. | 40 human points, 20 supplies |
| `establish_main_base` | Establish a Main Base foundation. | Main Base world generation or debug generation registration. | 100 human points, 80 supplies |

The chain is intentionally light. Missions auto-start when matching gameplay progress happens, and completion rewards are applied once.

## Objective Types

Mission config entries use:

```text
id|name|description|objectiveType|requiredCount|pointReward|supplyReward|researchRewardIds
```

Current objective types:

- `RECOVER_PARASITE_SAMPLE`: updates when Parasite Tissue Samples are picked up.
- `ESTABLISH_FIELD_COMMAND`: updates when a Field Command Post is registered.
- `STOCKPILE_SUPPLIES`: updates from global or local supply stockpiles.
- `COMPLETE_RESEARCH`: updates when research completes.
- `REACH_HUMAN_STAGE`: updates from the saved human stage ID.
- `DEFEND_OUTPOST`: updates when an outpost attack event starts.
- `DISCOVER_STRUCTURE`: generic generated-structure objective.
- `DISCOVER_CHECKPOINT`: Small Army Checkpoint generation.
- `DISCOVER_MAIN_BASE`: Main Base foundation generation.
- `MANUAL`: admin/debug progress only.

## Test Notes

- Use `/standandhold mission status` after each loop step to confirm persistence.
- Use `/standandhold mission active` to see only started, incomplete objectives.
- Use `/standandhold mission reset <id>` in an admin test world if you need to replay one mission.
- If an old config already exists, reset `config/standandhold.cfg` or update the `Missions` section manually to see the full Phase 37 chain.
- Some objective completions can happen without a player context, such as worldgen or passive building progress. Those still save and reward correctly, but may not produce a chat message.

## Future Mission Ideas

- Dedicated checkpoint discovery objectives.
- Research-specific missions rather than a generic first research objective.
- Lab-focused missions that inspect lab-local sample and supply storage.
- Main Base activation and Special Parasite Division deployment objectives.
- A proper mission GUI once the command-driven alpha flow is stable.
