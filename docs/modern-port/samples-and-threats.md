# Samples And Optional Threat Rewards

## Defaults And Attribution

Stand and Hold works with no threat mod installed. The only active default is **`minecraft:zombie` as a test entity**: 5 human points and a 35% chance of one Parasite Tissue Sample. Other zombie variants are not implicitly included. No external mod IDs/classes/assets are required or bundled.

Default rewards require the fatal damage source's owner to be a server player, including a player-owned projectile. Environmental deaths, unattended falls/burning, NPC kills and console `/kill` do not count by default. `requirePlayerKill = false` allows those deaths too; this is useful for isolated server tests but permits automated farms. [Human NPCs](human-npcs.md) reuse these selectors for targeting, even with zero rewards; players and other humans are excluded independently. No separate human-unit reward attribution is added.

The server observes `LivingDropsEvent`, after vanilla has accepted the death (so a cancelled `LivingDeathEvent` cannot award points). An event cancelled before this listener is skipped; the drop pipeline can still suppress samples if another mod cancels drops afterwards. A small persistent flag on the victim prevents repeated rewards/drops for the same entity, without a global entity-UUID map. Resurrection/reuse of that exact entity is not a fresh reward source. Samples respect the pinned version's `minecraft:mob_drops` gamerule; points can still accrue when it is false. Looting does not multiply this prototype drop. Reward overflow/unsupported saves refuse the reward rather than wrapping the balance.

## Mapping Configuration

Edit the server's `config/standandhold-threats.toml` (synced to clients). Values reload through NeoForge's config events; no per-tick config/JSON parsing or world scan is added.

```toml
requirePlayerKill = true
mappings = ["minecraft:zombie|5|0.35|1"]
```

Each string is `selector|points|drop chance|sample count`. Selectors are verified lower-case `namespace:path` entity IDs, or `#namespace:path` entity-type tags. Points are 0..1,000,000, chance is 0..1, count is 0..64. Zero disables that part of the reward; an empty mapping list disables all rewards. Only the first 256 entries are processed. Invalid entries are skipped independently with a warning at config load/reload, not on every death. No bare names or malformed IDs are guessed, and no default-registry fallback (such as pig) is used for absent entities.

An exact entity mapping wins over tag mappings, even if the tag appears first. Otherwise the first matching tag wins. Rewards never stack; duplicate selectors keep the first valid entry. Unknown IDs/missing mods and absent/empty tags simply do not match. An exact zero-reward mapping can intentionally suppress a broader tag reward.

The bundled `#standandhold:test_threats` tag contains only the test zombie and is **not an additional active reward mapping**. Example alternative:

```toml
mappings = ["#standandhold:test_threats|5|0.35|1"]
```

For optional modpack tags, a datapack can supply `data/<namespace>/tags/entity_type/<path>.json`. Set optional missing entries to `required: false`:

```json
{"replace":false,"values":[{"id":"example_threat:verified_entity","required":false}]}
```

`example_threat:verified_entity` is an **inactive documentation placeholder**, not a verified entity. Verify IDs and exact Minecraft/NeoForge artifact compatibility in the installed mod's official documentation, runtime command completion (`/summon <namespace>:`), or the spawned entity's F3+I debug data. Do not treat a display name or legacy 1.12 registry ID as proof. No contemporary SRP mapping is enabled or asserted here. Test a verified entity with its mod installed, then remove the mod in a disposable world and confirm Stand and Hold still loads. Do not use valuable saves for missing-content tests.

## Physical Samples And Buildings

`standandhold:parasite_tissue_sample` is a stackable item (64) with an original 16px specimen-pouch icon. It is also available in the Stand and Hold creative tab or `/give @s standandhold:parasite_tissue_sample <count>` for QA. No crafting recipe is added; recovery is the gameplay source.

In any building menu, the bottom-right mode button switches between Supplies and Samples. **Store samples** moves up to 64 from the 36 main inventory slots/offhand, limited by storage room. **Take samples** moves up to 64 into real free/mergeable main-inventory space. Remaining items stay at the source; full inventory/storage refuses movement. Creative-mode transfers still consume/return real items and do not use the vanilla creative `Inventory.add` discard shortcut. Named/component-modified tissue items are fungible when banked: names/components are not preserved on withdrawal. No other item type is consumed.

Local sample capacity is 4,096. Saved sample counts use the existing building schema; no legacy conversion/new save format is needed. The same active-menu, owner, distance, dimension, block-entity, server-thread and writable-save checks guard sample actions. Competing actions execute serially on the server. Normal building destruction returns remaining samples in at most 64 stacks; vanilla block-drop suppression and admin replacements can suppress recovery, so take stored items before destructive edits. Upgrades remain non-portable.

