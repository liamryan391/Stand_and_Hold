# Gameplay Direction And Reference Review

Review date: 2026-10-28. This is a design/research decision following Liam's M18 screenshots and play feedback, not an implementation or compatibility test. Exact project/file listings and selected public repository READMEs were reviewed; no external jars were installed, executed, bundled or mined for assets. Toolchain pins, gameplay defaults and the original-art policy are unchanged. Task order belongs to the [roadmap](phase-roadmap.md); live work belongs only to [STATUS.md](STATUS.md).

## The Experience To Build

**Stand and Hold: humanity rebuilds, organises and fights back.** The differentiator is a resistance that visibly develops around the player, not simply a collection of guns or another infection mod. Recovering samples should lead to useful research, stronger local infrastructure and recognisably different defenders; defending that investment should create stories worth sharing.

Liam's screenshots confirm the apron works, but expose two design problems: the normal experience looks like an operator procedure, and a narrow extra ring is not yet a satisfying base upgrade. Treat these as usability/layout feedback, not evidence that saves failed. `APPLIED` with a later protected-state mismatch can reflect ordinary lab/storage changes; `recover` was never intended as a routine gameplay step.

The intended ordinary loop is **open Command Post -> see requirements -> Upgrade**, with physical growth following its saved local level when Automatic mode is enabled. In Manual mode the player chooses when to apply the available layout. No player should copy a UUID, inspect an operation receipt or run recovery to perform an ordinary upgrade. Keep operator diagnostics available, but off the normal path.

