# Human Progression Foundation

The modern foundation provides world-global human points and seven stages. [Research, supplies, and missions](foundation-data.md) build on that state through commands. No physical point sources, buildings, units, or client HUD exist yet; this is not a conversion of Forge 1.12 saves.

## Stages And Configuration

| Level | Stage | Default minimum points |
| --- | --- | --- |
| 0 | Survivors | 0 |
| 1 | Local Army Response | 150 |
| 2 | Organised Military | 500 |
| 3 | Elite Units | 1200 |
| 4 | Super Elite Units | 2600 |
| 5 | Special Parasite Division | 5200 |
| 6 | Main Base / Endgame Counter-Offensive | 10000 |

Edit `config/standandhold-progression.toml` in the game/server directory while the world/server is stopped:

```toml
stageThresholds = [0, 150, 500, 1200, 2600, 5200, 10000]
```

Exactly seven strictly increasing whole numbers are required, beginning with zero and ending no higher than `9223372036854775807`. Invalid lists are corrected to defaults by NeoForge. Restart after editing. The pinned loader calls this a `SYNCED` config: the server owns it and synchronizes its configuration to connected clients. It is not per-player or per-dimension.

Points range from zero to Java's maximum signed `long`. The stage is the highest configured threshold reached; changing thresholds does not change points, but can change the derived stage on the next progression operation. The stored stage is a cache and is repaired only when needed.

## Commands

| Command | Permission | Result |
| --- | --- | --- |
| `/standandhold` or `/standandhold status` | Any player | Show global points and stage |
| `/standandhold addpoints <amount>` | Operator / gamemaster level 2 | Add a positive whole number |
| `/standandhold setstage <0..6>` | Operator / gamemaster level 2 | Set points to that stage's configured minimum |

Singleplayer mutations require commands enabled. Dedicated-server console commands omit the `/`. `setstage` is an admin reset as well as an advancement tool: it can lower points, and `setstage 0` resets them to zero. It is not an independent stage override. Invalid/missing arguments and overflow fail without changing progression.

## Save Contract

- `HumanWorldData` uses the server's global `SavedDataStorage`, shared by Overworld, Nether, End, and other dimensions. It is accessed on the server thread, without a static world cache or tick handler.
- Save file: `<world>/data/standandhold/human_progression.dat`. Minecraft wraps the mod compound under `data` with its own `DataVersion`.
- Mod schema: `data_version` (integer, currently 2), `points` (long), `stage` (integer, 0-6), and supply/research/mission state described in the foundation guide. Keep existing stage ordering and IDs stable; incompatible changes need a migration.
- Missing schema is modern version 0. Migration to 2 retains modern points and unknown fields and initializes new foundation state. Missing/malformed progression fields receive safe defaults; negative points become zero. This does **not** read legacy NBT or repair physically corrupt/compressed files.
- Migration, normalization, and actual changes mark data dirty. Identical operations and valid reloads do not. Minecraft handles saving and atomic file replacement.
- A schema newer than supported is retained verbatim and progression operations refuse access with a clear error. Restore the matching mod/backup instead of downgrading or manually deleting the data.

Back up valuable worlds before testing; use fresh disposable modern worlds for this foundation.

## Verification Recipe

1. Prepare Java as in [Building](building.md), then run `./gradlew.bat clean build --no-daemon --stacktrace`. The `progressionTest` task runs dependency-free rules and NBT codec checks as part of `check`; test classes are not packaged in the mod jar. Brigadier integration is tested in the actual loader runtime, not a simulated Minecraft bootstrap.
2. In a fresh singleplayer world with commands enabled, run `status`, `addpoints 149`, then `addpoints 1`. Expect 149/Stage 0 and 150/Stage 1. Save/quit, reopen, and confirm 150/Stage 1.
3. Run `setstage 2`: expect 500/Stage 2. Run `setstage 0`: expect zero/Stage 0. Check `addpoints -1`, `addpoints 0`, missing arguments, non-numbers, and `setstage 7` fail without changing the total.
4. On a private dedicated server, repeat the commands. Run `execute in minecraft:the_nether run standandhold addpoints 25`, then Overworld/End `status`: all must share the same total. Run `save-all flush`, `stop`, restart, and recheck.
5. Check a non-operator player can use `status` but cannot use mutations. This is a separate multiplayer permission check, not proven by an operator console test.
6. In a disposable world, reset to zero, add `9223372036854775807`, then add 1. Expect a clear overflow error and the unchanged maximum. Reset to zero afterward.
7. With the server stopped, try a valid custom threshold list; restart and verify `setstage` and derived stage use it. Try an invalid list and confirm safe correction. Restore defaults afterward. Never edit shared tester configs or saved NBT while the game is running.

No SRP or other threat mod is required. Registry/tag-based threat integration belongs to later phases.
