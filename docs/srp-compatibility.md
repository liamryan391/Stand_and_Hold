# Optional SRP Compatibility

Stand and Hold can work alongside Scape and Run: Parasites, but SRP is optional. Stand and Hold does not import SRP classes, copy SRP code, copy SRP assets, or require SRP to launch.

The compatibility layer is string-based:

- SRP loaded state is checked with Forge `Loader.isModLoaded`.
- Entity matching uses registry IDs such as `modid:entity_id`.
- Invalid, empty, or malformed mapping rows are skipped safely.
- If SRP is missing, SRP-specific mappings stay dormant.
- The default `minecraft:zombie` mappings remain the safe no-SRP test mappings.

During Phase 34, the supplied `SRParasites-1.10.6.jar` metadata was inspected only to confirm the mod id. Its `mcmod.info` reports:

```text
modid: srparasites
mcversion: 1.12.2
version: 1.10.6
```

Entity registry IDs still need to be verified in-game against the exact SRP build used by the tester or modpack.

## Finding SRP Entity Registry IDs

Use runtime verification before adding IDs to Stand and Hold config.

Recommended methods:

- In a test world with SRP installed, type `/summon srparasites:` and use tab completion to inspect registered entity IDs.
- Summon one candidate entity in a controlled Creative test area, then use the same exact ID in Stand and Hold config.
- Check `logs/latest.log` during startup for any Forge/entity registration output if the SRP build prints it.
- If using external tooling to inspect a jar, treat class names, translation keys, and display names as hints only. The final value must be a working registry ID.

Do not infer final config entries from display names alone.

## Config Locations

Forge writes Stand and Hold config to:

```text
config/standandhold.cfg
```

### Kill Rewards

Use `Point Sources` -> `Parasite Kill Rewards`:

```text
srparasites:<verified_entity_id>=12
```

This awards human progression points when that configured entity dies.

### Sample Drops

Use `Point Sources` -> `Parasite Sample Drop Chances`:

```text
srparasites:<verified_entity_id>=0.25
```

This gives that entity a 25 percent chance to drop a Parasite Tissue Sample.

### Human NPC Targeting

Use `Human NPCs` -> `Human Unit Target Entity IDs`:

```text
srparasites:<verified_entity_id>
```

This allows human NPCs to target that entity. Players and Stand and Hold human NPCs are still ignored by default.

### Combined SRP Mapping

Use `Compatibility` -> `SRP Parasite Mappings` for one-row SRP-specific mappings:

```text
srparasites:<verified_entity_id>|12|0.25|true
```

Format:

```text
entityId|killReward|sampleDropChance|humanTarget
```

- `killReward`: human points awarded on death.
- `sampleDropChance`: value from `0.0` to `1.0`.
- `humanTarget`: `true` lets human NPCs attack it.

If `humanTarget` is omitted, Stand and Hold treats it as `true` for that SRP mapping. Set it to `false` when you want rewards or drops without human NPC targeting.

## Safe Example

These are examples only. Replace `<verified_entity_id>` after testing with your exact SRP build.

```text
srpParasiteMappings=[
  "srparasites:<verified_entity_id>|12|0.25|true"
]
```

Do not add unverified SRP IDs as pack defaults.

## Testing Without SRP

1. Remove SRP from the test instance.
2. Launch Minecraft or the dedicated server.
3. Confirm Stand and Hold loads.
4. Run `/standandhold status`.
5. Kill `minecraft:zombie` and confirm the safe test mapping still works.

## Testing With SRP

1. Install SRP in the same Forge 1.12.2 test instance.
2. Launch once and confirm Stand and Hold reports SRP detection in the log.
3. Verify one SRP entity registry ID.
4. Add that ID to kill rewards, sample drops, and human targeting, or use one `srpParasiteMappings` row.
5. Relaunch or reload config as appropriate for the test environment.
6. Kill the verified SRP entity and confirm human points increase.
7. Kill it repeatedly and confirm sample drops can occur.
8. Spawn defenders near the verified entity and confirm they target it.
9. Remove SRP again and confirm Stand and Hold still launches.

## Current Limits

- No built-in SRP entity IDs are enabled by default.
- No SRP balance preset exists yet.
- Stand and Hold does not inspect SRP evolution stages or AI internals.
- Human NPC visuals and some GUI text remain alpha quality.
