package com.liamryan.standandhold;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import com.liamryan.standandhold.common.research.ResearchEntry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

final class FoundationChecks {
    private static int assertions;

    static void run() throws Exception {
        definitions();
        System.out.println("Foundation checks passed: " + assertions + " assertions.");
    }

    private static void definitions() throws Exception {
        FoundationCatalog catalog = defaults();
        check(catalog.research().size() == 3 && catalog.missions().size() == 7, "bounded starter definitions");
        check(catalog.research("parasite_samples").supplyCost() == 10, "short command IDs");
        check(catalog.mission("recover_parasite_samples").target() == 3, "sample objective");
        expect(() -> catalog.research("missing"));
        expect(() -> catalog.mission("BAD ID"));
        expect(() -> FoundationCatalog.parse("{"));
        expect(() -> FoundationCatalog.parse("null"));
        expect(() -> FoundationCatalog.parse("{\"format\":2}"));
        ResearchEntry first = catalog.research("parasite_samples");
        expect(() -> new FoundationCatalog(List.of(first, first), List.of()));
        ResearchEntry missing = research("standandhold:a", List.of("standandhold:missing"));
        expect(() -> new FoundationCatalog(List.of(missing), List.of()));
        expect(() -> new FoundationCatalog(List.of(research("standandhold:a", List.of("standandhold:b")),
                research("standandhold:b", List.of("standandhold:a"))), List.of()));
        expect(() -> new FoundationCatalog(List.of(research("standandhold:a", List.of("standandhold:a"))), List.of()));
        expect(() -> research("Not Namespaced", List.of()));
        expect(() -> new FoundationReward(-1, 0));
        for (String bad : List.of("1.5", "9223372036854775808", "-1", "\"10\"", "null")) {
            expect(() -> FoundationCatalog.parse("{\"format\":1,\"research\":[{\"id\":\"standandhold:x\","
                    + "\"title\":\"X\",\"supply_cost\":" + bad + "}]}"));
        }
        expect(() -> FoundationCatalog.read(new ByteArrayInputStream(new byte[65_537])));
    }

    static FoundationCatalog defaults() throws Exception {
        try (InputStream stream = FoundationChecks.class.getResourceAsStream("/data/standandhold/foundation/definitions.json")) {
            if (stream == null) { throw new AssertionError("Missing default definitions resource."); }
            return FoundationCatalog.read(stream);
        }
    }

    private static ResearchEntry research(String id, List<String> prerequisites) {
        return new ResearchEntry(id, "Test", 0, prerequisites, 0, 0, new FoundationReward(0, 0), List.of());
    }

    static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }

    static void expect(Action action) {
        assertions++;
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        catch (Exception exception) { throw new AssertionError("Unexpected exception", exception); }
        throw new AssertionError("Expected rejected operation.");
    }

    @FunctionalInterface
    interface Action { void run() throws Exception; }
}
