package com.liamryan.standandhold.common.building;

/** Lossless long encoding for vanilla signed-short container data packets. */
public final class MenuValues {
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
