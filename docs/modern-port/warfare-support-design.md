# Warfare Support And Vehicle Boundary

M13 design decision, 2026-10-27. The first prototype is a **supply-funded reinforcement call-in at a loaded Command Post**, not a drivable vehicle or aircraft. Stand and Hold owns military progression, deployment permissions, costs, quotas and persistence; a future optional vehicle integration may own physical movement. No runtime library, external mod, art or pin change is selected.

## Integration Review

Author-controlled release listings checked for the exact pinned Minecraft **26.3 AND NeoForge** combination:

| Candidate | Observed publication | Decision |
| --- | --- | --- |
| [Immersive Aircraft](https://www.curseforge.com/minecraft/mc-mods/immersive-aircraft/files/all) | Latest listed NeoForge file is 1.7.0 for **26.2**, dated Oct 18; 26.3 is absent from the listed game versions | No matching artifact verified; do not install a 26.2 jar on 26.3 |
| [Immersive Vehicles](https://modrinth.com/mod/immersive-vehicles/versions) | Author listing advertises through **26.1.x**, NeoForge/Forge; content packs are separate | No 26.3 artifact verified; do not infer compatibility from a nearby version |
| [Automobility unofficial port](https://modrinth.com/mod/automobility-unofficial-port/versions) | 26.3 listed, but **Fabric only** | Loader mismatch; not a NeoForge dependency |

This is an availability review, not runtime interoperability proof or an exhaustive claim that no vehicle mods exist. Listings may change. The Modrinth filtered-version API could not be retrieved through the web tool, so no exact matching artifact/hash or transitive set is claimed. Recheck exact files, licence, manifests, client/server and removal behavior before proposing installation under the dependency policy. No source/assets were copied.

## Selected Prototype Contract

- `/standandhold support status` and `support reinforce` are player actions, not admin summon cheats. Look directly at a loaded Field Command Post within 6 blocks; no saved-registry/world scan or remote deployment.
- Default unlock: Stage 1 plus Field Communications; default cost: 10 **global** supplies. Config can disable it and set stage, research and cost. It does not grant stage/research or bypass a dormant Main Base.
- One request uses the existing reinforcement service, shares its saved cooldown, post and nearby defender caps, bounded dry-ground search and patrol target. Success creates at most one existing human unit; there is no separate call-in entity budget.
- Supplies are reserved on the server before deployment. Known refusal returns the reservation. Successful deployment spends once, repeated/concurrent requests cannot double-spend or bypass cooldown. No asynchronous gameplay mutation; no new packets or client authority.
- A genuinely unexpected exception after reservation retains the debit and reports it in chat/server log: blindly refunding an unknown partial spawn could duplicate paid support. Operator recovery/whole-world backups are required in that exceptional case; this small prototype is not a cross-file power-loss journal.
- Ownership is the world's existing shared human faction/supply bank, not a new personal/private-base system. Spectators, console, distance and wrong-block attempts refuse safely. Multiplayer operators should recognize that any eligible nearby player can spend shared supplies; permissions/teams are a separately scoped extension.
- No physical delivery animation is implied: this is a reinforcement request, not an invisible aircraft or simulated transport convoy. Existing deployment warning/chat is the feedback.

## Using A Call-In

These are usage instructions, not another required numeric QA sequence:

1. Run `/standandhold support status` to see your server's configured requirements.
2. With defaults, reach human Stage 1, complete **Field Communications**, and have at least **10 global supplies**. Supplies stored locally in a building must be exported first if the global bank is short.
3. Close the building menu, stand within **6 blocks**, and look directly at the **Field Command Post** block with a clear line of sight. A dormant Main Base post cannot provide support.
4. Run `/standandhold support reinforce` in chat. A successful request deploys one eligible defender near the post, uses the existing bounded patrol behavior, and reports the supply payment.
5. Cooldowns, full post/area quotas and unsuitable ground can refuse the request. Known refusals spend nothing. The call-in does not expand the default 3-per-post / 8-nearby defender limits, and it is not an aircraft delivery animation. Both commands are available to ordinary players; progression/resource grants remain operator-only.

No need to reset cooldowns or remove healthy defenders to make a test pass. If the post is already full, a refusal is correct; judge the call-in during normal play when a deployment slot is available. Costs, quotas and restart behavior already have automated coverage.

## Hard Performance Budget

| Work | Upper bound / owner |
| --- | --- |
| Idle call-ins | Zero tick handler, zero new entities/packets |
| One request | One 6-block ray cast, one tile check, existing defender quota query, at most 8 spawn candidates x 5 heights |
| Region target | Existing nine local/adjacent saved-region lookups, no global threat sort |
| Population | Existing configured per-post 3 / nearby 8 defaults, unloaded UUID reservations still count |
| Persistence | Existing supplies, roster, response cooldown and NPC/home; no new save schema or offline catch-up |
| Failure / unload | No chunk loads, no queued replay on reload, existing backoff on unsuccessful eligible terrain/quota attempts |
| Multiplayer | Requests serialize on server thread; two-player contention, cap and debit/refund checks in QA; real multi-client latency/load remains separate |

## Physical Vehicles: Deferred Admission Gate

Any drivable vehicle needs a fresh design and approval if it needs a runtime library. Define at least: server ownership/driver authorization, bounded input rate, authoritative collision and velocity, passenger mounting/dismount recovery, original model/animation and hitboxes, stable UUID/schema/inventory, no chunk tickets, stop on unloaded boundaries, reconnect/driver loss and destroyed-vehicle recovery without duplication. Measure 1/4/8 active vehicles, multiple real clients, CPU/tick cost and packet/entity counts before setting a cap; do not invent a performance pass from headless players.

Aircraft support should begin with bounded call-ins or original non-ridable visual set pieces, not free-flight physics, autonomous bombing or distant force-loaded flight. Dedicated-server class safety and animation licensing/pipeline are admission requirements. Transport markers, supply deliveries, deployable cover, troop carriers, ground vehicles and planes are candidates, not additional M13 implementations.

## Verification Contract

GameTests must prove cost/unlock refusal, successful one-unit request, cooldown/no second debit, two-player contention, post/area caps, unsafe terrain, dormant/stale post and no chunk-boundary loading. The two-JVM fixture checks supplies/cooldowns/roster persist. Existing screenshots must stay readable; no new GUI is required. Pass these and the clean build/green CI before merging. Real multiplayer load, motion, balance and packaged-installs must remain explicitly unverified when not observed.
