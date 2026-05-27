# First Playable Alpha Loop

Use this guide to test the first small playable loop in `0.1.0-alpha.1`. This is still a technical alpha slice, so run it in a Creative/admin test world unless you are deliberately checking survival gaps.

## Test Setup

- Use `build/libs/standandhold-0.1.0-alpha.1.jar`, not the sources jar.
- Scape and Run: Parasites is optional. Without SRP, the default configured test parasite is `minecraft:zombie`.
- Let `config/standandhold.cfg` generate once, or reset it if you want the default test mappings.
- In Creative, take a Field Command Post, Research Lab, Supply Crate, and any test human spawn eggs from the Stand and Hold tab.
- Use `/standandhold help` and `/standandhold status` to orient the test.

## Loop Steps

1. Confirm the world starts at Stage 0.

   ```text
   /standandhold status
   ```

   Expected: Stage 0 Survivors, low or zero points, and current global supplies.

2. Optional but useful: start the first mission as an admin.

   ```text
   /standandhold mission start recover_parasite_sample
   /standandhold mission status recover_parasite_sample
   ```

   Expected: the mission becomes active and tracks one recovered Parasite Tissue Sample.

3. Kill the configured test parasite entity.

   Default config:

   ```text
   parasiteKillRewards: minecraft:zombie=3
   parasiteSampleDropChances: minecraft:zombie=0.15
   ```

   Expected: zombie kills add human points. Repeated kills can drop Parasite Tissue Samples. Picked-up samples update the active sample recovery mission.

4. Place a Field Command Post and Research Lab near the test area.

   Open both GUIs. The Command Post shows human points, stage, supplies, local stockpile, passive point generation, defender status, and upgrade controls. The Research Lab shows its target research, progress, requirements, stored samples, local supplies, global supplies, and whether completion is blocked or ready.

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

   Expected: the first lab research completes after enough loaded time if it has the required sample. The first research has no supply cost.

7. Add supplies to make the next research path understandable.

   Either claim a placed Supply Crate for global supplies, deposit a Supply Crate into a building as local supplies, or use the building GUI export button to move local supplies into the global pool.

   ```text
   /standandhold supplies status
   ```

   Expected: global supplies are visible, and later research such as `field_communications` can use them.

8. Reach the early army stage.

   Stage 1 begins at the configured human point threshold. With defaults, this usually takes repeated zombie kills plus research or mission rewards. For a short QA pass, an admin can use:

   ```text
   /standandhold addpoints 150
   ```

   Expected: `/standandhold status` reports Stage 1 Local Army Response once the threshold is met.

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

   Expected: outpost attacks spawn configured attackers, defaulting to zombies. Reinforcements spawn the strongest currently unlocked human tier. Natural events use the saved command-post cooldown and may take longer.

11. Check what to do next.

   ```text
   /standandhold help
   /standandhold research list
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
