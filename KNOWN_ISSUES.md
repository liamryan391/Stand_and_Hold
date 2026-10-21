# Known Issues

These notes describe the modern NeoForge foundation. The retained `0.1.0-alpha.1` value comes from the legacy alpha; it does not announce a modern release. Live build/launch results are in [STATUS.md](docs/modern-port/STATUS.md).

## Build And Test Limits

- Use Java 25 and Gradle 9.2.1 from the modern wrapper, not the legacy Java 8 helper. See [Building](docs/modern-port/building.md).
- The normal jar retains the legacy filename; inspect `META-INF/neoforge.mods.toml` to avoid confusing incompatible artifacts.
- Build, client, server, and remote CI evidence are separate gates. A server EULA stop is not a passed launch; Liam must accept it himself.
- The [selected NeoForge loader](docs/modern-port/toolchain.md) is a beta build. M0 accepted that choice subject to actual build and launch verification.
- This Windows development launch logged OSHI/Perflib performance-counter warnings and system-report exceptions, then continued through mod initialization and graphics/audio resource loading. Recheck on the installed client/server; these messages alone are not a Stand and Hold crash. No Windows registry changes are made by the project.

## Foundation Limits

- The [three core buildings](docs/modern-port/buildings.md) provide storage, menus, generation/lab work and upgrades. [Two human NPC tiers](docs/modern-port/human-npcs.md) add bounded melee defence/deployment, not guns, advanced squads or a full army. Equipment and events remain planned; legacy feature availability is not modern completion.
- [Samples/threat rewards](docs/modern-port/samples-and-threats.md) use only the test zombie mapping by default; this does not establish compatibility with any external mod/version. Default kill attribution requires the fatal damage's player owner. Environmental/NPC kills are excluded unless the configurable all-deaths option is enabled; that option permits farms. Same-entity resurrection does not reset the once-only reward marker.
- The shared recovery mission observes a single player's highest actual carried sample count, not cumulative pickups across players/trips. Carry 3 simultaneously for the default objective; Creative/admin items can qualify when observed. Banking samples removes custom names/components; withdrawals create canonical tissue items. Defence mission gameplay remains deferred; base discovery records a non-spectator's command-post interaction, not mere generation. See [Foundation Data](docs/modern-port/foundation-data.md).
- Schema 5 preserves research/mission receipts, command posts and defender reservations, and adds Main Base registration/discovery. Malformed/missing current-schema maps refuse access instead of silently resetting history/quota. An unloaded/missing UUID is not proof of death: external edits/partial restores can strand conservative reservations and reduce deployment. Whole-world backups and disposable reload/removal tests are required; automated cross-file repair is deferred.
- [Checkpoint/Main Base structures](docs/modern-port/structures.md) are small open-air foundations, not finished bases or `/locate structure`-indexed templates. Strict terrain checks skip many forests, water, slopes and occupied areas; they cannot distinguish player-placed natural ground or survey deep caves. Natural attempts need new chunks, allowed dimensions and the biome modifier; default Superflat has biome decoration disabled (debug placement still works). Later features/mods can alter a site; registration validates its command tile, not every perimeter block. Discovery/completed mission history deliberately survives removal. Larger exploration/external-worldgen tests remain separate QA.
- Building/global storage transactions validate before spending on the server thread, but chunk files and global SavedData are not a power-loss journal. Back up the entire world; mixed/partial restores can disagree. This limits wider-alpha confidence under abrupt termination, not normal save/reload. Retest clean restart and keep crash/partial-restore tests disposable.
- Normal destruction recovers local supplies once and drops remaining samples, subject to vanilla drop rules; upgrades are not portable. Admin replacement/structure edits may suppress removal side effects and discard storage. Drain before edits; list/nearest cleans stale loaded posts without loading chunks. At global long overflow, supply recovery is refused/logged. Test normal breaking separately from `/setblock ... replace`.
- Original 16px utility art now has distinct front/side/top faces and editable Blockbench sources; buildings remain full cubes with a fixed north-facing front, not finished furniture. Menus have bounded labels, hover details, translated states and completed progress bars. Unusual GUI scales, custom fonts and extreme datapack text still need visual QA. A definition reload intentionally requires reopening a menu. Two-player/network-fuzz and packaged-installation checks remain separate coverage.
- Human art now has distinct original cap/pack and helmet/vest silhouettes on explicit 64x64 box-UV atlases, not copied skins or final animated characters. The repository exporter supports a restricted Blockbench subset, not arbitrary rotated meshes/clips; desktop editor round-trips remain separate QA. Army Riflemen currently melee. Test eggs/admin summons bypass stage/cap restrictions and can exceed automatic deployment limits manually; separated posts are not a global population budget. No hostile-mod reaction AI, squad commands or custom equipment drops are implied.
- NPC acquisition is deliberately throttled and requires line of sight/reachability; it can feel slow. Responsiveness/path selection remains later AI polish, not a combat rebalance in this phase. Labs now require explicit Start after selecting a target; old modern labs missing the new optional running flag retain partial work but load paused.
- Progression schema hooks normalize missing/malformed fields and preserve unknown fields, but cannot recover a physically corrupt save file. Newer schemas refuse progression access; restore a compatible mod/backup. See [save contract and tests](docs/modern-port/progression.md).
- Legacy worlds, saved NBT, and configs have no supported conversion path in the foundation. Use separate modern test worlds and retain valuable legacy saves outside the checkout.
- External threat mods remain optional. Legacy SRP mapping/test results do not prove support on the modern Minecraft/NeoForge target. Exact artifacts and IDs need later verification.
- The modern build needs no extra runtime library. Original art exports use vanilla humanoid animation; external animation/AI libraries and combined threat-mod installations remain separate decisions/tests.

These are foundation limits, not reports of observed modern runtime crashes.

## Legacy Forge 1.12.2 Issues

Use the [preserved legacy known issues](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/KNOWN_ISSUES.md) for Java 8/ForgeGradle errors, historical Actions queue notes, sample/research/mission behavior, NPC art, and save cleanup tests.

The [legacy README](https://github.com/liamryan391/Stand_and_Hold/blob/1.12/README.md) links to the matching installation and gameplay guidance. Legacy guides have been retired from the modern checkout; use their preserved branch copies for legacy testing.

## Verification Records

- [Current roadmap and task states](docs/modern-port/phase-roadmap.md)
- [Progress and publication evidence](docs/modern-port/progress-log.md)
- [Legacy transition inventory](docs/modern-port/legacy-inventory.md)
- [Modern build and launch instructions](docs/modern-port/building.md)
