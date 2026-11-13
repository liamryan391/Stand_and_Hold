"""Builds the original M35 service rifle: an editable Blockbench source plus the exported item model and atlas.

The design is a generic modern service rifle drawn for Stand and Hold (olive furniture, dark metal, box magazine); it
copies no real-world product's markings and no other mod's model or texture. Requires Pillow:

    python scripts/generate-service-rifle.py

Outputs (all committed; ArtChecks and scripts/export-art.ps1 -Check compare them):
  art/blockbench/service_rifle.bbmodel                          editable source with the atlas embedded
  src/main/resources/assets/standandhold/textures/item/service_rifle.png
  src/main/resources/assets/standandhold/models/item/service_rifle.json
  src/main/resources/assets/standandhold/items/service_rifle.json

The model's (8, 8, 8) anchor sits at the pistol grip, where a soldier's hand holds it.
"""
import base64
import io
import json
import math
import pathlib
import random

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/standandhold"

# 4x4 swatches on a 16x16 atlas: (column, row) -> (base colour, pattern).
SWATCH = {
    "metal": ((0, 0), (47, 50, 53), "brushed"),
    "metal_top": ((1, 0), (66, 70, 74), "brushed"),
    "olive": ((2, 0), (78, 86, 50), "speckle"),
    "olive_top": ((3, 0), (98, 107, 64), "speckle"),
    "metal_shade": ((0, 1), (33, 35, 38), "brushed"),
    "bore": ((1, 1), (24, 25, 27), "bore"),
    "olive_shade": ((2, 1), (60, 66, 38), "speckle"),
    "grip": ((3, 1), (55, 60, 36), "stipple"),
    "sight": ((0, 2), (29, 30, 32), "plain"),
    "magazine": ((1, 2), (40, 42, 44), "ribs"),
    "pad": ((2, 2), (26, 27, 28), "ribs"),
    "sling": ((3, 2), (88, 94, 62), "plain"),
}


def atlas():
    rng = random.Random(3501)
    image = Image.new("RGB", (16, 16), (40, 42, 44))
    for (column, row), base, pattern in SWATCH.values():
        for y in range(4):
            for x in range(4):
                shade = 0
                if pattern == "brushed":
                    shade = (3 if x % 2 == 0 else -2) + rng.randint(-2, 2)
                elif pattern == "speckle":
                    shade = rng.choice((-6, -3, 0, 0, 2, 4))
                elif pattern == "stipple":
                    shade = -8 if (x + y) % 2 == 0 else 3
                elif pattern == "ribs":
                    shade = -7 if y % 2 == 0 else 4
                elif pattern == "bore":
                    shade = -20 if 1 <= x <= 2 and 1 <= y <= 2 else 6
                if y == 0 and pattern in ("brushed", "speckle"):
                    shade += 5  # light catches the upper edge
                image.putpixel((column * 4 + x, row * 4 + y), tuple(max(0, min(255, c + shade)) for c in base))
    # Row 3 keeps a worn variant of each finish, so every pixel is opaque and the atlas has room to grow.
    for x in range(16):
        for y in range(12, 16):
            source = image.getpixel((x, y - 12))
            image.putpixel((x, y), tuple(max(0, c - 4) for c in source))
    return image


def uv(name, width, height):
    """A face's UV inside its swatch: up to 4x4 texels, stretched along long faces."""
    (column, row), _, _ = SWATCH[name]
    w, h = min(4.0, max(1.0, width)), min(4.0, max(1.0, height))
    return [column * 4, row * 4, column * 4 + w, row * 4 + h]


# name, from, to, (sides, top, bottom, front, back)
PARTS = [
    ("Muzzle device", [7.25, 7.75, -9], [8.75, 9.25, -8], ("metal_shade", "metal_shade", "metal_shade", "bore", "metal_shade")),
    ("Barrel", [7.5, 8, -8], [8.5, 9, -2.5], ("metal", "metal_top", "metal_shade", "metal", "metal")),
    ("Front sight", [7.75, 9, -6.5], [8.25, 10, -6], ("sight", "sight", "sight", "sight", "sight")),
    ("Handguard", [7, 7.25, -2.5], [9, 9.25, 3.5], ("olive", "olive_top", "olive_shade", "olive_shade", "olive")),
    ("Receiver", [7, 7.5, 3.5], [9, 9.75, 9.5], ("metal", "metal_top", "metal_shade", "metal_shade", "metal")),
    ("Rear sight", [7.5, 9.75, 5.5], [8.5, 10.5, 8], ("sight", "sight", "sight", "sight", "sight")),
    ("Magazine", [7.25, 5, 4.5], [8.75, 7.5, 6], ("magazine", "magazine", "metal_shade", "magazine", "magazine")),
    ("Pistol grip", [7.25, 5, 8], [8.75, 7.5, 9.25], ("grip", "grip", "olive_shade", "grip", "grip")),
    ("Stock", [7, 6.75, 9.5], [9, 9.25, 15], ("olive", "olive_top", "olive_shade", "olive", "olive")),
    ("Butt pad", [7, 6.5, 15], [9, 9.5, 15.5], ("pad", "pad", "pad", "pad", "pad")),
]


