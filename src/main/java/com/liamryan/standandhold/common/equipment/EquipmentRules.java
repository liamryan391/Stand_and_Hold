package com.liamryan.standandhold.common.equipment;

import com.liamryan.standandhold.common.foundation.FoundationCatalog;
import java.util.function.Predicate;

/** Registry-independent policy, also exercised by headless regression checks. */
public final class EquipmentRules {
    private EquipmentRules() { }
    public static boolean unlocked(int stage, int required, String research, Predicate<String> defined, Predicate<String> completed) {
        if (stage < 0 || stage > 6 || required < 0 || required > 6 || stage < required || research == null) { return false; }
        if (research.isEmpty()) { return true; }
        try { FoundationCatalog.requireId(research); }
        catch (IllegalArgumentException exception) { return false; }
        return defined.test(research) && completed.test(research);
    }
    public static boolean validTarget(boolean living, boolean player, boolean human, boolean allied, boolean mapped, boolean hostile) {
        return living && !player && !human && !allied && (mapped || hostile);
    }
    public static float damage(double value) { return Double.isFinite(value) ? (float) Math.clamp(value, 0, 256) : 0; }
    public static int flightAge(int saved) { return saved >= 0 && saved <= 100 ? saved : 100; }
    public static boolean expired(int age, int lifetime) { return lifetime < 1 || lifetime > 100 || age >= lifetime || age < 0; }
}
