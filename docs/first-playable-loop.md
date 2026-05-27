# First Playable Alpha Loop

Use this guide to test the first small playable loop in `0.1.0-alpha.1`. This is still a technical alpha slice, so run it in a Creative/admin test world unless you are deliberately checking survival gaps.

## Test Setup

- Use `build/libs/standandhold-0.1.0-alpha.1.jar`, not the sources jar.
- Scape and Run: Parasites is optional. Without SRP, the default configured test parasite is `minecraft:zombie`.
- Let `config/standandhold.cfg` generate once, or reset it if you want the default test mappings.
- In Creative, take a Field Command Post, Research Lab, Supply Crate, and any test human spawn eggs from the Stand and Hold tab.
- Use `/standandhold help` and `/standandhold status` to orient the test.
- Use `/standandhold mission list` to confirm the Phase 37 alpha mission chain is present. If only the old single sample mission appears, reset or update `config/standandhold.cfg`.
- Optional structure testing details are in `docs/structures.md`.

## Loop Steps

1. Confirm the world starts at Stage 0.

   ```text
   /standandhold status
   ```

   Expected: Stage 0 Survivors, low or zero points, and current global supplies.

2. Check the mission chain.

   ```text
   /standandhold mission list
   /standandhold mission active
   ```

   Expected: the default chain includes sample recovery, field command, supplies, first research, Stage 1, outpost defence, and Main Base foundation objectives. No missions may be active yet; they auto-start when matching gameplay progress happens.

3. Kill the configured test parasite entity.

   Default config:

   ```text
   parasiteKillRewards: minecraft:zombie=3
   parasiteSampleDropChances: minecraft:zombie=0.15
   ```

   Expected: zombie kills add human points. Repeated kills can drop Parasite Tissue Samples. Picked-up samples auto-start and complete `recover_parasite_sample`.

4. Place a Field Command Post and Research Lab near the test area.

   Open both GUIs. The Command Post shows human points, stage, supplies, local stockpile, passive point generation, defender status, and upgrade controls. The Research Lab shows its target research, progress, requirements, stored samples, local supplies, global supplies, and whether completion is blocked or ready. Field Command Post placement or generated registration should progress `establish_field_command`.

5. Put one Parasite Tissue Sample into the Research Lab.

   Right-click the Research Lab while holding the sample.

   Expected: the lab stores the sample and targets `parasite_samples` if that research is still incomplete.

6. Let the Research Lab progress, or use the lab GUI to check readiness.

   Defaults:

   ```text
   researchLabTickInterval=400
   researchLabProgressPerInterval=8
   researchLabProgressRequired=160
   ```

   Expected: the first lab research completes after enough loaded time if it has the required sample. The first research has no supply cost. Completing any research should progress `complete_first_research`.

7. Add supplies to make the next research path understandable.

   Either claim a placed Supply Crate for global supplies, deposit a Supply Crate into a building as local supplies, or use the building GUI export button to move local supplies into the global pool.

   ```text
   /standandhold supplies status
   ```

   Expected: global supplies are visible, `stockpile_supplies` can progress, and later research such as `field_communications` can use the global pool.

8. Reach the early army stage.

   Stage 1 begins at the configured human point threshold. With defaults, this usually takes repeated zombie kills plus research or mission rewards. For a short QA pass, an admin can use:

   ```text
   /standandhold addpoints 150
   ```

   Expected: `/standandhold status` reports Stage 1 Local Army Response once the threshold is met.
   `reach_local_response` should complete once the saved stage reaches Stage 1.

9. Confirm outpost defenders.

   Keep the Field Command Post chunk loaded.

   ```text
   /standandhold commandpost nearest
   /standandhold commandpost status <x> <y> <z>
   ```

   Expected: the command post can spawn limited defenders over time. At Stage 0 it should use Survivor Defender; at Stage 1 it can use Army Rifleman.

10. Trigger or inspect a simple event.

   ```text
   /standandhold event status <x> <y> <z>
   /standandhold event outpostattack <x> <y> <z>
   /standandhold event reinforcement <x> <y> <z>
   ```

   Expected: outpost attacks spawn configured attackers, defaulting to zombies. Reinforcements spawn the strongest currently unlocked human tier. Natural events use the saved command-post cooldown and may take longer. A started outpost attack should progress `defend_outpost`.

11. Generate or locate a Main Base foundation.

   ```text
   /standandhold structure mainbase
   /standandhold mainbase list
   ```

   Expected: the Main Base foundation registers and progresses `establish_main_base`.

12. Check what to do next.

   ```text
   /standandhold help
   /standandhold research list
   /standandhold mission active
   /standandhold mission status
   /standandhold commandpost nearest
   ```

   Expected: testers can see current progression, available research, active mission state, and the nearest outpost.

## Troubleshooting

- No sample drops: the default zombie sample chance is intentionally modest. Kill more zombies or temporarily raise `minecraft:zombie` in `parasiteSampleDropChances`.
- Lab does not complete research: keep the lab chunk loaded, confirm it has enough progress, and confirm it has the required stored sample.
- `field_communications` does not complete: it needs global supplies. Export local supplies from a building GUI or claim a Supply Crate block.
- Defenders do not appear immediately: the default outpost defender interval is about three minutes, and spawn safety checks need air and a solid block near the command post.
- Higher-tier human units disappear: unit tiers are stage-gated, and Special Parasite Division also has a research gate.
- Event commands fail: stand near the target or pass loaded command-post coordinates.

## Current Limits

- This first loop assumes Creative/admin setup for core blocks and deterministic event testing.
- The default `minecraft:zombie` mapping is only a safe test mapping, not a real SRP balance preset.
- Human NPC tiers have simple distinguishable alpha uniforms, but final models and textures are still WIP.
- Natural events and passive generation are intentionally slow and bounded to avoid alpha-test spam.
