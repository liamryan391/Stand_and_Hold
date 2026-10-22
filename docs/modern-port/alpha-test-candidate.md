# Technical Test Candidate

Prepared for controlled testing, **not published or tagged as a modern release**. The reused `0.1.0-alpha.1` string and jar filename do not make this compatible with the historical Forge 1.12.2 alpha tag. Never open a legacy world.

## Exact Target

| Component | Required target |
| --- | --- |
| Minecraft | 26.3 |
| NeoForge | 26.3.0.57-beta (beta-loader risk accepted for this foundation) |
| Java runtime | 25, 64-bit |
| Local build | Pinned wrapper Gradle 9.2.1 / ModDevGradle 2.0.148 |
| Normal artifact | `build/libs/standandhold-0.1.0-alpha.1.jar` |
| Extra required mod/library | None |
| External test mods | None for the showcase. Tested Contagion 2.1.1 is unsupported on this loader. |

Use [Building](building.md) for the pinned Java selector/wrapper, and inspect `META-INF/neoforge.mods.toml`: mod ID `standandhold`, version `0.1.0-alpha.1`, Minecraft `[26.3]`, NeoForge `[26.3.0.57-beta,26.4)`. Do not use a sources jar or the legacy GitHub release. Installer/profile instructions follow the selected loader's official documentation linked in the toolchain/build guides; do not substitute a moving latest loader.

Local artifact size/hash and observed build/launch evidence are recorded in STATUS.md when verified. A GitHub Actions build artifact is separate from a published mod release. Source history identifies its implementation; no new tag is created by preparation.

## Scope Of This Candidate

The [first playable loop](showcase-loop.md) connects existing progression, samples, shared missions, global/local supplies, Command Post/Lab/Crate menus, research, deployment and one bounded attack. Read-only help/guide improve orientation without granting costs or rewards. No new balance, advanced weapons, entities, large structures or runtime libraries are introduced for the showcase.

M11's external gameplay/modded-world removal gate was explicitly deferred by Liam. That decision does not make Contagion compatible or waive independent usability/multiplayer/packaged testing. See [Compatibility](compatibility.md), [Changelog](../../CHANGELOG.md) and [Known Issues](../../KNOWN_ISSUES.md).

## Test Procedure

1. Back up valuable modern worlds/configs outside the checkout. Use the exact target in a separate profile and a fresh disposable modern world.
2. Run the Java selector and set `.gradle-home/modern` before `clean build --no-daemon --stacktrace`. Inspect normal-jar metadata and absence of bundled external mods/test classes. Verify the relevant CI result separately.
3. For development smoke tests, use `runShowcaseClient` and `runShowcaseServer`; they use `run/showcase-client` and `run/showcase-server`, not normal or M11 compatibility directories. The dedicated console must reach Done, accept status/help/guide/save/stop and exit cleanly. Only Liam accepts an EULA if requested.
4. Development source-run success is not packaged-launcher proof. Test the normal jar in a separate exact-loader client/server installation before broad distribution; do not put a second same-ID jar into a source-run mods directory.
5. Follow the play sheet for about 15 loaded minutes. Record familiarity, startup/menu/world/reload, real sample acquisition, supply order, explicit lab Start/Pause, one defence, unit counts, repeated warnings and command dependence.
6. Retain `latest.log`, crash reports, profiler output and original screenshots/clip. On the pinned target, `/debug stop` emits its elapsed/tick/TPS summary to chat/log; it does not itself create a profile-results file. Preserve that summary. Singleplayer pause time can affect its wall-time average, so run a separate unpaused controlled sample before performance claims. This describes a local integrated server, not real multiplayer or long-session scaling. Record failures and tests not run without promoting them to passes.

## Release-Note Draft

Stand and Hold's modern technical foundation has a small guided alpha loop: sample recovery, a field command/lab setup, funded research, human defenders and an outpost attack, with saved world/building progress. Defaults remain testing-focused. The NeoForge loader is beta, art/structures/weapons are still simple, and infrastructure/mission starts in the demonstration are operator-assisted. No complete Survival campaign, verified external infection support, vehicle system, cross-loader build or broad multiplayer readiness is promised.

Actual publishing requires a separate approval, release/version/tag decision, checked artifact, supported-combination statement and release checklist. The preserved `1.12` branch and `v0.1.0-alpha.1` tag must not be moved or reused.
