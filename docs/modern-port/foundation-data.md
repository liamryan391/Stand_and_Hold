# Research, Supplies, And Missions

## Definition Contract

Defaults live in `src/main/resources/data/standandhold/foundation/definitions.json`. A server datapack can override the same `data/standandhold/foundation/definitions.json` path as a whole and apply it with `/reload`. Definitions use `format: 1`; this is separate from the saved-world schema.

For the pinned Minecraft 26.3 target, a test pack at `<world>/datapacks/my-foundation-pack/` needs the override file and this `pack.mcmeta`:

```json
{
  "pack": {
    "description": "Stand and Hold definition override",
    "min_format": [121, 0],
    "max_format": [121, 0]
  }
}
```

Use `/datapack list` to check activation after `/reload`; `/datapack enable "file/my-foundation-pack"` enables it if needed. `/datapack disable "file/my-foundation-pack"` restores lower-priority/default definitions without deleting the pack or saved receipts. This is a whole-file override, not an additive merge of individual entries.

The catalog is an immutable snapshot owned by the server's reloadable resources, prepared once per reload and shared by commands/buildings. There is no static world cache, directory scan, per-tick JSON parsing, or client-side definition authority. It is limited to 64 KiB and 64 entries in each list. Invalid definitions pause affected operations; close/reopen building menus after `/reload`.

- IDs are lower-case namespaced strings, at most 128 characters. Commands accept short IDs in the `standandhold` namespace. Keep published IDs stable.
- Research fields: `id`, `title`, `required_stage` (0-6), `prerequisites`, `supply_cost`, `sample_cost`, `reward` (`points`, `supplies`), and `unlocks` (capability IDs for later systems).
- Mission fields: `id`, `title`, `objective`, `target`, optional research `subject`, `prerequisites`, and `reward`.
- Objectives: `sample_recovery`, `command_post`, `supply_total`, `research_completion`, `human_stage`, `outpost_defence`, `main_base_discovery`.
- Duplicate IDs, missing prerequisites, cycles, unsupported objectives, malformed IDs, negative costs/rewards, fractional/overflowing integers, and invalid research subjects are rejected before transactions. Definitions are not partially applied. Missing/removed definitions do not authorize deleting saved completion records.

Three initial research entries are Initial Sample Analysis (`parasite_samples`), Field Communications (`field_communications`), and Special Division Training (`special_division_training`). The seven small missions follow sample recovery, command setup, supplies, first research, early stage, outpost defence, and main base discovery. Their physical gameplay hooks belong to later owning phases; these definitions do not add items, structures, or mobs.

## Supplies And Research

Global supplies are a non-negative signed-long balance, not an item inventory. Add/spend amounts must be positive. Insufficient funds or overflow leave the balance unchanged.

Research spends supplies, not human progression points. Stage and prerequisite research are gates. Completion applies the supply cost, both rewards, and a permanent completion/reward receipt in one server-owned SavedData transaction. Repeating completion does nothing. An overflow cannot consume supplies or write completion. Capability IDs can be queried by later systems through `ResearchManager.hasUnlock`; they do not spawn or unlock content that has not been implemented.

| Research | Stage | Prerequisite | Supply / sample cost | Reward | Capability |
| --- | --- | --- | --- | --- | --- |
| `parasite_samples` | 0 | None | 10 / 0 | 10 points | `sample_analysis` |
| `field_communications` | 1 | `parasite_samples` | 20 / 0 | 20 points | `outpost_coordination` |
| `special_division_training` | 5 | `field_communications` | 100 / 5 | 25 points | `special_division_deployment` |

Physical [samples](samples-and-threats.md) can be recovered and stored in buildings. Entries with a nonzero sample cost must use a funded lab; command completion refuses before charging anything because it cannot select arbitrary local storage. Initial Sample Analysis retains its zero-sample M3 testing cost, not final gameplay balance. [Labs](buildings.md) spend local supplies/samples after timed work; commands spend global supplies immediately. Both share the same once-only research receipt.

## Mission State And Feedback

Missions are global cooperative objectives, not per-player quests. They must be started explicitly and prerequisite missions must already be complete. They do not automatically start the next mission. Completed missions cannot be reset or rewarded again.

| Mission ID | Objective / target | Previous mission | Point / supply reward |
| --- | --- | --- | --- |
| `recover_parasite_samples` | Sample recovery / 3 | None | 25 / 10 |
| `establish_field_command` | Command post / 1 | Sample recovery | 25 / 10 |
| `stockpile_supplies` | Global supply total / 20 | Field command | 25 / 0 |
| `complete_first_research` | Complete `parasite_samples` / 1 | Stockpile | 25 / 10 |
| `early_army_response` | Reach human stage / 1 | First research | 25 / 0 |
| `defend_outpost` | Outpost defence / 5 | Early army | 25 / 10 |
| `establish_main_base` | Main base discovery / 1 | Defence | 50 / 25 |

