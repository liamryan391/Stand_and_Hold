# Visual References

These are unedited F2 captures from the disposable modern development client, not assets used by the mod. Only Stand and Hold's models/textures are original project art; the surrounding Minecraft world belongs to its respective owners. Do not extract scene textures or props into runtime resources.

- `survivor-prototype-front.png` and `survivor-prototype-side.png`: 2026-10-21, Survivor-only prototype before the Rifleman variant. Liam reported front/back/sides, walking and zombie melee passed. These show the original cap, face, jacket and pack fitting the animated model.
- `rifleman-front.png`: final combined client, 2026-10-21. Original helmet/vest and double sleeve marks, distinct from the Survivor. Liam reported the combined visual/animation check passed.
- `utility-blocks-side-top.png`: original utility side/top faces and inventory forms; fronts intentionally face north, not toward the player.
- `lab-researching.png` and `command-post-menu.png`: bounded labels, work/progress bar, explicit action rows and blocked upgrade feedback in the final combined test. Liam reported menu/scale checks passed. Review of another F2 frame found the old substring classifier mistaking `parasite_samples` for sample shortage; regression checks now guard prerequisite feedback. These captures do not claim the later label fix was visually retested.

Editable sources are in `../blockbench/`; export and visual checks are in [Art And Identity](../../docs/modern-port/art-pipeline.md). Screenshot evidence is narrower than automated texture/UV validation and does not prove multiplayer, external mod or custom shader compatibility.
