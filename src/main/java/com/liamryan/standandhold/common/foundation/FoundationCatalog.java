package com.liamryan.standandhold.common.foundation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.liamryan.standandhold.common.mission.Mission;
import com.liamryan.standandhold.common.research.ResearchEntry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.StrictJsonParser;

/** A bounded, immutable snapshot. Resolve once per command, never from a tick handler. */
public final class FoundationCatalog {
    public static final Identifier RESOURCE = Identifier.fromNamespaceAndPath("standandhold", "foundation/definitions.json");
    private static final int MAX_BYTES = 65_536;
    private static final int MAX_ENTRIES = 64;
    private final Map<String, ResearchEntry> research;
    private final Map<String, Mission> missions;

    public FoundationCatalog(List<ResearchEntry> research, List<Mission> missions) {
        this.research = index(research, ResearchEntry::id);
        this.missions = index(missions, Mission::id);
        validateGraph(this.research, ResearchEntry::prerequisites);
        validateGraph(this.missions, Mission::prerequisites);
        for (Mission mission : missions) {
            if (mission.objective() == Mission.Objective.RESEARCH_COMPLETION && !this.research.containsKey(mission.subject())) {
                throw new IllegalArgumentException("Unknown mission research subject: " + mission.subject());
            }
        }
    }

    public Map<String, ResearchEntry> research() { return research; }
    public Map<String, Mission> missions() { return missions; }

    public ResearchEntry research(String input) {
        ResearchEntry entry = research.get(commandId(input));
        if (entry == null) { throw new IllegalArgumentException("Unknown research: " + input); }
        return entry;
    }

    public Mission mission(String input) {
        Mission entry = missions.get(commandId(input));
        if (entry == null) { throw new IllegalArgumentException("Unknown mission: " + input); }
        return entry;
    }

    public static FoundationCatalog load(ResourceProvider resources) {
        try (InputStream stream = resources.open(RESOURCE)) {
            return read(stream);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid foundation definitions: " + exception.getMessage(), exception);
        }
    }

    public static FoundationCatalog read(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) { throw new IllegalArgumentException("Definitions exceed 64 KiB."); }
        return parse(new String(bytes, StandardCharsets.UTF_8));
    }

    public static FoundationCatalog parse(String json) {
        JsonObject root;
        try {
            root = object(StrictJsonParser.parse(json));
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("Malformed foundation JSON.", exception);
        }
        if (number(root, "format", -1) != 1) { throw new IllegalArgumentException("Unsupported definition format."); }
        List<ResearchEntry> research = new ArrayList<>();
        for (JsonElement value : array(root, "research")) {
            JsonObject entry = object(value);
            research.add(new ResearchEntry(text(entry, "id", null), text(entry, "title", null),
                    integer(entry, "required_stage", 0), strings(entry, "prerequisites"),
                    number(entry, "supply_cost", 0), integer(entry, "sample_cost", 0), reward(entry), strings(entry, "unlocks")));
        }
        List<Mission> missions = new ArrayList<>();
        for (JsonElement value : array(root, "missions")) {
            JsonObject entry = object(value);
            missions.add(new Mission(text(entry, "id", null), text(entry, "title", null),
                    Mission.Objective.parse(text(entry, "objective", null)), integer(entry, "target", 1),
                    text(entry, "subject", ""), strings(entry, "prerequisites"), reward(entry)));
        }
        return new FoundationCatalog(research, missions);
    }

    public static String commandId(String input) {
        String id = input.contains(":") ? input : "standandhold:" + input;
        requireId(id);
        return id;
    }

    public static void requireId(String id) {
        if (id == null || id.length() > 128 || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("Expected a lower-case namespaced ID: " + id);
        }
    }

    public static void requireTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 80 || title.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Titles must be 1-80 printable characters.");
        }
    }

    private static FoundationReward reward(JsonObject entry) {
        JsonObject reward = entry.has("reward") ? object(entry.get("reward")) : new JsonObject();
        return new FoundationReward(number(reward, "points", 0), number(reward, "supplies", 0));
    }

    private static JsonObject object(JsonElement value) {
        if (!value.isJsonObject()) { throw new IllegalArgumentException("Expected a JSON object."); }
        return value.getAsJsonObject();
    }

    private static JsonArray array(JsonObject object, String key) {
        if (!object.has(key)) { return new JsonArray(); }
        if (!object.get(key).isJsonArray() || object.getAsJsonArray(key).size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("Expected an array of at most 64 entries: " + key);
        }
        return object.getAsJsonArray(key);
    }

    private static List<String> strings(JsonObject object, String key) {
        List<String> result = new ArrayList<>();
        for (JsonElement element : array(object, key)) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Expected string in " + key);
            }
            String id = element.getAsString();
            requireId(id);
            if (result.contains(id)) { throw new IllegalArgumentException("Duplicate ID in " + key + ": " + id); }
            result.add(id);
        }
        return List.copyOf(result);
    }

    private static String text(JsonObject object, String key, String fallback) {
        if (!object.has(key) && fallback != null) { return fallback; }
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected text: " + key);
        }
        return value.getAsString();
    }

    private static long number(JsonObject object, String key, long fallback) {
        if (!object.has(key)) { return fallback; }
        JsonElement value = object.get(key);
        try {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                throw new ArithmeticException();
            }
            return value.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("Expected a whole signed-long number: " + key);
        }
    }

    private static int integer(JsonObject object, String key, int fallback) {
        long value = number(object, key, fallback);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) { throw new IllegalArgumentException("Integer out of range: " + key); }
        return (int) value;
    }

    private static <T> Map<String, T> index(List<T> entries, Function<T, String> id) {
        if (entries.size() > MAX_ENTRIES) { throw new IllegalArgumentException("Too many definitions."); }
        Map<String, T> result = new LinkedHashMap<>();
        for (T entry : entries) {
            if (result.putIfAbsent(id.apply(entry), entry) != null) { throw new IllegalArgumentException("Duplicate definition: " + id.apply(entry)); }
        }
        return Collections.unmodifiableMap(result);
    }

    private static <T> void validateGraph(Map<String, T> entries, Function<T, List<String>> prerequisites) {
        Map<String, Integer> visits = new HashMap<>();
        for (String id : entries.keySet()) { visit(id, entries, prerequisites, visits); }
    }

    private static <T> void visit(String id, Map<String, T> entries, Function<T, List<String>> prerequisites, Map<String, Integer> visits) {
        if (!entries.containsKey(id)) { throw new IllegalArgumentException("Unknown prerequisite: " + id); }
        if (visits.getOrDefault(id, 0) == 1) { throw new IllegalArgumentException("Prerequisite cycle at " + id); }
        if (visits.getOrDefault(id, 0) == 2) { return; }
        visits.put(id, 1);
        for (String prerequisite : prerequisites.apply(entries.get(id))) { visit(prerequisite, entries, prerequisites, visits); }
        visits.put(id, 2);
    }
}
