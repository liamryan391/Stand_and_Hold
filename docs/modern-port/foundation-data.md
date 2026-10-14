# Research, Supplies, And Missions

## Definition Contract

Defaults live in `src/main/resources/data/standandhold/foundation/definitions.json`. A server datapack can override the same `data/standandhold/foundation/definitions.json` path as a whole and apply it with `/reload`. Definitions use `format: 1`; this is separate from the saved-world schema.

The catalog is an immutable snapshot resolved once per foundation command through the server resource manager. There is no static world cache, directory scan, tick handler, or client-side definition authority. It is limited to 64 KiB and 64 entries in each list; repeated automated gameplay hooks will need a server reload-owned snapshot in their owning phase rather than loading JSON each tick.

- IDs are lower-case namespaced strings, at most 128 characters. Commands accept short IDs in the `standandhold` namespace. Keep published IDs stable.
- Research fields: `id`, `title`, `required_stage` (0-6), `prerequisites`, `supply_cost`, `sample_cost`, `reward` (`points`, `supplies`), and `unlocks` (capability IDs for later systems).
- Mission fields: `id`, `title`, `objective`, `target`, optional research `subject`, `prerequisites`, and `reward`.
- Objectives: `sample_recovery`, `command_post`, `supply_total`, `research_completion`, `human_stage`, `outpost_defence`, `main_base_discovery`.
- Duplicate IDs, missing prerequisites, cycles, unsupported objectives, malformed IDs, negative costs/rewards, fractional/overflowing integers, and invalid research subjects are rejected before transactions. Definitions are not partially applied. Missing/removed definitions do not authorize deleting saved completion records.

Three initial research entries are Initial Sample Analysis (`parasite_samples`), Field Communications (`field_communications`), and Special Division Training (`special_division_training`). The seven small missions follow sample recovery, command setup, supplies, first research, early stage, outpost defence, and main base discovery. Their physical gameplay hooks belong to later owning phases; these definitions do not add items, structures, or mobs.
