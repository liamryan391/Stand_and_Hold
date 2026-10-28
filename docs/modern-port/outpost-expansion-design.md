# Outpost Expansion Design

M16 design decision: **PROCEED with new-site provenance and read-only planning/preview; DEFER expansion writes until their failure/recovery tests pass in a separately scoped milestone.** This document is a contract, not an implemented expansion system. The only M16 structure change is the separately requested empty lab in new checkpoints. No pins, dependencies, unit tiers, shared population caps or upgrade prices change.

## Distinct State And Eligibility

| State | Meaning / authority |
| --- | --- |
| World human stage | Technology and unit eligibility; never a command to rebuild sites |
| Command Post level | Existing saved 1-5 local upgrade level; controls M15 capacity, not physical shape |
| Layout ID and revision | Exact original authored layout, radius and anchor; separate from building level |
| Site provenance | Future server-generated UUID/origin receipt, not a claim over surrounding player land |
| Applied footprint | Last confirmed physical layout revision, independent of preview or local level |

Only a **new, successfully generated default-radius checkpoint** with a future supported provenance receipt can enter the first expansion path. Natural and debug generation use the same validated layout. For the first candidate, require radius 4, center at chunk-local (8,8), known floor height/orientation, an ordinary operational Command Post at the saved anchor and local level >=2. World stage is still checked by existing upgrade/unit rules; it does not substitute for the local level.

Player-placed posts, all existing M16-or-earlier sites without a receipt, unsupported radii/revisions, removed/replaced posts and Main Bases refuse with a specific reason. Do not infer ownership from matching stone bricks or silently adopt old structures. Adoption, configurable expansion sizes, rotated layouts and Main Base growth need their own design. A level-5 ordinary post is not automatically a generated Main Base.

Breaking the originating post invalidates its live site identity. Replacing it does not recreate the receipt, transfer ownership or clear surviving defender reservations. Missing/unloaded tiles remain conservative; never load their chunks to repair metadata. Discovery/research/mission receipts are historical and are not reset by a ruined site.

## One Small Physical Candidate

Candidate `checkpoint_outer_apron_v1`: a **9x9 to 13x13** outer fortification/apron, centered on the original checkpoint and still contained in one chunk. Existing inner walls stay; the intermediate ring provides a narrow walkable path. The original three-wide south entrance aligns with the new entrance.

- Add 88 floor cells in the outer annulus, 45 one-high outer wall cells and four supported corner torches: **137 planned writes**, with an implementation hard ceiling of **256 unique cells**. No new building blocks, loot, seeded supplies, NPCs or rewards.
- Do not move, replace or remove any cell in the original 9x9 footprint. In particular, never recreate the Command Post, lab or crate, or rewrite their NBT. Existing storage, lab work, menus, notice bits, UUID homes, deployment timers and cooldowns remain authoritative.
- Exact floor y comes from proven generation metadata, not a new heightmap search. First candidate requires flat outer support at that height: no digging, vertical fill, caves survey, terrain blending, walls through trees or bridge construction.
- No repeated repair/refill: an applied receipt stays applied even if the new blocks are later broken. Players repair normally. The local upgrade already paid its normal costs; physical application grants no extra capacity and has **no additional debit/reward** in this first design. Do not introduce another cross-file currency transaction.

This is a small fortification, not a barracks/vehicle facility or a full growing-base system. Its perceived space/pathing value needs a later in-motion play check. M15's capacity policy remains separate: level targets 3/5/6/7/8, explicit server ceiling, nearby-area ceiling and 16-UUID roster hard limit all still apply.

## Protection And Work Budgets

Server thread only, explicit operator action, no tick queue or automatic response to stage/level changes:

1. Resolve the looked-at loaded post within six blocks; require operator level 2, same dimension, non-spectator, supported site/building saves and actual tile identity. Recheck allowed dimensions, world border and build height. Do not accept arbitrary remote coordinates or a force/overwrite flag.
2. Check chunk availability before **every** terrain access. The entire 13x13 envelope (local x/z 2..14) must remain in the originating chunk. Reads, writes and ordinary neighbor support stay within its margins; never request a chunk/ticket.
3. Budget at most **169 columns, 2,048 block-state reads, 256 unique planned cells, and one bounded loaded-entity query** per action. Abort at the budget, not after a partial scan. Entity inspection stops after 128 candidates and refuses conservatively if saturated. No saved-world sort or global entity/chunk scan.
4. Verify the original Command Post identity and protected original-building positions. The delta contains no original-footprint cells. Reject any block entity, container, fluid/waterlogged block, non-replaceable overhead block, unsafe support, out-of-border cell or entity collision in the write delta. No clearing entities or automatic relocation.
5. Floor replacement accepts only the same dry, full natural-ground whitelist as initial generation, at the exact flat y, with two support blocks. Above-ground writes require air (or explicitly whitelisted replaceable flora). Do not overwrite player walls/chests just because an operator requested expansion.
6. State matching cannot distinguish player-placed grass/dirt identical to terrain. Preview must explicitly disclose the exact ground cells to be replaced; operator confirmation is consent for that bounded ground replacement, **not** a claim that all ground is untouched nature or that claim/protection mods approve it. Dedicated claim-mod integration is not promised. If this ambiguity is unacceptable on a server, leave expansion disabled.

Proposed application is disabled by default until the write/recovery exit gate. Read-only preview can be offered independently. No raised global population cap, dependency or protection bypass is needed.

## Preview And Confirmation Contract

Proposed operator commands (not implemented by M16):

