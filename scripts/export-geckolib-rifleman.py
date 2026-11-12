"""Converts the Army Rifleman Blockbench source into GeckoLib geometry for the M33 D5 animation experiment.

The `.bbmodel` stays the single source of shape and UVs. Its modded-entity coordinates already follow the Bedrock
humanoid convention (right arm at -X, feet at Y 0), so cubes copy across directly as origin = from, size = to - from.
Two empty bones carry held items: `RightHandItem` and `LeftHandItem`, the names GeckoLib's held-item layer looks for.
ArtChecks fails if this file drifts from the source. Re-run after editing the rifleman in Blockbench:

    python scripts/export-geckolib-rifleman.py
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "art/blockbench/army_rifleman.bbmodel"
TARGET = ROOT / "src/main/resources/assets/standandhold/geckolib/models/entity/army_rifleman.geo.json"
HAND_BONES = {"RightHandItem": ("right_arm", [-6, 12, 0]), "LeftHandItem": ("left_arm", [6, 12, 0])}


def number(value):
    return int(value) if float(value).is_integer() else round(float(value), 4)


def main():
    model = json.loads(SOURCE.read_text(encoding="utf-8"))
    elements = {element["uuid"]: element for element in model["elements"]}
    bones = []

    def visit(group, parent):
        bone = {"name": group["name"], "pivot": [number(v) for v in group["origin"]]}
        if parent:
            bone["parent"] = parent
        cubes = []
        for child in group["children"]:
            if isinstance(child, str):
                cube = elements[child]
                if any(cube.get("rotation", [0, 0, 0])) or cube.get("inflate", 0):
                    raise SystemExit(f"Unsupported rotated or inflated cube: {cube['name']}")
                entry = {"origin": [number(v) for v in cube["from"]],
                         "size": [number(b - a) for a, b in zip(cube["from"], cube["to"])],
                         "uv": [number(v) for v in cube["uv_offset"]]}
                if cube.get("mirror_uv"):
                    entry["mirror"] = True
                cubes.append(entry)
        if cubes:
            bone["cubes"] = cubes
        bones.append(bone)
        for child in group["children"]:
            if not isinstance(child, str):
                visit(child, group["name"])

    for group in model["outliner"]:
        visit(group, None)
    names = {bone["name"] for bone in bones}
    for name, (parent, pivot) in HAND_BONES.items():
        if parent not in names:
            raise SystemExit(f"Missing arm bone {parent}")
        bones.append({"name": name, "parent": parent, "pivot": pivot})
    geometry = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.standandhold.army_rifleman", "texture_width": model["resolution"]["width"],
                        "texture_height": model["resolution"]["height"], "visible_bounds_width": 2,
                        "visible_bounds_height": 3, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": bones}]}
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    TARGET.write_text(json.dumps(geometry, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(bones)} bones to {TARGET.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
