package com.liamryan.standandhold.common.mission;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import com.liamryan.standandhold.common.foundation.FoundationReward;
import java.util.List;
import java.util.Locale;

public record Mission(String id, String title, Objective objective, int target, String subject,
                      List<String> prerequisites, FoundationReward reward) {
    public Mission {
        FoundationCatalog.requireId(id);
        FoundationCatalog.requireTitle(title);
        prerequisites = List.copyOf(prerequisites);
        prerequisites.forEach(FoundationCatalog::requireId);
        if (objective == null || reward == null || target < 1 || target > 1_000_000
                || (objective == Objective.HUMAN_STAGE && target > 6)) {
            throw new IllegalArgumentException("Invalid mission objective for " + id);
        }
        if (objective == Objective.RESEARCH_COMPLETION) {
            FoundationCatalog.requireId(subject);
            if (target != 1) { throw new IllegalArgumentException("Research objectives have target 1."); }
        } else if (!subject.isEmpty()) {
            throw new IllegalArgumentException("Only research objectives accept a subject in this foundation.");
        }
    }

    public enum Objective {
        SAMPLE_RECOVERY, COMMAND_POST, SUPPLY_TOTAL, RESEARCH_COMPLETION, HUMAN_STAGE, OUTPOST_DEFENCE, MAIN_BASE_DISCOVERY;

        public static Objective parse(String name) {
            try {
                return valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unknown mission objective: " + name);
            }
        }
    }
}