```text
/standandhold outpost preview
/standandhold outpost expand <confirmation-token>
```

The first implementation candidate exposes **only preview**, not the second command. It reports supported/ineligible/blocked state, site UUID/layout revision, old/new bounds, 88 ground replacements, total write count, unchanged buildings/capacity and the first refusal reason. It does not place markers/ghost blocks, grant resources, change tile NBT, dirty SavedData or start work.

A later confirm implementation requires a server-issued single-use token bound to player UUID, site UUID/anchor, current local level, applied revision, config/policy fingerprint and a digest of expected-before/after delta states. Expiry: 200 unpaused game ticks. Bound the transient cache to 64 entries server-wide; expire lazily on action/disconnect, with no global tick handler. Restart invalidates tokens; one player's token cannot authorize another post/player.

On confirm, repeat the complete loaded/identity/dimension/terrain/entity/level/config validation. A cached preview is not permission to use stale terrain. Consume the token before side effects; serialize requests on the server thread and allow only one application per site revision. Wrong, expired, replayed or changed-state requests produce no writes or debit. Existing Main Base stage notices and menu actions do not invoke this path.

## Provenance, Saves And Failure

Future provenance belongs to the originating Command Post's chunk, not a new global site database. Suggested additive `site` compound, with its own `site_version = 1`, UUID, kind/origin, layout ID/revision, radius, floor/center/orientation and applied-footprint revision. Missing site data means **unproven**, not an automatic migration/adoption. Preserve unknown site fields; unsupported/malformed site versions disable site actions without resetting storage/progression. Older binaries may discard a new tile field when saving: downgrades are not round-trip safe, so restore whole-world backups rather than reconstruct provenance. HumanWorldData remains schema 5 unless an actual requirement proves otherwise; no legacy conversion.

Generate provenance only after successful placement and required-tile validation. Feature workers may initialize the new tile's metadata, but never access server SavedData. Existing server-thread `onLoad` registration remains the source of post/base registry entries; previews never register a nonexistent site. Do not change defender ledgers or seed buildings while creating site metadata.

Do **not** reuse `StructurePlacement.place` as an expansion applicator: it preflights an empty footprint, clears above it, reconstructs buildings and holds only in-memory block-state rollback. That is suitable for its current fresh-site contract, not existing-site ownership or crash recovery.

Before any future mutation, record a bounded intended delta/operation ID and expected-before/after states at the post. `setChanged()` is **not** fsync; an in-memory before-image is not a durable write-ahead journal. Blocks and tile receipt share a chunk, reducing cross-file scope, but M16 does not claim atomic disk snapshots or power-loss recovery. No async writes or yields between validated writes; check expected state again before each write because block hooks can change the world.

| Observed recovery state | Conservative behavior |
| --- | --- |
| No operation, supported provenance, original footprint | Preview only; never infer earlier permission |
| Prepared and all expected-before cells | No automatic replay; fresh validation/confirmation required |
| All expected-after cells and matching operation | Verify and seal applied revision; no cost/reward replay |
| Mixed before/after, callback failure, missing receipt or unknown state | Stop, retain diagnostic state, mark needs-review; no automatic rebuild/rollback/refund |
| Removed/replaced anchor or unloaded chunk | Refuse; do not resurrect the post or load chunks |

On ordinary mid-write failure, report operation ID/first changed/conflicting cell and retain the bounded delta for operator review. Do not roll back over a newly placed container/player block, reset the defender ledger, or refund unknown side effects. Whole-world backups remain required; recovery initially means explicit operator review/restore, not an automatic transaction guarantee. A future implementation must demonstrate how the intended delta survives serialization **before enabling writes**. If it cannot, keep confirm disabled and ship only read-only preview.

## Verification Plan And Admission

These are required **future tests**, not passed M16 expansion evidence:

| Ladder | Required proof |
| --- | --- |
| Logic | Exact 88/45/4 delta, unique <=256 cells, <=2048 reads, original footprint excluded, bounds for negative chunks, invalid/missing/future provenance, unsupported radii, token binding/expiry/replay |
| GameTest | New-site receipt vs old/manual/Main Base refusal; eligible level-2 preview; terrain, water, border, unloaded and occupied-container refusal; unchanged blocks/NBT/balances/registries/loaded-chunk count on preview |
| GameTest, before confirm admission | Actual 137 writes once; two-player stale-token contention; changed config/stage/level/terrain; entity collision/128-candidate saturation; injected refused writes/callback conflicts at first/middle/last cells; protected lab/crate/post and adjacent site unchanged |
| Two real JVMs | Supported provenance/applied receipt persists; no lab/crate refill, resource/reward/cap/UUID reset; prepared/all-before, mixed and completed states stop or resume only under the documented rule; new tokens required |
| qaClient | Real preview text fits at normal/large scales, no unintended command execution, exact bounds/refusal/ground-replacement warning visible; if confirm later exists, use an actual server-authoritative command |
| Human last | Only after automated mechanics: is the apron useful/not cramped, does entrance movement feel sensible, and is operator consent understandable? Real multiplayer/packaged installs remain separate |

The new checkpoint lab is separately verified using existing logic/GameTests/restart/qaClient. Those results cannot be presented as expansion/provenance/rollback proof.

**Admission verdict:** ready to implement a narrowly scoped **provenance + read-only preview** milestone with existing built-ins. No unresolved pin/library decision is needed. Actual expansion/confirm is deferred until protected-state and failure/restart fixtures demonstrate the above semantics. No full base generator, claims system, campaign, vehicles or animation library is included.
