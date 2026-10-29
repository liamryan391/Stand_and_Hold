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
        sources(); textBounds();
        System.out.println("Art checks passed: " + assertions + " assertions.");
    }
    private static void sources() throws Exception {
        Path root = Path.of("src/main/resources/assets/standandhold");
        String meshes = Files.readString(Path.of("src/main/java/com/liamryan/standandhold/client/HumanNpcMeshes.java"));
        for (String id : List.of("survivor_defender", "army_rifleman", "heavy_soldier", "elite_soldier", "field_command_post", "research_lab", "supply_crate")) {
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