The [growth interaction contract](outpost-expansion-design.md#player-facing-growth-direction) separates this target from the still-shipping M18 command prototype. It keeps protection of player builds, server authority and once-only resource effects; those are implementation responsibilities, not chores for the player.

## A Base Should Visibly Earn Its Level

These are original **design targets**, not implemented layouts, new prices, population defaults or promised features in a single milestone. A placed post already starts at Level 1; the first upgrade is Level 1 -> 2. Global human stage unlocks technology/units, while the local post level controls that particular site's development. A high-level ordinary outpost does not silently become a registered Main Base.

| Local level | Intended identity | Visible/useful direction |
| --- | --- | --- |
| 1 | Field Camp | Compact protected command area, usable Lab, modest storage, simple lighting/perimeter and a few defenders |
| 2 | Reinforced Outpost | Actually usable extra floor space, clearer gate, improved storage/cover and a small watch position; stronger garrison within shared limits |
| 3 | Military Outpost | Larger usable zones, watchtower and a second floor where terrain permits; patrol routes, fencing and supply/research space |
| 4 | Fortified Base | Stronger perimeter, guarded approaches, dedicated barracks/logistics areas and clearer defensive roles |
| 5 | Major Command Site | Coordinated larger defence and specialist facilities; separate generated Main Base identity/activation rules still apply |

Fences, wire, lights and sensors must be labelled honestly: decoration is not automatically damaging wire, detection or a working searchlight. Add one tested function at a time. Patrols need reachable routes/return-to-post behaviour, not more entities standing in a larger box. Extra storage means real saved capacity and usable access, not refilling loot on every expansion. Second floors and removal of old walls need new authored deltas and protection tests; the existing M18 apron cannot safely pretend to do them.

The existing local capacity profile is 3/5/6/7/8, subject to server ceilings, shared area limits and retained reservations. This review does not raise it. Larger armies require measured AI/load budgets, not just larger buildings.

## Enjoyment And Discovery

There is no researched formula here that guarantees popularity, and download totals do not prove why a mod succeeded. These are explicit project hypotheses to test with Liam during alpha and outside beta testers later:

- **Low friction, meaningful decisions.** Spend attention on where to defend, which research to fund and whether to grow, not remembering command syntax. Recognition-based menus reduce the recall burden; applying that to our UUID workflow is a design inference from [NN/g's interface research](https://www.nngroup.com/articles/recognition-and-recall/).
- **Visible payoff.** One earned upgrade should change useful space or defensive capability, with clear costs and a readable blocked reason. Show progress where work genuinely takes time; do not add a waiting timer solely to look complicated.
- **Automation with control.** Let players choose Manual or Automatic and pause growth; do not secretly spend their entire supply bank or overwrite a custom build. [MineColonies' Builder documentation](https://minecolonies.com/wiki/buildings/builder/) is a useful interaction reference: resource visibility and manual/automatic work assignment. That is not evidence that its automatic mode freely upgrades all buildings, nor a plan to copy its colony engine.
- **Readable original roles.** Silhouette, uniform accents, posture, held equipment, effects and audio should communicate purpose. Heavy/Elite units should change choices, not just add opaque stat multipliers. Improve one coherent model/animation pipeline before producing many near-identical soldiers.
- **Combat that feels responsive.** Judge detection delay, target switching, friendly behaviour, firing direction and base navigation before adding complex tactics. Keep configurable optional threat mappings and useful baseline play without an infection mod.
- **An identity worth showing.** Record real before/after base growth and a short defence encounter with the HUD visible, rather than a long logo intro or unbuilt feature list. [Valve's trailer guidance](https://partner.steamgames.com/doc/store/trailer) recommends showing actual gameplay quickly and making it understandable without sound; applying it to Minecraft mod clips is our inference, not a claim about Steam statistics predicting this mod's audience.
- **Trust before reach.** Publish accurate version/loader requirements, original screenshots, concise changes and known limitations. Offer an optional tested modpack/preset later, not a required shopping list of incompatible mods. Respect creators' community rules; no spam, fabricated footage or unsupported compatibility claims.

For alpha reviews, record approximate time to the first useful upgrade, whether it was achievable through the UI without admin commands, the first confusing blocker, and whether defending the improved site was enjoyable. No telemetry SDK or growth-marketing dependency is needed. Keep beta external feedback separate from Liam's maintainer tests; do not relabel either as the other.

## Reference Survey: Weapons, Equipment And Vehicles

Versions below were visible on author-controlled project listings on the review date. They are not exhaustive support matrices. **No verified 26.3 file** means this review found no exact candidate, not proof that a future port is impossible. A loader name alone never makes a 1.20.1/1.21.1 jar work on the pinned 26.3 target. Listed licences are observations, not blanket clearance of every bundled asset.

| Reference and primary source | Observed target/licence boundary | Useful lesson for an original implementation |
| --- | --- | --- |
| [TaCZ](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero), [author repository](https://github.com/MCModderAnchor/TACZ) | Main file 1.20.1 Forge 1.1.8-hotfix2; no verified 26.3 file. README separates GPL-3.0 code from CC BY-NC-ND 4.0 assets | Consistent first/third-person handling and data-driven equipment; not an asset/model pool |
| [Vic's Point Blank](https://www.curseforge.com/minecraft/mc-mods/vics-point-blank) | Listings include 26.1.2 NeoForge/Fabric and older targets, not a verified 26.3 artifact here; All Rights Reserved | Inspectable equipment, clear handling feedback and content-pack separation |
| [Mo' Than Enough Guns](https://www.curseforge.com/minecraft/mc-mods/mo-than-enough-guns) | 1.20.1 Forge/NeoForge addon; GPLv2 project badge; no verified 26.3 file | Purposeful additions to an equipment ecosystem rather than building every subsystem twice |
| [LesRaisins Tactical Equipements](https://www.curseforge.com/minecraft/mc-mods/tacz-lesraisins-tactical-equipements), [author repository](https://github.com/LesRaisins-Studios/LesRaisins-Tactical-Equipements) | 1.20.1 Forge TaCZ addon; repository separates GPL-3.0 code and reserved art | Role-specific gear and consistent attachment presentation |
| [Superb Warfare](https://www.curseforge.com/minecraft/mc-mods/superb-warfare), [author repository](https://github.com/Mercurows/SuperbWarfare) | 1.20.1 Forge / 1.21.1 NeoForge; code and asset terms differ, with art reserved; no verified 26.3 file | Distinct battlefield roles and vehicle/support tradeoffs; not permission to import its engine/art |
| [Vintage Vehicle Pack](https://www.curseforge.com/minecraft/mc-mods/sbw-vintage-vehicle-pack) | 1.20.1 Forge SBW addon; GPLv3 badge alongside explicit asset-rights reservations | Cohesive vehicle family; verify each content layer, not just the host mod's licence |
| [MCSP](https://www.curseforge.com/minecraft/mc-mods/mcsp-military-vehicle) | SBW addon, 1.20.1 Forge / 1.21.1 NeoForge; GPLv3 badge | Specialist vehicle silhouettes and a bounded addon boundary |
| [AshVehicles](https://www.curseforge.com/minecraft/mc-mods/ashvehicles) | 1.20.1 Forge / 1.21.1 NeoForge; GPLv3 badge; current description says standalone | Do not assume an old addon relationship still describes the current release |
| [VEB Automobilwerke Schwikau](https://www.curseforge.com/minecraft/mc-mods/veb-aws) | Immersive Vehicles content pack, including 1.20.1 Forge; public-domain project badge | Separate the vehicle engine from content packs; badge alone does not audit third-party components |
| [Survival Instinct](https://www.curseforge.com/minecraft/mc-mods/survival-instinct), [author-linked authorised port](https://www.curseforge.com/minecraft/mc-mods/survival-instinct-authorized-neoforge-port) | Original 1.20.1 Forge; authorised NeoForge listing 26.1.2/26.2; All Rights Reserved, no verified 26.3 file | Cohesive survival props/equipment; an authorised port is different from unrestricted copying |
| [Fracture Point, formerly Warborn Resurgence](https://www.curseforge.com/minecraft/mc-mods/fracture-point) | 1.20.1 / 1.21.1 listings; All Rights Reserved | Readable tactical equipment sets; do not infer reuse rights from a source link |
| [LesRaisins Armor](https://www.curseforge.com/minecraft/mc-mods/lesraisins-armor) | 1.20.1 Forge and older; All Rights Reserved | Different functional gear silhouettes rather than colour swaps alone |
| [Keerdm's Zombie Apocalypse Essentials](https://www.curseforge.com/minecraft/mc-mods/keerdm-zombie-apocalypse-essentials) | 1.20.1 firearm-specific variants; All Rights Reserved | Encounter/loot integration and environmental storytelling |
| [BlockFront](https://www.curseforge.com/minecraft/mc-mods/blockfront-world-war-ii) | Author listing includes 1.21.1 NeoForge; All Rights Reserved; no verified 26.3 file | Immediately readable battlefield identity and class roles, not copied uniforms or modes |

## Reference Survey: Defence, Threats And Presentation

| Reference and primary source | Observed boundary | Lesson / testing relevance |
| --- | --- | --- |
| [Simply Traps](https://www.curseforge.com/minecraft/mc-mods/simply-traps) | 1.20.1 Forge / 1.21.1 NeoForge; All Rights Reserved | Clear obstacle function and counterplay; permission for modpacks is not asset reuse permission |
| [SecurityCraft](https://www.curseforge.com/minecraft/mc-mods/security-craft/files/9139293) | Exact 26.3 NeoForge beta file found; MIT project, not installed here | Readable access/defence tools; borrow interaction principles, not its security-oriented workflow as the gameplay identity |
| [Doomsday Decoration](https://www.curseforge.com/minecraft/mc-mods/doomsday-decoration) | 1.20.1 Forge; All Rights Reserved | Original props can make a base feel inhabited; decoration must not imply unimplemented mechanics |
| [Chovy's Apocalypse](https://www.curseforge.com/minecraft/mc-mods/chovys-apocalypse) | 1.20.1 Forge / 1.21.1 NeoForge; All Rights Reserved | A consistent survival environment across structures and equipment |
| [Explosion Overhaul](https://www.curseforge.com/minecraft/mc-mods/explosion-overhaul-a-new-level-of-destruction) | Older-target releases including 1.21.1 NeoForge; custom licence | Separate feedback polish from terrain destruction; effects need performance/comfort controls |
| [EnhancedVisuals](https://www.curseforge.com/minecraft/mc-mods/enhancedvisuals) | Author lists a 26.3 NeoForge 1.8.35 file; LGPL-2.1 project badge | Optional screen effects are not a HUD framework; avoid obscuring targets or mandatory screen clutter |
| [Undead Nights](https://www.curseforge.com/minecraft/mc-mods/undead-nights/files/8958998) | Exact 26.3 NeoForge 2.2.6 file found; All Rights Reserved | Candidate isolated horde stress test after approval; do not assume a video's detection/destruction settings describe this file |
| [Zombie Awareness](https://www.curseforge.com/minecraft/mc-mods/zombie-awareness) | Visible listings through 1.21.1, including NeoForge; All Rights Reserved | Threat responses to sound/light create choices; bounded AI and optional compatibility still matter |
| [The Flesh That Hates](https://www.curseforge.com/minecraft/mc-mods/the-flesh-that-hates) | 1.20.1 Forge and older; CC BY-NC 3.0 badge; no verified 26.3 file | Escalation/territorial pressure reference, not a modern integration claim |
| [Dawn of the Flood](https://www.curseforge.com/minecraft/mc-mods/dawn-of-the-flood) | 1.20.1 Forge/NeoForge; custom terms distinguish code and protected assets | Coherent threat escalation and animation presentation; do not import Halo-derived identity/assets |
| [Fungal Infection Spore](https://www.curseforge.com/minecraft/mc-mods/fungal-infection-spore) | Includes 1.21.1 NeoForge; All Rights Reserved, no verified 26.3 file | Environmental infection pressure to test against later, not a dependency now |
| [Mutationcraft](https://www.curseforge.com/minecraft/mc-mods/mutationcraft) | Visible older Forge targets through 1.19.4; MIT badge with separate credits | Distinct threat tiers; its description links Dawn of the Flood, not a verified 26.3 port |
| [The Lost Cities](https://www.curseforge.com/minecraft/mc-mods/the-lost-cities/files/9277219) | Exact 26.3 NeoForge 12.0.0 file found; MIT project | Later urban-terrain coexistence QA, especially refusal around buildings/containers; not a required world preset |
| [DeceasedCraft](https://www.curseforge.com/minecraft/modpacks/deceasedcraft) | 1.20.1 Forge modpack, not a standalone compatibility library; All Rights Reserved | A connected loot/research/defence loop matters more than a large feature count |

ZOMBIEMANIA, Zombie Apocalypse: Optimized and the loosely named Mutants/Brute references were not resolved to exact author artifacts in this pass; no mod list, support or licence claim is made for those names. The supplied [HUD/UI video](https://www.youtube.com/watch?v=8nOsyODF4MM) could not be retrieved (HTTP 429); it was not watched/transcribed. The linked video descriptions were not treated as authoritative version or licensing evidence. More specialised HUD recommendations need exact project pages, not guessed video contents.

## Candidate Files Are Not Approved Dependencies

These checks establish **listed artifacts only**. None has passed Stand and Hold's exact pinned loader/client/server/coexistence/removal test in this review. All installations or runtime additions still require Liam's approval under the [dependency policy](foundation-scope.md#admission-and-exceptions). Check required/transitive mods and exact loader ranges before any test; a matching Minecraft label alone is insufficient.

| Candidate | Exact reviewed artifact | Admission decision |
| --- | --- | --- |
| [Undead Nights](https://www.curseforge.com/minecraft/mc-mods/undead-nights/files/8958998) | `UndeadNights-2.2.6-NeoForge-mc26.3.jar` | Propose one isolated horde QA instance later; no installation approval assumed |
| [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/8945638) | `geckolib-neoforge-26.3-5.5.7.jar`, MIT project | Preferred animation spike candidate only when an identified animation need justifies a required runtime library |
| [SecurityCraft](https://www.curseforge.com/minecraft/mc-mods/security-craft/files/9139293) | `[26.3] SecurityCraft v1.10.2.1-beta4.jar`; changelog states NeoForge minimum `26.3.0.53-beta` | Our `.57-beta` meets that declared lower bound, not proof of runtime/protection integration; optional later coexistence test |
| [The Lost Cities](https://www.curseforge.com/minecraft/mc-mods/the-lost-cities/files/9277219) | `LostCities-26.3-12.0.0.jar` | Optional later urban-terrain/worldgen test, not a toolchain or world-preset decision |
| [YACL](https://www.curseforge.com/minecraft/mc-mods/yacl/files/8935497) | `yet_another_config_lib_v3-3.9.7+26.3-neoforge.jar`, LGPLv3 project | Config-screen candidate, not a replacement for server-authoritative gameplay menus |
| [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/8952995) | `26.3.159`, NeoForge listing marked `26.3-snapshot`, LGPLv3 project | Snapshot label needs further exact-target validation; not selected alongside YACL by default |

The author listings also show [Architectury 22.0.5](https://www.curseforge.com/minecraft/mc-mods/architectury-api), [Balm 26.3.0.3](https://www.curseforge.com/minecraft/mc-mods/balm) and [Curios 17.0.0-beta.3+26.3](https://www.curseforge.com/minecraft/mc-mods/curios). These are listing-level observations, not an exact metadata audit. Architectury does not make incompatible mods cross-load; Curios slots do not supply a soldier model/AI; Balm belongs to BlayTheNinth's ecosystem, not BlameJared's. None is needed for the present base-control work.

Fabric API is for Fabric, not our NeoForge project. Kotlin for Forge is unnecessary for a Java-only implementation unless an approved dependency requires it. Player animation, NPC animation and NPC AI are separate requirements. Do not assume Fabric-oriented LibGui/owo-ui examples work on NeoForge. Pehkui, Placebo, Bookshelf and Moonlight remain unselected utility candidates without an exact artifact audit here; adding a list of libraries is not itself an improvement.

Use the current original Blockbench/export pipeline and native menus first. If animation outgrows it, evaluate **one** library using a small original soldier/weapon animation with first/third-person, armour, dedicated-server and normal-jar tests. For vehicles, follow the existing [warfare design](warfare-support-design.md): prove a small useful ground role, controls, collision, storage, saves and budgets before aircraft or a combined-arms catalogue. A new engine requires a real use case and approval, not only a compelling reference video.

## Reuse And Compatibility Rules

Not all Minecraft mods are open source. Public downloads, source visibility, modpack permission, code licences and asset licences are different things. TaCZ's code/assets split and the reserved art in the Superb Warfare/LesRaisins repositories are concrete examples above. Do not copy or derive our uniforms, models, textures, sounds, animations or code from their distributed content. Stand and Hold's existing hard rule remains **original implementation and assets**, even where an upstream licence might permit more. Any proposed exception needs an explicit project-policy decision and exact provenance review first.

Learn from high-level interaction patterns, role design, content schemas and publicly documented integration contracts. Prefer optional tags/registry mappings and public APIs to copied internals. Never download another mod during player startup, redistribute a pack without permission, or mark a combination supported because its file page looks promising. The known Contagion bootstrap failure remains unresolved; finding Undead Nights does not retroactively turn M11 into a passed external-infection test.

## Scope Discipline

Improve the current roadmap now rather than create a second competing plan. The unimplemented Heavy/Elite work is retained after the base-control milestone; the small physical-layout follow-up addresses the cramped apron separately. Towers, patrol improvements, additional floors, expanded storage and functional defences belong to bounded follow-ups with original layouts and automated tests. A larger content roadmap can be consolidated after these existing systems feel good together; no giant simultaneous guns/vehicles/aircraft/UI rewrite is approved by this review.
