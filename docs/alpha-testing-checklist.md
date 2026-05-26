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

## World Save Tests

- [ ] Create a new world and confirm there is no crash entering it.
- [ ] Reload an existing world.
- [ ] Run `/standandhold addpoints <amount>`, save, quit, reload, and confirm points persist.
- [ ] Break placed Field Command Posts and confirm later command-post lists/status commands do not crash.
- [ ] Remove or alter generated structures during testing and confirm stale command post or Main Base saved positions do not crash commands.

## Command Tests

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

- [ ] Place a Field Command Post.
- [ ] Open the Field Command Post GUI.
- [ ] Close and reopen the Field Command Post GUI and confirm points/stage/supplies do not desync obviously.
- [ ] Use Field Command Post supply import/export buttons when supplies are available.
- [ ] Place a Research Lab.
- [ ] Open the Research Lab GUI.
- [ ] Close and reopen the Research Lab GUI and confirm selected/progress/supplies do not desync obviously.
- [ ] Use Research Lab research selection/completion buttons where requirements are met.
- [ ] Deposit a Supply Crate into a Field Command Post.
- [ ] Deposit a Supply Crate into a Research Lab.

## Parasite/Test Mapping Tests

- [ ] Kill `minecraft:zombie` and confirm default human point rewards can apply.
- [ ] Kill `minecraft:zombie` repeatedly and confirm sample drops can occur with the default test chance.
- [ ] Reset config and confirm the default zombie test mapping returns.
- [ ] Add a custom test entity mapping and confirm config parsing does not crash.
- [ ] With SRP installed, add a verified SRP registry ID mapping and confirm kills/samples/targeting if available.

## Human NPC Tests

- [ ] Spawn each available human NPC spawn egg in a test world.
- [ ] Confirm human NPCs do not attack players by default.
- [ ] Confirm human NPCs can target configured parasite/test entities.
- [ ] Confirm defender spawning near loaded Field Command Posts respects configured caps.

## Structure Tests

- [ ] Use the checkpoint debug structure command.
- [ ] Use the Main Base debug structure command.
- [ ] Reload the world after generating each structure.
- [ ] Confirm generated structures do not crash on reload.
- [ ] Check command-post and mainbase list/status commands after structure generation.

## Log Review

- [ ] Check `latest.log` for repeated Stand and Hold errors or warnings.
- [ ] Confirm there is no repeated log spam every tick during idle testing.
- [ ] Confirm any ForgeGradle build-time network/version-check warnings are not confused with in-game runtime errors.

## Config Reset Test

- [ ] Back up `config/standandhold.cfg`.
- [ ] Delete or rename the config file.
- [ ] Relaunch and confirm a fresh config generates.
- [ ] Confirm default commands and zombie test mappings still work.
