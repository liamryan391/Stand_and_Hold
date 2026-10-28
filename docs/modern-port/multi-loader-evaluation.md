# Multi-Loader Evaluation

Decision: **DEFER Fabric implementation**, 2026-10-28 (M14). Keep the working NeoForge project intact. This completes the evaluation/proceed-or-defer gate, not a Fabric prototype or parity claim; roadmap 14.4-14.5 remain unexecuted. No build/source/resource changes, downloads, added libraries or pin selections are made by this decision.

## Baseline And Availability

The pinned NeoForge alpha has observed clean-build, 2,442 logic-assertion, 22 GameTest, screenshot-client and dedicated-restart proof. This is technical stability, not a stable public release: real multiplayer latency/sustained load, packaged installs and successful external infection-mod gameplay remain separate gaps. Duplicating platform maintenance now would compete with finishing that alpha.

- The [official NeoForge index](https://maven.neoforged.net/releases/net/neoforged/neoforge) lists newer artifacts without the beta suffix, including [26.3.0.76](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.76/). Therefore this is **not** a claim that all 26.3 NeoForge builds are still beta. The project remains on its approved/tested **26.3.0.57-beta**; no newer artifact has been installed or tested here.
- [Fabric's 26.3 developer announcement](https://www.fabricmc.net/2026/09/15/263.html) recommends Loom 1.17 and Gradle 9.6.0 and names Loader 0.19.5 at publication. That differs from this project's Gradle 9.2.1/ModDevGradle stack. It is a review requirement, not proof that another configuration cannot work, nor permission to replace this build.
- The [Fabric API release listing](https://github.com/FabricMC/fabric-api/releases) includes **0.165.0+26.3**, published October 23. Matching published versions establish availability, not binary/gameplay/save compatibility. No Fabric Loader/API/Loom artifact is approved as a new pin or dependency by this evaluation.
- [Architectury's 26.3 setup documentation](https://docs.architectury.dev/api/getting-started/setup/) describes common/Fabric/NeoForge modules and additional API/build tooling. It is a possible future approach, not installed or verified support for Stand and Hold. Never copy its moving snapshot example into our pinned build.

Fabric has a potential audience and loader-specific packs, but no demand/usage estimate or market-share claim was measured. Availability alone does not justify twice the testing/support surface. Threat mods remain optional on either loader; a Fabric-only mod is not a NeoForge integration.

## Source Audit

Inspected the actual entrypoints, registry classes, event/reload callbacks, menus/buildings, save ownership, worldgen hooks and QA registration. Files below are relative to `src/main/java/com/liamryan/standandhold/` unless noted. A class lacking a NeoForge import is not automatically portable: injected Minecraft methods and assumptions also matter.

| Boundary | Existing coupling | Port/parity obligation |
| --- | --- | --- |
| Lifecycle and metadata | `StandAndHold`, client `StandAndHoldClient`, `@Mod`, mod/event buses, `neoforge.mods.toml` | Separate entrypoints/physical sides and dependency metadata; dedicated server must never link client classes |
| Registries and attributes | `BuildingRegistry`, `NpcRegistry`, `SampleItems`, `EquipmentRegistry`; `DeferredRegister`, entity attributes, menu extension | Same IDs/components/recipes, different registration/attribute hooks; no QA content in either jar |
| Events | `ThreatEvents`, `ResponseEvents`, `SampleEvents`, `EquipmentEvents`, command registration | Reproduce death/drop attribution, cancellation/order, pickup receipts, equipment stats and once-only rewards; do not assume similarly named callbacks are equivalent |
| Config and optional mods | `config/*` uses `ModConfigSpec`/synced config; `OptionalThreatCompat` uses FML mod discovery | Explicit adapter for server config authority/sync, validation/defaults and optional loaded-mod checks; never add hard threat dependencies |
| Definitions/reload | `FoundationResources` uses retained NeoForge listener keys/server resource managers | Preserve server-owned catalog, validation and stale-menu rejection; no static world cache |
| Menus/networking | `IMenuTypeExtension`, `openMenu(building, building::writeMenuData)`, vanilla `ContainerData`/button packets | Recreate bounded opening payload/long-value sync and owner/distance/action checks; clients cannot spend or spawn independently |
| Saves and lifecycle | `HumanWorldData`, `ThreatWorldData`, building/NPC save methods; tile `onLoad`/removal hooks | Preserve logical receipts/schema/thread checks; port lifecycle registration/recovery deliberately. Fresh worlds only; no promise that saves can switch loaders |
| Injected methods | Entity `getPersistentData`, spawn cancellation and menu-opening extensions | Identify loader additions even without imports; persist death markers, raid home/expiry and spawn rollback through verified target APIs |
| World generation | `ResistanceSites` registration and `data/standandhold/neoforge/biome_modifier/resistance_sites.json` | Different biome attachment mechanism; retain seed/chance/dimension/one-chunk preflight and no-force-load behavior |
| Rendering/assets | Renderer/screen/layer events; vanilla humanoid model and original JSON/PNG assets | Registration/renderer differences still need compilation and screenshots; original art/data are reuse candidates, not proof of a shared source set |
| Verification/build | MDG runs, NeoForge `RegisterGameTestsEvent`, QA mod metadata/source set | Platform-specific run/registration adapters; same behavior assertions, real two-process persistence and screenshots; independent CI artifacts |

Potential shared pieces include progression/amount rules, definition validation, mission/research transactions, immutable state records, menu number packing/text fitting and original data/art. Many still use Minecraft types or config/services. Do not move whole packages into a nominal `common` module on the strength of import counts.

## Strategy

| Option | Benefit | Cost / decision |
| --- | --- | --- |
| Independent full ports | An isolated initial spike cannot break NeoForge's build | Permanent duplication risks behavior/save/security drift; acceptable only as a short-lived proof, not the default long-term architecture |
| Shared rules/data plus thin platform modules | Reuse proven behavior without requiring a cross-loader runtime abstraction | Later gradual extraction, platform bootstrap/config/hooks remain explicit; preferred direction **if** a Fabric proof warrants it, not a refactor authorized now |
| Architectury API/build tooling | Offers cross-platform abstraction and module conventions | Additional runtime/build dependencies, licensing/transitives, changed tooling and exact-target testing need admission; not selected by this evaluation |

Keep the current single NeoForge project. An approved future Fabric spike should first prove only startup, progression persistence and one existing building interaction in isolation. Decide module extraction from that evidence; do not restructure the working mod before the proof.

## Reopening Gate And Parity Contract

Before executing 14.4, Liam must accept the NeoForge alpha stability evidence and approve an exact isolated Fabric toolchain plus any Loader/API/library additions under [dependency policy](foundation-scope.md). Review any NeoForge pin upgrade independently; never silently substitute a newer version to fix another mod. No automatic install, fallback or prototype is queued by this document.

If approved, verify launch/client/dedicated server; unchanged point/stage thresholds; research/mission receipts; supplies/samples and menu authority; defensive malformed-save handling; structure registration/cleanup; NPC protection/loadouts; raid/call-in caps, cooldowns/refunds and restart behavior. Reuse all 22 behavior cases where applicable and identify missing platform cases explicitly. Check original art/screens and both production jars' QA isolation. Add separate parity CI/support matrices **only after** the spike succeeds (14.5). Mixed-loader networking and cross-loader world conversion are not promised.

No Fabric build, run, GameTest, multiplayer, library compatibility or save-switch test was performed. Published API/version documentation is not a substitute for those observations. The defer decision preserves the user's NeoForge-first, stable-alpha-before-expansion policy.
