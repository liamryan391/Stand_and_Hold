# Ecosystem Research

This file records references and lessons from nearby mods. Use this for positioning and compatibility planning, not for copying assets or internals.

The adjacent-mod descriptions below provide design context, not a compatibility guarantee. Official release/MDK availability was checked in step 0.3.1; exact published Minecraft/NeoForge matches and selected jar manifests were checked in step 0.3.2. Use [Ecosystem Compatibility](ecosystem-compatibility.md) for the current verified artifact matrix and remaining runtime tests.

## Loader And Tooling Snapshot

Verified on 2026-10-07; full evidence and immutable template links are in [Release Verification](release-verification.md).

- Minecraft 26.3 is the latest Java release; 26.4-snapshot-3 is a snapshot, excluded from the scaffold candidates. Source: [Mojang version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json).
- NeoForge 26.3.0.57-beta is published; the inspected 26.3 MDK still defaults to 26.3.0.52-beta. Minecraft 26.2 has non-beta NeoForge 26.2.0.88. Source: [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml).
- Official ModDevGradle and NeoGradle MDKs exist for 26.3, 26.2, 26.1.2, 1.21.11, and 1.21.1. The inspected ModDevGradle templates use plugin 2.0.148 and Gradle 9.2.1. Source: [NeoForge MDKs](https://github.com/NeoForgeMDKs).
- Mojang metadata and the inspected templates agree on Java 25 for these 26.x candidates and Java 21 for these 1.21.x candidates.
- Fabric is still a later evaluation. Its separate build tooling is not a source for this NeoForge scaffold's Java/Gradle/plugin pins.

## Similar Or Adjacent Mods

### Undead Nights

Source: https://modrinth.com/mod/undead-nights

- Exact NeoForge releases verified for 26.3 (2.2.6) and 26.2 (2.2.5).
- Adds configurable zombie horde nights, making it a useful first test of defence, targeting, and capped reinforcements.
- Recommended first optional threat test for the modern port. Its raids and Stand and Hold's future events should be tested separately before enabling both schedulers.

### Contagion

Source: https://modrinth.com/mod/contagion

- Exact NeoForge 2.1.1 releases verified separately for 26.3 and 26.2.
- Adds infection pressure around zombies; it is a coexistence test candidate, not a substitute for SRP's parasite evolution.
- Do not infer that infected players should become soldier targets or promise special cure/effect integration.

### Scape and Run: Parasites

Source: https://www.curseforge.com/minecraft/mc-mods/scape-and-run-parasites

- Forge 1.12.2.
- Very large audience and still active.
- Core strengths: parasite evolution, reinforcements, merge mechanics, territory pressure, iconic enemies.
- Lesson: escalation is compelling when enemies visibly adapt and the world reacts.
- Stand and Hold angle: build the human counter-escalation side instead of cloning parasite escalation.

### The Flesh That Hates

Source: https://www.curseforge.com/minecraft/mc-mods/the-flesh-that-hates

- Forge 1.20.1/1.19.2/1.16.5.
- Uses biomass as a dynamic difficulty layer.
- Infection punishes players who ignore it and rewards players who fight it.
- Lesson: world pressure needs a readable resource or threat meter.
- Stand and Hold angle: human intelligence, supplies, and research should be the counter-resource.

### Fungal Infection: Spore

Sources:

- https://modrinth.com/mod/fungal-infectionspore/versions
- https://www.fungalinfectionspore.wiki/

- The step 0.3.2 check verified `spore_1.21.1_2.2.0j_neo.jar` for NeoForge 1.21.1. No exact match was found for the newer candidate Minecraft versions; older Forge lines remain design reference.
- Focuses on infected forms, evolution, terraforming, structures, effects, items, blocks, and config.
- Wiki lists compatibility with mods such as Create, Farmer's Delight, Better Combat, Alex's Caves, Bigger Reactors, and more.
- Lesson: compatibility with popular ecosystem mods helps infection-style mods fit into modpacks.
- Stand and Hold angle: integrate with major tech/combat/info mods as a resistance infrastructure layer.

### Sculk Horde

Source: https://modrinth.com/mod/sculk-horde

- Forge, 1.20.x and earlier lines.
- End-game challenge with evolution, a central intelligence concept, and world corruption.
- Lesson: a named central intelligence/faction identity makes threat progression easier to understand.
- Stand and Hold angle: humanity should have equally readable command structure: local response, army command, special division, main base.

### From Another World

Source: https://modrinth.com/mod/from-another-world

- Fabric, Forge, NeoForge, Quilt for 1.20.1 and nearby versions.
- Uses a consistent non-random presence that spreads like an organism and emphasizes survival fairness.
- Requires AzureLib and FromAnotherLibrary.
- Lesson: players appreciate enemies that feel persistent and fair rather than arbitrary.
- Stand and Hold angle: use persistent bases, patrols, and threat records so human response also feels grounded.

### Project Parasites

Source: https://modrinth.com/mod/project-parasites

- Fabric 1.21.1.
- Meteor outbreak, corruption phases, difficulty modes, adaptive enemies, safe-zone tools, scanners, flamethrowers, hazmat gear, purification tools.
- Lesson: a clear difficulty choice and counter-tools help players understand an extreme threat.
- Stand and Hold angle: provide configurable resistance strength presets for modpacks.

### The Hordes

Source: https://www.curseforge.com/minecraft/mc-mods/the-hordes/about

- Supports Forge and NeoForge depending on Minecraft version; NeoForge 1.21.1 has a verified published artifact, but its Modrinth Atlas Lib dependency link points to an incompatible legacy file. See the compatibility record before installing.
- Horde wave attacks, configurable nights, infection options, zombie-player mechanics.
- Lesson: scheduled wave pressure is easy for players to understand and good for video moments.
- Stand and Hold angle: outpost defense events and reinforcements should be readable and configurable.

## Market Gap

Many adjacent mods focus on the infection side:

- spreading terrain
- evolving hostile factions
- infected mobs
- boss nodes or central minds
- survival horror pressure

Stand and Hold can stand out by focusing on the opposite fantasy:

- human bases that grow from camps to main bases
- research labs converting samples into countermeasures
- soldiers, scientists, engineers, medics, and special units
- supply lines and evacuation/defense objectives
- compatibility with several threat mods instead of dependence on one

## Attention Strategy

The mod should be easy to show in screenshots and short videos:

- before and after base upgrades
- a Field Command Post under attack
- soldiers arriving as reinforcements
- lab research turning parasite samples into countermeasures
- a main base lighting up when the world reaches a danger threshold
- a clear "humanity fights back" tagline

Recommended public-facing pitch:

> Stand and Hold is a human resistance and base-defense mod for parasite, infection, and apocalypse modpacks. As the world gets worse, humanity responds with research, outposts, soldiers, supplies, and special divisions.

## Content Direction Notes

- Early content should be survival-readable: samples, command post, lab, defender, first raid.
- Mid-game should be about logistics: supplies, outposts, patrol routes, research upgrades.
- Late-game should be about spectacle: main bases, elite units, counter-offensives, aircraft or artillery-style support.
- Vehicles and planes should come late. Start with scripted support systems or deployable assets before complex free-moving vehicle physics.
- Advanced weapons should be game abstractions. Avoid realistic construction detail and keep gameplay focused on balance and readability.
