package com.liamryan.standandhold.common.threat;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Bounded, registry-independent parsing. Missing mods never resolve through a default entity. */
public record ThreatRules(List<Rule> rules, List<String> issues) {
    public static final List<String> DEFAULTS = List.of("minecraft:zombie|5|0.35|1");
    public ThreatRules { rules = List.copyOf(rules); issues = List.copyOf(issues); }
    public record Rule(String id, boolean tag, long points, double chance, int samples) {
        public int dropCount(double roll) { return roll >= 0 && roll < 1 && roll < chance ? samples : 0; }
    }
    public static ThreatRules parse(List<?> values) {
        List<Rule> rules = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        var seen = new HashSet<String>();
        if (values.size() > 256) { issues.add("Only the first 256 threat mappings are considered."); }
        for (int i = 0; i < Math.min(256, values.size()); i++) {
            Object raw = values.get(i);
            try {
                if (!(raw instanceof String value) || value.length() > 512) { throw new IllegalArgumentException(); }
                String[] parts = value.split("\\|", -1);
                if (parts.length != 4) { throw new IllegalArgumentException(); }
                String selector = parts[0].trim();
                boolean tag = selector.startsWith("#");
                String id = tag ? selector.substring(1) : selector;
                FoundationCatalog.requireId(id);
                long points = Long.parseLong(parts[1].trim());
                double chance = Double.parseDouble(parts[2].trim());
                int samples = Integer.parseInt(parts[3].trim());
                if (points < 0 || points > 1000000 || !Double.isFinite(chance) || chance < 0 || chance > 1 || samples < 0 || samples > 64) {
                    throw new IllegalArgumentException();
                }
                if (!seen.add(selector)) { issues.add("Duplicate threat selector at entry " + (i + 1) + "; first entry wins."); continue; }
                rules.add(new Rule(id, tag, points, chance, samples));
            } catch (IllegalArgumentException exception) { issues.add("Skipped invalid threat mapping at entry " + (i + 1) + ". Expected entity ID or #tag|points 0..1000000|chance 0..1|samples 0..64."); }
        }
        return new ThreatRules(rules, issues);
    }
    public Optional<Rule> match(String registeredId, Predicate<String> tagMatches) {
        if (registeredId == null || registeredId.isEmpty()) { return Optional.empty(); }
        for (Rule rule : rules) { if (!rule.tag() && rule.id().equals(registeredId)) { return Optional.of(rule); } }
        for (Rule rule : rules) { if (rule.tag() && tagMatches.test(rule.id())) { return Optional.of(rule); } }
        return Optional.empty();
    }
}
