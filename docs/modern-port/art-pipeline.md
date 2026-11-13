# Art And Identity

## Visual Contract

Stand and Hold depicts a practical human resistance, not a copy of an existing game's army. Shapes must read at ordinary Minecraft distances; equipment is stylised and does not describe real weapon construction. Use original geometry, pixel art and markings only. No downloaded skins, Minecraft textures, other-mod assets or unlicensed reference meshes enter the build.

| Role | Silhouette | Palette and markings |
| --- | --- | --- |
| Survivor Defender | Soft cap with a short brim, small field pack, no armour bulk | Muted red jacket, charcoal trousers, ochre strap; one shoulder stripe |
| Army Rifleman | Rounded-off block helmet, broad protective vest | Teal uniform/helmet, graphite vest, ivory double marks; not just a recoloured Survivor |
| Heavy Soldier | Deeper helmet, broad vest and shoulder plates | Olive/charcoal, amber bars; slower armoured melee role |
| Elite Soldier | Short helmet/visor and compact pack | Blue-grey uniform, mint marks; mobile shared-launcher role |
| Super Elite | Broad shoulder plates, heavy chest and helmet brow | Charcoal armour, brass chevron; blade role |
| Special Parasite Division Operative | Asymmetric helmet scanner and compact rear pod | Ivory and teal shell; mobile launcher role |
| Command Post | Rugged radio/map cabinet | Olive shell, dark screen, green route marks, warm controls |
| Research Lab | Clean specimen workstation | Cool white shell, cyan window, contained magenta tissue, yellow caution pixels |
| Supply Crate | Reinforced utility container | Neutral metal, yellow straps, clear latch; no random camouflage |
| Tissue Sample | Sealed specimen pouch | Light seal, teal containment, magenta tissue; transparent outside the silhouette |

All six implemented NPC tiers have their own models. Visual accessories do not themselves add stats, inventories or equipment drops; role stats/equipment are server-controlled separately.

Keep full base body faces opaque. Do not apply a 64x64 player skin to the old 64x32 UV layout: these are original box-UV models with an explicitly declared atlas. Reuse the classic body rectangles in the top half, reserving the lower half for accessories. Faces, hands and boots must remain readable in daylight and torchlight. Accessories must follow their body part through walking, head turning and melee animation, without changing hitboxes.

Menus are compact utility panels: fixed rows, restrained colour, no decorative full-screen background. Research selection stays separate from Start/Pause. Show full values or a hover tooltip when space requires ellipsis; never let long titles or blocked text overlap controls. Completion, readiness and blocked states must have words as well as colour. Test default and large GUI scales, long titles and large balances.

## Animation Decision (2026-10-21)

