# Combat Equipment

## Scope And Roles

This is a small Minecraft-scale equipment set, not a modern weapons simulator. No ammo inventory, magazines, reload sequence, new NPC tier or runtime library is introduced. Optional entity/tag mappings identify infection threats and rewards; ordinary hostile mobs are also valid defensive targets. No external threat mod is required.

| Equipment | Configured stage/research gate | Role and default stats |
| --- | --- | --- |
| Anti-Threat Blade | Stage 1 + Initial Sample Analysis | 6 total player base damage, 1.5x against configured threats; player hits can hurt all non-player mobs, including humans |
| Army armour, four pieces | Stage 1 + Field Communications | Helmet/chest/legs/boots defence 2/6/5/2; ordinary durable field protection |
| Elite armour, four pieces | Stage 3 + Field Communications | Defence 3/7/6/3 and 1 toughness per piece; later protection, no set bonuses |
| Prototype Launcher | Stage 1 + Field Communications | One original bolt per use, 6 damage, 20-tick player cooldown, 30-tick maximum flight; hostile mobs or configured threats |

Equipment is available in the existing creative tab, through `/give` and ordinary JSON crafting recipes. **Players can use/equip found gear at any stage by default**, including Elite armour and the launcher; research is not required simply to use an item. NPC launcher/loadout gates remain mandatory. Modpacks can opt into the former strict player gates with `gatePlayerEquipment=true`; only in that mode do the table's gates restrict player use/equip. Recipes/possession are never gated. Creative/admin inventory edits are not a security boundary; already-worn armour is not stripped after an administrative stage reset.

NPC/opt-in strict-player unlocks use world-global progression and research. Missing/malformed/future saved data or a missing required definition fails those gates closed; ordinary free player gear does not query progression at all. No unlock receipts, supplies or research are consumed by equipping/firing. Synced `standandhold-equipment.toml` controls this policy, gate values and combat stats; static durability/basic recipes remain simple. Re-equip armour after configured protection changes.

Army Riflemen may receive a non-dropping prototype in an empty main hand after unlock. Empty-handed Survivor Defenders may receive a non-dropping Blade after Stage 1 + Initial Sample Analysis; `survivorBladeEnabled` controls new issue. An issued blade and its saved entitlement stay usable after stage drops or disabling new issue; new unarmed defenders still need the gate. Losing/removing the physical blade does not reissue one below the gate. Existing nonempty hands are not overwritten. Vanilla held-item modifiers add to intrinsic NPC attack (default Survivor 3 + blade modifier 5 = 8, or 12 raw against mapped threats, before mitigation). Riflemen retain their launcher role. No new deployment budget, NPC target class, real weapon construction details or external assets are added.

## Crafting And Repair

Use an ordinary crafting table. Obtaining iron teaches the ten recipes through one ordinary hidden recipe advancement; this grants no currency/research. `/recipe give @s *` exposes recipes in a disposable QA world, or use an exact ID such as `/recipe give @s standandhold:army_helmet`. Namespaced wildcards are not supported. No custom crafting UI or serializer is needed.

| Recipe | Ingredients |
| --- | --- |
| Army Helmet | 3 iron, 2 leather; rows `III` / `L L` |
| Army Chestplate | 7 iron, 1 leather; `I I` / `ILI` / `III` |
| Army Leggings | 5 iron, 2 leather; `III` / `L L` / `I I` |
| Army Boots | 2 iron, 2 leather; `L L` / `I I` |
| Each Elite piece | Shapeless: matching Army piece, 2 iron, 1 gold, 1 tissue sample |
| Blade | 2 iron, 1 sample, 1 leather; ` I` / `SI` / `L ` |
| Launcher | 4 iron, 1 copper, 1 sample, 1 leather; `ICI` / `ISI` / ` L ` |

`I` is iron, `L` leather, `S` a Stand and Hold tissue sample, `C` copper. Each recipe makes one item. Elite crafting consumes the Army item and its old damage/enchantments/components; it is not a component-preserving smithing system. Use ordinary iron anvil repair for armour, blade and launcher. Armour/blade durability uses static material defaults (Army multiplier 15, Elite 25, blade 250); launcher durability is 256. Ordinary sword/armour and durability tags classify these items for vanilla systems; specific enchantment/trim combinations still need separate QA. Creative uses follow vanilla durability rules. The bolt icon is a renderer/test item, not consumed ammunition and has no recipe.

## Authority And Bounds