def faces(start, end, materials):
    sides, top, bottom, front, back = materials
    dx, dy, dz = (end[i] - start[i] for i in range(3))
    return {  # scripts/export-art.ps1 emits faces in this order
        "north": uv(front, dx, dy), "east": uv(sides, dz, dy), "south": uv(back, dx, dy),
        "west": uv(sides, dz, dy), "up": uv(top, dx, dz), "down": uv(bottom, dx, dz),
    }


def rotate(vector, degrees):
    """Minecraft's ItemTransform order: rotationXYZ, which applies Z, then Y, then X to a vector."""
    x, y, z = vector
    rx, ry, rz = (math.radians(a) for a in degrees)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    return x, y, z


def centred(rotation, scale, lift=0.0):
    """A display transform that puts the rifle's bounding-box centre on the slot or frame centre."""
    low = [min(p[1][i] for p in PARTS) for i in range(3)]
    high = [max(p[2][i] for p in PARTS) for i in range(3)]
    offset = rotate([((low[i] + high[i]) / 2 - 8) * scale for i in range(3)], rotation)
    return {"rotation": rotation, "translation": [round(-offset[0], 2) + 0.0, round(-offset[1] + lift, 2) + 0.0, 0], "scale": [scale] * 3}


def display():
    hand = {"rotation": [0, 0, 0], "translation": [0, 1.4, -0.5], "scale": [0.8] * 3}
    first = {"rotation": [0, 10, 0], "translation": [1.5, 1.5, -2], "scale": [0.6] * 3}
    return {
        "gui": centred([26, 137, 0], 0.55),
        "ground": centred([0, 0, 0], 0.45, 2),
        "fixed": centred([0, 90, 0], 0.55),
        "firstperson_righthand": first, "firstperson_lefthand": first,
        "thirdperson_righthand": hand, "thirdperson_lefthand": hand,
    }


def tidy(value):
    """Whole numbers as integers, as PowerShell's ConvertTo-Json writes them, so both exporters agree byte for byte."""
    if isinstance(value, dict):
        return {key: tidy(item) for key, item in value.items()}
    if isinstance(value, list):
        return [tidy(item) for item in value]
    if isinstance(value, float) and value.is_integer():
        return int(value)
    return value


def main():
    image = atlas()
    buffer = io.BytesIO()
    image.save(buffer, "PNG", optimize=False)
    png = buffer.getvalue()
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    (ASSETS / "textures/item/service_rifle.png").write_bytes(png)
    elements, model_elements = [], []
    for index, (name, start, end, materials) in enumerate(PARTS):
        face_uv = faces(start, end, materials)
        elements.append({"name": name, "type": "cube", "uuid": f"5e7a1f1e-35a0-4c2b-8d11-{index + 1:012d}", "from": start, "to": end,
                         "box_uv": False, "faces": {side: {"uv": value, "texture": 0} for side, value in face_uv.items()}})
        model_elements.append({"from": start, "to": end, "faces": {side: {"uv": value, "texture": "#0"} for side, value in face_uv.items()}})
    shown = tidy(display())
    source = {
        "meta": {"format_version": "4.12", "model_format": "java_block", "box_uv": False},
        "name": "Service Rifle", "credit": "Original Stand and Hold service rifle art (M35)",
        "resolution": {"width": 16, "height": 16}, "elements": elements, "outliner": [e["uuid"] for e in elements],
        "display": shown,
        "textures": [{"name": "service_rifle.png", "id": "0", "uuid": "5e7a1f1e-35a0-4c2b-8d11-1000000000aa", "mode": "bitmap",
                      "source": "data:image/png;base64," + base64.b64encode(png).decode()}],
    }
    (ROOT / "art/blockbench/service_rifle.bbmodel").write_text(json.dumps(tidy(source), indent=2) + "\n")
    model = tidy({"textures": {"particle": "#0", "0": "standandhold:item/service_rifle"}, "display": shown, "elements": model_elements})
    # Same compact form as scripts/export-art.ps1, which re-exports this from the .bbmodel and fails CI on any difference.
    (ASSETS / "models/item/service_rifle.json").write_text(json.dumps(model, separators=(",", ":")) + "\n")
    item = {"model": {"type": "minecraft:model", "model": "standandhold:item/service_rifle"}}
    (ASSETS / "items/service_rifle.json").write_text(json.dumps(item, indent=2) + "\n")
    print("Wrote the service rifle source, model, atlas and item definition.")


if __name__ == "__main__":
    main()