Author-published Modrinth metadata returned exact Minecraft 26.3/NeoForge releases for [GeckoLib 5.5.7](https://modrinth.com/mod/geckolib/version/2s1O42FX) (2026-09-22) and [AzureLib 4.0.14](https://modrinth.com/mod/azurelib/version/4kva3IhA) (2026-10-16). These are availability checks, not combined-runtime tests or approvals to install either library.

M8 uses the pinned platform's `HumanoidModel` animation with original exported geometry. Walking, head look and melee movement need no external library or animation controller. Blockbench animation clips are **not** automatically executed by these models. The [official format guide](https://blockbench.net/wiki/blockbench/formats/) distinguishes model export from runtime animation support. A later clip-driven requirement must pass the dependency policy and Liam's approval first; do not install both libraries or change toolchain pins to follow an art tutorial.

### Launcher Motion

M27 keeps the existing Rifleman rig and adds a native carry-to-aim blend over four
client ticks. The running server ranged goal reports aim only for a visible target in
the existing ranged distance band. Its synced value changes on transitions, not every
tick. A successful bolt spawn sends one entity event for a five-tick arm-only kick.
No camera effect, animation packets on failed shots, saved pose fields, new firing
rules or extra runtime library. Walking legs, head look and vanilla swing/swim/death
remain native. Non-launcher equipment and the other roles retain their old pose path;
an occupied off hand falls back to the vanilla item pose. Aim is not an off-cooldown cue.
The Division operative reuses the same carry, aim and shot motion, with its own rig accents and unchanged firing rules.

## Sources And Export

Editable `.bbmodel` projects live in `art/blockbench/`; they are source assets, not shipped runtime resources. NPC projects use Modded Entity box UVs and named humanoid pivots. Their geometry and embedded original texture are authoritative. In Blockbench, save the project with embedded textures, then run the repository exporter rather than replacing generated Java with a version-specific built-in Java template. The official Blockbench codec/schema was consulted, not copied into the project.

`scripts/GenerateNpcTextures.java`, `GenerateBuildingTextures.java` and `GenerateSampleTexture.java` are original deterministic seed-art tools. Regenerating seeds is an explicit reset of those PNG outputs, not a substitute for preserving a hand-painted Blockbench source. Do not regenerate them over an artist's edited source without reviewing the diff.

From the repository root in Windows PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-art.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-art.ps1 -Check
& .\scripts\use-modern-java.ps1
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle-home\modern'
.\gradlew.bat clean build --no-daemon --stacktrace
```

The first command writes generated `HumanNpcMeshes.java`, the six entity PNGs, utility block JSON/PNGs, and the Launcher model/atlas. `-Check` writes nothing and fails on stale output; Windows CI runs it before the clean build. The build checks source hashes, embedded/runtime PNG equality, dimensions, opaque body/accessory UV faces, face assignments, Launcher transforms and bounded GUI text. Keep sources and generated outputs in the same commit. The exporter runs from any working directory, without Java, plugins or an installed Blockbench.

### Editing Contract

1. Open `art/blockbench/<id>.bbmodel` in Blockbench. NPCs use **Modded Entity**, utility blocks **Java Block/Item**; project metadata uses the portable 4.12 model format. Embedded PNGs are the source of truth. Do not import a player skin or add absolute texture paths.
2. Preserve root `head`, `body`, `right_arm`, `left_arm`, `right_leg`, `left_leg`, and `hat` nested directly under `head`. Vanilla walking/head/melee/held-item transforms use these names and pivots. Keep accessories in the corresponding head/body group.
3. NPCs support at most 64 unrotated, non-inflated box-UV cubes and 32 uniquely named groups, one 64x64 atlas named after the model, and positive whole-pixel cube dimensions up to 32. Fractional positions/pivots are allowed; rotations, per-face UV painting layouts, meshes, animation clips and extra atlases are outside this exporter. Mirror UVs are supported. All six faces must use texture 0.
4. Utility projects intentionally remain one full 0..16 cube with three embedded 16x16 textures: front, side, top. Full-face UVs and texture indices 0..2 only. This preserves existing collision, interaction and placement behavior. The front is world north; there is no facing property yet. Custom inventory display transforms are not exported.
5. The `service_rifle.bbmodel` source (M35) is generated with its atlas, item model and item definition by `python scripts/generate-service-rifle.py` (Pillow): ten unrotated boxes within Minecraft's -16..32 model bounds, because a rifle is longer than a block, with the (8, 8, 8) anchor at the pistol grip and the muzzle to local north (-Z). It is a generic original design with no real product markings. `export-art.ps1` accepts up to twelve boxes for it and re-exports the model byte for byte; `ArtChecks` compares the same files.
6. The `prototype_launcher.bbmodel` Java Block/Item source allows up to eight unrotated boxes inside 0..16 and one opaque 16x16 embedded atlas. The sealed emitter faces local north (-Z), with the grip near the item centre. Edit both hands' explicit held transforms and the GUI/ground/fixed transforms in the source. The model is a fictional pulse tool, not a functional mechanism. Keep the Rifleman face clear in carry and aim poses.
6. Save the project, export, run `-Check` and the clean build, then inspect it in Minecraft. Do not hand-edit generated Java or use Blockbench's version-specific Java exporter as the runtime output. Unsupported edits should be rejected, not silently treated as animated features.

For deliberate seed resets, run `java .\scripts\GenerateNpcTextures.java`, `java .\scripts\GenerateBuildingTextures.java` or `java .\scripts\GenerateSampleTexture.java` after selecting the modern JDK. NPC/block sources must then be deliberately updated with the desired embedded PNGs; exporting otherwise restores their existing source images. The sample and egg icons are direct generated item art, not Blockbench projects.

### Soldier Variants (M33)

Each exported soldier atlas is the source of 12 appearance variants in `textures/entity/variants/<tier>_<n>.png`. Variant 0 is the atlas unchanged; the others are derived by `src/progressionTest/.../NpcVariantArt.java`, which only repaints pixels that are already opaque: skin tone, eyes, hair at the sideburns and hairline, facial hair or a scar, a headgear pattern, 0-3 rank stripes and boot wear. The renderer picks a variant and a render-only build from the entity UUID (`NpcVariants`), so nothing is saved or synced and hitboxes do not change. After editing a soldier in Blockbench, export, then run:

```powershell
.\gradlew.bat generateNpcVariants --no-daemon
```

and commit the regenerated PNGs with the source. `ArtChecks` regenerates every variant in memory and fails if a shipped file differs, if alpha changed, or if any two variants of a tier are identical. To change the variety itself (tones, patterns), edit the generator, not the PNGs.

### Sounds (M33, M35)

`assets/standandhold/sounds/` holds original procedurally synthesised samples and, since M35 (D10), a few CC0 public-domain recordings. Every file must be mono 44.1 kHz Vorbis so Minecraft can position it. `SoundChecks` decodes every file with Minecraft's own decoder and checks format, length, level, subtitles, a spectral-centroid band per event and that nothing unreferenced ships. Never add audio from other mods, and never add a recording without a CC0 (or approved) licence and a row in `THIRD_PARTY_NOTICES.md`.

- `python scripts/generate-sounds.py` (numpy, scipy, ffmpeg with libvorbis) synthesises the low kit layer (`npc/kit1-2`), launcher fire, calm radio of four lengths (`post/radio1-4`), urgent radio (`post/urgent1-3`) and the service-rifle report with its action (`rifle/fire1-3`). The recipes are seeded, so a re-run reproduces the same files unless a recipe changes.
- `python scripts/import-cc0-sounds.py <extracted Kenney RPG Audio folder>` converts Kenney's CC0 `cloth1-3` into the soldier kit rustle: band-passed 90-3200 Hz, trimmed, levelled to -14 dB peak.
- M35 targets, from Liam's playtest: the kit layer stays low (centroid band 150-1600 Hz; the retired M33 clinks measured about 4.2 kHz) and plays on about one step in three, on top of the block's own footstep. Calm radio sits at 600-1800 Hz, urgent radio at 800-2600 Hz. The rifle's checked window (the middle 8,192 samples: echo tail and action) sits near 1.1 kHz.

### GeckoLib Experiment (D5, M33 step 33.4)

On `main` since PR #7, the Army Rifleman renders through GeckoLib 5.5.7 (MIT, required dependency). Every other tier keeps the native rig. Sources:

- Geometry: `assets/standandhold/geckolib/models/entity/army_rifleman.geo.json`, generated from `art/blockbench/army_rifleman.bbmodel` by `python scripts/export-geckolib-rifleman.py`. It copies cubes, pivots and UVs and adds the empty `RightHandItem`/`LeftHandItem` bones that GeckoLib uses for held items. Edit the `.bbmodel`, never the generated file; `ArtChecks` fails on drift.
- Clips: `assets/standandhold/geckolib/animations/entity/army_rifleman.animation.json` (Bedrock animation format, editable in Blockbench with the GeckoLib plugin): `idle` (8 s: breathing, a slow look left and right, and a weapon check), `walk` (leg swing locked to real movement through `query.limb_swing`, hip sway, the rifle bouncing in the carry), `run` (M35: long strides with the rifle at port arms, chosen above 0.62 smoothed limb speed, so a Rifleman closing on a target runs while strolling and patrolling stay walks), `aim`, `fire` (kick plus the support-hand cycle that lines up with the sound), `hurt` and `death`. The body bone pivots at the neck beside the legs, so body leans stay at 6 degrees or less to keep the hip closed. Rotation only, so proportions never change. `ArtChecks` rejects unknown bones, keyframes past a clip's length and any Molang outside a short allow-list.
- Logic: `RiflemanAnimation` picks clips from state the client already has (movement, the synced aim flag, the launcher-shot event, hurt, death). Nothing new is saved or sent. Variants and the render-only build apply as on the native rig.

The QA client's `perf-riflemen-8` and `perf-riflemen-32` fixtures report mean frame time with an uncapped frame rate; run once normally and once with `-PqaNativeRifleman=true` to compare against the native rig on the same machine. The first PC run (2026-11-12) measured 33.4 ms for both renderers at both sizes: vanilla's AFK limit had capped the input-less client at 30 fps. The fixture now lifts that limit while it measures and logs the throttle reason with each result.

### Visual QA

- Compare both humans front/back/sides in daylight and torchlight, including head turns, walking and melee swings. Survivor cap/pack and Rifleman helmet/vest must read as different silhouettes. Check every face for transparency, stretched UVs and missing-texture patterns.
- Test a harmless vanilla held item without changing NPC defaults: `/item replace entity @e[type=standandhold:army_rifleman,sort=nearest,limit=1] weapon.mainhand with minecraft:stick`. It should follow the hand through movement; this is a disposable visual test, not a new equipment system.
- The scripted client additionally follows a Rifleman carrying, walking into range, aiming, firing, hurt, dying and holding a fallback item. It reloads resources and captures the player-held Launcher and creative inventory. Judge transition timing and feel separately in the isolated normal client.
- The scripted client captures soldier variant lineups: twelve Riflemen by day, two variants of every tier, four faces close up, and the Rifleman row by torchlight. Judge skin tones in torchlight and whether variants still read as one army.
- Check the three placed blocks' front/top/sides and inventory forms, both eggs and tissue sample. Building models remain lightweight full cubes, not finished furniture.
- Check all menus at default and large GUI scale, in a window that leaves at least 300x230 GUI pixels. Exercise ready/running/paused/blocked/completed lab states; selection must not auto-start. A globally completed target shows a full bar without modifying saved local work. Shortened fields expose full text on hover; button tooltips explain transfer batches and actions.
- Use a disposable datapack with an 80-character research title and large balances to visually stress hover text. Automated checks also cover 80/500-character words, Unicode boundaries and long-limit labels, but do not substitute for resource-pack/font/scale visual tests. A definition reload requires reopening a menu.
- Press F2 for unit and menu references, then save/reopen and fully quit. Keep selected references under `art/references/`; originals remain in the ignored run directory. Screenshots are Minecraft scene evidence, never texture sources.

The Survivor export was tested in game before creating the Rifleman variant. Reference images and their scope are described in [art/references/README.md](../../art/references/README.md). Blockbench desktop import/save-roundtrip, custom fonts/resource packs and animation-clip libraries require separate checks; none is claimed from the JSON/export tests alone.
