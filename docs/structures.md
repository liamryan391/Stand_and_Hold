# Structures

Stand and Hold currently uses small procedural Java block placement for structures. There are no external schematic dependencies, and structure generation stays bounded to the current chunk-sized footprint.

## Safety Rules

- Structures only generate in configured allowed dimensions.
- Admin debug generation ignores spawn chance but still respects allowed dimensions and terrain safety.
- Footprints are clamped to a safe single-chunk range.
- Generation skips terrain with too much height difference.
- Generation skips liquid ground and liquid or tile-entity blocks in the clearance space.
- Generation does not force-load distant chunks.
- Stale saved positions are cleaned only when the relevant chunk is already loaded.

## Small Army Checkpoint

Purpose: early human world presence and a lightweight outpost anchor.

Contains:

- Stone-brick floor and cobblestone path cross.
- Low cobblestone-wall perimeter with a front gate opening.
- Central Field Command Post.
- Two Supply Crates.
- Simple iron-bar interior protection and torches.

Expected saved data:

- The central Field Command Post should register in `HumanWorldData`.
- `/standandhold commandpost list` should show the generated command post after generation.
- Breaking the Field Command Post should unregister it or be cleaned safely by later list/status commands while the chunk is loaded.

Debug command:

```text
/standandhold structure checkpoint
```

Expected success feedback includes:

- Structure origin.
- Command Post position.
- Whether the Command Post is registered.
- Total registered Field Command Posts.

## Main Base Foundation

Purpose: late-game human presence and future expansion anchor.

Contains:

- Larger fortified footprint than a checkpoint.
- Stone-brick floor, cobblestone path cross, and perimeter walls.
- Central Field Command Post command area.
- Defender spawn pads.
- Research Lab blocks in lab zones.
- Supply Crates in a supply zone.
- Simple barracks/future-zone floor markers.
- Glowstone corner lighting.

Expected saved data:

- The central Field Command Post should register in `HumanWorldData`.
- The Main Base should register using the central Field Command Post position.
- `/standandhold mainbase list` should show the generated Main Base.
- If the human stage is high enough, the base may activate and upgrade the command post to Main Base level.
- Removing the central Field Command Post should not crash list/status/activation commands while the chunk is loaded; stale records should clean or deactivate safely.

Debug command:

```text
/standandhold structure mainbase
```

Expected success feedback includes:

- Structure origin.
- Command Post position.
- Whether the Main Base is registered.
- Activation state.
- Initial defender count.
- Total registered Main Bases.

## Config

Relevant config section:

```text
World Generation
```

Important values:

- `enableArmyCheckpointGeneration`
- `armyCheckpointSpawnChance`
- `armyCheckpointWidth`
- `armyCheckpointDepth`
- `armyCheckpointWallHeight`
- `armyCheckpointMaxTerrainHeightDifference`
- `armyCheckpointAllowedDimensions`
- `enableMainBaseGeneration`
- `mainBaseSpawnChance`
- `mainBaseWidth`
- `mainBaseDepth`
- `mainBaseWallHeight`
- `mainBaseMaxTerrainHeightDifference`
- `mainBaseActivationStage`
- `mainBaseInitialDefenders`
- `mainBaseDefenderPatrolRadius`
- `mainBaseAllowedDimensions`

## Manual Tests

1. Generate a checkpoint with `/standandhold structure checkpoint`.
2. Run `/standandhold commandpost list`.
3. Run `/standandhold commandpost nearest`.
4. Save and reload the world near the checkpoint.
5. Break the checkpoint Field Command Post and run `/standandhold commandpost list`.
6. Generate a Main Base with `/standandhold structure mainbase`.
7. Run `/standandhold mainbase list`.
8. Run `/standandhold mainbase status <x> <y> <z>` using the command post coordinates from the generation message.
9. Save and reload the world near the Main Base.
10. Break the Main Base Field Command Post and confirm main base list/status commands do not crash while the chunk is loaded.

## Known Limitations

- Structures are still alpha foundations, not final bases.
- Layouts are procedural and simple, not authored schematics.
- Main Bases are intentionally contained to a small footprint for stability.
- Main Base rooms are markers for future systems, not complete facilities yet.
- Natural generation remains rare and terrain-dependent.

## Planned Future Structures

- Outpost
- Research Lab facility
- Barracks
- Supply depot
- Main Base expansion
- Special Parasite Division facility
