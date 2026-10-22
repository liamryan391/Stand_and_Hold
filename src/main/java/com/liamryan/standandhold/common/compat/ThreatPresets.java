package com.liamryan.standandhold.common.compat;

import com.liamryan.standandhold.common.threat.ThreatRules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/** Opt-in mapping data only; no classes, effects or assets from optional mods. */
public final class ThreatPresets {
    public static final String CONTAGION_VERSION = "2.1.1-NeoForge-mc26.3";
    public record Preset(String id, String modId, String version, List<String> mappings) {
        public Preset { mappings = List.copyOf(mappings); }
    }
    public static final List<Preset> CATALOG = List.of(
            new Preset("vanilla_undead", "", "", List.of("#standandhold:vanilla_undead|5|0.35|1")),
            // Reproduction candidate only: this release fails bootstrap on the pinned loader (missing config SERVER enum).
            new Preset("contagion_2_1_1", "contagion", CONTAGION_VERSION, List.of("#standandhold:test_threats|5|0.35|1")));
    public record Resolved(ThreatRules rules, List<String> active) {
        public Resolved { active = List.copyOf(active); }
    }
    private ThreatPresets() { }

    public static Resolved resolve(List<?> manual, List<?> selected, Function<String, String> installedVersion) {
        List<Object> combined = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        List<String> active = new ArrayList<>();
        var seen = new HashSet<String>();
        for (int i = 0; i < Math.min(manual.size(), 256); i++) { combined.add(manual.get(i)); }
        if (manual.size() > 256) { issues.add("Only the first 256 manual threat mappings are considered."); }
        if (selected.size() > 16) { issues.add("Only the first 16 threat preset selections are considered."); }
        for (int i = 0; i < Math.min(selected.size(), 16); i++) {
            Object raw = selected.get(i);
            if (!(raw instanceof String name) || name.length() > 64) { issues.add("Skipped invalid threat preset at entry " + (i + 1) + "."); continue; }
            var preset = CATALOG.stream().filter(value -> value.id().equals(name)).findFirst();
            if (preset.isEmpty()) { issues.add("Skipped unknown threat preset at entry " + (i + 1) + "."); continue; }
            if (!seen.add(name)) { issues.add("Skipped duplicate threat preset: " + name + "."); continue; }
            Preset value = preset.get();
            if (!value.modId().isEmpty() && !value.version().equals(installedVersion.apply(value.modId()))) {
                issues.add("Skipped " + name + ": requires loaded " + value.modId() + " " + value.version() + "."); continue;
            }
            if (combined.size() + value.mappings().size() > 256) { issues.add("Skipped " + name + ": total mapping limit is 256."); continue; }
            combined.addAll(value.mappings()); active.add(name);
        }
        ThreatRules parsed = ThreatRules.parse(combined);
        issues.addAll(parsed.issues());
        return new Resolved(new ThreatRules(parsed.rules(), issues), active);
    }
}
