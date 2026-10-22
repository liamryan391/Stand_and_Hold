package com.liamryan.standandhold;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liamryan.standandhold.common.equipment.EquipmentKind;
import com.liamryan.standandhold.common.equipment.EquipmentRules;
import com.liamryan.standandhold.config.EquipmentConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

final class EquipmentChecks {
    private static int assertions;
    static void run() throws Exception {
        gates(); targetsAndFlight(); assetsAndRecipes();
        check(EquipmentConfig.value(EquipmentConfig.COOLDOWN) == 20 && EquipmentConfig.value(EquipmentConfig.DAMAGE) == 6.0, "safe defaults before config load");
        check(EquipmentConfig.defense(EquipmentKind.ARMY, 1) == 6 && EquipmentConfig.defense(EquipmentKind.ELITE, 1) == 7, "four-piece config order");
        System.out.println("Equipment checks passed: " + assertions + " assertions.");
    }
    private static void gates() {
        check(!EquipmentConfig.value(EquipmentConfig.PLAYER_GATES), "found player gear is usable by default before config load");
        for (boolean player : new boolean[]{false, true}) { for (boolean strict : new boolean[]{false, true}) { for (boolean unlocked : new boolean[]{false, true}) {
            check(EquipmentRules.actorUnlocked(player, strict, () -> unlocked) == (player && !strict || unlocked), "NPC gates remain mandatory; player gates opt-in");
        } } }
        check(EquipmentRules.actorUnlocked(true, false, () -> { throw new AssertionError("Free player gear must not query progression"); }), "found gear independent of progression/research availability");
        for (int stage = 0; stage <= 6; stage++) {
            for (int required = 0; required <= 6; required++) {
                check(EquipmentRules.unlocked(stage, required, "standandhold:parasite_samples", id -> true, id -> true) == (stage >= required), "exact stage gate");
                check(!EquipmentRules.unlocked(stage, required, "standandhold:parasite_samples", id -> true, id -> false), "missing completion blocks even at high stage");
                check(!EquipmentRules.unlocked(stage, required, "standandhold:parasite_samples", id -> false, id -> true), "removed definition cannot authorize a saved receipt");
            }
        }
        for (EquipmentKind kind : EquipmentKind.values()) {
            check(!EquipmentRules.unlocked(kind.stage - 1, kind.stage, kind.research, id -> true, id -> true), "default lower stage locked: " + kind.id);
            check(EquipmentRules.unlocked(kind.stage, kind.stage, kind.research, id -> true, id -> true), "default boundary unlocked: " + kind.id);
        }
        for (String value : new String[]{null, "bad id", "missingnamespace", ":", "#minecraft:zombie"}) {
            check(!EquipmentRules.unlocked(6, 0, value, id -> true, id -> true), "invalid research gate fails closed");
        }
        check(EquipmentRules.unlocked(0, 0, "", id -> false, id -> false), "explicit empty research disables only research requirement");
        check(!EquipmentRules.unlocked(-1, 0, "", id -> true, id -> true), "invalid current stage blocked");
        check(!EquipmentRules.unlocked(6, 7, "", id -> true, id -> true), "invalid required stage blocked");
    }
    private static void targetsAndFlight() {
        for (int bits = 0; bits < 64; bits++) {
            boolean living = (bits & 1) != 0, player = (bits & 2) != 0, human = (bits & 4) != 0, allied = (bits & 8) != 0, mapped = (bits & 16) != 0;
            boolean hostile = (bits & 32) != 0;
            check(EquipmentRules.validTarget(living, player, human, allied, mapped, hostile) == (living && !player && !human && !allied && (mapped || hostile)), "all friendly/mapping/hostile combinations");
        }
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1, 0, 6, 64, 256, 500}) {
            float damage = EquipmentRules.damage(value);
            check(Float.isFinite(damage) && damage >= 0 && damage <= 256, "saved/config damage bounded");
        }
        check(EquipmentRules.damage(6) == 6, "normal damage unchanged");
        for (int age = 0; age <= 100; age++) {
            check(EquipmentRules.flightAge(age) == age, "valid saved flight age never resets");
            check(EquipmentRules.expired(age, 30) == (age >= 30), "exact lifetime cutoff");
        }
        for (int bad : new int[]{-1, Integer.MIN_VALUE, 101, Integer.MAX_VALUE}) {
            check(EquipmentRules.expired(EquipmentRules.flightAge(bad), 100), "malformed saved age expires rather than restarting");
        }
        check(EquipmentRules.expired(0, 0) && EquipmentRules.expired(0, 101), "invalid saved lifetime expires");
    }
    private static void assetsAndRecipes() throws Exception {
        Path assets = Path.of("src/main/resources/assets/standandhold"), data = Path.of("src/main/resources/data/standandhold");
        JsonObject lang = json(assets.resolve("lang/en_us.json"));
        for (String key : List.of("message.standandhold.equipment_locked", "tooltip.standandhold.equipment_gate", "tooltip.standandhold.friendly_safe")) {
            check(lang.has(key), "translated equipment feedback");
        }
        List<String> ids = new ArrayList<>(List.of("anti_threat_blade", "prototype_launcher", "prototype_bolt"));
        for (String tier : List.of("army", "elite")) {
            for (String part : List.of("helmet", "chestplate", "leggings", "boots")) { ids.add(tier + "_" + part); }
            var equipment = json(assets.resolve("equipment/" + tier + ".json")).getAsJsonObject("layers");
            for (String layer : List.of("humanoid", "humanoid_leggings", "humanoid_baby")) {
                check(equipment.getAsJsonArray(layer).get(0).getAsJsonObject().get("texture").getAsString().equals("standandhold:" + tier), "own equipment asset reference");
                var atlas = ImageIO.read(assets.resolve("textures/entity/equipment/" + layer + "/" + tier + ".png").toFile());
                check(atlas.getWidth() == 64 && atlas.getHeight() == 32, "worn armour classic atlas");
                if (!layer.equals("humanoid_leggings")) {
                    check(atlas.getRGB(11, 13) >>> 24 == 0 && atlas.getRGB(11, 8) >>> 24 == 255, "helmet leaves the wearer face visible");
                    check(atlas.getRGB(22, 25) >>> 24 == 255 && atlas.getRGB(5, 30) >>> 24 == 255, "vest/boots faces opaque");
                }
            }
        }
        for (String id : ids) {
            check(lang.has("item.standandhold." + id), "translated equipment item");
            var clientItem = json(assets.resolve("items/" + id + ".json"));
            check(clientItem.getAsJsonObject("model").get("model").getAsString().equals("standandhold:item/" + id), "item points to its own model");
            var model = json(assets.resolve("models/item/" + id + ".json"));
            check(model.getAsJsonObject("textures").get("layer0").getAsString().equals("standandhold:item/" + id), "model points to own texture");
            var image = ImageIO.read(assets.resolve("textures/item/" + id + ".png").toFile());
            check(image.getWidth() == 16 && image.getHeight() == 16 && image.getRGB(0, 0) >>> 24 == 0, "original 16px cutout icon");
            boolean visible = false;
            for (int y = 0; y < 16; y++) { for (int x = 0; x < 16; x++) { visible |= image.getRGB(x, y) >>> 24 != 0; } }
            check(visible, "item icon nonblank");
            if (id.equals("elite_helmet")) {
                for (int y = 11; y < 16; y++) { for (int x = 0; x < 16; x++) { check(image.getRGB(x, y) >>> 24 == 0, "no detached helmet icon pixels"); } }
            }
            if (id.equals("prototype_bolt")) { continue; }
            var recipe = json(data.resolve("recipe/" + id + ".json"));
            check(recipe.getAsJsonObject("result").get("id").getAsString().equals("standandhold:" + id), "recipe result correct");
            if (recipe.get("type").getAsString().equals("minecraft:crafting_shaped")) {
                var pattern = recipe.getAsJsonArray("pattern");
                check(pattern.size() > 0 && pattern.size() <= 3, "bounded recipe rows");
                int width = pattern.get(0).getAsString().length();
                for (var row : pattern) {
                    check(width > 0 && width <= 3 && row.getAsString().length() == width, "recipe width agrees");
                    for (char c : row.getAsString().toCharArray()) { check(c == ' ' || recipe.getAsJsonObject("key").has(String.valueOf(c)), "every recipe symbol defined"); }
                }
            } else { check(recipe.getAsJsonArray("ingredients").size() == 5, "small explicit upgrade recipe"); }
        }
        check(json(data.resolve("tags/item/repairs_equipment.json")).getAsJsonArray("values").get(0).getAsString().equals("minecraft:iron_ingot"), "repair tag uses normal iron");
        var learned = json(data.resolve("advancement/recipes/equipment.json")).getAsJsonObject("rewards").getAsJsonArray("recipes");
        check(learned.size() == 10 && learned.asList().stream().allMatch(e -> Files.isRegularFile(data.resolve("recipe/" + e.getAsString().split(":")[1] + ".json"))), "ordinary iron discovery grants only existing gear recipes");
    }
    private static JsonObject json(Path path) throws Exception { return JsonParser.parseString(Files.readString(path)).getAsJsonObject(); }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
