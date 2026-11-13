package com.liamryan.standandhold;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liamryan.standandhold.client.GuiTextLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.function.ToIntFunction;
import javax.imageio.ImageIO;

final class ArtChecks {
    private static int assertions;
    static void run() throws Exception {
        sources(); launcher(); textBounds(); variants(); geckoRifleman();
        System.out.println("Art checks passed: " + assertions + " assertions.");
    }
    /** M33: shipped soldier variants must match a fresh derivation from the exported atlases, keep every opaque pixel and differ. */
    /** M33 D5: GeckoLib geometry mirrors the Blockbench source exactly, and every clip uses known bones and queries. */
    private static void geckoRifleman() throws Exception {
        Path geckolib = Path.of("src/main/resources/assets/standandhold/geckolib");
        var source = JsonParser.parseString(Files.readString(Path.of("art/blockbench/army_rifleman.bbmodel"))).getAsJsonObject();
        var geo = JsonParser.parseString(Files.readString(geckolib.resolve("models/entity/army_rifleman.geo.json"))).getAsJsonObject()
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        check(geo.getAsJsonObject("description").get("texture_width").getAsInt() == 64, "GeckoLib rifleman uses the 64x64 atlas");
        java.util.Map<String, com.google.gson.JsonObject> bones = new java.util.HashMap<>();
        for (var bone : geo.getAsJsonArray("bones")) { bones.put(bone.getAsJsonObject().get("name").getAsString(), bone.getAsJsonObject()); }
        java.util.Map<String, com.google.gson.JsonObject> elements = new java.util.HashMap<>();
        for (var element : source.getAsJsonArray("elements")) { elements.put(element.getAsJsonObject().get("uuid").getAsString(), element.getAsJsonObject()); }
        java.util.ArrayDeque<com.google.gson.JsonObject> groups = new java.util.ArrayDeque<>();
        source.getAsJsonArray("outliner").forEach(group -> groups.add(group.getAsJsonObject()));
        int sourceCubes = 0, geoCubes = 0;
        while (!groups.isEmpty()) {
            var group = groups.pop(); String name = group.get("name").getAsString(); var bone = bones.get(name);
            check(bone != null && bone.getAsJsonArray("pivot").equals(group.getAsJsonArray("origin")), "GeckoLib bone and pivot match the source: " + name);
            var cubes = bone.has("cubes") ? bone.getAsJsonArray("cubes") : new com.google.gson.JsonArray();
            int index = 0;
            for (var child : group.getAsJsonArray("children")) {
                if (child.isJsonObject()) { groups.add(child.getAsJsonObject()); continue; }
                var element = elements.get(child.getAsString()); var cube = cubes.get(index++).getAsJsonObject(); sourceCubes++;
                var from = element.getAsJsonArray("from"); var to = element.getAsJsonArray("to");
                for (int axis = 0; axis < 3; axis++) {
                    check(cube.getAsJsonArray("origin").get(axis).getAsDouble() == from.get(axis).getAsDouble()
                            && cube.getAsJsonArray("size").get(axis).getAsDouble() == to.get(axis).getAsDouble() - from.get(axis).getAsDouble(), "cube shape copied: " + name);
                }
                check(cube.getAsJsonArray("uv").equals(element.getAsJsonArray("uv_offset")), "cube UV copied: " + name);
                check((cube.has("mirror") && cube.get("mirror").getAsBoolean()) == (element.has("mirror_uv") && element.get("mirror_uv").getAsBoolean()), "cube mirror copied: " + name);
            }
            check(index == cubes.size(), "no extra cubes in bone " + name); geoCubes += cubes.size();
        }
        check(sourceCubes == geoCubes && sourceCubes > 0, "every source cube is exported once");
        check("right_arm".equals(bones.get("RightHandItem").get("parent").getAsString()) && "left_arm".equals(bones.get("LeftHandItem").get("parent").getAsString()),
                "held-item bones ride the arms");
        var clips = JsonParser.parseString(Files.readString(geckolib.resolve("animations/entity/army_rifleman.animation.json"))).getAsJsonObject().getAsJsonObject("animations");
        String controller = Files.readString(Path.of("src/main/java/com/liamryan/standandhold/common/npc/RiflemanAnimation.java"));
        var allowed = java.util.regex.Pattern.compile("(query\\.(head_x_rotation|head_y_rotation|limb_swing_amount|limb_swing|anim_time)|math\\.(sin|cos)|[-+*/(). 0-9])*");
        for (String clip : java.util.List.of("idle", "walk", "run", "aim", "fire", "hurt", "death")) {
            check(clips.has(clip) && controller.contains('"' + clip + '"'), "clip exists and is used: " + clip);
            var animation = clips.getAsJsonObject(clip); double length = animation.get("animation_length").getAsDouble();
            for (var bone : animation.getAsJsonObject("bones").entrySet()) {
                check(bones.containsKey(bone.getKey()), "clip " + clip + " animates a real bone: " + bone.getKey());
                for (var channel : bone.getValue().getAsJsonObject().entrySet()) {
                    check(channel.getKey().equals("rotation"), "rotation-only clips keep the rig's proportions: " + clip);
                    java.util.List<com.google.gson.JsonElement> vectors = new java.util.ArrayList<>();
                    if (channel.getValue().isJsonArray()) { vectors.add(channel.getValue()); }
                    else { for (var frame : channel.getValue().getAsJsonObject().entrySet()) {
                        check(Double.parseDouble(frame.getKey()) <= length + 1e-9, "keyframe inside the clip: " + clip);
                        vectors.add(frame.getValue().getAsJsonObject().get("vector"));
                    } }
                    for (var vector : vectors) { for (var value : vector.getAsJsonArray()) {
                        check(value.getAsJsonPrimitive().isNumber() || allowed.matcher(value.getAsString()).matches(), "known Molang only in " + clip + ": " + value);
                    } }
                }
            }
        }
    }
    private static void variants() throws Exception {
        Path entity = Path.of("src/main/resources/assets/standandhold/textures/entity");
        check(NpcVariantArt.COUNT == com.liamryan.standandhold.common.npc.NpcVariants.COUNT, "generator and renderer agree on the variant count");
        try (var listed = Files.list(entity.resolve("variants"))) {
            check(listed.count() == (long) NpcVariantArt.TIERS.size() * NpcVariantArt.COUNT, "exactly one PNG per tier and variant, no strays");
        }
        for (String tier : NpcVariantArt.TIERS) {
            BufferedImage base = ImageIO.read(entity.resolve(tier + ".png").toFile());
            var seen = new java.util.HashSet<String>();
            for (int i = 0; i < NpcVariantArt.COUNT; i++) {
                BufferedImage shipped = ImageIO.read(entity.resolve("variants/" + tier + "_" + i + ".png").toFile());
                BufferedImage fresh = NpcVariantArt.variant(base, tier, i);
                check(shipped.getWidth() == 64 && shipped.getHeight() == 64, "variant atlas is 64x64");
                StringBuilder key = new StringBuilder();
                boolean same = true, alphaKept = true;
                for (int y = 0; y < 64; y++) { for (int x = 0; x < 64; x++) {
                    int a = shipped.getRGB(x, y), b = fresh.getRGB(x, y);
                    same &= a == b || (a >>> 24 == 0 && b >>> 24 == 0);
                    alphaKept &= (a >>> 24) == (base.getRGB(x, y) >>> 24);
                    key.append(Integer.toHexString(a));
                } }
                check(same, "variant " + tier + "_" + i + " is current with its exported atlas (run gradlew generateNpcVariants)");
                check(alphaKept, "variant keeps exactly the authored opaque/cutout shape");
                check(seen.add(key.toString()), "variants of " + tier + " are all different");
            }
        }
        var counts = new int[com.liamryan.standandhold.common.npc.NpcVariants.COUNT];
        var random = new java.util.Random(4242);
        for (int i = 0; i < 12000; i++) {
            var id = new java.util.UUID(random.nextLong(), random.nextLong());
            counts[com.liamryan.standandhold.common.npc.NpcVariants.index(id)]++;
            float h = com.liamryan.standandhold.common.npc.NpcVariants.height(id), w = com.liamryan.standandhold.common.npc.NpcVariants.width(id);
            if (i < 200) { check(h >= 0.949F && h <= 1.051F && w >= 0.959F && w <= 1.041F, "render-only build stays within a few percent"); }
            if (i < 50) { check(com.liamryan.standandhold.common.npc.NpcVariants.index(id) == com.liamryan.standandhold.common.npc.NpcVariants.index(new java.util.UUID(id.getMostSignificantBits(), id.getLeastSignificantBits())), "same UUID, same look on every client"); }
        }
        for (int count : counts) { check(count > 800 && count < 1200, "UUIDs spread evenly over the variants"); }
    }
    private static void sources() throws Exception {
        Path root = Path.of("src/main/resources/assets/standandhold");
        String meshes = Files.readString(Path.of("src/main/java/com/liamryan/standandhold/client/HumanNpcMeshes.java"));
        for (String id : List.of("survivor_defender", "army_rifleman", "heavy_soldier", "elite_soldier", "super_elite",
                "special_division_operative", "field_command_post", "research_lab", "supply_crate")) {
            Path path = Path.of("art/blockbench/" + id + ".bbmodel");
            var source = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            boolean human = source.getAsJsonObject("meta").get("model_format").getAsString().equals("modded_entity");
            check(source.get("credit").getAsString().contains("Original"), "original source ownership recorded");
            var textures = source.getAsJsonArray("textures");
            check(textures.size() == (human ? 1 : 3), "bounded source textures");
            BufferedImage atlas = null;
            for (var entry : textures) {
                var texture = entry.getAsJsonObject();
                String data = texture.get("source").getAsString();
                check(data.startsWith("data:image/png;base64,"), "portable embedded PNG, no machine-specific path");
                byte[] bytes = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
                atlas = ImageIO.read(new ByteArrayInputStream(bytes));
                check(atlas != null && atlas.getWidth() == (human ? 64 : 16) && atlas.getHeight() == (human ? 64 : 16), "declared and actual atlas dimensions agree");
                Path output = root.resolve("textures/" + (human ? "entity/" : "block/") + texture.get("name").getAsString());
                check(Arrays.equals(bytes, Files.readAllBytes(output)), "source texture equals runtime export");
                if (!human) { check(opaque(atlas, 0, 0, 16, 16), "utility block face fully opaque"); }
            }
            if (human) {
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readString(path).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));
                check(meshes.contains("Source SHA-256: " + hash) && meshes.contains("LayerDefinition " + id + "()"), "generated model identifies exact editable source");
                for (var raw : source.getAsJsonArray("elements")) {
                    var cube = raw.getAsJsonObject();
                    int w = size(cube, 0), h = size(cube, 1), d = size(cube, 2);
                    int u = cube.getAsJsonArray("uv_offset").get(0).getAsInt(), v = cube.getAsJsonArray("uv_offset").get(1).getAsInt();
                    check(u >= 0 && v >= 0 && u + 2 * (w + d) <= 64 && v + d + h <= 64, "all cube UVs inside atlas");
                    check(opaque(atlas, u + d, v, 2 * w, d), "both cap faces opaque");
                    check(opaque(atlas, u, v + d, 2 * (w + d), h), "all four side faces opaque");
                    for (String face : List.of("north", "east", "south", "west", "up", "down")) {
                        check(cube.getAsJsonObject("faces").getAsJsonObject(face).get("texture").getAsInt() == 0, "every humanoid face has the source atlas assigned");
                    }
                }
            } else {
                var runtime = JsonParser.parseString(Files.readString(root.resolve("models/block/" + id + ".json"))).getAsJsonObject();
                var face = runtime.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
                check(face.getAsJsonObject("north").get("texture").getAsString().equals("#0")
                        && face.getAsJsonObject("up").get("texture").getAsString().equals("#2"), "cabinet front and top are distinct runtime faces");
            }
        }
        var lang = JsonParser.parseString(Files.readString(root.resolve("lang/en_us.json"))).getAsJsonObject();
        for (int i = 0; i <= 10; i++) { check(lang.has("gui.standandhold.status." + i), "translated GUI status " + i); }
    }
    private static void launcher() throws Exception {
        var root = Path.of("src/main/resources/assets/standandhold");
        var source = JsonParser.parseString(Files.readString(Path.of("art/blockbench/prototype_launcher.bbmodel"))).getAsJsonObject();
        var model = JsonParser.parseString(Files.readString(root.resolve("models/item/prototype_launcher.json"))).getAsJsonObject();
        check(source.get("credit").getAsString().contains("Original"), "launcher has original editable source");
        var textures = source.getAsJsonArray("textures");
        check(textures.size() == 1, "one launcher atlas");
        String data = textures.get(0).getAsJsonObject().get("source").getAsString();
        check(data.startsWith("data:image/png;base64,"), "launcher atlas embedded");
        byte[] bytes = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
        var atlas = ImageIO.read(new ByteArrayInputStream(bytes));
        check(atlas != null && atlas.getWidth() == 16 && atlas.getHeight() == 16 && opaque(atlas, 0, 0, 16, 16), "opaque launcher atlas");
        check(Arrays.equals(bytes, Files.readAllBytes(root.resolve("textures/item/prototype_launcher.png"))), "launcher texture export exact");
        check(model.getAsJsonObject("display").equals(source.getAsJsonObject("display")), "held, menu and ground transforms match source");
        var authored = source.getAsJsonArray("elements"); var exported = model.getAsJsonArray("elements");
        check(authored.size() == 8 && exported.size() == authored.size(), "bounded original launcher geometry");
        for (int i = 0; i < authored.size(); i++) {
            var box = authored.get(i).getAsJsonObject(); var output = exported.get(i).getAsJsonObject();
            check(box.get("from").equals(output.get("from")) && box.get("to").equals(output.get("to")), "launcher box preserved");
            for (String side : List.of("north", "east", "south", "west", "up", "down")) {
                var face = output.getAsJsonObject("faces").getAsJsonObject(side);
                check(face.get("texture").getAsString().equals("#0") && face.get("uv").equals(box.getAsJsonObject("faces").getAsJsonObject(side).get("uv")), "launcher face preserved");
            }
        }
    }
    private static int size(JsonObject cube, int axis) {
        double n = cube.getAsJsonArray("to").get(axis).getAsDouble() - cube.getAsJsonArray("from").get(axis).getAsDouble();
        check(n > 0 && Math.abs(n - Math.rint(n)) < 0.00001, "positive whole-pixel cuboid size");
        return (int) Math.rint(n);
    }
    private static boolean opaque(BufferedImage image, int x, int y, int w, int h) {
        for (int py = y; py < y + h; py++) { for (int px = x; px < x + w; px++) { if (image.getRGB(px, py) >>> 24 != 255) { return false; } } }
        return true;
    }
    private static void textBounds() {
        ToIntFunction<String> measure = s -> s.codePointCount(0, s.length()) * 6;
        for (String value : List.of("Initial Sample Analysis", "W".repeat(80), "W".repeat(500),
                "Local supplies: 9223372036854775807 / 1000000", "Prerequisite research incomplete", "Test \uD83D\uDEE1 model")) {
            for (int width : new int[]{1, 2, 6, 18, 90, 184, 284}) {
                String fitted = GuiTextLayout.fit(value, width, measure);
                check(measure.applyAsInt(fitted) <= width, "single-line pixel bounds");
                var lines = GuiTextLayout.lines(value, width, 2, measure);
                check(lines.size() <= 2 && lines.stream().allMatch(s -> measure.applyAsInt(s) <= width), "two-line height and width bounds");
                check(fitted.codePoints().noneMatch(c -> c >= 0xd800 && c <= 0xdfff), "no unpaired surrogate truncation");
            }
        }
        check(GuiTextLayout.fit("", 10, measure).isEmpty(), "empty label");
        check(GuiTextLayout.lines("ignored", 0, 2, measure).isEmpty(), "zero-width field");
        check(GuiTextLayout.lines("ignored", 10, 0, measure).isEmpty(), "zero-height field");
        check(GuiTextLayout.fit("Ready", 90, measure).equals("Ready"), "short labels unchanged");
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
