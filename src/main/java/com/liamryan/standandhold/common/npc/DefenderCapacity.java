package com.liamryan.standandhold.common.npc;

import java.util.List;

/** Pure capacity policy; saved reservations and loaded-area population remain separate quotas. */
public final class DefenderCapacity {
    public static final int ROSTER_LIMIT = 16;
    public static final List<Integer> DEFAULT_LEVELS = List.of(3, 5, 6, 7, 8);
    private DefenderCapacity() { }

    public static boolean validProfile(Object candidate) {
        if (!(candidate instanceof List<?> values) || values.size() != 5) { return false; }
        int previous = 0;
        for (Object value : values) {
            if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) { return false; }
            long number = ((Number) value).longValue();
            if (number < previous || number > ROSTER_LIMIT) { return false; }
            previous = (int) number;
        }
        return true;
    }

    public static int effective(int level, Object profile, int ceiling, int areaCap) {
        if (level < 1 || level > 5 || ceiling < 0 || ceiling > ROSTER_LIMIT || areaCap < 1 || areaCap > 64) {
            throw new IllegalArgumentException("Invalid defender capacity bounds.");
        }
        var values = validProfile(profile) ? (List<?>) profile : DEFAULT_LEVELS;
        return Math.min(((Number) values.get(level - 1)).intValue(), Math.min(ceiling, Math.min(areaCap, ROSTER_LIMIT)));
    }
}