- NPC and opt-in strict-player stage/research gates are checked on the logical server, including melee and bolt impact. Default player access is free, not an admin/Creative-only bypass. In strict mode, armour right-click/ordinary slots check the gate; already-worn protection is not stripped after admin resets. Dispenser armour equipping remains disabled. Friendly protection, cooldowns and durability still apply in both modes.
- Armour and blade modifiers use synced config. Re-equip after config edits so the living entity refreshes its modifiers. Recipe ingredients, durability and visuals are static; they are not driven by research rewards or supply transactions. Invalid-length defence lists fall back to the documented four defaults.
- Launcher cooldown uses vanilla item cooldowns shared across copies/hands. Relogging follows vanilla cooldown behavior, not a new persisted player timer. A shot spends durability and starts cooldown only after successful server spawn. No client packet supplies damage, position or a victim.
- Bolts save age, lifetime, damage and vanilla owner/velocity. Invalid/missing age or invalid lifetime expires safely; reload never grants a fresh lifetime. Invalid/unavailable/dead/other-dimension owners cannot damage anything. Ownerless admin summons are harmless. No portals, explosions, terrain damage, redstone projectile callbacks, homing or pickup drops.
- Each flight step checks at most four already-loaded chunks along its short movement box and the world border/height; it never requests chunks or tickets. Four impact particles and one sound are emitted per collision. There is no global projectile scan. Default lifetimes/cooldowns bound each continuously firing actor to a few live bolts; manual admin summons are outside normal use limits.
- Player bolts/blade hits can damage non-player mobs whether hostile, passive, mapped or human; avoid shooting your defenders. Players and allied teams remain protected. NPC bolts still require hostile/mapped targets and cannot hurt players, other human NPCs or allies; ineligible bodies block rather than transmit shots. The blade's extra multiplier remains mapped-only. Broader player hits/targeting grant no extra point/sample rewards: mappings and attribution still apply.
- Humans target hostile mobs by default (`targetHostileMobs` in `standandhold-npcs.toml`). Setting this false restores mapping-only NPC targeting, including their bolt impact check. Vanilla's `Enemy` classification includes normally neutral monsters such as Endermen; it does not include every angry animal. Add explicit mappings for optional threat entities that do not implement `Enemy`.
- Riflemen use existing 16-block bounded acquisition/home checks, vanilla ranged navigation, a 12-block firing limit, line of sight and a configurable minimum shot interval (40 ticks by default). Inside 3 blocks or while locked/disabled, they use melee. Auto-equipped launchers do not drop and do not overwrite occupied hands. Shot timestamps and the held stack save normally; no world-global ticking or new deployment source is added.

## Reproducible QA

1. Use a fresh modern world with cheats enabled, no threat mods. Confirm Mods, items, tooltips/models. At Stage 0 with no research, player blade/launcher use and normal right-click/Survival armour-slot equip must work for both sets, while a test Rifleman must not receive a launcher. In a separate disposable config with `gatePlayerEquipment=true`, repeat the old fail-without-item-loss checks; Creative/admin replacement is not a permission boundary.
2. Run the following setup once. In a fresh world it spends exactly 30 global supplies, completes two research entries and leaves 180 human points at Stage 1:

   ```text
   /standandhold supplies add 30
   /standandhold setstage 1
   /standandhold research complete parasite_samples
   /standandhold research complete field_communications
   ```

3. Equip Army and Elite armour at Stage 0, inspect F5 front/back/sides/face and attributes. Only opt-in strict player mode locks Elite until stage 3/research. Save/reopen with partial durability and verify no duplicates/stacked attributes. Test recipes/anvil repair in Survival with disposable materials.
4. Kill zombies with the blade and launcher (not `/kill`). A player-attributed kill still gives the default +5 human points and possible tissue drop. Keep firing/alternate copies and hands: no faster than the configured cooldown. Test walls, water, misses, shield/ally interception and range expiry; no terrain changes or accumulating bolts. Check Survival durability loss/breaking and Creative behavior separately.
5. Player-fire at an unmapped animal and a human NPC: both should take damage; players/allied teams must remain protected. NPC shots must still spare animals/players/humans and use hostile/mapped targets only. On a disposable save, spawn an empty-handed Survivor, reach stage 1 and complete Initial Sample Analysis: expect a blade after about one loaded second, normal melee and no drop. Save/reopen; reset stage to 0: the previously issued blade stays usable, but a new Survivor remains unarmed. Save/reopen and repeat; the existing issued entitlement must not disappear. Test actual two-client/PVP and one verified optional threat separately.
6. Spawn a Rifleman and a zombie 4-12 blocks apart after unlock. Expect one held launcher pointing away from his face, bounded shots/sounds and no friendly hits. Check a skeleton/creeper is also targeted, without new points/samples under the default zombie-only reward mapping; Survivors should approach those mobs too. Inspect both main-arm poses and the Elite Helmet icon (no detached bright pixels). Reset stage or disable prototype use, move targets behind a wall/outside home and confirm no ranged shots; do not treat eggs as automatic deployment tests. Save/reopen and confirm one existing weapon/quota, not extra units/items. Inspect a reload with an in-flight bolt: no new lifetime or damage reset.
7. Retest failed/removed research and unsupported saves in disposable backups: NPC/strict-player authorization must fail closed without crashes; default player found gear remains usable. Config armour changes need re-equipping. Check no per-tick log spam and missing/invalid optional threat mappings separately from pure tests.

Original icons and 64x32 worn atlases are generated from `scripts/GenerateEquipmentTextures.java`, not imported player skins. Select the modern JDK and run `java .\scripts\GenerateEquipmentTextures.java` only when intentionally resetting this art. Armour uses the platform's ordinary equipment model/layer format, not custom animated geometry or an external library. Unedited [F2 references](../../art/references/README.md) record the worn Army set, corrected inventory icon and held launcher; the simple flat sprite is not final weapon art. The pinned platform source is the API reference; [NeoForge's item documentation](https://docs.neoforged.net/docs/items/) explains the component-based approach but may describe a different documentation version.

Observed build/runtime/CI and Liam's manual results belong in STATUS.md. Pure gate/flight/JSON tests do not establish real multiplayer, physical projectile reload, packaged-installation or external-mod results.
