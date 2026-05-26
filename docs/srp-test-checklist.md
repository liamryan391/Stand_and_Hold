# SRP Compatibility Test Checklist

Use this checklist for optional Scape and Run: Parasites testing. Record the exact SRP jar/version, Forge version, Stand and Hold jar, config changes, and `latest.log` path.

## Baseline Without SRP

- [ ] Remove SRP from the test instance.
- [ ] Launch the client without SRP.
- [ ] Launch the dedicated server without SRP, if testing server compatibility.
- [ ] Confirm Stand and Hold appears in the Mods list.
- [ ] Run `/standandhold status` without SRP installed.
- [ ] Kill `minecraft:zombie` and confirm the default test reward can apply.
- [ ] Kill `minecraft:zombie` repeatedly and confirm default test sample drops can occur.

## SRP Installed

- [ ] Install SRP in the Forge 1.12.2 test instance.
- [ ] Confirm the SRP jar version being tested.
- [ ] Launch the client with SRP installed.
- [ ] Launch the dedicated server with SRP installed, if testing server compatibility.
- [ ] Confirm Stand and Hold still appears in the Mods list.
- [ ] Confirm `/standandhold status` works with SRP installed.
- [ ] Check `latest.log` for the Stand and Hold SRP compatibility detection message.

## Verify One SRP Entity ID

- [ ] Use `/summon srparasites:` tab completion or another runtime method to find one SRP entity registry ID.
- [ ] Summon or encounter that exact entity in a controlled test area.
- [ ] Add the verified ID to `parasiteKillRewards`.
- [ ] Add the same verified ID to `parasiteSampleDropChances`.
- [ ] Add the same verified ID to `humanUnitTargetEntityIds`, or use `srpParasiteMappings` with `humanTarget=true`.
- [ ] Relaunch or reload config as appropriate.

## Reward And Drop Tests

- [ ] Run `/standandhold status` and note current points.
- [ ] Kill the verified SRP entity.
- [ ] Confirm human points increase by the configured amount.
- [ ] Kill the verified SRP entity repeatedly.
- [ ] Confirm Parasite Tissue Samples can drop at the configured chance.
- [ ] Confirm malformed mapping rows do not crash launch or gameplay.

## Human NPC Target Tests

- [ ] Place or find a Field Command Post.
- [ ] Reach/set the required stage for an early defender test if needed.
- [ ] Spawn or wait for human defenders near the verified SRP entity.
- [ ] Confirm defenders target the configured SRP entity.
- [ ] Confirm defenders still do not attack players by default.

## Remove SRP Again

- [ ] Remove SRP from the test instance.
- [ ] Relaunch the client.
- [ ] Relaunch the dedicated server, if testing server compatibility.
- [ ] Confirm Stand and Hold still loads.
- [ ] Run `/standandhold status`.
- [ ] Reset or remove SRP config mappings.
- [ ] Confirm default `minecraft:zombie` test mappings still work.

## Notes

- Do not treat class names, language keys, or display names as final registry IDs until verified in game.
- Do not enable unverified SRP IDs as defaults for wider alpha testers.
- Keep a copy of the exact config used for any successful SRP test.
