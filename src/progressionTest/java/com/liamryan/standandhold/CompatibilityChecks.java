package com.liamryan.standandhold;

import com.google.gson.JsonParser;
import com.liamryan.standandhold.common.compat.ThreatPresets;
import com.liamryan.standandhold.common.threat.ThreatRules;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class CompatibilityChecks {
    private static int assertions;
    static void run() throws Exception {
        var baseline = ThreatPresets.resolve(ThreatRules.DEFAULTS, List.of(), id -> { throw new AssertionError("No optional probes for default mappings"); });
        check(baseline.active().isEmpty() && baseline.rules().equals(ThreatRules.parse(ThreatRules.DEFAULTS)), "default zombie-only behavior unchanged");
        var selected = List.of("contagion_2_1_1");
        var absent = ThreatPresets.resolve(List.of(), selected, id -> null);
        check(absent.active().isEmpty() && absent.rules().rules().isEmpty() && absent.rules().issues().size() == 1, "missing mod skips optional preset without fallback rewards");
        for (String wrong : List.of("2.1.1", "2.1.1-NeoForge-mc26.2", "2.2.0-NeoForge-mc26.3", "", "bad")) {
            check(ThreatPresets.resolve(List.of(), selected, id -> wrong).rules().rules().isEmpty(), "untested versions fail closed");
        }
        var supported = ThreatPresets.resolve(List.of(), selected, id -> id.equals("contagion") ? ThreatPresets.CONTAGION_VERSION : null);
        check(supported.active().equals(selected) && supported.rules().issues().isEmpty(), "exact optional release accepted");
        var zombie = supported.rules().match("minecraft:zombie", tag -> tag.equals("standandhold:test_threats")).orElseThrow();
        check(zombie.points() == 5 && zombie.chance() == .35 && zombie.samples() == 1, "preset uses existing test rewards and drop rates");
        check(supported.rules().match("minecraft:pig", tag -> false).isEmpty(), "unmapped passive mob not rewarded");
        check(supported.rules().match(null, tag -> true).isEmpty(), "null registry ID safe with optional presets");
        var manual = ThreatPresets.resolve(List.of("minecraft:zombie|2|1|3"), selected, id -> ThreatPresets.CONTAGION_VERSION);
        check(manual.rules().match("minecraft:zombie", tag -> true).orElseThrow().points() == 2, "manual exact rule beats preset tag; no stacked reward");
        var manualTag = ThreatPresets.resolve(List.of("#standandhold:test_threats|1|0|0"), selected, id -> ThreatPresets.CONTAGION_VERSION);
        check(manualTag.rules().rules().size() == 1 && manualTag.rules().rules().getFirst().points() == 1, "duplicate custom tag keeps manual reward");
        var removed = ThreatPresets.resolve(List.of(), selected, id -> null);
        check(removed.rules().match("minecraft:zombie", tag -> true).isEmpty(), "restart without optional mod removes only preset rules");
        check(ThreatPresets.resolve(ThreatRules.DEFAULTS, selected, id -> null).rules().rules().equals(baseline.rules().rules()), "manual rules survive optional mod removal");
        var generic = ThreatPresets.resolve(List.of(), List.of("vanilla_undead"), id -> { throw new AssertionError("Generic preset must not need a mod"); });
        check(generic.active().equals(List.of("vanilla_undead")) && generic.rules().issues().isEmpty(), "generic preset independent of optional mods");
        check(generic.rules().match("minecraft:husk", tag -> tag.equals("standandhold:vanilla_undead")).isPresent(), "generic tag routed to same mapping pipeline");
        var invalid = new ArrayList<Object>(List.of("", "unknown", "CONTAGION_2_1_1", "x".repeat(65), 12)); invalid.add(null);
        var invalidResolved = ThreatPresets.resolve(List.of("missing:entity|1|1|1", "bad"), invalid, id -> null);
        check(invalidResolved.active().isEmpty() && invalidResolved.rules().issues().size() == invalid.size() + 1, "malformed selections and mappings skipped independently");
        check(invalidResolved.rules().match("minecraft:zombie", tag -> false).isEmpty(), "unknown entity mapping never falls back to zombie");
        check(ThreatPresets.resolve(List.of(), List.of("vanilla_undead", "vanilla_undead"), id -> null).rules().rules().size() == 1, "duplicate presets do not duplicate rules");
        var manySelections = new ArrayList<String>(); for (int i = 0; i < 1000; i++) { manySelections.add("unknown"); }
        check(ThreatPresets.resolve(List.of(), manySelections, id -> null).rules().issues().size() == 17, "preset processing/warnings bounded to 16 entries");
        var manyRules = new ArrayList<String>(); for (int i = 0; i < 1000; i++) { manyRules.add("test:entity_" + i + "|0|0|0"); }
        var full = ThreatPresets.resolve(manyRules, List.of("vanilla_undead"), id -> null);
        check(full.rules().rules().size() == 256 && full.active().isEmpty(), "manual-plus-preset global bound enforced without oversized copies");
        var mutable = new ArrayList<String>(List.of("vanilla_undead"));
        var snapshot = ThreatPresets.resolve(List.of(), mutable, id -> null); mutable.clear();
        check(snapshot.active().size() == 1, "resolved snapshot does not reference mutable input");
        tag("test_threats", List.of("minecraft:zombie"));
        tag("vanilla_undead", List.of("minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_villager"));
        jei();
        System.out.println("Compatibility checks passed: " + assertions + " assertions.");
    }
    /** M35 (D11): JEI stays optional. Only the annotated plugin touches its API, the build never ships it, and every page has text. */
    private static void jei() throws Exception {
        Path main = Path.of("src/main/java"), plugin = main.resolve("com/liamryan/standandhold/common/compat/jei/StandAndHoldJeiPlugin.java");
        String source = Files.readString(plugin);
        check(source.contains("@JeiPlugin") && source.contains("implements IModPlugin"), "JEI discovers the plugin by its annotation");
        try (var files = Files.walk(main)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                check(file.equals(plugin) || !Files.readString(file).contains("mezz.jei"), "only the JEI plugin references JEI: " + file);
            }
        }
        String gradle = Files.readString(Path.of("build.gradle"));
        check(!gradle.contains("implementation \"mezz.jei") && !gradle.contains("jarJar") && gradle.contains("compileOnly \"mezz.jei:jei-${minecraft_version}-common-api"),
                "JEI is compile-time API only, never bundled");
        String toml = Files.readString(Path.of("src/main/templates/META-INF/neoforge.mods.toml")).replace("\r\n", "\n"); // Windows checkouts
        check(toml.contains("modId=\"jei\"\ntype=\"optional\""), "JEI is declared optional");
        var lang = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/standandhold/lang/en_us.json"))).getAsJsonObject();
        var ids = java.util.regex.Pattern.compile("INFO = List\\.of\\(([^)]*)\\)").matcher(source);
        check(ids.find(), "info page list found");
        int pages = 0;
        for (String raw : ids.group(1).split(",")) {
            String id = raw.strip().replace("\"", "");
            check(Files.isRegularFile(Path.of("src/main/resources/assets/standandhold/items/" + id + ".json")), "info page names a real item: " + id);
            check(lang.has("jei.standandhold.info." + id), "info page translated: " + id);
            pages++;
        }
        check(pages >= 7, "info pages cover the buildings, samples and weapons");
    }
    private static void tag(String name, List<String> expected) throws Exception {
        var json = JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/standandhold/tags/entity_type/" + name + ".json"))).getAsJsonObject();
        check(!json.get("replace").getAsBoolean(), "shipped tags do not replace modpack extensions");
        var values = new ArrayList<String>(); json.getAsJsonArray("values").forEach(value -> values.add(value.getAsString()));
        check(values.equals(expected), "only pinned-source verified vanilla IDs shipped in " + name);
    }
    private static void check(boolean condition, String message) { assertions++; if (!condition) { throw new AssertionError(message); } }
}
