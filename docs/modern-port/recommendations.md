# Recommendations

## Product Direction

Stand and Hold should become a resistance framework for hostile-world modpacks, not a single-threat addon. The strongest identity is:

> The enemy evolves. Humanity responds.

That gives the mod a clear role next to parasite, fungus, sculk, alien, zombie, and other apocalypse mods.

## Loader Recommendation

Use NeoForge first.

Reasons:

- It is closer in spirit to the old Forge project.
- It is well suited for block entities, data generation, capabilities, networking, worldgen, and large content mods.
- Many large modpack ecosystems have moved toward NeoForge for modern versions.

Use Fabric later only after the NeoForge gameplay foundation is stable. A multi-loader split too early would slow the core mod down.

## Version Recommendation

Plan for the latest viable NeoForge track, but keep a stability gate.

- Step 0.3.2 confirmed matching libraries and two useful external threat mods for both 26.3 and 26.2. Carry 26.3 forward as the preferred candidate given the latest-release goal, accepting that its loader is still beta; keep 26.2 as the nearest non-beta-loader fallback.
- Select exact compatible versions before scaffolding, then validate them with a clean MDK build in step 1.4.1.
- Keep 1.21.1 available if immediate Spore support is more important than the newest release. Its verified Spore file does not run on 26.3 merely because both use NeoForge.

The [release verification record](release-verification.md) contains the candidate versions, Java requirements, and official template snapshots. [Ecosystem Compatibility](ecosystem-compatibility.md) records exact library/threat artifacts and testing limits. No final toolchain choice has been made.

## First Modern Playable Goal

The first modern demo should be smaller than the full 1.12 alpha:

1. Start world.
2. Kill default test threat.
3. Gain human points and samples.
4. Place Field Command Post.
5. Place Research Lab.
6. Complete first research.
7. Spawn one defender.
8. Trigger one small outpost attack.
9. Save/reload and confirm data persists.

This is the slice that should get polished before adding large systems.

## Features That Can Make The Mod Stand Out

- Human stage escalation shown in-world, not just config numbers.
- Bases that visibly upgrade.
- Soldiers that feel like a coordinated faction rather than random helpers.
- Research that explains why new tools exist.
- Threat-response map data: areas become "hot zones" and the army reacts.
- Compatibility presets for multiple threat mods.
- Server-friendly controls for spawn caps, structure rates, and event frequency.
- A guidebook or mission chain that makes the player understand what to do.

## Art And Model Pipeline

- Use original art only.
- Use Blockbench for entity, armour, block, and vehicle concepts.
- Prototype GeckoLib after the target is pinned and animated models are needed; keep AzureLib as the verified alternative. Do not add both animation stacks to Stand and Hold without a concrete need.
- Create a simple silhouette guide for each human unit tier before detailed textures.
- Keep alpha models readable at Minecraft distance.

Suggested unit visual language:

- survivors: improvised gear
- local army: simple uniform and helmet
- organized military: heavier armor and packs
- elite units: darker, cleaner kit
- super elite units: reinforced armour and visible tech
- special parasite division: sealed suit, respirator, specialist markings

## Weapons And Vehicles

Do weapons in layers:

1. melee countermeasure
2. simple projectile weapon
3. deployable turret or support crate
4. squad equipment roles
5. vehicle or aircraft support call-ins
6. physical vehicles and aircraft only after networking, animation, and AI are mature

Avoid detailed real-world construction mechanics. Keep weapons as Minecraft gameplay abstractions with clear counters, cooldowns, ammo/supply costs, and upgrade paths.

## Compatibility Recommendations

Start with generic compatibility:

- entity tags for hostile threat targets
- config/datapack reward mappings
- optional support for JEI/EMI recipe display
- optional Jade/WTHIT block info
- optional Better Combat-style combat compatibility
- optional Create-style supply/logistics integration later

Do not hardcode internals from other mods.

First optional test candidates are Undead Nights (horde defence) and Contagion (infection coexistence), both verified as published NeoForge releases for 26.3 and 26.2. Verify their actual entity IDs and behavior in the selected test instance before enabling any mappings. Published releases are not combined runtime proof.

## Community And Attention

Public interest will come from clarity:

- show one strong loop in video form before promising huge systems
- post screenshots of base progression
- share a public roadmap with "done / next / later"
- write modpack-maker docs early
- invite testers to submit threat-mod config presets
- keep changelogs honest about alpha state

Good first public materials:

- 30 to 60 second base defense clip
- one image showing stage progression
- one image showing lab research
- one image showing a checkpoint or main base foundation
- README section for "What makes this different?"
