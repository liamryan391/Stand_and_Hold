# Status
Updated: 2026-11-11

## Position
- Isolated M30 trial passed with approved Undead Nights 2.2.7 on NeoForge 26.3.0.102. M30 and this trial are not merged.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- Main remains on NeoForge 26.3.0.57-beta. Trial retains MC 26.3 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Trial clean build (34,623 logic assertions), all 168 GameTests, two-JVM restart, and fresh prior-pin save read passed.
- Trial qaClient passed (94 captures); reviewed guide, Main Base, Lab and specialist views (8a4a22b4 prefix).
- Exact trial jar reached Done in dedicated DEV, verified progression and Main Base placement, saved and stopped. Art/pack preparation and Windows CI 43035903118 passed.
- Exact trial jar passed two installed production server launches: progression, research and Main Base stock persisted, with clean stops.
- Packaged Modrinth client passed Mods, new world, all three menus, zombie combat/drop and world reload with the exact normal jar.
- [Undead Nights trial](compatibility.md#isolated-undead-nights-227-result) passed isolated client/server coexistence and backed-up removal; vanilla mapping still works.

## Risks And Decisions
- No packaged external-mod install, real multiplayer, natural Horde Night load or infection proof. Main's beta pin is still incompatible with 2.2.7.
- Decide separately whether to promote 26.3.0.102 to main or keep the external combination unsupported there before M31.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Preserve `1.12`, alpha tag, fixtures and user saves; no claim-mod/power-loss guarantee or public-release approval.

## Next Milestone
`Continue: M30 steps 30.1-30.4 closeout (decide whether to promote the verified NeoForge 26.3.0.102 pin or keep the optional combination trial-only before M31).`
