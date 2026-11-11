# Status
Updated: 2026-11-11

## Position
- Isolated M30 loader trial approved for NeoForge 26.3.0.102 only. No threat mod approved or installed. M30 and this trial are not merged.
- The root handoff.md is the Codex/Claude entry point. STATUS remains authoritative; alternate implementation ownership and review.
- Main remains on NeoForge 26.3.0.57-beta. Trial retains MC 26.3 / Java 25 / Gradle 9.2.1 / MDG 2.0.148.

## Verified
- Trial clean build (34,620 logic assertions), all 168 GameTests, two-JVM restart, and fresh prior-pin save read passed.
- Trial qaClient passed (94 captures); reviewed guide, Main Base, Lab and specialist views (8a4a22b4 prefix).
- Exact trial jar reached Done in dedicated DEV, verified progression and Main Base placement, saved and stopped. Art/pack preparation and Windows CI 43035903118 passed.
- Exact trial jar passed two installed production server launches: progression, research and Main Base stock persisted, with clean stops.

## Risks And Decisions
- Packaged launcher profile imported with the verified normal jar. First launch and world reload pending. No real multiplayer or external coexistence/removal proof on this pin.
- [M30 screening](compatibility.md#m30-artifact-screening-2026-11-11): trial meets the newer artifacts' declared loader minimum. That is not runtime evidence; exact test-artifact approval is still required.
- Back up specialist saves; older builds do not understand the new entity IDs or Division roster marker. No downgrade or migration to legacy worlds.
- Runtime libraries/test mods, pins and fresh EULA prompts retain approval boundaries. Preserve `1.12`, alpha tag, M22 fixture, worlds/configs and original code/art.
- Growth remains bounded; no power-loss/claim-mod guarantee or automatic repair. Independent beta/public release approval remains separate.

## Next Milestone
`Continue: M30 steps 30.1-30.4 (finish the packaged client baseline, then test exact approved Undead Nights 2.2.7 or record explicit deferral) on codex/m30-neoforge-102-spike.`
