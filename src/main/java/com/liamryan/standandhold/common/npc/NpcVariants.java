package com.liamryan.standandhold.common.npc;

import java.util.UUID;

/**
 * Per-soldier appearance derived from the entity UUID, so every client agrees without saved data or packets. The
 * texture index picks one of the generated atlas variants; height and width are render-only body scales (hitboxes,
 * reach and AI are unchanged). Variant 0 is the authored atlas.
 */
public final class NpcVariants {
    public static final int COUNT = 12;
    private NpcVariants() { }

    private static long mix(UUID id) {
        long h = id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 17);
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL; h ^= h >>> 33; h *= 0xc4ceb9fe1a85ec53L; h ^= h >>> 33;
        return h;
    }
    public static int index(UUID id) { return (int) Math.floorMod(mix(id), (long) COUNT); }
    /** 0.95-1.05 in 0.01 steps. */
    public static float height(UUID id) { return 0.95F + Math.floorMod(mix(id) >>> 20, 11L) * 0.01F; }
    /** 0.96-1.04 in 0.01 steps, independent of height. */
    public static float width(UUID id) { return 0.96F + Math.floorMod(mix(id) >>> 40, 9L) * 0.01F; }
}