Supply totals, research completion, human stage, and registered Command Posts can be observed now: checked on mission start, `/mission status`, successful supply/research mutation commands, post registration, and bounded building transactions/intervals. Progress is monotonic. Stage mutations are observed at the next such refresh. No global scans, automatic campaign, per-tick mission checks, or recursive reward callbacks are added.

Sample recovery observes the highest number actually carried by one player on a successful pickup, withdrawal, or player-issued mission start. Carry 3 at once for the default mission; repeated drop/re-pick cannot farm progress. Partial progress and the once-only reward remain world-global. Outpost defence/base discovery still have debug progress only. `mission progress` and `mission complete` are explicit operator test overrides. Command Post registration is a real building hook; silent interval refreshes never spam chat. Command feedback reports rewards only for a new completion.

## Commands

Read-only `supplies status`, `research list/status`, and `mission list/active` are public. `mission status` may finish an already-earned active objective; its rewards are still server-validated. Mutating/debug commands require operator/gamemaster level 2 (commands enabled in singleplayer).

```text
/standandhold supplies status
/standandhold supplies add <positive amount>
/standandhold supplies spend <positive amount>
/standandhold research list
/standandhold research status
/standandhold research complete <id>
/standandhold mission list
/standandhold mission status
/standandhold mission active
/standandhold mission start <id>
/standandhold mission progress <id> <positive amount>
/standandhold mission complete <id>
```

Short IDs in the table above and their full `standandhold:` forms both work. Unknown IDs and invalid/missing arguments fail safely. Console commands omit `/`. A malformed definition override refuses the affected operation rather than falling back silently or consuming resources.

## Saved Data

Modern schema 3 stores `<world>/data/standandhold/human_progression.dat` with `supplies`, `research`, `missions`, and dimension-aware `command_posts` compounds. The same server-global owner is used in every dimension. Version 0-2 modern saves retain progression, receipts, and unknown fields, with empty newly introduced state. No legacy save import is added.

Research entries store `completed` and `rewarded` flags. Mission entries also store `progress`. A receipt and its balance changes are committed together. Either historical completion/reward flag is conservatively treated as claimed, preventing uncertain old rewards being regranted. Missing fields in an existing mission entry default safely; invalid progress/flags or invalid map types refuse that entry/map rather than silently resetting it. A current-schema missing receipt map also refuses access, since prior rewards cannot safely be inferred.

Unknown/removed definition IDs and unrelated nested fields are retained in saved NBT. Re-adding a definition with the same ID does not reset its completion. Removing a definition removes its active capability mapping, not its saved receipt. Future schemas remain protected against downgrade writes. Back up worlds; these hooks cannot repair physically corrupted files or recover deleted receipts.

## Test Recipe

Use a new disposable modern world and defaults. The build's `progressionTest` covers definition rejection, transaction atomicity, schema migration, malformed/unknown data, compressed saves, full-chain balances, and once-only rewards.

1. `research complete parasite_samples` with zero supplies must fail; points and supplies stay zero. `supplies add 10`, then complete it: expect 10 points, zero supplies. Repeat: no changes. Save/reopen and check research/status/supplies again.
2. On a separate fresh world, start `recover_parasite_samples`, debug-progress it by 2, save/reload, then progress by 1. Expect 25 points and 10 supplies. Repeat completion: no extra reward.
3. Start/complete `establish_field_command`, then start `stockpile_supplies`. Existing 20 supplies completes the stockpile mission immediately. Start `complete_first_research`, then complete `parasite_samples`: expect 110 points and 20 supplies in total after both research and mission rewards.
4. Start `early_army_response`, run `setstage 1`, then `mission status`: expect 175 points at Stage 1. Start/complete `defend_outpost` and `establish_main_base`: expect 250 points and 55 supplies. Save/restart and repeat completion attempts: balances must remain unchanged.
5. Test missing/unknown IDs, negative/zero amounts, insufficient supplies, overflow, stage/prerequisite gates, and command refusal of nonzero physical-sample costs. Test actual local sample consumption with the [sample QA recipe](samples-and-threats.md). Test a non-operator: public inspection works; mutations/debug commands are denied.
6. In a disposable test datapack, override one definition/reward, `/reload`, and verify it applies only to an uncompleted entry. Test a duplicate/cyclic/malformed override: a clear command error, no resource changes, no server crash. Disable that pack and `/reload` to restore defaults; keep removed-ID receipts.
