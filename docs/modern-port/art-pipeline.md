# Art And Identity

## Visual Contract

Stand and Hold depicts a practical human resistance, not a copy of an existing game's army. Shapes must read at ordinary Minecraft distances; equipment is stylised and does not describe real weapon construction. Use original geometry, pixel art and markings only. No downloaded skins, Minecraft textures, other-mod assets or unlicensed reference meshes enter the build.

| Role | Silhouette | Palette and markings |
| --- | --- | --- |
| Survivor Defender | Soft cap with a short brim, small field pack, no armour bulk | Muted red jacket, charcoal trousers, ochre strap; one shoulder stripe |
| Army Rifleman | Rounded-off block helmet, broad protective vest | Teal uniform/helmet, graphite vest, ivory double marks; not just a recoloured Survivor |
| Command Post | Rugged radio/map cabinet | Olive shell, dark screen, green route marks, warm controls |
| Research Lab | Clean specimen workstation | Cool white shell, cyan window, contained magenta tissue, yellow caution pixels |
| Supply Crate | Reinforced utility container | Neutral metal, yellow straps, clear latch; no random camouflage |
| Tissue Sample | Sealed specimen pouch | Light seal, teal containment, magenta tissue; transparent outside the silhouette |

Only the two implemented NPC tiers receive models. Later tiers need their own silhouettes when implemented. Both current humans remain melee prototypes; visual armour, packs and helmets do not add stats, inventories or equipment drops.

Keep full base body faces opaque. Do not apply a 64x64 player skin to the old 64x32 UV layout: these are original box-UV models with an explicitly declared atlas. Reuse the classic body rectangles in the top half, reserving the lower half for accessories. Faces, hands and boots must remain readable in daylight and torchlight. Accessories must follow their body part through walking, head turning and melee animation, without changing hitboxes.

Menus are compact utility panels: fixed rows, restrained colour, no decorative full-screen background. Research selection stays separate from Start/Pause. Show full values or a hover tooltip when space requires ellipsis; never let long titles or blocked text overlap controls. Completion, readiness and blocked states must have words as well as colour. Test default and large GUI scales, long titles and large balances.

## Animation Decision (2026-10-21)

Author-published Modrinth metadata returned exact Minecraft 26.3/NeoForge releases for [GeckoLib 5.5.7](https://modrinth.com/mod/geckolib/version/2s1O42FX) (2026-09-22) and [AzureLib 4.0.14](https://modrinth.com/mod/azurelib/version/4kva3IhA) (2026-10-16). These are availability checks, not combined-runtime tests or approvals to install either library.

M8 uses the pinned platform's `HumanoidModel` animation with original exported geometry. Walking, head look and melee movement need no external library or animation controller. Blockbench animation clips are **not** automatically executed by these models. The [official format guide](https://blockbench.net/wiki/blockbench/formats/) distinguishes model export from runtime animation support. A later clip-driven requirement must pass the dependency policy and Liam's approval first; do not install both libraries or change toolchain pins to follow an art tutorial.

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

The first command writes generated `HumanNpcMeshes.java`, the two entity PNGs, and utility block JSON/PNGs. `-Check` writes nothing and fails on stale output; Windows CI runs it before the clean build. The build checks source hashes, embedded/runtime PNG equality, dimensions, opaque body/accessory UV faces, block face assignments and bounded GUI text. Keep sources and generated outputs in the same commit. The exporter runs from any working directory, without Java, plugins or an installed Blockbench.

### Editing Contract

1. Open `art/blockbench/<id>.bbmodel` in Blockbench. NPCs use **Modded Entity**, utility blocks **Java Block/Item**; project metadata uses the portable 4.12 model format. Embedded PNGs are the source of truth. Do not import a player skin or add absolute texture paths.
2. Preserve root `head`, `body`, `right_arm`, `left_arm`, `right_leg`, `left_leg`, and `hat` nested directly under `head`. Vanilla walking/head/melee/held-item transforms use these names and pivots. Keep accessories in the corresponding head/body group.
3. NPCs support at most 64 unrotated, non-inflated box-UV cubes and 32 uniquely named groups, one 64x64 atlas named after the model, and positive whole-pixel cube dimensions up to 32. Fractional positions/pivots are allowed; rotations, per-face UV painting layouts, meshes, animation clips and extra atlases are outside this exporter. Mirror UVs are supported. All six faces must use texture 0.
4. Utility projects intentionally remain one full 0..16 cube with three embedded 16x16 textures: front, side, top. Full-face UVs and texture indices 0..2 only. This preserves existing collision, interaction and placement behavior. The front is world north; there is no facing property yet. Custom inventory display transforms are not exported.
5. Save the project, export, run `-Check` and the clean build, then inspect it in Minecraft. Do not hand-edit generated Java or use Blockbench's version-specific Java exporter as the runtime output. Unsupported edits should be rejected, not silently treated as animated features.

For deliberate seed resets, run `java .\scripts\GenerateNpcTextures.java`, `java .\scripts\GenerateBuildingTextures.java` or `java .\scripts\GenerateSampleTexture.java` after selecting the modern JDK. NPC/block sources must then be deliberately updated with the desired embedded PNGs; exporting otherwise restores their existing source images. The sample and egg icons are direct generated item art, not Blockbench projects.

### Visual QA

- Compare both humans front/back/sides in daylight and torchlight, including head turns, walking and melee swings. Survivor cap/pack and Rifleman helmet/vest must read as different silhouettes. Check every face for transparency, stretched UVs and missing-texture patterns.
- Test a harmless vanilla held item without changing NPC defaults: `/item replace entity @e[type=standandhold:army_rifleman,sort=nearest,limit=1] weapon.mainhand with minecraft:stick`. It should follow the hand through movement; this is a disposable visual test, not a new equipment system.
- Check the three placed blocks' front/top/sides and inventory forms, both eggs and tissue sample. Building models remain lightweight full cubes, not finished furniture.
- Check all menus at default and large GUI scale, in a window that leaves at least 300x230 GUI pixels. Exercise ready/running/paused/blocked/completed lab states; selection must not auto-start. A globally completed target shows a full bar without modifying saved local work. Shortened fields expose full text on hover; button tooltips explain transfer batches and actions.
- Use a disposable datapack with an 80-character research title and large balances to visually stress hover text. Automated checks also cover 80/500-character words, Unicode boundaries and long-limit labels, but do not substitute for resource-pack/font/scale visual tests. A definition reload requires reopening a menu.
- Press F2 for unit and menu references, then save/reopen and fully quit. Keep selected references under `art/references/`; originals remain in the ignored run directory. Screenshots are Minecraft scene evidence, never texture sources.

The Survivor export was tested in game before creating the Rifleman variant. Reference images and their scope are described in [art/references/README.md](../../art/references/README.md). Blockbench desktop import/save-roundtrip, custom fonts/resource packs and animation-clip libraries require separate checks; none is claimed from the JSON/export tests alone.