Labs/Command Post upgrades spend exact local samples through their existing checked transactions; completion is still once-only world research. The research admin command cannot take samples from an arbitrary building and refuses nonzero sample costs. Initial Sample Analysis/Field Communications retain their M3 zero-sample testing costs; Special Division Training consumes 5 samples plus 100 local supplies at Stage 5 after both prerequisites. A pack can change `sample_cost` in [research definitions](foundation-data.md) to exercise early sample-funded research. This phase does not rebalance existing research or stages.

When a lab completes research, all online players receive `Research complete: <title>` in chat, even with the menu closed. Research is global, so another lab on that target, later ticks and save/reload do not replay the notice or rewards. Old completed research does not announce retroactively; `/standandhold research status` still shows it.

## Recovery Missions

Start `/standandhold mission start recover_parasite_samples` (operator/commands enabled). A successful physical sample pickup, sample withdrawal, or player-issued mission start observes the actual count carried in main inventory/offhand. The default objective is **carry 3 at once**; the highest observed count is saved monotonically. Three separate deposit-after-each-one trips do not count as three, and different players' smaller counts do not add together. This small cooperative mission shares completion/rewards globally, but observes one player's carried count at a time.

Dropping/re-picking the same sample cannot raise that maximum or repeatedly grant rewards. Full inventory/cancelled pickups do not produce progress. Completion gives 25 human points and 10 supplies once; progress-only feedback goes to the observing player only when it changes. Items obtained by Creative/admin commands can qualify when observed; this is not an anti-cheat/provenance system. Missions still start explicitly; there is no campaign GUI or automatic next-mission activation.

## QA Recipe

1. Launch without other mods, confirm the sample name/icon/creative entry and `/standandhold status`. At night, spawn a `minecraft:zombie` and kill it by player melee/projectile. Expect +5 points once; repeat kills to observe probabilistic drops (not every kill). An unconfigured entity must not award points/samples. `/kill`/environmental damage must not count with the default attribution.
2. Start the recovery mission, carry 1 sample, drop/re-pick it repeatedly: still 1/3. Carry 2, save/reload: still 2/3. Carry 3: exactly +25 points/+10 supplies; repeated pickups/restarts do not regrant. Also test a partial pickup and a full player inventory.
3. Store/take samples through each building; compare actual inventory + local totals before/after. Check near-full (4,095), full (4,096), empty storage and full inventory, including Creative mode. Unrelated items and equipment slots must remain untouched. Test two players competing over the final stored sample; only one can receive it. Repeat after GUI close/reopen and save/reload.
4. For a fast gated-consumption test in a disposable world, use `/standandhold supplies add 150`, `/standandhold setstage 5`, and complete `parasite_samples` / `field_communications` by command. Give/recover 5 samples, import 100 supplies into a lab, store the samples, select Special Division Training and press **Start research**. Selecting alone must not advance work or spend. Pause/resume should retain work and match the progress bar. At defaults it needs 12 work/60 loaded seconds, then spends exactly 100 supplies/5 samples and grants 25 points once. Expect one `Research complete: Special Division Training` chat notice, also with the menu closed. Start with only 4 samples: no work/spending/notice; add the fifth and start. Reload with partial/paused work; test a second lab/repeated completion cannot pay/reward or announce again after more ticks, menu reopen and save/reload.
5. Test valid tag mappings, exact-over-tag precedence, an empty list, malformed/NaN/overflow entries, unknown entity IDs and missing tags. For deterministic server-only tests temporarily set `requirePlayerKill=false`, chance 0/1 and sample counts 0/64; verify `mob_drops=false` suppresses samples. Restore the saved config afterwards. These overrides are not default balance evidence.
6. Break a stocked building normally: remaining physical samples return once. Admin replacement may discard them; test only disposable storage. Remote/stale menu actions after moving >8 blocks, dimension change, spectator state or block removal must not move items. Keep genuine two-client/packet-fuzz coverage separate from pure transaction tests.
7. Re-test any external threat mod only when a matching artifact/verified IDs are available. Unknown/absent mappings must not crash or accidentally reward vanilla fallback entities. No external threat-mod runtime pass is implied by vanilla testing.

Build with the [pinned modern toolchain](building.md); exact observed build/launch/CI evidence and pending manual coverage are recorded only in STATUS.md.
