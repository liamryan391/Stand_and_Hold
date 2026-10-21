package com.liamryan.standandhold.common.building;

/** Pure menu presentation and lossless signed-short data encoding. */
public final class MenuValues {
    public static int statusCode(String reason) {
        if (reason.equals("Ready")) { return 0; }
        if (reason.equals("Completed")) { return 1; }
        if (reason.equals("Maximum level")) { return 8; }
        if (reason.contains("human points")) { return 9; }
        if (reason.startsWith("Requires stage ")) { return 2; }
        if (reason.contains("local supplies")) { return 4; }
        if (reason.contains("local samples")) { return 5; }
        return 3;
    }
    public static int progressWidth(long work, long required, int width) {
        if (required <= 0 || width <= 0) { return 0; }
        return (int) ((double) Math.clamp(work, 0, required) / required * width);
    }
    private MenuValues() { }
    public static int limb(long value, int limb) { return (int) (value >>> (limb * 16)) & 0xffff; }
    public static long accept(long old, int limb, int value) {
        int shift = limb * 16;
        return old & ~(0xffffL << shift) | ((long) value & 0xffffL) << shift;
    }
}
