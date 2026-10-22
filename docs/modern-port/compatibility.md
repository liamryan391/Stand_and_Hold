# Optional Threat Compatibility

Artifact metadata checked on 2026-10-22 for Minecraft 26.3, NeoForge 26.3.0.57-beta and Java 25. These are installation candidates, **not runtime-tested supported combinations**. The dated [M0 ecosystem review](ecosystem-compatibility.md) remains historical research.

## Boundary

- Stand and Hold must launch and retain its core progression, missions, samples and defence without any external threat mod.
- Integrations use verified registry IDs/tags and configuration, not imports of another mod's classes, copied code/assets or a required threat-mod dependency.
- `minecraft:zombie` remains the safe default test mapping. An infection-themed mod title does not establish new entity IDs, and infected players must not become NPC targets.
- Keep approved external test mods in separate disposable client/server instances, outside normal development runs, build dependencies and the release jar. Never install a legacy SRP jar on the modern loader.

## Exact Artifact Candidates

Author-controlled release records were filtered for both `26.3` and `neoforge`. Each downloaded artifact was inspected in memory only and matched its release's SHA-512; no jar was installed, executed or saved into the repository during this review.

| Candidate | Exact release/file | Declared required mods | Physical sides | Licence evidence |
| --- | --- | --- | --- | --- |
| Contagion | [2.1.1-NeoForge-mc26.3](https://modrinth.com/mod/contagion/version/4tdqhokG), `Contagion-2.1.1-NeoForge-mc26.3.jar` (141,723 bytes; published 2026-09-24) | `neoforge [26.3.0.16-beta,)`, `minecraft [26.3, 27.1)` | Client and server | Project metadata: MIT; this jar's `neoforge.mods.toml`: All Rights Reserved. Treat redistribution/reuse permission as unresolved; do not copy or bundle. |
| Undead Nights | [2.2.6-NeoForge-mc26.3](https://modrinth.com/mod/undead-nights/version/hXnSFPmW), `UndeadNights-2.2.6-NeoForge-mc26.3.jar` (402,078 bytes; published 2026-09-23) | `neoforge [26.2.0.7-beta,)`, `minecraft [26.3, 27.1)` | Client and server | Project and jar metadata: All Rights Reserved; do not copy or bundle. |

Both records are author-labelled releases, list no additional Modrinth dependencies, and their inspected manifests declare only Minecraft/NeoForge as required. The pinned platform satisfies those declared ranges; that is not binary/runtime proof. The first inspected class in each jar has major version 69 (Java 25); this is not an audit of every class or mixin.

Exact SHA-512 values:

```text
Contagion-2.1.1-NeoForge-mc26.3.jar
b0c7f75437cbd960e9d15eecee7e0297f3ea6dac48d52e129b17ce8282f29b9f165d0584858aea68f9df15c7864a0085113fa009dfff43fc2ac066edef666bd1

UndeadNights-2.2.6-NeoForge-mc26.3.jar
9ac855ebfc5558f5ffc1b3f5780951fc901cc1f27232ee792304d37f11f3a24102e6481f4e7b861c13b8e66c6f2bee2e83f2f69156d65dd49e27975b35393b8a
```

Primary metadata: [Contagion project](https://api.modrinth.com/v2/project/contagion), [exact release](https://api.modrinth.com/v2/version/4tdqhokG), [author source repository](https://github.com/MC-Mods-Pete/Contagion); [Undead Nights project](https://api.modrinth.com/v2/project/undead-nights), [exact release](https://api.modrinth.com/v2/version/hXnSFPmW), [author source repository](https://github.com/MC-Mods-Pete/UndeadNights).

## Proposed Test Admission

| Decision field | Proposal |
| --- | --- |
| Need | M11 optional infection coexistence and mapping/removal tests; platform-only tests cannot demonstrate coexistence with another mod. |
| Selection | Contagion only, at the exact release/hash above. Its [author description](https://modrinth.com/mod/contagion) identifies zombie-driven infection; Undead Nights is a horde scheduler, not sufficient infection-mod evidence by itself. |
| Runtime contract | Optional QA-instance content on both sides, no required Stand and Hold dependency, no extra declared library, no default-run modification. Maintainer approval is required before installing/executing it. |
| Verification | Baseline without it; separate client/dedicated launches with it; verified target IDs, points/samples/mission/defender behavior; backed-up disposable removal and missing-mapping checks. None of these M11 runtime checks is established by this metadata review. |
| Maintenance/removal | No infection-effect API, immunity or infected-player targeting promised. Test one external mod at a time, preserve exact mod lists/configs and keep a full disposable-world backup before removal. An external mod's saved content is not guaranteed to survive its removal. |
| Alternative | Keep vanilla-only QA and explicitly leave the external-mod exit gate unmet. No fallback/pin change is proposed. |

Use the [dependency policy](foundation-scope.md) for admission and [STATUS.md](STATUS.md) for the live decision. Do not infer verified registry mappings or activate an external preset from this artifact table alone.
