# Wider Alpha Testing Checklist

Use this checklist before wider testing of `0.1.0-alpha.1`. Record Minecraft logs, Forge logs, config changes, and whether SRP was installed.

## Environment

- [ ] Confirm Minecraft Forge 1.12.2 is installed.
- [ ] Confirm the tested jar came from `build/libs/standandhold-0.1.0-alpha.1.jar`.
- [ ] Confirm the normal jar is installed, not `standandhold-0.1.0-alpha.1-sources.jar`.
- [ ] Confirm a fresh `config/standandhold.cfg` can generate.
- [ ] Confirm Java 8 is used for local Gradle builds.

## Launch Tests

- [ ] Client reaches the main menu without crashing.
- [ ] Stand and Hold appears in the Minecraft Mods list.
- [ ] Client launches without Scape and Run: Parasites installed.
- [ ] Dedicated server launches without Scape and Run: Parasites installed.
- [ ] Client launch with Scape and Run: Parasites installed, if available.
- [ ] Dedicated server launch with Scape and Run: Parasites installed, if available.
- [ ] Mod still loads without SRP installed.
- [ ] If testing SRP, follow `docs/srp-test-checklist.md`.

## World Save Tests

- [ ] Create a new world and confirm there is no crash entering it.
- [ ] Reload an existing world.
- [ ] Run `/standandhold addpoints <amount>`, save, quit, reload, and confirm points persist.
- [ ] Load a world created before Phase 32, save it once, reload it, and confirm progression data still works.
- [ ] Break placed Field Command Posts and confirm later command-post lists/status commands do not crash.
- [ ] After breaking a loaded Field Command Post, run `/standandhold commandpost list` and confirm stale loaded records are cleaned or absent.
- [ ] Break a loaded Research Lab, run `/standandhold status` or `/standandhold research status`, and confirm no stale saved lab crash.
- [ ] Remove or alter generated structures during testing and confirm stale command post or Main Base saved positions do not crash commands.
- [ ] Remove the Field Command Post from a generated Main Base, then run `/standandhold mainbase list` and `/standandhold mainbase status <x> <y> <z>` while the chunk is loaded.
- [ ] Confirm stale cleanup does not force-load distant or unloaded chunks.

## Command Tests

- [ ] `/standandhold help`
- [ ] `/standandhold status`
- [ ] `/standandhold addpoints <amount>`
- [ ] `/standandhold research list`
- [ ] `/standandhold research status`
- [ ] `/standandhold research complete <id>`
- [ ] `/standandhold threat status`
- [ ] `/standandhold threat list`
- [ ] `/standandhold threat reinforce`
- [ ] `/standandhold threat reset`
- [ ] `/standandhold event status`
- [ ] `/standandhold event outpostattack`
- [ ] `/standandhold event reinforcement`
- [ ] Commands with missing or invalid arguments fail safely with usage/error text.
- [ ] Non-admin players cannot use admin-only commands, if testing on a server.

## Building And GUI Tests

- [ ] Follow `docs/first-playable-loop.md` in a Creative/admin test world.
- [ ] Place a Field Command Post.
- [ ] Open the Field Command Post GUI.
- [ ] Confirm the Field Command Post GUI clearly shows points, stage, level, global supplies, local stockpile, passive point generation, and defender cap/timer.
- [ ] Close and reopen the Field Command Post GUI and confirm points/stage/supplies do not desync obviously.
- [ ] Use Field Command Post supply import/export buttons when supplies are available.
- [ ] Place a Research Lab.
- [ ] Open the Research Lab GUI.
- [ ] Confirm the Research Lab GUI clearly shows target ID/name, progress, required samples/supplies, stored samples, local supplies, global supplies, and blocked/ready status.
- [ ] Close and reopen the Research Lab GUI and confirm selected/progress/supplies do not desync obviously.
- [ ] Use Research Lab research selection/completion buttons where requirements are met.
- [ ] Deposit a Supply Crate into a Field Command Post.
- [ ] Deposit a Supply Crate into a Research Lab.

## Parasite/Test Mapping Tests

- [ ] Kill `minecraft:zombie` and confirm default human point rewards can apply.
- [ ] Kill `minecraft:zombie` repeatedly and confirm sample drops can occur with the default test chance.
- [ ] Pick up a Parasite Tissue Sample while `recover_parasite_sample` is active and confirm mission progress can update.
- [ ] Reset config and confirm the default zombie test mapping returns.
- [ ] Add a custom test entity mapping and confirm config parsing does not crash.
- [ ] With SRP installed, add a verified SRP registry ID mapping and confirm kills/samples/targeting if available.
- [ ] Remove SRP after adding SRP mappings and confirm Stand and Hold still launches.

## Human NPC Tests

- [ ] Spawn each available human NPC spawn egg in a test world.
- [ ] Reach or set Stage 1 and confirm early army defenders can spawn near a loaded Field Command Post.
- [ ] Confirm higher-tier human units are stage-gated and may not remain spawned until their required stage or research is met.
- [ ] Confirm human NPCs do not attack players by default.
- [ ] Confirm human NPCs can target configured parasite/test entities.
- [ ] Confirm defender spawning near loaded Field Command Posts respects configured caps.
- [ ] Confirm each human NPC tier has a visually distinguishable uniform and no purple/black missing texture.

## Visual Asset Tests

- [ ] Confirm Field Command Post, Research Lab, and Supply Crate blocks render with Stand and Hold alpha textures.
- [ ] Confirm Parasite Tissue Sample, Prototype Ranged Weapon, and Anti-Parasite Blade item icons render without missing textures.
- [ ] Confirm Army, Elite, and Special Division armour item icons render without missing textures.
- [ ] Equip each armour set and confirm worn armour textures render without missing textures.

## Structure Tests

- [ ] Use the checkpoint debug structure command.
- [ ] Use the Main Base debug structure command.
- [ ] Reload the world after generating each structure.
- [ ] Confirm generated structures do not crash on reload.
- [ ] Check command-post and mainbase list/status commands after structure generation.

## Log Review

- [ ] Check `latest.log` for repeated Stand and Hold errors or warnings.
- [ ] Confirm there is no repeated log spam every tick during idle testing.
- [ ] With debug logging enabled, confirm saved-data migration/cleanup messages appear only when migration or cleanup actually happens.
- [ ] Confirm any ForgeGradle build-time network/version-check warnings are not confused with in-game runtime errors.

## Config Reset Test

- [ ] Back up `config/standandhold.cfg`.
- [ ] Delete or rename the config file.
- [ ] Relaunch and confirm a fresh config generates.
- [ ] Confirm default commands and zombie test mappings still work.
