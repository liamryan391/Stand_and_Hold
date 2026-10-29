# Outpost Expansion Design

M16 design decision: **PROCEED with new-site provenance and read-only planning/preview; DEFER expansion writes until their failure/recovery tests pass in a separately scoped milestone.** This document records that contract, not a durability guarantee. The only M16 structure change was the separately requested empty lab in new checkpoints. No pins, dependencies, unit tiers, shared population caps or upgrade prices change.

M17 implements only the admitted provenance/planner/read-only preview portion. M18 adds the single default-disabled confirmed apron path after native fault tests; see [operator usage and receipt format](structures.md#confirmed-apron-application). This is not power-loss or arbitrary third-party callback proof.

M18 implementation policy: keep `outpost preview` read-only. A separate `outpost prepare` action, available only when `expansionEnabled` is explicitly enabled, issues a transient 200-tick consent token for `outpost expand <token>`. An additive bounded `site.operation` receipt records the exact 137 before/after cells, operation/site IDs and protected original-footprint digest before writes. `outpost status` only observes; explicit `outpost recover` may seal a fully matching all-after receipt, never place/repair blocks. Prepared/all-before state needs fresh preparation; mixed/conflicting/unsupported state stops for operator review. Receipt changes preserve unknown fields. No on-load scan/replay, auto-rollback, extra cost/reward or durable-transaction guarantee is admitted. The application command is wired only after fault fixtures pass.

## Player-Facing Growth Direction

Decision following Liam's 2026-10-28 M18 feedback: **retain the tested backend, replace the operator procedure as the ordinary interaction.** M19 implements the menu/manual-or-automatic interface below using the same bounded applicator. The detailed M18 apron/token contract remains the retained operator path, not a requirement for ordinary players. Larger authored layouts and automatic level funding remain future work. The [reference review](gameplay-direction-and-references.md) records the gameplay rationale and original-art/dependency boundaries.

Liam's clarification: **player controls supplement commands; they do not remove them.** Keep existing preview/prepare/expand/status/recover tools for development, diagnostics and server administration, including their explicit confirmation where useful. Add focused commands when a real test or administration need warrants them, with help, argument completion, clear results and appropriate permissions. Ordinary players should not need those tools to progress. GUI actions and commands share authoritative gameplay services rather than drifting into different cost, placement or capacity rules; explicit admin grants/debug shortcuts remain clearly distinguished from earned play. Test both entry points. A simpler player interface is not a reduction in developer tooling.

- Normal manual path: right-click the Command Post, see the available growth/requirements, press a clearly labelled Expand button. No typed token, mandatory preview/status command, or recovery step. A concise affected-area description belongs in the menu; the server obtains a fresh plan and rechecks immediately when the action executes. A nearby valid menu is not permission to trust arbitrary client coordinates or block lists.
- Save a per-site Manual/Automatic preference. Existing sites start Manual, and switching to Automatic is one explicit opt-in for that site's supported growth; a server disable still wins. Ordinary play uses the existing local menu interaction rules and server policy rather than requiring operator UUID commands. Do not invent a faction/claims system for this small interaction; document that external claim-mod integration is unverified and preserve its opt-out.
- For the first implementation, Automatic means **apply the one supported apron after the post has already earned local level 2**, not automatically spend supplies, increase levels, add units or grant rewards. Check only loaded eligible tiles on a bounded existing cadence; never hash/scan every post every tick, create chunk tickets or retry a failure continuously. Show a readable blocked reason and pause on ambiguity. Existing price, stage, research and population rules stay unchanged.
- Preserve old operation receipts and operator commands as advanced diagnostics. An already applied site displays Expanded even if later legitimate storage changes differ from the historical digest; do not treat that as an order to repair or erase the receipt. Mixed/unknown interrupted state pauses growth for review rather than silently replaying writes. Normal messages are concise and transition-only, not a warning on every menu open/tick.
- Larger authored layouts and automatic funding/level progression are separate follow-ups. New usable yard space, removal/replacement of obsolete inner walls, watchtowers and later second floors need an explicit protected edit set, sensible entrances and AI access. Never blindly regenerate the whole base, refill crates or remove custom builds. An unsupported old/manual/Main Base site stays usable as a building but cannot be silently adopted into managed growth.
- Verify menu permissions/range, stale and duplicate actions, manual/automatic choice, disabled/blocked state, repeated loaded ticks and chunk unload/restart with GameTests and a two-JVM fixture. Review normal/large-scale screenshots yourself. This is not permission to weaken M18's protected-block, bounded-write, once-only or unknown-save tests; extend those tests for the new interaction.

The desired longer-term experience is open post -> Upgrade, with automatic physical growth if chosen and resources available, or manual timing if preferred. Local building level, physical layout revision and global human stage remain distinct under the hood. No security terminology, receipts or developer identifiers should be necessary to understand that experience.

M19 implementation contract: an additive `growth` compound on the originating tile has version 1, Manual/Automatic mode and a bounded paused-reason code. Missing data means Manual; malformed/future data is retained and disables growth without disabling normal building storage. Mode changes are explicit/idempotent local menu actions; operators also get `outpost grow` and `outpost mode manual|automatic`. All original commands remain. Manual uses immediate fresh preflight and the existing applicator; it does not consume a typed token. Automatic checks at the existing loaded-post response cadence (default 200 ticks), never at load time, never loads chunks, and never resumes an existing interrupted receipt. A refused attempt saves a pause, not a repeated retry loop; explicit Expand or mode selection retries after the obstruction is cleared. Applied state is authoritative even after legitimate storage changes. The menu displays only cheap saved-policy/eligibility state, not terrain hashes. Existing menus' player/identity/distance rules apply, with expansion additionally requiring six-block proximity; server disable always wins. No new price, capacity, population or offline catch-up is admitted.

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

Operator commands (preview introduced in M17; separate opt-in actions in M18):

```text
/standandhold outpost preview
/standandhold outpost prepare
/standandhold outpost expand <confirmation-token>
/standandhold outpost status
/standandhold outpost recover
```

The M17 preview remains read-only. It reports supported/ineligible/blocked state, site UUID/layout revision, old/new bounds, 88 ground replacements, total write count, unchanged buildings/capacity and the first refusal reason. It does not place markers/ghost blocks, grant resources, change tile NBT, dirty SavedData or start work. M18 preparation is separate and keeps consent transient until confirmation.

Confirmation requires a server-issued single-use token bound to player UUID, dimension/site UUID/anchor, current local level/stage, applied revision, config/policy fingerprint and exact expected-before/after delta plus protected-footprint digest. Expiry: 200 unpaused game ticks. The transient cache is bounded to 64 entries server-wide; expiry/revocation happens on action/disconnect, with no global tick handler. Restart invalidates tokens; one player's token cannot authorize another post/player.

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

This contract is checked by the automated ladder; observed M18 results belong in [Automated Testing](automated-testing.md). It must not be attributed to the earlier M16 lab-only pass:

| Ladder | Required proof |
| --- | --- |
| Logic | Exact 88/45/4 delta, unique <=256 cells, <=2048 reads, original footprint excluded, bounds for negative chunks, invalid/missing/future provenance, unsupported radii, token binding/expiry/replay |
| GameTest | New-site receipt vs old/manual/Main Base refusal; eligible level-2 preview; terrain, water, border, unloaded and occupied-container refusal; unchanged blocks/NBT/balances/registries/loaded-chunk count on preview |
| GameTest, before confirm admission | Actual 137 writes once; two-player stale-token contention; changed config/stage/level/terrain; entity collision/128-candidate saturation; injected refused writes/callback conflicts at first/middle/last cells; protected lab/crate/post and adjacent site unchanged |
| Two real JVMs | Supported provenance/applied receipt persists; no lab/crate refill, resource/reward/cap/UUID reset; prepared/all-before, mixed and completed states stop or resume only under the documented rule; new tokens required |
| qaClient | Real preview text fits at normal/large scales, no unintended command execution, exact bounds/refusal/ground-replacement warning visible; if confirm later exists, use an actual server-authoritative command |
| Human last | Only after automated mechanics: is the apron useful/not cramped, does entrance movement feel sensible, and is operator consent understandable? Real multiplayer/packaged installs remain separate |

The new checkpoint lab is separately verified using existing logic/GameTests/restart/qaClient. Those results cannot be presented as expansion/provenance/rollback proof.

**Admission verdict:** M17 provenance/read-only preview was followed by the M18 single opt-in application only after required native fault tests passed. Final release/merge still requires its complete build/restart/client/CI ladder. No full base generator, automatic growth, claims system, campaign, vehicles, animation library or durable transaction guarantee is included.
